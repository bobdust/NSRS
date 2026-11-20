package bobdust.rpc.sockets.impl;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import bobdust.rpc.sockets.*;

abstract class Requestor {
	
	private CommandPipeline pipeline;
	
	protected Requestor(String host, int port) throws UnknownHostException, IOException
	{
		Socket socket = new Socket(host, port);
		pipeline = new CommandPipeline(
				new SocketPipeline(socket), 
				new CommandDeserializer(){

					@Override
					public CommandBase deserialize(byte[] bytes) {
						return CommandResultImpl.fromBytes(CommandResultImpl.class, bytes);
					}});
		pipeline.open();
	}
	
	protected CommandResult send(Command command) throws InterruptedException, IOException, TimeoutException
	{
		CommandResult result = (CommandResult)pipeline.post(command);
		return result;
	}
	
	protected Object invoke(Object[] values, String contractType, String methodName, String[] parameterNames, String[] parameterTypes) throws Throwable
	{
		Parameter[] parameters = new Parameter[values.length];
		for(int i = 0; i < values.length; i++)
		{
			parameters[i] = new Parameter(parameterNames[i], values[i], parameterTypes[i]);
		}
		Command command = new CommandImpl(contractType, methodName, parameters);
		CommandResult result = send(command);
		Object returnValue = result.getReturnValue(); 
		if(returnValue != null)
		{
			return returnValue;
		}
		else
		{
			var exception = result.getException();
			if(exception != null)
			{
				throw(exception);
			}
		}
		return null;
	}
	
}
