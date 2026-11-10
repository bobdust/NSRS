package bobdust.sockets;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import bobdust.threading.*;

abstract class PipelineBase implements Pipeline {

	protected final int BUFFER_SIZE = 8192;
	
	private Task receivingTask;
	private ConcurrentHashMap<UUID, ConcurrentLinkedQueue<Package>> receivingQueues;
	
	private PipelineReceiver receiver;
	
	private String id;
	
	@Override
	public String getId() {
		return id;
	}
	
	protected PipelineBase()
	{
		id = UUID.randomUUID().toString();
		
		receivingQueues = new ConcurrentHashMap<UUID, ConcurrentLinkedQueue<Package>>();
		final PipelineBase me = this;
		receivingTask = new Task(){
			@Override
			public void run() throws IOException {
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = read(buffer);
				if(bytesRead > 0)
				{
					byte[] temp = new byte[bytesRead];
					System.arraycopy(buffer, 0, temp, 0, bytesRead);
					final Package p = Package.fromBytes(Package.class, temp);
					Runnable action = new Runnable(){
						@Override
						public void run() {
							try {
								packageReceived(p);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					};
					action.run();
				}
			}

			@Override
			public void handleException(Exception exception) {
				me.onException(exception);
			}			
		};
	}
	
	@Override
	public void onException(Exception exception)
	{
		exception.printStackTrace();
	}
	
	private void packageReceived(Package p) throws IOException
	{
		UUID token = p.getToken();
		synchronized(receivingQueues)
		{
			if(!receivingQueues.containsKey(token))
			{
				receivingQueues.put(token, new ConcurrentLinkedQueue<Package>());
			}
		}
		ConcurrentLinkedQueue<Package> queue = receivingQueues.get(token);
		synchronized(queue)
		{
			queue.add(p);
			if(queue.size() == p.getCount())
			{
				receiveCompleted(token);
			}
		}
	}
	
	protected void receiveCompleted(UUID token) throws IOException
	{
		if(receiver != null)
		{
			BinarySequence data = receive(token);
			if(data != null)
			{
				receiver.receive(this, data, token);
			}
		}
	}
	
	protected BinarySequence receive(UUID token)
	{
		ConcurrentLinkedQueue<Package> queue = receivingQueues.remove(token);
		if(queue != null)
		{
			Package p = Package.Join(queue);
			return deserialize(p.getData());
		}
		return null;
	}
	
	protected abstract BinarySequence deserialize(byte[] bytes);

	public abstract void write(byte[] buffer) throws IOException;
	
	public abstract int read(byte[] buffer) throws IOException;

	@Override
	public void open() {
		receivingTask.start();
	}

	@Override
	public void close() {
		receivingTask.stop();
		receivingQueues.clear();
	}

	@Override
	public void send(BinarySequence data, UUID token) throws IOException {
		byte[] bytes = data.toBytes();
		int dataSize = BUFFER_SIZE - Package.HEADER_SIZE;
		int length = bytes.length;
		int count = length / dataSize + (length % dataSize > 0 ? 1 : 0);
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		for(int index = 1; index <= count; index++)
		{
			int remainLength = stream.available();
			int bytesCount = remainLength > dataSize ? dataSize : remainLength;
			byte[] packageData = new byte[bytesCount];
			stream.read(packageData);
			Package p = new Package(token, index, count, packageData);
			write(p.toBytes());
		}
		stream.close();
	}
	
	protected UUID CreateDataToken()
	{
		UUID token = UUID.randomUUID();
		return token;
	}

	@Override
	public void setReceiver(PipelineReceiver receiver) {
		this.receiver = receiver;
	}

}
