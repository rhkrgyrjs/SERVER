package game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import Server.Start;
import chat.ChatServer;
import db.Query;
import elo.EloCalculator;
import form.Card;
import form.ChatForm;
import form.GameBoardInfoForm;
import form.LoginReplyForm;
import image.Blob;
import socket.SendObject;

public class GameRoom 
{
	// 게임중인지 여부 저장 
	private boolean onGame = false;
	
	// 방 이름 / 방장(호스트ID) / 참여자(Guest)ID 저장 
	private String roomName = null;
	private String hostId = null;
	private String guestId = null;
	
	// 관전자의 소켓 저장
	public Map<String, Socket> spectors = Collections.synchronizedMap(new HashMap<String, Socket>());
	
	// 누구의 턴인지? -> true : 호스트 턴, false : 게스트 턴 
	private boolean turn = true;
	
	// 덱 arrayList로 관리 
	private ArrayList<Card> hostDeck = null;
	private ArrayList<Card> guestDeck = null;
	
	// 덱 장수 카운터 
	private int hostDeckCount = 0;
	private int guestDeckCount = 0;
	
	private boolean ringCount = false;
	
	// 유저의 명령 저장. ring : 벨울리기, flip : 카드펼치기. 
	public String command = null;
	
	// 펼쳐진 카드 
	private Card A = null;
	private Card B = null;
	private Card C = null;
	private Card D = null;
	
	// 카드 뭉치들
	private ArrayList<Card> ADeck = null;
	private ArrayList<Card> BDeck = null;
	private ArrayList<Card> CDeck = null;
	private ArrayList<Card> DDeck = null;
	
	// 앞면 과일 갯수 카운터 
	private int bananaNum = 0;
	private int limeNum = 0;
	private int strawberryNum = 0;
	private int plumNum = 0;
	
	// 편의상 유저의 소켓 저장
	private Socket hostSocket =  null;
	private Socket guestSocket =  null;
	
	// 생성자
	public GameRoom(String roomName, String hostId)
	{
		this.roomName = roomName;
		this.hostId = hostId;
	}
	
	private void shuffleDeck(ArrayList<Card> deck)
	{
		Collections.shuffle(deck);
	}
	
