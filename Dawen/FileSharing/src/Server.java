import java.net.*;
import java.util.Map;

public class Server {
	
	private int port;
	
	private DatagramSocket socket;
	
	public Server(int port){
		this.port = port;
	}
	
	public void run(){
		try{
			 this.socket = new DatagramSocket(port);
			 byte[] receivingBuffer = new byte[1024];
			 byte[] replyBuffer = null;
			 DatagramPacket req = null;
			 Map<String, Object> request = null;
			 Map<String, Object> response = null;
			 DatagramPacket reply = null;
			 while(true){
				System.out.println("------------------------------------");
				//receive UDP packet
				req = new DatagramPacket(receivingBuffer, receivingBuffer.length);
				socket.receive(req);
				System.out.println("[Client]: "+req.getAddress());
				//unmarshal request
				request = Util.unmarshall(receivingBuffer);
				System.out.println("[Unmarshalling]"+request);
				//handle request
				switch((Integer)request.get("op")){
					//read operation
					case 0:
						response = ReqHandler.readFile(request);
						break;
					default:
						System.out.println("[Exception] invalid operation");
				}
				System.out.println("[Mashalling]" + response);
				//marshal respond
				replyBuffer = Util.marshall(response);
				//sending UDP packet
				reply = new DatagramPacket(replyBuffer, replyBuffer.length, req.getAddress(), req.getPort());
				socket.send(reply);
				System.out.println("[Sending to client]");
			 }
		}catch(Exception e){
			System.out.println("[Exception]: "+e.toString());
			socket.close();
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Server s = new Server(9800);
		s.run();
	}
}
