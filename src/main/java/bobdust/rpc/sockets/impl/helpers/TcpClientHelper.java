package bobdust.rpc.sockets.impl.helpers;

import java.net.*;

public final class TcpClientHelper {
    private TcpClientHelper() {
        // Private constructor to prevent instantiation
    }

    public static boolean isSameDestination(String hostOrIp1, String hostOrIp2) {
        try {
            InetAddress addr1 = InetAddress.getByName(hostOrIp1);
            InetAddress addr2 = InetAddress.getByName(hostOrIp2);
            return addr1.equals(addr2);
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
