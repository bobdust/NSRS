package bobdust.sockets;

import java.io.Serializable;

record Parameter(String name, Object value, String type) implements Serializable {
    private static final long serialVersionUID = 1L;

}
