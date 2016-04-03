package server;

import java.net.SocketAddress;
import java.util.*;

class MonitorHandler {

    public static Map<String, PriorityQueue<Client>> map = new HashMap<>();

    public static void register(SocketAddress socket, Map<String, Object> request, Map<String, Object> response) {
        String fileName = (String) request.get("f");
        int interval = (Integer) request.get("dur");
        System.out.println("[Monitor] " + fileName + " is registered to " + socket.toString() + " for " + interval + "seconds.");
        response.clear();

        long endTime = new Date().getTime() + interval * 1000;

        PriorityQueue<Client> pQueue = map.get(fileName);
        if (pQueue == null)
            pQueue = new PriorityQueue<>(new Comparator<Client>() {
                @Override
                public int compare(Client c1, Client c2) {
                    return Long.compare(c1.endTime, c2.endTime);
                }
            });
        boolean inserted = false;
        for (Client client : pQueue) {
            if (client.socket.equals(socket)) {
                client.endTime = client.endTime > endTime ? client.endTime : endTime;
                inserted = true;
            }
        }
        if (!inserted)
            pQueue.add(new Client(socket, endTime));
        map.put(fileName, pQueue);
        response.put("ACK", "Success");
    }

    public static void remove(String fileName) {
        PriorityQueue<Client> pQueue = map.get(fileName);
        while (pQueue != null && !pQueue.isEmpty() && pQueue.peek().endTime < new Date().getTime()) {
            Client client = pQueue.poll();
            System.out.println("[Monitor] " + client.socket.toString() + " on file " + fileName + " expired");
        }
    }

}
