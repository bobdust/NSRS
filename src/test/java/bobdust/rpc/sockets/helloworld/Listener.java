package bobdust.rpc.sockets.helloworld;

import java.io.IOException;
import bobdust.rpc.sockets.dummies.impl.*;
import bobdust.rpc.sockets.impl.*;

public class Listener {
	
	public static void main(String[] args) throws IOException
	{
		var assistant = ResponderFactory.get(GreetingAssistantImpl.class, () -> new GreetingAssistantImpl(), 1234);
		assistant.start();
		System.out.println("Greeting Assistant started.");
	}

}
