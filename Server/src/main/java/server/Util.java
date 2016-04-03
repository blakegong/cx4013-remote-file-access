package server;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Util {
	
	public static Map<String, Object> unmarshall(byte[] bytes) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		//get number of variables; first 4 bytes
		int varNo = (bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16
                | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
		//remaining bytes
		int index = 4;
		while(map.size() < varNo){
			//format 'key':'data' for one piece of data 
	        //'key'
			int end = index;
	        while (bytes[++end] != '\0');
	        String key = new String(bytes, index, end - index);
	        //':'
	        if (bytes[index = end + 1] != ':'){
	        	System.out.println(map.size()+","+varNo);
	        	throw new IOException("Data format error.");	
	        }
	        index++;
	        //data
	        Object value = null;
	        switch (bytes[index++]) {
	        	//string
	        	case 'r':
	                while (bytes[++end] != '\0') ;
	                value = new String(bytes, index, end - index);
	                index = end+1;
	                break;
                //byte
                case 'b':
                    value = bytes[index + 1];
                    index++;
                    break;
                //short
                case 's':
                    value = (short) (bytes[index + 1] << 8 | bytes[index + 2]);
                    index += 2;
                    break;
	            //integer
	            case 'i':
	                value = (bytes[index] & 0xff) << 24 | (bytes[index + 1] & 0xff) << 16
	                        | (bytes[index + 2] & 0xff) << 8 | (bytes[index + 3] & 0xff);
	                index += 4;
	                break;
                //long
                case 'l':
                    value = (bytes[index + 1] & 0xffL) << 56 | (bytes[index + 2] & 0xffL) << 48
                            | (bytes[index + 3] & 0xffL) << 40 | (bytes[index + 4] & 0xffL) << 32
                            | (bytes[index + 5] & 0xffL) << 24 | (bytes[index + 6] & 0xffL) << 16
                            | (bytes[index + 7] & 0xffL) << 8 | (bytes[index + 8] & 0xffL);
                    index += 8;
                    break;
	            //unknown data type
	            default:
	            	throw new IOException("Type not recognized.");
	        }
	        //put into hash map
	        map.put(key, value);
		}
		return map;
	}
	
    public static byte[] marshall(Map<String, Object> map) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //write how many variables
        int varNo = map.size();
        outputStream.write(new byte[]{(byte) (varNo >>> 24), (byte) (varNo >>> 16), (byte) (varNo >>> 8), (byte) varNo});
        //write remaining data; format ['key':'value']
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            //write key
            outputStream.write(key.getBytes());
            outputStream.write(0);
            outputStream.write(':');
            //string
            if (value instanceof String) {
                outputStream.write('r');
                outputStream.write(((String) value).getBytes());
                outputStream.write(0);
            }
            //byte
            else if (value instanceof Byte) {
                byte b = (Byte) value;
                outputStream.write('b'); // prefix for type Byte
                outputStream.write(b);
            }
            //short
            else if (value instanceof Short) {
                short s = (Short) value;
                outputStream.write('s'); // prefix for type Short
                outputStream.write(new byte[]{(byte) (s >>> 8), (byte) s});
            }
            //integer
            else if (value instanceof Integer) {
                int i = (Integer) value;
                outputStream.write('i'); // prefix for type Integer
                outputStream.write(new byte[]{(byte) (i >>> 24), (byte) (i >>> 16), (byte) (i >>> 8), (byte) i});
            }
            //long
            else if (value instanceof Long) {
                long l = (Long) value;
                outputStream.write('l'); // prefix for type Long
                outputStream.write(new byte[]{(byte) (l >>> 56), (byte) (l >>> 48), (byte) (l >>> 40),
                        (byte) (l >>> 32), (byte) (l >>> 24), (byte) (l >>> 16), (byte) (l >>> 8), (byte) l});
            }
            //exception
            else {
                throw new IOException("Error. Type of value must be Sring, Byte, Short, Integer or Long.");
            }
        }
        return outputStream.toByteArray();
    }

}
