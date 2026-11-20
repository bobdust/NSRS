package bobdust.rpc.sockets.impl;

import java.io.*;
import java.util.*;
import bobdust.rpc.sockets.*;

class Package extends BinarySequenceBase implements BinarySequence, Comparable<Package> {

	public static final int HEADER_SIZE = 16 + Integer.SIZE / 8 + Integer.SIZE / 8; 
	
	@Override
	public int compareTo(Package p) {
		return Integer.compare(index,  p.getIndex());
	}
	
	public static Package Empty;
	static
	{
		Empty = new Package(UUID.randomUUID(), 0, 0, new byte[]{});
	}
	
	public static Package Join(Collection<Package> packages)
	{
		List<Package> orderedPackages = new ArrayList<Package>(packages);
		Collections.sort(orderedPackages);
		Package result = Empty;
		for (Package p : orderedPackages) {
			result = result.Concat(p);
		}
		return result;
	}
	
	private UUID token;
	private int index;
	private int count;
	private byte[] data;
	
	public int getIndex() {
		return index;
	}

	private void setIndex(int index) {
		this.index = index;
	}

	public int getCount() {
		return count;
	}

	private void setCount(int count) {
		this.count = count;
	}

	public byte[] getData() {
		return data;
	}

	private void setData(byte[] data) {
		this.data = data;
	}

	public UUID getToken() {
		return token;
	}

	private void setToken(UUID token) {
		this.token = token;
	}

	public Package()
	{}
	
	public Package(UUID token, int index, int count, byte[] bytes)
	{
		setToken(token);
		setIndex(index);
		setCount(count);
		setData(bytes);
	}
	
	public Package Concat(Package p)
	{
		if(this.equals(Empty))
		{
			return p;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] mergedData;
		try
		{
			stream.write(data);
			stream.write(p.getData());
			mergedData = stream.toByteArray();
			stream.close();
		}
		catch(IOException e)
		{
			return null;
		}
		return new Package(token, index, count - 1, mergedData);
	}
	
	public boolean isDeliverable()
	{
		return index == count && index == 1;
	}
	
	@Override
	public void write(OutputStream stream) throws IOException {	
		DataOutputStream writer = new DataOutputStream(stream);
		try
		{
			writer.writeLong(token.getMostSignificantBits());
			writer.writeLong(token.getLeastSignificantBits());
			writer.writeInt(index);
			writer.writeInt(count);
			writer.write(data);
		}
		finally
		{
			writer.close();
		}
	}

	@Override
	public void read(InputStream stream) throws IOException {
		DataInputStream reader = new DataInputStream(stream);
		try
		{			
			long mostSignificantBits = reader.readLong();
			long leastSignificantBits = reader.readLong();
			setToken(new UUID(mostSignificantBits, leastSignificantBits));
			setIndex(reader.readInt());
			setCount(reader.readInt());
			byte[] buffer = new byte[stream.available()];
			reader.read(buffer);
			setData(buffer);
		}
		finally
		{
			reader.close();
		}
	}

}
