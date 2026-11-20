package bobdust.rpc.sockets.impl;

import java.io.*;
import java.net.*;
import bobdust.rpc.sockets.*;

class SocketPipeline extends PipelineBase {
	
	private Socket sendSocket;
	private Socket receiveSocket;

	public SocketPipeline(Socket socket)	
	{		
		this(socket, socket);
	}
	
	public SocketPipeline(Socket sendSocket, Socket receiveSocket)
	{
		this.sendSocket = sendSocket;
		this.receiveSocket = receiveSocket;
	}

	@Override
	protected BinarySequence deserialize(byte[] bytes) {
		return Package.fromBytes(Package.class, bytes);
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		sendSocket.getOutputStream().write(buffer);
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return receiveSocket.getInputStream().read(buffer);
	}
	
	@Override
	public void close()
	{
		try {
			sendSocket.close();
			receiveSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onException(Exception exception)
	{
		super.onException(exception);
		close();
	}
}
