package bobdust.rpc.sockets;

import java.io.IOException;
import java.net.UnknownHostException;

public interface Channel {
    Channel connect() throws UnknownHostException, IOException ;
    <T> Channel mount(Class<T> clazz);
    <T> T As(Class<T> clazz);
}
