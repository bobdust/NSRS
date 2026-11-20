package bobdust.rpc.sockets;

public interface CommandResult extends BinarySequence {

	String getOperationName();
	Object getReturnValue();
	Throwable getException();
	
}
