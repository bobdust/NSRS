package bobdust.sockets;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import bobdust.threading.*;

public abstract class Responder<TCommander> {
	
	private ServerSocket listener;
	private Task listenTask;
	private List<Pipeline> pipelines;
	private Map<String, TCommander> commanders;
	
	protected Responder(int port) throws IOException
	{
		listener = new ServerSocket(port);
		pipelines = Collections.synchronizedList(new ArrayList<Pipeline>());
		commanders = Collections.synchronizedMap(new HashMap<String, TCommander>());
		listenTask = new Task(){

			@Override
			public void run() throws IOException {
				Socket socket = listener.accept();
				CommandPipeline pipeline = new CommandPipeline(
					new SocketPipeline(socket), 
					new CommandDeserializer(){
						@Override
						public CommandBase deserialize(byte[] bytes) {
							return CommandImpl.fromBytes(CommandImpl.class, bytes);
						}
					}
				);
				pipeline.setReceiver(new PipelineReceiver(){
					@Override
					public void receive(Pipeline pipeline, BinarySequence data, UUID token) throws IOException {
						CommandResult result;
						Command command = (Command)data;
						try
						{
							TCommander commander;
							String key = pipeline.getId();
							synchronized(commanders)
							{
								if(commanders.containsKey(key))
								{
									commander = commanders.get(key);
								}
								else
								{
									commander = getCommander();
									commanders.put(key, commander);
								}
							}
							Object[] parameters = command.getParameters();
							List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
							for(Object o : parameters)
							{
								parameterTypes.add(o.getClass());
							}
							Class<?>[] types = new Class<?>[parameterTypes.size()];
							parameterTypes.toArray(types);
							Method method = commander.getClass().getMethod(
									command.getOperationName(), types);
							Object returnValue = method.invoke(commander, parameters);
							if(method.getReturnType() == void.class)
							{
								result = command.doReturn();
							}
							else
							{
								result = command.doReturn(returnValue);
							}
						}
						catch(Exception e)
						{
							if(e instanceof InvocationTargetException)
							{
								result = command.doThrow(e.getCause());
							}
							else
							{
								result = command.doThrow(e);
							}
						}
						pipeline.send(result, token);
					}});
				pipeline.open();
				pipelines.add(pipeline);
			}

			@Override
			public void handleException(Exception exception) {
				exception.printStackTrace();
			}};
	}
	
	protected abstract TCommander getCommander() throws InstantiationException, IllegalAccessException;
	
	public void start()
	{
		listenTask.start();
	}
	
	public void stop() throws IOException
	{
		for(Pipeline p : pipelines)
		{
			p.close();
		}
		listenTask.stop();
		listener.close();
	}

}
