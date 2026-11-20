package bobdust.rpc.sockets;

public interface Command extends BinarySequence {
	String getContractType();
	String getOperationName();
	Parameter[] getParameters();
	CommandResult doReturn();
	CommandResult doReturn(Object value);
	CommandResult doThrow(Throwable exception);
	
}
