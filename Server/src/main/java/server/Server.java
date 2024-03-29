package server;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server {

    private static final int READ = 0;
    private static final int INSERT = 1;
    private static final int MONITOR = 2;
    private static final int CLEAR = 3;
    private static final int DELETE = 4;
    private static final int VERSION = 5;

    private static double packetLossRate = 0;
    private static boolean isAtLeastOnce = false;
    private static int port;
    private static DatagramSocket socket;

    public static void run() {
        try {
            Server.socket = new DatagramSocket(Server.port);
            System.out.println("Server is running at: " + Inet4Address.getLocalHost().getHostAddress() + ":" + Server.port);
            byte[] receivingBuffer = new byte[1024];
            byte[] replyBuffer = null;
            DatagramPacket req = null;
            Map<String, Object> request = null;
            Map<String, Object> response = new HashMap<>();
            Map<String, Map<String, Object>> cachedResponse = new HashMap<>();
            DatagramPacket reply = null;
            while (true) {
                System.out.println("------------------------------------");
                //receive UDP request
                req = new DatagramPacket(receivingBuffer, receivingBuffer.length);
                socket.receive(req);
                System.out.println("[Client]: " + req.getSocketAddress());

                //package loss condition
                if (Math.random() < packetLossRate) {
                    System.out.println("[Packet loss] mocked packet loss at receiving");
                    continue;
                }

                //unmarshal request
                request = Util.unmarshall(receivingBuffer);
                System.out.println("[Unmarshalling]" + request);

                //cache
                if (!isAtLeastOnce) {
                    Map<String, Object> res = cachedResponse.get(req.getSocketAddress().toString() + request.get("time") + request.get("op"));
                    System.out.println(req.getSocketAddress().toString());

                    if (res != null && !res.isEmpty()) {
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
                    MonitorHandler.register(req.getSocketAddress(), request, response);
                } else
                    route(request, response);
                //handle monitor
                if ((op == INSERT || op == CLEAR || op == DELETE) && response.get("Exception") == null)
                    notifyClients(request);
                //marshal respond
                for (Map.Entry<String, Object> entry : response.entrySet()) {
                    System.out.println("[Mashalling]" + entry.getKey() + " " + entry.getValue());
                }
                replyBuffer = Util.marshall(response);
                //sending UDP response
                reply = new DatagramPacket(replyBuffer, replyBuffer.length, req.getAddress(), req.getPort());

                if (!isAtLeastOnce) {
                    cachedResponse.put(req.getSocketAddress().toString() + request.get("time") + request.get("op"), response);
                }

                //package loss condition
                if (Math.random() < packetLossRate) {
                    System.out.println("[Packet loss] mocked packet loss at replying");
                    continue;
                }

                System.out.println("[Sending to client] IP: " + req.getAddress() + " port: " + req.getPort());
                socket.send(reply);
            }
        } catch (Exception e) {
            System.out.println("[Exception]: " + e.toString());
            socket.close();
            e.printStackTrace();
        }
    }

    public static void route(Map<String, Object> request, Map<String, Object> response) {
        response.clear();
        String fileName = (String) request.get("f");
        String data = (String) request.get("data");
        Integer length = (int) request.getOrDefault("len", 0);
        Integer offset = (int) request.getOrDefault("off", 0);

        switch ((Integer) request.get("op")) {
            case READ:
                FileHandler.read(fileName, offset, length, response);
                break;
            case INSERT:
                FileHandler.insert(fileName, offset, data, response);
                break;
            case CLEAR:
                FileHandler.clear(fileName, response);
                break;
            case DELETE:
                FileHandler.delete(fileName, offset, length, response);
                break;
            case VERSION:
                FileHandler.version(fileName, response);
                break;
            default:
                System.out.println("[Exception] invalid operation");
                response.put("Exception", "invalid operation");
        }
    }

    private static void notifyClients(Map<String, Object> request) throws IOException {
        String fileName = (String) request.get("f");
        System.out.println("[Monitor] removing expired monitor");
        //remove expired
        MonitorHandler.remove(fileName);
        Map<String, Object> response = new HashMap<>();
        DatagramPacket reply = null;
        byte[] replyBuffer = null;
        int index = 1;

        System.out.println("[Monitor] sending updates to registered clients");

        //send updates
        index = 1;
        PriorityQueue<Client> pq = MonitorHandler.map.get(fileName);
        Iterator<Client> iter;
        if (pq != null) {
            iter = pq.iterator();
            while (iter.hasNext()) {
                SocketAddress soc = iter.next().socket;
                System.out.println("\t" + (index++) + ":" + soc);
                replyBuffer = Util.marshall(request);
                reply = new DatagramPacket(replyBuffer, replyBuffer.length);
                reply.setSocketAddress(soc);
                socket.send(reply);
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        Server.init();
        Server.port = 9800;
        Server.run();
    }

    public static void init() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Invocation schematics: 1. At-most-once 2. At-least-once");
            if (!sc.hasNextInt()) {
                sc.next();
                continue;
            }
            int choice = sc.nextInt();
            if (choice == 1) {
                Server.isAtLeastOnce = false;
                break;
            } else if (choice == 2) {
                Server.isAtLeastOnce = true;
                break;
            }
        }

        while (true) {
            System.out.println("Enter packet loss rate (between 0.0 to 1.0): ");
            if (!sc.hasNextDouble()) {
                sc.next();
                continue;
            }
            double packetLossRate = sc.nextDouble();
            if (packetLossRate >= 0 && packetLossRate <= 1) {
                Server.packetLossRate = packetLossRate;
                break;
            }
        }
        sc.close();

        System.out.println("Server successfully initialised");
        System.out.println("Invocation schematics: " + (isAtLeastOnce ? "At-Least-Once" : "At-Most-Once"));
        System.out.println("Packet loss rate: " + Server.packetLossRate);
    }
}