package server;

import java.net.SocketAddress;

public class Client {
    public SocketAddress socket;
    public long endTime;

    public Client(SocketAddress socket, long endTime) {
        this.socket = socket;
        this.endTime = endTime;
    }
}