package bobdust.rpc.sockets.impl;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import bobdust.rpc.sockets.*;
import bobdust.rpc.sockets.Parameter;
import bobdust.core.threading.*;

abstract class ResponderImpl implements Responder {

	private ServerSocket listener;
	private Task listenTask;
	private List<Pipeline> pipelines;
	private Map<Map.Entry<String, String>, Object> commanders;
	protected ConcurrentHashMap<String, Supplier<Object>> factories = new ConcurrentHashMap<String, Supplier<Object>>();

	protected ResponderImpl(int port) throws IOException {
		listener = new ServerSocket(port);
		pipelines = Collections.synchronizedList(new ArrayList<Pipeline>());
		commanders = Collections.synchronizedMap(new HashMap<Map.Entry<String, String>, Object>());
		listenTask = new Task() {

			@Override
			public void run() throws IOException {
				Socket socket = listener.accept();
				CommandPipeline pipeline = new CommandPipeline(
						new SocketPipeline(socket),
						new CommandDeserializer() {
							@Override
							public CommandBase deserialize(byte[] bytes) {
								return CommandImpl.fromBytes(CommandImpl.class, bytes);
							}
						});
				pipeline.setReceiver(new PipelineReceiver() {
					@Override
					public void receive(Pipeline pipeline, BinarySequence data, UUID token) throws IOException {
						CommandResult result;
						Command command = (Command) data;
						try {
							Object commander;
							var contractType = getContractType(command);
							Map.Entry<String, String> key = Map.entry(pipeline.getId(), contractType);
							synchronized (commanders) {
								if (commanders.containsKey(key)) {
									commander = commanders.get(key);
								} else {
									commander = getCommander(contractType);
									commanders.put(key, commander);
								}
							}
							var parameters = command.getParameters();
							List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
							for (Parameter o : parameters) {
								parameterTypes.add(Class.forName(o.type()));
							}
							Class<?>[] types = new Class<?>[parameterTypes.size()];
							parameterTypes.toArray(types);
							Method method = commander.getClass().getMethod(
									command.getOperationName(), types);
							Object returnValue = method.invoke(commander,
									Arrays.stream(parameters).map(x -> x.value()).toArray());
							if (method.getReturnType() == void.class) {
								result = command.doReturn();
							} else {
								result = command.doReturn(returnValue);
							}
						} catch (Exception e) {
							if (e instanceof InvocationTargetException ite) {
								result = command.doThrow(ite.getTargetException());
							} else {
								result = command.doThrow(e);
							}
						}
						pipeline.send(result, token);
					}
				});
				pipeline.open();
				pipelines.add(pipeline);
			}

			@Override
			public void handleException(Exception exception) {
				exception.printStackTrace();
			}
		};
	}

	protected String getContractType(Command command) {
		return command.getContractType();
	}

	protected Object getCommander(String contractType)
	{
		if(!factories.containsKey(contractType))
		{
			throw new IllegalArgumentException(String.format("No factory registered for contract type %s", contractType));
		}
		return factories.get(contractType).get();
	}

	public void start() {
		listenTask.start();
	}

	public void stop() throws IOException {
		for (Pipeline p : pipelines) {
			p.close();
		}
		listenTask.stop();
		listener.close();
	}

	public <TContract, TImplementation extends TContract> Responder register(Class<TImplementation> clazz, Supplier<TImplementation> factory) {
		factories.put(clazz.getName(), () -> factory.get());
		return this;
	}
}

class SingleResponderImpl<TCommander> extends ResponderImpl implements SingleResponder<TCommander> {
	private final Class<TCommander> type;

	protected SingleResponderImpl(int port, Class<TCommander> type, Supplier<TCommander> factory) throws IOException {
		super(port);
		this.type = type;
		register(type, factory);
	}

	@Override
	protected Object getCommander(String contractType) {
		if (!factories.containsKey(contractType)) {
			return super.getCommander(type.getName());
		}
		return super.getCommander(contractType);
	}
}
