package dawen;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {

    private static final int FRAME_SIZE = 1024;
    private static final String ROOT = "directory/";

    public static Map<String, Object> readFile(Map<String, Object> request) {
        //extract parameters
        String fileName = (String) request.get("f");
        int offset = (Integer) request.get("off");
        int len = (Integer) request.get("len");
        System.out.println("[read]" + fileName + ", offset: " + offset + ", len: " + len);
        //read from files
        Map<String, Object> response = new HashMap<>();
        byte[] data = new byte[len];
        try (RandomAccessFile r = new RandomAccessFile(ROOT + fileName, "r")) {
            if (r.length() < offset + len) {
                response.put("Exception", "invalid offset");
                return response;
            }
            r.seek(offset);
            r.read(data, 0, len);
            System.out.println("[Data]:" + data.length);
            response.put("data", new String(data));
        } catch (FileNotFoundException e) {
            System.out.println("[Exception] cannot find file");
            response.put("Exception", "cannot find file");
        } catch (IOException e) {
            System.out.println("[Exception] cannot read file");
            response.put("Exception", "cannot read file");
        }
        return response;
    }

    public static Map<String, Object> insertFile(Map<String, Object> request) {
        String fileName = (String) request.get("f");
        int offset = (Integer) request.get("off");
        String data = (String) request.get("data");
        Map<String, Object> response = new HashMap<String, Object>();
        try {
            RandomAccessFile r = new RandomAccessFile(ROOT + fileName, "rw");
            r.seek(offset);
            int remain = (int) r.length() - offset;
            //buffer temp
            byte[] temp = null;
            if (remain > 0) {
                System.out.println("[insert]" + fileName + ", offset: " + offset + ", data: " + data);
                temp = new byte[remain];
                r.read(temp);
                r.seek(offset);
                r.writeBytes(data);
                response.put("ACK", "Success");
                r.seek(offset + data.length());
                r.writeBytes(new String(temp));
            } else if (remain == 0) {
                System.out.println("[Append]" + fileName + ", offset: " + offset + ", data: " + data);
                r.seek(offset);
                r.writeBytes(data);
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
        return response;
    }

    public static Map<String, Object> clearFile(Map<String, Object> request) {
        String fileName = (String) request.get("f");
        System.out.println("[clear]" + fileName);
        Map<String, Object> response = new HashMap<String, Object>();
        try {
            PrintWriter writer = new PrintWriter(ROOT + fileName);
            writer.print("");
            writer.close();
            response.put("ACK", "Success");
        } catch (FileNotFoundException e) {
            System.out.println("[Exception] cannot find file");
            response.put("Exception", "cannot find file");
        }
        return response;
    }

    public static Map<String, Object> deleteFile(Map<String, Object> request) {
        String fileName = (String) request.get("f");
        int off = (int) request.get("off");
        int length = (int) request.get("len");
        System.out.println("[delete]" + fileName);
        Map<String, Object> response = new HashMap<String, Object>();
        Path path = FileSystems.getDefault().getPath(ROOT, fileName);
        try {
            byte[] data = Files.readAllBytes(path);
            if (data.length < off + length) {
                response.put("Exception", "specified offset/length out of range");
                return response;
            }
            byte[] output = new byte[data.length - length];
            System.arraycopy(data, 0, output, 0, off);
            System.arraycopy(data, off + length, output, off, data.length - off - length);
            FileOutputStream fos = new FileOutputStream(ROOT + fileName);
            fos.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.put("ACK", "Success");
        return response;
    }
}
