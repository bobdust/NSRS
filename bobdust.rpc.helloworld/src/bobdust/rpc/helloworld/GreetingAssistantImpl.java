package bobdust.rpc.helloworld;

public class GreetingAssistantImpl implements GreetingAssistant {

	@Override
	public String hello() {
		return "Hello, world!";
	}

	@Override
	public String welcome(String name, Greeting greeting) {
		if (name == null || name.isEmpty()) {
			return greeting.message();
		}
		return "Welcome, " + name + "!";
	}

}
