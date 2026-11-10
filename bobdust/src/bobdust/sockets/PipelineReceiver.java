package bobdust.sockets;

import java.io.IOException;
import java.util.UUID;

interface PipelineReceiver {

	void receive(Pipeline pipeline, BinarySequence data, UUID token) throws IOException;
	
}
