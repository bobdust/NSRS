package bobdust.rpc.sockets;

import java.io.IOException;
import java.util.UUID;

public interface PipelineReceiver {

	void receive(Pipeline pipeline, BinarySequence data, UUID token) throws IOException;
	
}
