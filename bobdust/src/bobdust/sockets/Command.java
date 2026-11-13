package bobdust.sockets;

interface Command extends BinarySequence {
	String getContractType();
	String getOperationName();
	Parameter[] getParameters();
	CommandResult doReturn();
	CommandResult doReturn(Object value);
	CommandResult doThrow(Exception exception);
	
}
