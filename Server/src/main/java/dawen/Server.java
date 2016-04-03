package dawen;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server {

    private static enum OP {READ, INSERT, MONITOR, CLEAR, DELETE}

    private static final int READ = 0;
    private static final int INSERT = 1;
    private static final int MONITOR = 2;
    private static final int CLEAR = 3;
    private static final int DELETE = 4;
    private static boolean AT_LEAST_ONCE = true;
    private int port;

    private DatagramSocket socket;

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        try {
            this.socket = new DatagramSocket(port);
            byte[] receivingBuffer = new byte[1024];
            byte[] replyBuffer = null;
            DatagramPacket req = null;
            Map<String, Object> request = null;
            Map<String, Object> response = null;
            Map<String, Map<String, Object>> cachedResponse = new HashMap<>();
            DatagramPacket reply = null;
            while (true) {
                System.out.println("------------------------------------");
                //receive UDP request
                req = new DatagramPacket(receivingBuffer, receivingBuffer.length);
                socket.receive(req);
                System.out.println("[Client]: " + req.getSocketAddress());
                //unmarshal request
                request = Util.unmarshall(receivingBuffer);
                System.out.println("[Unmarshalling]" + request);

                MonitorHandler.remove((String) request.get("f"));
//                MonitorHandler.removeExpiredClients((String) request.get("f"));

                //package loss condition
                /*Random r = new Random();
                if (r.nextBoolean())
                    continue;*/

                //cache
                if (!AT_LEAST_ONCE) {
                    Map<String, Object> res = cachedResponse.get(req.getSocketAddress().toString() + request.get("t"));
                    System.out.println(req.getSocketAddress().toString());

                    if (!res.isEmpty()) {
                        replyBuffer = Util.marshall(res);
                        reply = new DatagramPacket(replyBuffer, replyBuffer.length, req.getAddress(), req.getPort());
                        socket.send(reply);
                        System.out.println("[Sending to client]");
                        continue;
                    }
                }

                //handle file request
                int op = (Integer) request.get("op");
                if (op == MONITOR) {
                    if (response == null)
                        response = new HashMap<>();
                    MonitorHandler.register(req.getSocketAddress(), request, response);
                }
//                    response = MonitorHandler.registerClient(req.getSocketAddress(), request);
                else
                    response = route(request);
                //handle monitor
                if (op == INSERT || op == CLEAR || op == DELETE)
                    informRegisteredClients(request);
                //marshal respond
                System.out.println("[Mashalling]" + response.keySet());
                replyBuffer = Util.marshall(response);
                //sending UDP response
                reply = new DatagramPacket(replyBuffer, replyBuffer.length, req.getAddress(), req.getPort());
                System.out.println(req.getAddress());
                System.out.println(req.getPort());
                socket.send(reply);

                if (!AT_LEAST_ONCE) {
                    cachedResponse.put(req.getSocketAddress().toString() + request.get("t"), response);
                }

                System.out.println("[Sending to client]");
            }
        } catch (Exception e) {
            System.out.println("[Exception]: " + e.toString());
            socket.close();
            e.printStackTrace();
        }
    }

    public static Map<String, Object> route(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<String, Object>();
        switch ((Integer) request.get("op")) {
            case READ:
                response = FileHandler.readFile(request);
                break;
            case INSERT:
                response = FileHandler.insertFile(request);
                break;
            case CLEAR:
                response = FileHandler.clearFile(request);
                break;
            case DELETE:
                response = FileHandler.deleteFile(request);
                break;
            default:
                System.out.println("[Exception] invalid operation");
                response.put("Exception", "invalid operation");
        }
        return response;
    }

    private void informRegisteredClients(Map<String, Object> request) throws IOException {
        String fileName = (String) request.get("f");
        System.out.println("[Monitor] removing expired monitor");
        //remove expired
//        List<SocketAddress> expired = MonitorHandler.removeExpiredClients(fileName);
        MonitorHandler.remove(fileName);
        Map<String, Object> response = new HashMap<>();
        DatagramPacket reply = null;
        byte[] replyBuffer = null;
        int index = 1;
        /*response.put("Expired", fileName);
        for (SocketAddress soc : expired) {
            System.out.println("\t" + (index++) + ":" + soc);
            replyBuffer = Util.marshall(response);
            reply = new DatagramPacket(replyBuffer, replyBuffer.length);
            reply.setSocketAddress(soc);
            socket.send(reply);
        }*/

        System.out.println("[Monitor] sending updates to registered clients");

        //send updates
        index = 1;
//        for (SocketAddress soc : MonitorHandler.getClientLists(fileName)) {
        PriorityQueue pq = MonitorHandler.map.get(fileName);
        Iterator<Client> iter;
        if (pq != null) {
            iter = pq.iterator();
            while (iter.hasNext()) {
                SocketAddress soc = iter.next().socket;
                System.out.println("\t" + (index++) + ":" + soc);
                replyBuffer = Util.marshall(response);
                reply = new DatagramPacket(replyBuffer, replyBuffer.length);
                reply.setSocketAddress(soc);
                socket.send(reply);
            }
        }
    }

    public static void main(String[] args) {
        Server s = new Server(9800);
        s.run();
    }
}
