package socket;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class SendObject 
{
	private SendObject() {}
	
	public static void toClient(Socket socket, Object toSend)
	{
		OutputStream os = null;
		ObjectOutputStream oos = null;
		try
		{
			os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(toSend);
			oos.flush();
		}
		catch (IOException e) { try {socket.close();} catch(IOException a) {}}
		//catch (SocketException a) { try {socket.close();} catch(IOException e) {}}
	}
	
	public static void toClient_throws(Socket socket, Object toSend) throws IOException
	{
		OutputStream os = null;
		ObjectOutputStream oos = null;
		os = socket.getOutputStream();
		oos = new ObjectOutputStream(os);
		oos.writeObject(toSend);
		oos.flush();
	}
}
