import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReqHandler {
	
	public static Map<String, Object> readFile(Map<String, Object> request){
		//extract parameters
		String fileName = (String)request.get("filename");
		int offset = (Integer)request.get("offset");
		int len = (Integer)request.get("len");
		System.out.println("[read]"+"filename:"+request.get("filename")+", offset: "+offset+",len: "+len);
		//read from files
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			FileReader file = new FileReader("./directory/"+fileName);
			BufferedReader buffFile = new BufferedReader(file);
			//read from offset
			int index = 0;
			int temp = 0;
			String data = "";
			while((temp = buffFile.read()) > 0){
				if(++index >= offset)
					data += (char)temp;
				if(index >= offset + len)
					break;
			}
			buffFile.close();
			//prepare response
			if(data.length() > 0)
				response.put("data", data);
			else
				response.put("Exception", "invalid pair of offset and len");
		}catch (FileNotFoundException e) {
			System.out.println("[Exception] cannot find file");
			response.put("Exception", "cannot find file");
		}catch (IOException e) {
			System.out.println("[Exception] cannot read file");
			response.put("Exception", "cannot read file");
		}
		return response;
	}
	
}