	// 게임 시작전 초기화 
	private void initGame()
	{
		// 호스트부터 카드 펼침
		turn = true;
		
		// 덱 초기화 
		hostDeck = new ArrayList<Card>();
		guestDeck = new ArrayList<Card>();
		
		ADeck = new ArrayList<Card>();
		BDeck = new ArrayList<Card>();
		CDeck = new ArrayList<Card>();
		DDeck = new ArrayList<Card>();
		// 0:빈카드, 1:바나나, 2:라임, 3:딸기, 4:자두 
		// 1. host의 덱에 게임에 필요한 모든 카드를 추가한다.
		for (int i=0; i<5; i++)
		{
			// 과일 1개 그려진 카드 : 5장씩 
			hostDeck.add(new Card(1, 1));
			hostDeck.add(new Card(2, 1));
			hostDeck.add(new Card(3, 1));
			hostDeck.add(new Card(4, 1));
		}
		for (int i=0; i<3; i++)
		{
			// 과일 2개, 3개 그려진 카드 : 3장씩 
			hostDeck.add(new Card(1, 2));
			hostDeck.add(new Card(2, 2));
			hostDeck.add(new Card(3, 2));
			hostDeck.add(new Card(4, 2));
			hostDeck.add(new Card(1, 3));
			hostDeck.add(new Card(2, 3));
			hostDeck.add(new Card(3, 3));
			hostDeck.add(new Card(4, 3));
		}
		for (int i=0; i<2; i++)
		{
			// 과일 4개 그려진 카드 : 2장씩 
			hostDeck.add(new Card(1, 4));
			hostDeck.add(new Card(2, 4));
			hostDeck.add(new Card(3, 4));
			hostDeck.add(new Card(4, 4));
		}
		// 과일 5개 그려진 카드 : 1장씩 
		hostDeck.add(new Card(1, 5));
		hostDeck.add(new Card(2, 5));
		hostDeck.add(new Card(3, 5));
		hostDeck.add(new Card(4, 5));
		
		// hostDeck에 모든 카드 추가 완료. 
		// hostDeck 섞기 
		shuffleDeck(hostDeck);
		
		// hostDeck에서 상위 23장 guestDeck에 넣기 -> 카드의 분배 완료.  
		for (int i=0; i<28; i++)
		{
			guestDeck.add(hostDeck.get(0));
			hostDeck.remove(0);
		}
		
		// 덱의 장수 세기 
		hostDeckCount = hostDeck.size();
		guestDeckCount = guestDeck.size();
		
		// A, B, C, D 에 올려진 카드 비우기 
		A = new Card(0, 0);
		B = new Card(0, 0);
		C = new Card(0, 0);
		D = new Card(0, 0);
	}
		
	
	// 유저 초대 + 게임 시작하기
	public void inviteAndStart(String guestId)
	{
		this.guestId = guestId;
		// 게임 시작하는 루틴 필요. 
		hostSocket =  ChatServer.users.get(hostId);
		guestSocket =  ChatServer.users.get(guestId);
		this.onGame = true;
		// 호스트에게 게스트가 들어왔음을 알리는 객체 전송하기. 
		ChatForm toSend = new ChatForm(3, hostId, guestId, "@Server", "게스트의 ID정보를 전송함");
		try {SendObject.toClient_throws(hostSocket, toSend);}
		catch (IOException e) {/*호스트 패배*/}
		initGame();
		broadCast("< 게임이 시작되었습니다! >");
		broadCast("[" + hostId + "] 의 턴...\n");
		System.out.println(this.roomName + "방, 방장이름 : " + this.hostId + "에" + this.guestId + "초대됨" );
	}
	
	public void inviteSpector(String id)
	{
		spectors.put(id, ChatServer.users.get(id));
		broadCast("< "+ id + " 관전 중.. >\n\n");
	}
	
