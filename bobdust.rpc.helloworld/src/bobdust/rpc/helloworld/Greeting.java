package bobdust.rpc.helloworld;

import java.io.Serializable;

/**
 * Greeting
 */
public record Greeting(String message) implements Serializable {
    private static final long serialVersionUID = 1L;
}
