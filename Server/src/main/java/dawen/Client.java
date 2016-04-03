package dawen;

import java.net.SocketAddress;
import java.util.Comparator;

public class Client implements Comparator<Client> {
    public SocketAddress socket;
    public long endTime;

    public Client(SocketAddress socket, long endTime) {
        this.socket = socket;
        this.endTime = endTime;
    }

    @Override
    public int compare(Client c1, Client c2) {
        return Long.compare(c1.endTime, c2.endTime);
    }
}