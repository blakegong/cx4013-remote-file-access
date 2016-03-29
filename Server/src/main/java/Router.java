import java.util.HashMap;
import java.util.Map;

public class Router {

    public static void main(String... args) {
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("type", "read");
        mockData.put("read:path", "C:\\filename.txt");
        mockData.put("read:offset", 489);
        mockData.put("read:length", 313);
        route(mockData);
    }

    public static void route(Map<String, Object> body) {
        String type = (String) body.get("type");
        String path, data;
        int offset, length;
        long interval;
        switch (type) {
            case "read":
                path = (String) body.get("read:path");
                offset = (Integer) body.get("read:offset");
                length = (Integer) body.get("read:length");
                Handler.read(path, offset, length);
                break;
            case "insert":
                path = (String) body.get("insert:path");
                offset = (Integer) body.get("insert:offset");
                data = (String) body.get("insert:data");
                Handler.insert(path, offset, data);
                break;
            case "monitor":
                path = (String) body.get("monitor:path");
                interval = (Long) body.get("monitor:interval");
                Handler.monitor(path, interval);
        }
    }
}