	public void gameEnd(String loserId)
	{
		// 패자의 id 인자로 받기 
		// 승자/패자 레이팅 계산후 업데이트 
		// 승자/패자 알리고 게임 마무리
		// 게임방 소멸시키기
		this.onGame = false;
		
		// 전적 업데이트 
		int hostWin = 0;
		int hostLose = 0;
		int hostElo = 0;
		int guestWin = 0;
		int guestLose = 0;
		int guestElo = 0;
		String[] hostIds = {hostId};
		ResultSet hostRes = Query.getResultSet("SELECT win, lose, elo FROM userinfo WHERE id=?", 1, hostIds);
		try 
		{
			if (hostRes.next()) 
				{
					hostWin = Integer.parseInt(hostRes.getString("win"));
					hostLose = Integer.parseInt(hostRes.getString("lose"));
					hostElo = Integer.parseInt(hostRes.getString("elo"));
				}
		}
		catch(SQLException e) {}
		Query.close();
		String[] guestIds = {guestId};
		ResultSet guestRes = Query.getResultSet("SELECT win, lose, elo FROM userinfo WHERE id=?", 1, guestIds);
		try 
		{
			if (guestRes.next()) 
				{
					guestWin = Integer.parseInt(guestRes.getString("win"));
					guestLose = Integer.parseInt(guestRes.getString("lose"));
					guestElo = Integer.parseInt(guestRes.getString("elo"));
				}
		}
		catch(SQLException e) {}
		Query.close();
		
		if (loserId.equals(guestId))
		{
			// 게스트가 패배했을 경우 
			// 호스트 승수 늘리기
			// 게스트 패배수 늘리기 
			// 호스트 elo, 게스트 elo 조정 
			hostWin += 1;
			guestLose += 1;
			int newHostElo = EloCalculator.calc(hostElo, guestElo, "w");
			int newGuestElo = EloCalculator.calc(guestElo, hostElo, "l");

			String[] hostWinParam = {Integer.toString(hostWin), Integer.toString(newHostElo), hostId};
			Query.execute("UPDATE userinfo SET win=?, elo=? WHERE id=?", 3, hostWinParam);
			Query.close();
			
			String[] hostWinParam_1 = {Integer.toString(guestLose), Integer.toString(newGuestElo), guestId};
			Query.execute("UPDATE userinfo SET lose=?, elo=? WHERE id=?", 3, hostWinParam_1);
			Query.close();
			
			// 게임 끝났음을 알리는 메시지 전송
			// 승자의 아이디 id 에 담아 전송함. 
			ChatForm toSend = new ChatForm(4, hostId, hostId, "@Server", hostId + "승리");
			try {SendObject.toClient_throws(hostSocket, toSend);}
			catch (IOException e) {}
			try {SendObject.toClient_throws(guestSocket, toSend);}
			catch (IOException e) {}
			ChatForm toSend_spec = new ChatForm(6, hostId, hostId, "@Server", hostId + "승리");
			sendAll(toSend_spec);
		}
		else if (loserId.equals(hostId))
		{
			// 호스트가 패배했을 경우 
			// 게스트 승수 늘리기 
			// 호스트 패배수 늘리기 
			// 호스트 elo, 게스트 elo 조정 
			guestWin += 1;
			hostLose += 1;
			int newGuestElo = EloCalculator.calc(guestElo, hostElo, "w");
			int newHostElo = EloCalculator.calc(hostElo, guestElo, "l");
			
			String[] guestWinParam = {Integer.toString(guestWin), Integer.toString(newGuestElo), guestId};
			Query.execute("UPDATE userinfo SET win=?, elo=? WHERE id=?", 3, guestWinParam);
			Query.close();
			
			String[] hostWinParam_1 = {Integer.toString(hostLose), Integer.toString(newHostElo), hostId};
			Query.execute("UPDATE userinfo SET lose=?, elo=? WHERE id=?", 3, hostWinParam_1);
			Query.close();

			// 게임 끝났음을 알리는 메시지 전송
			// 승자의 아이디 id 에 담아 전송함. 
			ChatForm toSend = new ChatForm(4, hostId, guestId, "@Server", guestId + "승리");
			try {SendObject.toClient_throws(hostSocket, toSend);}
			catch (IOException e) {}
			try {SendObject.toClient_throws(guestSocket, toSend);}
			catch (IOException e) {}
			ChatForm toSend_spec = new ChatForm(6, hostId, guestId, "@Server", guestId + "승리");
			sendAll(toSend_spec);
		}
		
		// 방 없애기. 
		ChatServer.games.remove(hostId);
		
		// 테스트 
		System.out.println("패자의 ID : " + loserId);
	}
	
	public void gameEndDraw()
	{
		// 카드를 한번도 가져간 사람이 없는 경우 (ringCount = false일 때)
		// 무승부 처리 함. 
		// 두 유저의 elo 변동시키고, 무승부 수 하나씩 올리기. 
		// 게임방 소멸시키기. 
		int hostDraw = 0;
		int guestDraw = 0;
		int hostElo = 0;
		int guestElo = 0;

		String[] hostIds = {hostId};
		ResultSet hostRes = Query.getResultSet("SELECT draw, elo FROM userinfo WHERE id=?", 1, hostIds);
		try 
		{
			if (hostRes.next()) 
				{
					hostDraw = Integer.parseInt(hostRes.getString("draw"));
					hostElo = Integer.parseInt(hostRes.getString("elo"));
				}
		}
		catch(SQLException e) {}
		Query.close();
		
		String[] guestIds = {guestId};
		ResultSet guestRes = Query.getResultSet("SELECT draw, elo FROM userinfo WHERE id=?", 1, guestIds);
		try 
		{
			if (guestRes.next()) 
				{
					guestDraw = Integer.parseInt(guestRes.getString("draw"));
					guestElo = Integer.parseInt(guestRes.getString("elo"));
				}
		}
		catch(SQLException e) {}
		Query.close();
		
		guestDraw += 1;
		hostDraw += 1;
		
		int newHostElo = EloCalculator.calc(hostElo, guestElo, "d");
		int newGuestElo = EloCalculator.calc(guestElo, hostElo, "d");
		
		String[] hostParam = {Integer.toString(hostDraw), Integer.toString(newHostElo), hostId};
		Query.execute("UPDATE userinfo SET draw=?, elo=? WHERE id=?", 3, hostParam);
		Query.close();
		
		String[] guestParam = {Integer.toString(guestDraw), Integer.toString(newGuestElo), guestId};
		Query.execute("UPDATE userinfo SET draw=?, elo=? WHERE id=?", 3, guestParam);
		Query.close();
		
		ChatForm toSend = new ChatForm(4, hostId, "@Draw", "@Server", "무승부");
		try {SendObject.toClient_throws(hostSocket, toSend);}
		catch (IOException e) {}
		try {SendObject.toClient_throws(guestSocket, toSend);}
		catch (IOException e) {}
		ChatForm toSend_spec = new ChatForm(6, hostId, "@Draw", "@Server", "무승부");
		sendAll(toSend_spec);
		
		// 방 없애기. 
		ChatServer.games.remove(hostId);
		
	}
	
