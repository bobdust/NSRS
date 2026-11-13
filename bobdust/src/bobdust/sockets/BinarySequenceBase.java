package bobdust.sockets;

import java.io.*;

abstract class BinarySequenceBase {

	public static <T extends BinarySequence> T fromBytes(Class<T> clazz, byte[] bytes)
	{
		T instance;
		try {
			instance = clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
			instance = null;
		}
		if(instance != null)
		{
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			try
			{
				instance.read(stream);
			}
			catch(IOException e)
			{
				instance = null;
			}
		}
		return instance;
	}
	
	public byte[] toBytes()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try
		{
			write(stream);
		}
		catch(IOException e)
		{
			return null;
		}
		return stream.toByteArray();
	}
	
	public abstract void write(OutputStream stream) throws IOException;
	
	public abstract void read(InputStream stream) throws IOException;
	
}
