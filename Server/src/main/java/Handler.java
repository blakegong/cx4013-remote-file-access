public class Handler {
    public static void read(String path, int offset, int length) {
        System.out.println("READ path: " + path + " offset: " + offset + " length:" + length);
    }

    public static void insert(String path, int offset, String data) {
        System.out.println("INSERT path: " + path + " offset: " + offset + " data: " + data);
    }

    public static void monitor(String path, long interval) {
        System.out.println("MONITOR path: " + path + " interval: " + interval);
    }

}
