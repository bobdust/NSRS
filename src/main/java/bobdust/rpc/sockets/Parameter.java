package bobdust.rpc.sockets;

import java.io.Serializable;

public record Parameter(String name, Object value, String type) implements Serializable {
    private static final long serialVersionUID = 1L;

}
