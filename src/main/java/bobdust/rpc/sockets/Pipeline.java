package bobdust.rpc.sockets;

import java.io.IOException;
import java.util.UUID;

public interface Pipeline {
	
	String getId();
	void write(byte[] buffer) throws IOException;
	int read(byte[] buffer) throws IOException;
	void open();
	void close();
	void send(BinarySequence data, UUID token) throws IOException;
	void setReceiver(PipelineReceiver receiver);
	void onException(Exception exception);
}
