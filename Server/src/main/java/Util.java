import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static void main(String[] args) {
        test();
    }

    public static byte[] marshall(Map<String, Object> map) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            outputStream.write('n');// indicate variable name
            outputStream.write(key.getBytes(StandardCharsets.UTF_8));
            outputStream.write(0);
            outputStream.write(':');

            if (value instanceof String) {
                outputStream.write('r'); // prefix for type String
                outputStream.write(((String) value).getBytes(StandardCharsets.UTF_8));
                outputStream.write(0);
            } else if (value instanceof Byte) {
                byte b = (Byte) value;
                outputStream.write('b'); // prefix for type Byte
                outputStream.write(b);
            } else if (value instanceof Short) {
                short s = (Short) value;
                outputStream.write('s'); // prefix for type Short
                outputStream.write(new byte[]{(byte) (s >>> 8), (byte) s});
            } else if (value instanceof Integer) {
                int i = (Integer) value;
                outputStream.write('i'); // prefix for type Integer
                outputStream.write(new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i});
            } else if (value instanceof Long) {
                long l = (Long) value;
                outputStream.write('l'); // prefix for type Long
                outputStream.write(new byte[]{(byte) (l >>> 56), (byte) (l >>> 48), (byte) (l >>> 40),
                        (byte) (l >>> 32), (byte) (l >>> 24), (byte) (l >>> 16), (byte) (l >>> 8), (byte) l});
            } else {
                throw new IOException("Error. Type of value must be Sring, Byte, Short, Integer or Long.");
            }

            outputStream.write(',');
        }
        return outputStream.toByteArray();
    }

    public static Map<String, Object> unmarshall(byte[] bytes) throws IOException {
        Map<String, Object> map = new HashMap<>();
        int index = 0;
        while (index < bytes.length) {
            if (bytes[index] != 'n')
                throw new IOException("Data format error.");
            index++;
            int end = index;
            while (bytes[++end] != '\0') ;
            String key = new String(bytes, index, end - index, StandardCharsets.UTF_8);
            if (bytes[index = end + 1] != ':')
                throw new IOException("Data format error.");
            index++;

            Object value = null;
            switch (bytes[index]) {
                case 'r':
                    while (bytes[++end] != '\0') ;
                    value = new String(bytes, index, end - index, StandardCharsets.UTF_8);
                    index = end;
                    break;
                case 'b':
                    value = bytes[index + 1];
                    index++;
                    break;
                case 's':
                    value = bytes[index + 1] << 8 | bytes[index + 2];
                    index += 2;
                    break;
                case 'i':
                    value = (bytes[index + 1] & 0xff) << 24 | (bytes[index + 2] & 0xff) << 16
                            | (bytes[index + 3] & 0xff) << 8 | (bytes[index + 4] & 0xff);
                    index += 4;
                    break;
                case 'l':
                    value = (bytes[index + 1] & 0xffL) << 56 | (bytes[index + 2] & 0xffL) << 48
                            | (bytes[index + 3] & 0xffL) << 40 | (bytes[index + 4] & 0xffL) << 32
                            | (bytes[index + 5] & 0xffL) << 24 | (bytes[index + 6] & 0xffL) << 16
                            | (bytes[index + 7] & 0xffL) << 8 | (bytes[index + 8] & 0xffL);
                    index += 8;
                    break;
                default:
                    throw new IOException("Type not recognized.");
            }

            if (bytes[++index] != ',')
                throw new IOException("Data format error.");
            index++;

            map.put(key, value);
        }

        return map;
    }

    private static void test() {
        Map<String, Object> map = new HashMap<>();
        map.put("string", "helijoisiio\n\t");
        map.put("byte", (byte) -93);
        map.put("short", (short) -3287);
        map.put("int", -1695609641);
        map.put("long", -169560964116956096L);

        System.out.println("Original map:");
        System.out.println(map + "\n");

        System.out.println("Byte array:");
        byte[] bMap = null;
        try {
            bMap = marshall(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (byte b : bMap) {
            System.out.print(b + "\t");
        }
        System.out.println("\n");

        System.out.println("Byte array in char:");
        for (byte b : bMap) {
            System.out.print((char) b + "\t");
        }
        System.out.println("\n");

        System.out.println("Back to map:");
        try {
            System.out.println(unmarshall(bMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\n");
    }
}
