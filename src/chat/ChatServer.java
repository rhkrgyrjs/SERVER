package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import form.ChatForm;
import socket.ReceiveObject;
import socket.SendObject;


public class ChatServer 
{
	// 채팅 서버 포트 하드코딩 
	public static final int CHAT_PORT = 8011;
	
	// 유저id - 유저 소켓. 닫히면 로그아웃 된 것으로 판단함. 
	public static Map<String, Socket> users = Collections.synchronizedMap(new HashMap<String, Socket>());
	
	// 싱글톤 처리 
	private ChatServer() {}
	
	private static ChatServer single_instance = null;
	public static ChatServer getInstance()
	{
		if (single_instance == null) single_instance = new ChatServer();
		return single_instance;
	}
	
	// 채팅 서버 시작 메소드 
	public void start()
	{
		System.out.println("[채팅 서버] 시작됨");
		ChatReqThread chatReqThread = new ChatReqThread();
		chatReqThread.start();
	}
	
	// 통신요청 받는 쓰레드 
	class ChatReqThread extends Thread
	{	
		@Override
		public void run()
		{
			ServerSocket reqSocket = null;
			Socket comSocket = null;
			try
			{
				reqSocket = new ServerSocket(CHAT_PORT);
				while(true)
				{
					comSocket = reqSocket.accept();
					ChatHandleThread chatHandleThread = new ChatHandleThread(comSocket);
					chatHandleThread.start();
				}
			}
			catch (IOException e) {e.printStackTrace();}
			finally
			{
				if (reqSocket != null)
				{
					try {reqSocket.close();}
					catch (IOException e) {e.printStackTrace();}
				}
			}
		}
	}
	
	// 유저 접속중이면 계속 상주하는 쓰레드(소켓 닫히면 소멸) -> 들어오는 채팅 모든 유저에게 브로드캐스팅.
	// 유저는 자신이 속한 채팅방과 같은 채팅방을 대상으로 하는 메시지면 받아들이고, 아니면 파기함.
	class ChatHandleThread extends Thread
	{
		Socket socket = null;
		ChatForm received = null;
		String socketId = null;
		ChatHandleThread(Socket socket) {this.socket = socket;}
		
		@Override
		public void run()
		{
			received = (ChatForm) ReceiveObject.fromClient(socket);
			ChatServer.users.put(received.getId(), socket);
			sendAll(received);
			while (true)
			{
				try {received = (ChatForm) ReceiveObject.fromClient_throws(socket);} 
				catch (IOException e) 
				{
					ChatServer.users.remove(received.getId());
					break;
				}
				sendAll(received);
			}
		}
		
		// 접속중인 모든 유저에게 브로드캐스팅 하는 메소
		private void sendAll(ChatForm toSend)
		{
			Set<String> keySet = ChatServer.users.keySet();
			
			for (String key : keySet)
			{
				try {SendObject.toClient_throws(ChatServer.users.get(key), toSend);}
				catch (IOException e) {ChatServer.users.remove(key);}
			}

			System.out.println("접속중 유저 목록");
			for (String key : keySet)
				System.out.println(key);
		}
	}
}