	public GameBoardInfoForm getBoardInfo()
	{
		hostDeckCount = hostDeck.size();
		guestDeckCount = guestDeck.size();
		GameBoardInfoForm result = new GameBoardInfoForm(A, B, C, D, hostDeckCount, guestDeckCount);
		// 게임 승패 판별해서 게임 끝났는지 판별하자. 
		result.setCommand(command);
		return result;
	}
	

	private void sendAll(ChatForm toSend)
	{
		Set<String> keySet = spectors.keySet();
		
		for (String key : keySet)
		{
			try {SendObject.toClient_throws(spectors.get(key), toSend);}
			catch (IOException e) 
			{
				spectors.remove(key);
			}
		}
	}
	
	// 게임이 끝났는지 판별하는 함수.
	public boolean isGameEnd()
	{
		// 자신의 턴이 왔는데 카드를 펼칠 수 없는 상황인 경우 -> 패배. 
		if (turn == true)
		{
			if (hostDeck.size() == 0)
				return true;
		}
		else if (turn == false)
		{
			if (guestDeck.size() == 0)
				return true;
		}
		return false;
	}
	
	public void cardFlip(String id)
	{
		if (isGameEnd())
		{
			// 게임 끝났을때 루틴 짜기.
			if ((turn == true) && (ringCount == true))
			{
				// 호스트 패배 
				gameEnd(hostId);
			}
			else if (turn == false && (ringCount == true))
			{
				// 게스트 패배 
				gameEnd(guestId);
			}
			else if (ringCount == false)
			{
				// 둘 중 아무도 카드를 가져가지 못한 경우 (과일 5개가 한번도 안 나왔을때) 무승부 처리. 
				gameEndDraw();
			}
		}
		// id 유저가 카드 펼침
		if (id.equals(hostId) && (turn==true) && (bananaNum != 5) && (limeNum != 5) && (strawberryNum != 5) && (plumNum != 5) && (hostDeck.size() != 0))
		{
			// 호스트가 카드 펼쳤을 경우 
			System.out.println("호스트가 카드 펼침 ");
			Card hostDeckTopCard = hostDeck.get(0);
			hostDeck.remove(0);
			Random random = new Random();
			int randomNumber = random.nextInt(4) + 1;
			switch(randomNumber)
			{
				case 1:
					this.A = hostDeckTopCard;
					ADeck.add(hostDeckTopCard);
				break;
				
				case 2:
					this.B = hostDeckTopCard;
					BDeck.add(hostDeckTopCard);
				break;
				
				case 3:
					
					this.C = hostDeckTopCard;
					CDeck.add(hostDeckTopCard);
				break;
				
				case 4:
					this.D = hostDeckTopCard;
					DDeck.add(hostDeckTopCard);
				break;
			}
			bananaNum = 0;
			limeNum = 0;
			strawberryNum = 0;
			plumNum = 0;
			switch(A.getFruit())
			{
				case 1:
					bananaNum += A.getNumber();
				break;
				case 2:
					limeNum += A.getNumber();
				break;
				case 3:
					strawberryNum += A.getNumber();
				break;
				case 4:
					plumNum += A.getNumber();
				break;
			}
			switch(B.getFruit())
			{
				case 1:
					bananaNum += B.getNumber();
				break;
				case 2:
					limeNum += B.getNumber();
				break;
				case 3:
					strawberryNum += B.getNumber();
				break;
				case 4:
					plumNum += B.getNumber();
				break;
			}
			switch(C.getFruit())
			{
				case 1:
					bananaNum += C.getNumber();
				break;
				case 2:
					limeNum += C.getNumber();
				break;
				case 3:
					strawberryNum += C.getNumber();
				break;
				case 4:
					plumNum += C.getNumber();
				break;
			}
			switch(D.getFruit())
			{
				case 1:
					bananaNum += D.getNumber();
				break;
				case 2:
					limeNum += D.getNumber();
				break;
				case 3:
					strawberryNum += D.getNumber();
				break;
				case 4:
					plumNum += D.getNumber();
				break;
			}
			turn = false;
			if (isGameEnd())
			{
				// 게임 끝났을때 루틴 짜기.
				if ((turn == true) && (ringCount == true))
				{
					// 호스트 패배 
					gameEnd(hostId);
				}
				else if (turn == false && (ringCount == true))
				{
					// 게스트 패배 
					gameEnd(guestId);
				}
				else if (ringCount == false)
				{
					// 둘 중 아무도 카드를 가져가지 못한 경우 (과일 5개가 한번도 안 나왔을때) 무승부 처리. 
					gameEndDraw();
				}
			}
			broadCast("[" + id + "] 가 카드 펼침");
			command = null;
			broadCast("[" + guestId + "] 의 턴...\n");
			
		}
		else if (id.equals(guestId) && (turn==false) && (bananaNum != 5) && (limeNum != 5) && (strawberryNum != 5) && (plumNum != 5) && (guestDeck.size() != 0))
		{
			// 게스트가 카드 펼쳤을 경우 
			System.out.println("게스트가 카드 펼침 ");
			Card guestDeckTopCard = guestDeck.get(0);
			guestDeck.remove(0);
			Random random = new Random();
			int randomNumber = random.nextInt(4) + 1;
			switch(randomNumber)
			{
				case 1:
					this.A = guestDeckTopCard;
					ADeck.add(guestDeckTopCard);
				break;
				
				case 2:
					this.B = guestDeckTopCard;
					BDeck.add(guestDeckTopCard);
				break;
				
				case 3:
					this.C = guestDeckTopCard;
					CDeck.add(guestDeckTopCard);
				break;
				
				case 4:
					this.D = guestDeckTopCard;
					DDeck.add(guestDeckTopCard);
				break;
			}
			bananaNum = 0;
			limeNum = 0;
			strawberryNum = 0;
			plumNum = 0;
			switch(A.getFruit())
			{
				case 1:
					bananaNum += A.getNumber();
				break;
				case 2:
					limeNum += A.getNumber();
				break;
				case 3:
					strawberryNum += A.getNumber();
				break;
				case 4:
					plumNum += A.getNumber();
				break;
			}
			switch(B.getFruit())
			{
				case 1:
					bananaNum += B.getNumber();
				break;
				case 2:
					limeNum += B.getNumber();
				break;
				case 3:
					strawberryNum += B.getNumber();
				break;
				case 4:
					plumNum += B.getNumber();
				break;
			}
			switch(C.getFruit())
			{
				case 1:
					bananaNum += C.getNumber();
				break;
				case 2:
					limeNum += C.getNumber();
				break;
				case 3:
					strawberryNum += C.getNumber();
				break;
				case 4:
					plumNum += C.getNumber();
				break;
			}
			switch(D.getFruit())
			{
				case 1:
					bananaNum += D.getNumber();
				break;
				case 2:
					limeNum += D.getNumber();
				break;
				case 3:
					strawberryNum += D.getNumber();
				break;
				case 4:
					plumNum += D.getNumber();
				break;
			}
			turn = true;
			if (isGameEnd())
			{
				// 게임 끝났을때 루틴 짜기.
				if ((turn == true) && (ringCount == true))
				{
					// 호스트 패배 
					gameEnd(hostId);
				}
				else if (turn == false && (ringCount == true))
				{
					// 게스트 패배 
					gameEnd(guestId);
				}
				else if (ringCount == false)
				{
					// 둘 중 아무도 카드를 가져가지 못한 경우 (과일 5개가 한번도 안 나왔을때) 무승부 처리. 
					gameEndDraw();
				}
			}
			broadCast("[" + id + "] 가 카드 펼침");
			command = null;
			broadCast("[" + hostId + "] 의 턴...\n");
		}
	}
	
