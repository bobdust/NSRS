package bobdust.sockets;

class CommandImpl extends CommandBase implements Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6501789125198521864L;
	private Parameter[] parameters;
	
	public CommandImpl()
	{
		super();
	}
	
	public CommandImpl(String contractType,String operationName, Parameter[] parameters)
	{
		super(contractType, operationName);
		this.parameters = parameters;
	}
	
	@Override
	protected void copyFrom(CommandBase commandBase)
	{
		super.copyFrom(commandBase);
		Command command = (Command)commandBase;
		parameters = command.getParameters();
	}
		
	@Override
	public Parameter[] getParameters() {
		return parameters;
	}

	@Override
	public CommandResult doReturn() {
		return new CommandResultImpl(operationName);
	}

	@Override
	public CommandResult doReturn(Object value) {
		return new CommandResultImpl(operationName, value);
	}

	@Override
	public CommandResult doThrow(Exception exception) {
		return new CommandResultImpl(operationName, exception);
	}

}
