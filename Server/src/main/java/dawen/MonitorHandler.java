package dawen;

import java.net.SocketAddress;
import java.util.*;

public class MonitorHandler {

    //format Map<fileName, Map<IPaddress, [Date, interval]
//    private static Map<String, Map<SocketAddress, Object[]>> monitorLists = new HashMap<>();

    /*public static Map<String, Object> registerClient(SocketAddress clientIpPort, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String fileName = (String) request.get("f");
        int interval = (Integer) request.get("dur");
        Date begin = new Date();
        System.out.println("[Monitor] filename:" + fileName + ", time stamp: " + begin + ", interval: " + interval);
        //check existence of file
        Map<SocketAddress, Object[]> clients = monitorLists.get(fileName);
        if (clients == null) {
            clients = new HashMap<>();
            monitorLists.put(fileName, clients);
        }
        //check existence of clients
        Object[] settings = clients.get(clientIpPort);
        if (settings != null) {
            response.put("Exception", "Client already monitor the file");
            System.out.println("[Exception]: Client already monitor the file");
        } else {
            settings = new Object[2];
            settings[0] = begin;
            settings[1] = interval;
            clients.put(clientIpPort, settings);
            response.put("ACK", "Success");
            System.out.println("[Monitor] Success");
        }
        return response;
    }*/


    public static Map<String, PriorityQueue<Client>> map = new HashMap<>();

    public static void register(SocketAddress socket, Map request, Map response) {
        String fileName = (String) request.get("f");

        int interval = (Integer) request.get("dur");
        long endDate = new Date().getTime() + interval;

        PriorityQueue<Client> pQueue = map.get(fileName);
        if (pQueue == null)
            pQueue = new PriorityQueue<>();
        pQueue.add(new Client(socket, endDate));
        map.put(fileName, pQueue);
        response.put("ACK", "Success");
        System.out.println("[Monitor] Success");
    }

    public static void remove(String fileName) {
        PriorityQueue<Client> pQueue = map.get(fileName);
        while (pQueue != null && !pQueue.isEmpty() && pQueue.peek().endTime < new Date().getTime()) {
            Client client = pQueue.poll();
            System.out.println("[Monitor] " + client.socket.toString() + " on file " + fileName + " expired");
        }
    }

    /*public static List<SocketAddress> removeExpiredClients(String fileName) {
        List<SocketAddress> expired = new ArrayList<>();
        Map<SocketAddress, Object[]> clients = monitorLists.get(fileName);
        Long current = new Date().getTime();
        if (clients != null) {
            for (SocketAddress ip : clients.keySet()) {
                Object[] settings = clients.get(ip);
                long delta = (long) (current - ((Date) settings[0]).getTime());
                //convert from milliseconds to seconds
                long interval = (long) ((Integer) settings[1] * 1000);
                if (delta >= interval) {
                    clients.remove(ip);
                    expired.add(ip);
                }
            }
        }
        return expired;
    }

    public static List<SocketAddress> getClientLists(String fileName) {
        List<SocketAddress> sockets = new ArrayList<>();
        Map<SocketAddress, Object[]> clients = monitorLists.get(fileName);
        if (clients != null)
            for (SocketAddress ip : clients.keySet())
                sockets.add(ip);
        return sockets;
    }*/
}
