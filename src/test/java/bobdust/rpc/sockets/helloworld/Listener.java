package bobdust.rpc.sockets.helloworld;

import java.io.IOException;

import bobdust.rpc.sockets.dummies.*;
import bobdust.rpc.sockets.dummies.impl.*;
import bobdust.rpc.sockets.impl.*;

public class Listener {
	
	public static void main(String[] args) throws IOException
	{
		final int port = 1234;
		var assistant = ResponderFactory.get(GreetingAssistantImpl.class, port);
		assistant.start();
		ResponderFactory
			.listen(port)
			.<Gratitude, GratitudeImpl>register(Gratitude.class, () -> new GratitudeImpl())
			.start();
		System.out.println("Greeting Assistant started.");
	}

}
