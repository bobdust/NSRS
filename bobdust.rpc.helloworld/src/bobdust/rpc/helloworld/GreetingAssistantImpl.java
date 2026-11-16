package bobdust.rpc.helloworld;

public class GreetingAssistantImpl implements GreetingAssistant {

	@Override
	public String hello() {
		return "Hello, world!";
	}

	@Override
	public String welcome(String name, Greeting greeting) {
		if (name == null || name.isEmpty()) {
			if (greeting != null) {
				return greeting.message();
			}
			throw new IllegalArgumentException("Name cannot be null or empty");
		}
		return "Welcome, " + name + "!";
	}

}
