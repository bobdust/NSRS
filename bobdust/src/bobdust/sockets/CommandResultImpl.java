package bobdust.sockets;

class CommandResultImpl extends CommandBase implements CommandResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3869099192752520136L;
	private Object returnValue;
	private Exception exception;
	
	public CommandResultImpl()
	{
		super();
	}

	public CommandResultImpl(String operationName)
	{
		super(null, operationName);
	}
	
	public CommandResultImpl(String operationName, Object returnValue)
	{
		this(operationName);
		this.returnValue = returnValue;
	}
	
	public CommandResultImpl(String operationName, Exception exception)
	{
		this(operationName);
		this.exception = exception;
	}

	@Override
	protected void copyFrom(CommandBase commandBase) {
		super.copyFrom(commandBase);
		CommandResult commandResult = (CommandResult)commandBase;
		returnValue = commandResult.getReturnValue();
		exception = commandResult.getException();
	}

	@Override
	public Object getReturnValue() {
		return returnValue;
	}

	@Override
	public Exception getException() {
		return exception;
	}

}
