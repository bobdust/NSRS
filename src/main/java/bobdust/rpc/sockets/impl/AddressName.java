package bobdust.rpc.sockets.impl;

import bobdust.rpc.sockets.impl.helpers.TcpClientHelper;

public record AddressName(String hostOrIp) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AddressName other = (AddressName) obj;
        return hostOrIp.trim().equalsIgnoreCase(other.hostOrIp.trim())
                || TcpClientHelper.isSameDestination(hostOrIp, other.hostOrIp);
    }

    @Override
    public int hashCode() {
        return hostOrIp == null ? 0 : hostOrIp.toLowerCase().hashCode();
    }
}
