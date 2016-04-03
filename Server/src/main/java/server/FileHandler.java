package server;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class FileHandler {

    private static final String ROOT = "directory/";

    private static String getLastModified(String fileName) {
        File file = new File(ROOT + fileName);
        return ((Long) file.lastModified()).toString();
    }

    public static void read(String fileName, int offset, int length, Map<String, Object> response) {
        System.out.println("[read]" + fileName + ", offset: " + offset + ", length: " + length);

        //read from files
        byte[] data = new byte[length];
        try (RandomAccessFile r = new RandomAccessFile(ROOT + fileName, "r")) {
            if (r.length() < offset + length) {
                response.put("Exception", "invalid offset");
                return;
            }
            r.seek(offset);
            r.read(data, 0, length);
            System.out.println("[Data]:" + data.length);
            response.put("version", getLastModified(fileName));
            response.put("data", new String(data));
        } catch (FileNotFoundException e) {
            System.out.println("[Exception] cannot find file");
            response.put("Exception", "cannot find file");
        } catch (IOException e) {
            System.out.println("[Exception] cannot read file");
            response.put("Exception", "cannot read file");
        }
    }

    public static void insert(String fileName, int offset, String data, Map<String, Object> response) {
        System.out.println("[insert] " + fileName + " offset: " + offset + " data: " + data);
        try {
            RandomAccessFile r = new RandomAccessFile(ROOT + fileName, "rw");
            r.seek(offset);
            int remain = (int) r.length() - offset;
            //buffer temp
            byte[] temp = null;
            if (remain > 0) {
                temp = new byte[remain];
                r.read(temp);
                r.seek(offset);
                r.writeBytes(data);
                response.put("version", getLastModified(fileName));
                response.put("ACK", "Success");
                r.seek(offset + data.length());
                r.writeBytes(new String(temp));
            } else if (remain == 0) {
                System.out.println("[Append]" + fileName + ", offset: " + offset + ", data: " + data);
                r.seek(offset);
                r.writeBytes(data);
                response.put("version", getLastModified(fileName));
                response.put("ACK", "Success");
            } else
                response.put("Exception", "invalid offset");
            r.close();
        } catch (FileNotFoundException e) {
            System.out.println("[Exception] cannot find file");
            response.put("Exception", "cannot find file");
        } catch (IOException e) {
            System.out.println("[Exception] cannot read file");
            response.put("Exception", "cannot read file");
        }
    }

    public static void clear(String fileName, Map<String, Object> response) {
        System.out.println("[clear]" + fileName);

        try {
            PrintWriter writer = new PrintWriter(ROOT + fileName);
            writer.print("");
            writer.close();
            response.put("version", getLastModified(fileName));
            response.put("ACK", "Success");
        } catch (FileNotFoundException e) {
            System.out.println("[Exception] cannot find file");
            response.put("Exception", "cannot find file");
        }
    }

    public static void delete(String fileName, int off, int length, Map<String, Object> response) {
        System.out.println("[delete]" + fileName);

        Path path = FileSystems.getDefault().getPath(ROOT, fileName);
        FileOutputStream fos;
        try {
            byte[] data = Files.readAllBytes(path);
            if (data.length < off + length) {
                response.put("Exception", "invalid offset");
                return;
            }
            byte[] output = new byte[data.length - length];
            System.arraycopy(data, 0, output, 0, off);
            System.arraycopy(data, off + length, output, off, data.length - off - length);
            fos = new FileOutputStream(ROOT + fileName);
            fos.write(output);
            fos.close();
        } catch (IOException e) {
            System.out.println("[Exception] cannot find file");
            response.put("Exception", "cannot find file");
        }
        response.put("version", getLastModified(fileName));
        response.put("ACK", "Success");
    }

    public static void version(String fileName, Map<String, Object> response) {
        System.out.println("[version]" + fileName);
        response.clear();
        response.put("version", getLastModified(fileName));
    }
}
