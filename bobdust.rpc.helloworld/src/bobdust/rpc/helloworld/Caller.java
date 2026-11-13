package bobdust.rpc.helloworld;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import bobdust.sockets.*;
import javassist.CannotCompileException;
import javassist.NotFoundException;

public class Caller {
	
	public static void main(String[] args) throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NotFoundException, CannotCompileException, IOException
	{
		String host = "127.0.0.1";
		int port = 1234;
		GreetingAssistant assistant = RequestorFactory.get(GreetingAssistant.class, host, port);
		String words = assistant.hello();
		System.out.println(words);
		words = assistant.welcome(null, new Greeting("Cuckoo!"));
		System.out.println(words);
	}

}
