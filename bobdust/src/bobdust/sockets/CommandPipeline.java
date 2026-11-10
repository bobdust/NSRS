package bobdust.sockets;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

class CommandPipeline extends PipelineDecorator {

	public static final long TIMEOUT = 60 * 1000;
	
	private CommandDeserializer deserializer;
	
	public CommandPipeline(Pipeline pipeline, CommandDeserializer deserializer) {
		super(pipeline);
		this.deserializer = deserializer;
	}

	@Override
	protected BinarySequence deserialize(byte[] bytes) {
		return deserializer.deserialize(bytes);
	}

	@Override
	protected void receiveCompleted(UUID token) throws IOException {
		ChannelContext context = ChannelContext.get(token);
		if(context == null)
		{
			context = ChannelContext.create(token);
			super.receiveCompleted(token);
			context.close();
		}
		else
		{
			Object lock = context.getWaitHandle();
			synchronized(lock)
			{
				lock.notify();
			}
		}
	}
	
	public BinarySequence post(BinarySequence request) throws InterruptedException, IOException, TimeoutException
	{
		ChannelContext context = ChannelContext.create();
		var token = context.getToken();
		send(request, token);
		Object lock = context.getWaitHandle();
		synchronized(lock)
		{
			lock.wait(TIMEOUT);
		}
		BinarySequence response = receive(token);
		if(response == null)
		{
			throw(new TimeoutException());
		}
		context.close();
		return response;
	}
	
}
