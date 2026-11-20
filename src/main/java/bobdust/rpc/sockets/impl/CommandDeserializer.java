package bobdust.rpc.sockets.impl;

interface CommandDeserializer {
	CommandBase deserialize(byte[] bytes);
}
