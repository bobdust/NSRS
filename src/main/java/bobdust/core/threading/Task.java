package bobdust.core.threading;

import java.io.IOException;

public abstract class Task {
	private Thread.State state;

	public Thread.State getState()
	{
		synchronized(this)
		{
			return this.state;
		}
	}
	
	public abstract void run() throws IOException;
	
	public abstract void handleException(Exception exception);
	
	private void keepRunning()
	{
		Thread.State state = this.state;
		while(state == Thread.State.RUNNABLE)
		{
			try
			{
				run();
			}
			catch(Exception exception)
			{
				handleException(exception);
			}
			synchronized(this)
			{
				state = this.state;
			}
		}
	}
	
	public void start()
	{
		synchronized(this)
		{
			this.state = Thread.State.RUNNABLE;
		}
		final Task me = this;
		Runnable action = new Runnable(){

			@Override
			public void run() {
				me.keepRunning();
			}
		};
		Thread thread = new Thread(action);
		thread.start();
	}
	
	public void stop()
	{
		synchronized(this)
		{
			this.state = Thread.State.TERMINATED;
		}
	}
	
}
