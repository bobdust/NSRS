package bobdust.rpc.sockets.impl;

import java.util.function.Function;

import bobdust.rpc.sockets.*;

abstract class Requestor {
	
	private final Function<Command, CommandResult> send;
	
	protected Requestor(Function<Command, CommandResult> send)
	{
		this.send = send;
	}
	
	protected Object invoke(Object[] values, String contractType, String methodName, String[] parameterNames, String[] parameterTypes) throws Throwable
	{
		Parameter[] parameters = new Parameter[values.length];
		for(int i = 0; i < values.length; i++)
		{
			parameters[i] = new Parameter(parameterNames[i], values[i], parameterTypes[i]);
		}
		Command command = new CommandImpl(contractType, methodName, parameters);
		CommandResult result = this.send.apply(command);
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
