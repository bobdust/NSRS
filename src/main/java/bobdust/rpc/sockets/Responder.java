package bobdust.rpc.sockets;

import java.io.IOException;
import java.util.function.Supplier;

public interface Responder {
    void start();
    void stop() throws IOException;
    <TContract, TImplementation extends TContract> Responder register(Class<TImplementation> clazz, Supplier<TImplementation> factory);
}
