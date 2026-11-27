package bobdust.rpc.sockets.dummies.impl;

public class GratitudeImpl implements bobdust.rpc.sockets.dummies.Gratitude {

    @Override
    public bobdust.rpc.sockets.dummies.GreetingResult thanks(bobdust.rpc.sockets.dummies.Greeting greeting,
            String greetingFollowup) {
        return new bobdust.rpc.sockets.dummies.GreetingResult(
                "Thanks, %s. %s".formatted(greeting.message(), greetingFollowup));
    }

}
