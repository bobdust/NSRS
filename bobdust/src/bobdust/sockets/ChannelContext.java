package bobdust.sockets;

import java.util.*;
import java.util.concurrent.*;

class ChannelContext {
	private static ConcurrentHashMap<UUID, ChannelContext> contexts;
	static
	{
		contexts = new ConcurrentHashMap<UUID, ChannelContext>();
	}
	
	public static ChannelContext get(UUID token)
	{
		if(contexts.containsKey(token))
		{
			return contexts.get(token);
		}
		return null;
	}
	
	public static ChannelContext create(UUID token)
	{
		ChannelContext context = new ChannelContext(token);
		contexts.put(token, context);
		return context;
	}
	
	public static ChannelContext create()
	{
		return create(UUID.randomUUID());
	}
	
	private UUID token;
	private Object waitHandle;
	
	public UUID getToken() {
		return token;
	}

	public Object getWaitHandle() {
		return waitHandle;
	}

	private ChannelContext(UUID token)
	{
		this.token = token;
		waitHandle = new Object();
	}

	public void close() {
		contexts.remove(token);
	}	
	
}
