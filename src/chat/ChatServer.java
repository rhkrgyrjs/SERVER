package chat;

/*
 * 채팅 서버 : 유저의 동적인 요청(접속을 유지해야 하는 요청들)을 처리함.
 * 채팅이나 게임의 상호작용 등임.
 * 유저는 요청을 보낼 뿐이며, 자신의 요청이 잘 실행됐는지 여부는 리턴받지 않음. 
 * 유저의 요청은 코드를 통해 분류되고 처리됨.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Server.Start;
import form.ChatForm;
import socket.ReceiveObject;
import socket.SendObject;
import game.GameRoom;
import window.MainMonitor;


public class ChatServer 
{
	// 채팅 서버 포트 하드코딩 
	public static final int CHAT_PORT = 8011;
	
	// 접속중 유저 저장하는 set : 유저id - 유저 소켓. 닫히면 로그아웃 된 것으로 판단함. 
	public static Map<String, Socket> users = Collections.synchronizedMap(new HashMap<String, Socket>());
	
	// 게임방 저장하는 set : 유저id - 게임방 객체. 유저 접속 종료시 게임방 있으면 없애는 루틴 필요. 
	public static Map<String, GameRoom> games = Collections.synchronizedMap(new HashMap<String, GameRoom>());
	
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
			monitorRefresh();
			// sendAll(received);
			while (true)
			{
				try {received = (ChatForm) ReceiveObject.fromClient_throws(socket);} 
				catch (IOException e) 
				{
					Start.mainMonitor.showRequest("[접속 종료] " + received.getId() + "가 접속 종료함");
					ChatServer.users.remove(received.getId());
					monitorRefresh();
					/*if (ChatServer.games.containsKey(received.getId()) == true)
					{
						Start.mainMonitor.showRequest("[게임 종료] " + received.getId() + "가 호스팅하던 게임 종료됨");
						// 진행중인 게임이 있는 유저가 나가면 패배 처리 필요함. 
						ChatServer.games.remove(received.getId());
					} else {}*/
					//ChatForm off = new ChatForm("@ServerMain", received.getId(), received.getNickName(), "["+received.getNickName()+"] 님이 접속을 종료했습니다.");
					//sendAll(off);
					if (received.getRoomId().equals("@ServerMain"))
					{
						// 게임중이지 않은 유저가 나갔을 때 
					}
					else
					{
						// 게임중인 유저가 나갔을 때 
						if (ChatServer.games.containsKey(received.getRoomId()) == true)
						{
							if (ChatServer.games.get(received.getRoomId()).getOnGame() == true)
							{
								if (ChatServer.games.get(received.getRoomId()).getHostId().equals(received.getId()) || ChatServer.games.get(received.getRoomId()).getGuestId().equals(received.getId()))
								{
									// 게임중인 유저중 한 명이 나감 
									ChatServer.games.get(received.getRoomId()).gameEnd(received.getId());
									ChatServer.games.remove(received.getRoomId());
								}
								else
								{
									//관전자가 나감. 관전자 제거하기.
									ChatServer.games.get(received.getRoomId()).spectors.remove(received.getId());
								}
							}
							else
							{
								ChatServer.games.remove(received.getRoomId());
							}
						}
						else
						{
							// 방이 없을떄? 
						}
					}
					break;
				}
				switch (received.getReqType())
				{
					case 1:
						// 채팅 요청일 때 
						sendAll(received);
					break;
				
					case 2:
						// 게임 관련 요청일 때 
						if (received.getMsg().equals("flip"))
						{
							// 카드 펼치기 요청일 때 
							ChatServer.games.get(received.getRoomId()).command = "flip";
							ChatServer.games.get(received.getRoomId()).cardFlip(received.getId());
						}
						else if (received.getMsg().equals("ring"))
						{
							// 종 울리기 요청일 때 
							ChatServer.games.get(received.getRoomId()).command = "ring";
							ChatServer.games.get(received.getRoomId()).ringBell(received.getId());
						}
						// ChatServer.games.get(received.getRoomId()).broadCast("게임 보드 정보");
						// broadCast 메시지 파라미터 수정해서 게임 로그 띄우기 
						// 아니면 cardFlip, ringBell에 boradCast 합치기 
					break;
					
					case 3:
						// 유저가 방에 입장/퇴장 했음을 알아차리기 위해 얻는 더미 객체 받았을때.
					break;
					
					case 4:
						// 유저가 관전 그만하려고 할 때 
						if (ChatServer.games.containsKey(received.getRoomId()) == true)
						{
							if (ChatServer.games.get(received.getRoomId()).spectors.containsKey(received.getId()) == true)
							{
								ChatServer.games.get(received.getRoomId()).spectors.remove(received.getId());
							}
						}
					break;
				}
				
				
			}
		}
		
		// 접속중인 모든 유저에게 브로드캐스팅 하는 메소
		private void sendAll(ChatForm toSend)
		{
			Set<String> keySet = ChatServer.users.keySet();
			if (toSend.getPicBlob() == null)
			{
				Start.mainMonitor.showChat("[" + toSend.getId() +" # "+ toSend.getNickName() +"]");
				Start.mainMonitor.showChat(" >> " + toSend.getMsg());
				Start.mainMonitor.showChat("");
			}
			else if (toSend.getPicBlob() != null)
			{
				Start.mainMonitor.showChat("[" + toSend.getId() +" # "+ toSend.getNickName() +"]");
				Start.mainMonitor.showChat("[사진 전송]");
				Start.mainMonitor.showChat("");
			}
			
			for (String key : keySet)
			{
				try {SendObject.toClient_throws(ChatServer.users.get(key), toSend);}
				catch (IOException e) 
				{
					ChatServer.users.remove(key);
					monitorRefresh();
				}
			}
		}
		
		private void monitorRefresh()
		{
			Set<String> keySet = ChatServer.users.keySet();
			Start.mainMonitor.setUserList(keySet);
			
		}
	}
}
