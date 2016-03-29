import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UtilTest {

    private static Map<String, Object> originalMap;
    private static Map<String, Object> recoveredMap;

    @Before
    public void setUp() throws IOException {
        originalMap = new HashMap<>();
        originalMap.put("string", "helijoisiio\n\t");
        originalMap.put("byte", (byte) -93);
        originalMap.put("short", (short) -3287);
        originalMap.put("int", -1695609641);
        originalMap.put("long", -169560964116956096L);

        byte[] bytes = Util.marshall(originalMap);

        recoveredMap = Util.unmarshall(bytes);
    }

    @Test
    public void stringTest() {
        assertEquals(originalMap.get("string"), recoveredMap.get("string"));
    }

    @Test
    public void byteTest() {
        assertEquals(originalMap.get("byte"), recoveredMap.get("byte"));
    }

    @Test
    public void shortTest() {
        assertEquals(originalMap.get("short"), recoveredMap.get("short"));
    }

    @Test
    public void intTest() {
        assertEquals(originalMap.get("int"), recoveredMap.get("int"));
    }

    @Test
    public void longTest() {
        assertEquals(originalMap.get("long"), recoveredMap.get("long"));
    }
}