	public void ringBell(String id)
	{
		// 만약 카드를 한번이라도 딴 사람이 있다면 -> 무승부는 아님. 
		if ( (ADeck.size() != 0) || (BDeck.size() != 0) || (CDeck.size() != 0) || (DDeck.size() != 0) )
			ringCount = true;
		
		// id 유저가 벨 울림 
		if (id.equals(hostId))
		{
			// 호스트가 종 울렸을 경우 
			// 맞게 울린경우 : 카드 다 가져가기 
			// 틀리게 울린경우 : 카드 다 상대에게 주기 .
			if ((bananaNum == 5) || (limeNum == 5) || (strawberryNum == 5) || (plumNum == 5))
			{
				// 호스트가 종 맞게 울린 경우 
				for (int i=0; i<ADeck.size(); i++)
					hostDeck.add(ADeck.get(i));
				for (int i=0; i<BDeck.size(); i++)
					hostDeck.add(BDeck.get(i));
				for (int i=0; i<CDeck.size(); i++)
					hostDeck.add(CDeck.get(i));
				for (int i=0; i<DDeck.size(); i++)
					hostDeck.add(DDeck.get(i));
				shuffleDeck(hostDeck);
				A = new Card(0,0);
				B = new Card(0,0);
				C = new Card(0,0);
				D = new Card(0,0);
				ADeck.clear();
				BDeck.clear();
				CDeck.clear();
				DDeck.clear();
				bananaNum = 0;
				limeNum = 0;
				strawberryNum = 0;
				plumNum = 0;
				turn = true;
				broadCast("[" + id + "] 가 종 울림");
				command = null;
				broadCast("[" + id + "] 가 카드 가져감");
				broadCast("[" + hostId + "] 의 턴...\n");
			}
			else
			{
				// 호스트가 종 틀리게 울린 경우 
				for (int i=0; i<ADeck.size(); i++)
					guestDeck.add(ADeck.get(i));
				for (int i=0; i<BDeck.size(); i++)
					guestDeck.add(BDeck.get(i));
				for (int i=0; i<CDeck.size(); i++)
					guestDeck.add(CDeck.get(i));
				for (int i=0; i<DDeck.size(); i++)
					guestDeck.add(DDeck.get(i));
				shuffleDeck(guestDeck);
				A = new Card(0,0);
				B = new Card(0,0);
				C = new Card(0,0);
				D = new Card(0,0);
				ADeck.clear();
				BDeck.clear();
				CDeck.clear();
				DDeck.clear();
				bananaNum = 0;
				limeNum = 0;
				strawberryNum = 0;
				plumNum = 0;
				turn = true;
				broadCast("[" + id + "] 가 종 울림");
				command = null;
				broadCast("[" + guestId + "]가 카드 가져감");
				broadCast("[" + hostId + "] 의 턴...");
			}
			System.out.println("호스트가 종 울림 ");
		}
		else if (id.equals(guestId))
		{
			// 게스트가 종 울렸을 경우 
			if ((bananaNum == 5) || (limeNum == 5) || (strawberryNum == 5) || (plumNum == 5))
			{
				// 게스트가 종 맞게 울린 경우 
				for (int i=0; i<ADeck.size(); i++)
					guestDeck.add(ADeck.get(i));
				for (int i=0; i<BDeck.size(); i++)
					guestDeck.add(BDeck.get(i));
				for (int i=0; i<CDeck.size(); i++)
					guestDeck.add(CDeck.get(i));
				for (int i=0; i<DDeck.size(); i++)
					guestDeck.add(DDeck.get(i));
				shuffleDeck(guestDeck);
				A = new Card(0,0);
				B = new Card(0,0);
				C = new Card(0,0);
				D = new Card(0,0);
				ADeck.clear();
				BDeck.clear();
				CDeck.clear();
				DDeck.clear();
				bananaNum = 0;
				limeNum = 0;
				strawberryNum = 0;
				plumNum = 0;
				turn = false;
				broadCast("[" + id + "] 가 종 울림");
				command = null;
				broadCast("[" + guestId + "]가 카드 가져감");
				broadCast("[" +	guestId + "] 의 턴...\n");
			}
			else
			{
				// 게스트가 종 틀리게 울린 경우 
				for (int i=0; i<ADeck.size(); i++)
					hostDeck.add(ADeck.get(i));
				for (int i=0; i<BDeck.size(); i++)
					hostDeck.add(BDeck.get(i));
				for (int i=0; i<CDeck.size(); i++)
					hostDeck.add(CDeck.get(i));
				for (int i=0; i<DDeck.size(); i++)
					hostDeck.add(DDeck.get(i));
				shuffleDeck(hostDeck);
				A = new Card(0,0);
				B = new Card(0,0);
				C = new Card(0,0);
				D = new Card(0,0);
				ADeck.clear();
				BDeck.clear();
				CDeck.clear();
				DDeck.clear();
				bananaNum = 0;
				limeNum = 0;
				strawberryNum = 0;
				plumNum = 0;
				turn = false;
				broadCast("[" + id + "] 가 종 울림");
				command = null;
				broadCast("[" + hostId + "]가 카드 가져감");
				broadCast("[" + guestId + "] 의 턴...\n");
			}
			System.out.println("게스트가 종 울림 ");
		}
		if (isGameEnd())
		{
			// 게임 끝났을때 루틴 짜기.
			if ((turn == true) && (ringCount == true))
			{
				// 호스트 패배 
				gameEnd(hostId);
			}
			else if (turn == false && (ringCount == true))
			{
				// 게스트 패배 
				gameEnd(guestId);
			}
			else if (ringCount == false)
			{
				// 둘 중 아무도 카드를 가져가지 못한 경우 (과일 5개가 한번도 안 나왔을때) 무승부 처리. 
				gameEndDraw();
			}
		}
	}
	
	// 게임판의 정보 전달함. 
	public void broadCast(String msg)
	{
		ChatForm toSend = new ChatForm(2, hostId, "@Server", "@Server", msg);
		toSend.setBoardInfo(getBoardInfo());
		try {SendObject.toClient_throws(hostSocket, toSend);}
		catch (IOException e) {/*호스트 패배*/}
		try {SendObject.toClient_throws(guestSocket, toSend);}
		catch (IOException e) {/*게스트 패배*/}
		// 관전자에게 보내는 루틴도 추가하자. 
		ChatForm toSend_spec = new ChatForm(5, hostId, "@Server", "@Server", msg);
		toSend_spec.setBoardInfo(getBoardInfo());
		sendAll(toSend_spec);
	}
	
	public String getHostId() {return this.hostId;}
	public String getGuestId() {return this.guestId;}
	public String getRoomName() {return this.roomName;}
	public boolean getOnGame() {return this.onGame;}
	
	// 테스트용 
	public static void main(String[] args)
	{
		GameRoom gr = new GameRoom("test", "test");
		gr.initGame();
	}
	
}
