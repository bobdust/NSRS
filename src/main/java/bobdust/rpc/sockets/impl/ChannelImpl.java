package bobdust.rpc.sockets.impl;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.*;

import bobdust.rpc.sockets.*;

class ChannelImpl implements Channel, Closeable {
	private final String host;
	private final int port;
	private Socket socket;
	private CommandPipeline pipeline;
	private boolean isClosed;
	private final ConcurrentHashMap<Class<?>, Object> requestors;
	private final BiFunction<Class<?>, Function<Command, CommandResult>, Object> requestorFactory;

	protected ChannelImpl(String host, int port, BiFunction<Class<?>, Function<Command, CommandResult>, Object> requestorFactory) {
		this.host = host;
		this.port = port;
		this.requestorFactory = requestorFactory;
		this.requestors = new ConcurrentHashMap<>();
		this.isClosed = true;
	}

	public boolean isClosed() {
		return isClosed;
	}

	protected CommandResult send(Command command) throws InterruptedException, IOException, TimeoutException {
		CommandResult result = (CommandResult) pipeline.post(command);
		return result;
	}

	@Override
	public Channel connect() throws UnknownHostException, IOException {
		if (socket == null || isClosed) {
			socket = new Socket(host, port);
			pipeline = new CommandPipeline(
					new SocketPipeline(socket),
					new CommandDeserializer() {

						@Override
						public CommandBase deserialize(byte[] bytes) {
							return CommandResultImpl.fromBytes(CommandResultImpl.class, bytes);
						}
					});
			pipeline.open();
			isClosed = false;
		}
		return this;
	}

	@Override
	public <T> Channel mount(Class<T> clazz) {
		if (!requestors.containsKey(clazz)) {
			var requestor = requestorFactory.apply(clazz, command -> {
				try {
					return send(command);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			requestors.put(clazz, requestor);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T As(Class<T> clazz) {
		if (!requestors.containsKey(clazz)) {
			throw new IllegalStateException(
					String.format("Requestor for class %s has not been mounted. Call mount() first.", clazz.getName()));
		}
		return (T) requestors.get(clazz);
	}

	@Override
	public void close() throws IOException {
		pipeline.close();
		socket.close();
		isClosed = true;
	}

}
