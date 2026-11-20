package bobdust.rpc.sockets;

import java.io.*;

public interface BinarySequence {
	
	void write(OutputStream stream) throws IOException;
	void read(InputStream stream) throws IOException;
	byte[] toBytes();

}
