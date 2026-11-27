package bobdust.rpc.sockets.helloworld;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import bobdust.rpc.sockets.dummies.*;
import bobdust.rpc.sockets.impl.*;
import javassist.*;

public class Caller {

	public static void main(String[] args)
			throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NotFoundException, CannotCompileException, IOException {
		var ip = "127.0.0.1";
		var host = "localhost";
		int port = 1234;
		GreetingAssistant assistant = RequestorFactory.get(GreetingAssistant.class, ip, port);
		String words = assistant.hello();
		System.out.println(words);
		words = assistant.welcome(null, new Greeting("Cuckoo!"));
		System.out.println(words);
		try {
			words = assistant.welcome(null, null);
		} catch (Exception e) {
			System.out.println("Expected exception: " + e.getMessage());
		}

		var gratitude = RequestorFactory.connect(host, port).mount(Gratitude.class).As(Gratitude.class);
		var result = gratitude.thanks(new Greeting("dude"), "Nice day!");
		System.out.println(result);
	}

}
