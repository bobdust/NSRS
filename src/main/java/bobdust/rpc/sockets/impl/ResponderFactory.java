package bobdust.rpc.sockets.impl;

import java.io.IOException;
import java.util.function.Supplier;

import bobdust.rpc.sockets.Responder;

public abstract class ResponderFactory<T> {
	
	public static <T> Responder get(final Class<T> clazz, Supplier<T> factory, int port) throws IOException
	{
		var responder = new SingleResponderImpl<T>(port, clazz, factory);
		return responder;
	}

	private ResponderFactory()
	{		
	}
	
}
