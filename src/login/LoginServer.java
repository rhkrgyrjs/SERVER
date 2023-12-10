package login;

import java.net.ServerSocket;
import java.sql.*;
import java.util.Set;

import javax.imageio.ImageIO;

import Server.Start;
import chat.ChatServer;
import form.LoginReplyForm;
import form.LoginRequestForm;
import game.GameRoom;
import image.Blob;

import java.net.*;
import java.awt.image.BufferedImage;
import java.io.*;

import socket.*;

import window.MainMonitor;

import db.Query;

/*
 * 로그인 서버 : 정적인 요청(서버-클라이언트 접속을 유지할 필요가 없는 요청들)을 처리함. 
 * 각 요청의 종료는 요청 코드로 구분되며, 처리됨.
 * 유저는 자신의 요청이 잘 실행되었는지 여부를 리턴받음.  
 * 유저의 요청이 종료된 후에는 접속을 끊음. 
 */

public class LoginServer 
{
	public static final int LOGIN_PORT = 8010;
	
	// 싱글톤 처리 
	private LoginServer() {}
	
	private static LoginServer single_instance = null;
	public static LoginServer getInstance()
	{
		if (single_instance == null) single_instance = new LoginServer();
		return single_instance;
	}
	
	public void start()
	{
		LoginReqThread loginReqThread = new LoginReqThread();
		loginReqThread.start();
		System.out.println("[로그인 서버] 시작됨");
	}

	class LoginReqThread extends Thread
	{	
		@Override
		public void run()
		{
			ServerSocket reqSocket = null;
			Socket comSocket = null;
			try
			{
				reqSocket = new ServerSocket(LOGIN_PORT);
				while(true)
				{
					comSocket = reqSocket.accept();
					LoginHandleThread loginHandleThread = new LoginHandleThread(comSocket);
					loginHandleThread.start();
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
	
	class LoginHandleThread extends Thread
	{
		Socket socket = null;
		LoginRequestForm received = null;
		LoginReplyForm toSend = null;
		LoginHandleThread(Socket socket)
		{
			this.socket = socket;
		}
		
		@Override
		public void run()
		{
			received = (LoginRequestForm) ReceiveObject.fromClient(socket);
			switch (received.getReqType())
			{
				case 1:
					// 로그인 요청일 때 -> DB에 조회 해보고 로그인 할지 말지. 
					String[] parameters1 = {received.getId(), received.getPw()};
					ResultSet result1 = Query.getResultSet("SELECT pw, id, nickname FROM userinfo WHERE id=?", 1, parameters1);
					try {
					if (result1.next())
					{
						if (result1.getString("pw").equals(received.getPw()))
						{
							if (ChatServer.users.containsKey(received.getId()) == false)
							{
							toSend = new LoginReplyForm(1, true, "로그인 성공");
							toSend.setId(result1.getString("id"));
							toSend.setNickName(result1.getString("nickname"));
							}
							else
							{
								toSend = new LoginReplyForm(1, false, "로그인 실패");
							}
						}
						else {toSend = new LoginReplyForm(1, false, "로그인 실패");}
					}
					else
					{
						toSend = new LoginReplyForm(1, false, "로그인 실패");
					}}
					catch (SQLException e) {e.printStackTrace();}
					Query.close();
					// 모니터 리프레시  
					String monitorMessage1 = "[로그인 요청] " + received.getId() + "가 로그인 요청함...    ";
					if (toSend.getResult() == true)
						monitorMessage1 += "성공";
					else
						monitorMessage1 += "실패";
					Start.mainMonitor.showRequest(monitorMessage1);
				break;
				
				case 2:
					// 회원가입 요청일 때 -> DB에 넣고 요청 회신 
					String[] parameters2 = {received.getId(), received.getPw(), received.getNickName(), received.getPhoneNumber(), received.getEmail(), received.getZipcode(), received.getPw(), received.getAddress(), "0", "0", "0", "1500"};
					boolean result2 = Query.execute("INSERT INTO userinfo (id, pw, nickname, phonenumber, email, zipcode, used_pw, address, win, lose, draw, elo)" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 12, parameters2);
					if (result2 == true) 
					{
						BufferedImage profile = Blob.toBufferedImage(received.getPicBlob());
						File outputFile = new File("profile/" + received.getId() + ".jpg");
						try{ImageIO.write(profile, "jpg", outputFile);} catch (IOException a) {a.printStackTrace();}
						toSend = new LoginReplyForm(2, true, "회원가입 성공");
					}
					else toSend = new LoginReplyForm(2, false, "회원가입 실패");
					Query.close();
					// 모니터 리프레시 
					String monitorMessage2 = "[회원가입 요청] ID " + received.getId() + "로 회원가입 요청함...    ";
					if (toSend.getResult() == true)
						monitorMessage2 += "성공";
					else
						monitorMessage2 += "실패";
					Start.mainMonitor.showRequest(monitorMessage2);
				break;
				
				case 3:
					// id 중복체크 요청일 때 -> DB에 조회 해보고 회신 
					String[] parameters3 = {received.getId()};
					ResultSet result3 = Query.getResultSet("SELECT id FROM userinfo WHERE id=?", 1, parameters3);
					try 
					{
						if (result3.next()) toSend = new LoginReplyForm(3, false, "ID 사용 불가능");
						else toSend = new LoginReplyForm(3, true, "ID 사용가능");
					}
					catch(SQLException e) {}
					Query.close();
					// 모니터 리프레시 
					String monitorMessage3 = "[ID중복체크 요청] " + received.getId() + "가 사용 가능한지...    ";
					if (toSend.getResult() == true)
						monitorMessage3 += "사용가능";
					else
						monitorMessage3 += "사용불가";
					Start.mainMonitor.showRequest(monitorMessage3);
				break;
				
				case 4:
					// 우편번호 찾기 요청일 때 -> DB에 조회하고, 가능성 있는 리스트 뽑아서 회신 
					// 시도+시군구 / 시도+시군구+도로명 / 시도+시군구+도로명+건물번호1 까지만 검색해주자. 
					String[] parameters4 = null;
					int paraNum = 0;
					String sql = "SELECT zipcode, sido, sigungu, doro, buildno1, buildno2, buildname FROM zipcode WHERE (sido LIKE ?) AND (sigungu LIKE ?)";
					ResultSet result4 = null;
					String res[][] = null;
					if (received.getDoro() == null)
					{
						// 시도+시군구 
						paraNum = 2;
						parameters4 = new String[] {received.getDo(), received.getSi()};
					}
					else if (received.getZipNum() == null)
					{
						// 시도+시군구+도로명 
						paraNum = 3;
						sql = sql + " AND (doro LIKE ?)";
						parameters4 = new String[] {received.getDo(), received.getSi(), received.getDoro()};
					}
					else if (received.getZipNum() != null)
					{
						// 시도+시군구+도로명+건물번호 
						paraNum = 4;
						sql = sql + " AND (doro LIKE ?) AND (buildno1 LIKE ?)";
						parameters4 = new String[] {received.getDo(), received.getSi(), received.getDoro(), received.getZipNum()};
					}
					else
					{
						toSend = new LoginReplyForm(4, false, "파라미터 갯수 안맞음");
					}
					result4 = Query.getLikeResultSet(sql, paraNum, parameters4);
					try 
					{
						result4.last();
						res = new String[result4.getRow()][2];
						result4.beforeFirst();
					} catch (SQLException e) {}
					paraNum = 0;
					while (true)
					{
						try
						{
							if (result4.next())
							{
								res[paraNum][0] = result4.getString("zipcode");
								res[paraNum][1] = result4.getString("sido") 
										+ " " + result4.getString("sigungu") 
										+ " " + result4.getString("doro") 
										+ " " + result4.getString("buildno1") 
										+ " " + result4.getString("buildno2") 
										+ " " + result4.getString("buildname");
								paraNum++;
							} else break;
						} catch (SQLException e) {}
					}
					toSend = new LoginReplyForm(4, true, "주소 리스트 리턴함");
					toSend.setSearchResult(res);
					Query.close();
					// 모니터 리프레시 
					String monitorMessage4 = "[우편번호 요청] " + received.getDo() + " " + received.getSi() + "의 우편번호 정보 요청...    ";
					if (toSend.getResult() == true)
						monitorMessage4 += "처리됨";
					else
						monitorMessage4 += "오류";
					Start.mainMonitor.showRequest(monitorMessage4);
				break;
				
				case 5:
					// -> 게임방 생성 요청 
					// 만약 방장 이름으로 된 게임방이 없다면 -> 게임방 생성후 방 생성 성공 메시지 전송
					// 만약 방장 이름으로 된 게임방이 있다면 -> 실패 메시지 전송 
					if (ChatServer.games.containsKey(received.getId()) == false)
					{
						GameRoom newRoom = new GameRoom(received.getRoomName(), received.getId());
						ChatServer.games.put(received.getId(), newRoom);
						toSend = new LoginReplyForm(5, true, "게임방 생성됨");
					}
					else
					{
						toSend = new LoginReplyForm(5, false, "게임방 생성 실패");
					}
					String monitorMessage5 = "[방 생성 요청] " +received.getId() + "가 방 생성 요청... ";
					if (toSend.getResult() == true)
					{
						monitorMessage5 += "성공";
						Start.mainMonitor.showRequest(monitorMessage5);
						Start.mainMonitor.showRequest("[게임방 생성] " + received.getId() + "가 " + received.getRoomName() + " 호스팅중");
					}
					else
					{
						monitorMessage5 += "실패";
						Start.mainMonitor.showRequest(monitorMessage5);
					}
				break;
				
				case 6:
					// 본인인증을 할 때. 
					String[] parameters6 = {received.getId(), received.getPhoneNumber(), received.getZipcode()};
					ResultSet result6 = Query.getResultSet("SELECT id FROM userinfo WHERE id=? and phonenumber=? and zipcode=?" , 3, parameters6);
					try 
					{
						if (result6.next()) toSend = new LoginReplyForm(6, true, "인증 성공");
						else toSend = new LoginReplyForm(6, false, "인증 실패");
					}
					catch(SQLException e) {}
					Query.close();
					// 모니터 리프레시 
					String monitorMessage6 = "[본인인증 요청] " + received.getId() + "가 본인인증 요청함...    ";
					if (toSend.getResult() == true)
						monitorMessage6 += "성공";
					else
						monitorMessage6 += "실패";
					Start.mainMonitor.showRequest(monitorMessage6);
				break;
				
				case 7:
					// 비밀번호 바꿀 때 -> 바꾸려는 pw가 현재 pw과 used_pw과 달라야 함. 
					// 바꾸는 방식 : 현재 pw를 used_pw로 대입하고, pw에 바꾸려는 pw 넣기. 
					String[] step_one = {received.getId()};
					ResultSet res_one = Query.getResultSet("SELECT pw, used_pw FROM userinfo WHERE id=?", 1, step_one);
					String cur_pw = null;
					String used_pw = null;
					try 
					{
						res_one.next();
						cur_pw = res_one.getString("pw");
						used_pw = res_one.getString("used_pw");
					}
					catch (SQLException e) {e.printStackTrace();}
					Query.close();
					if (!cur_pw.equals(received.getPw()) && !used_pw.equals(received.getPw()))
					{
						String[] step_two = {received.getPw(), cur_pw, received.getId()};
						boolean res_two = Query.execute("UPDATE userinfo SET pw=?, used_pw=? WHERE id=?", 3, step_two);
						if (res_two == true)
							toSend = new LoginReplyForm(7, true, "패스워드 변경됨");
						else
							toSend = new LoginReplyForm(7, false, "패스워드 변경 실패");
					}
					else
					{
						toSend = new LoginReplyForm(7, false, "패스워드 변경 실패");
					}
					Query.close();
					// 모니터 리프레시 
					String monitorMessage7 = "[PW변경 요청] " + received.getId() + "가 비밀번호 변경함...    ";
					if (toSend.getResult() == true)
						monitorMessage7 += "변경됨";
					else
						monitorMessage7 += "변경실패 ";
					Start.mainMonitor.showRequest(monitorMessage7);
				break;
				
				case 8:
					// 유저가 로비 정보 요청할때 
					String[][] lobInfo = null;
					synchronized(ChatServer.games)
					{
						int i = 0;
						Set<String> keySet = ChatServer.games.keySet();
						lobInfo = new String[ChatServer.games.size()][3]; 
						for (String key : keySet) 
						{
							lobInfo[i][0] = ChatServer.games.get(key).getRoomName();
							lobInfo[i][1]= key;
							if (ChatServer.games.get(key).getOnGame() == false)
								lobInfo[i][2] = "대기중";
							else
								lobInfo[i][2] = "게임중";
							i++;
						}
					}
					toSend = new LoginReplyForm(8, true, "로비 리스트 리턴함");
					toSend.setSearchResult(lobInfo);
					Start.mainMonitor.showRequest("[로비 조회] " + received.getId() + "가 로비 정보 조회함.");
				break;
				
				case 9:
					// 유저가 게임방 참가 요청할때 
					// 게임방이 게임 진행중임(2명의 유저가 게임중임) -> 실패 메시지 반환
					// 게임방이 게임 대기중임(방장이 같이 게임할 유저를 기다리고 있음) -> 성공 메시지 반환 
					// 유저는 아이디, 닉네임, 참가할 방의 방장ID (방ID)를 리퀘스트 폼으로 전달함 
					synchronized(ChatServer.games)
					{
						if (ChatServer.games.containsKey(received.getRoomName()) == true)
						{
							// 들어가고자 하는 방이 존재하는 경우 
							if (ChatServer.games.get(received.getRoomName()).getOnGame() == false)
							{
								// 해당 방이 대기중인 경우 
								ChatServer.games.get(received.getRoomName()).inviteAndStart(received.getId());
								toSend = new LoginReplyForm(9, true, "게임방에 접속됨");
							}
							else
							{
								// 해당 방이 게임중인 경우 
								toSend = new LoginReplyForm(9, false, "이미 게임중인 방임");
							}
						}
						else
						{
							// 들어가고자 하는 방이 존재하지 않는 경우 
							toSend = new LoginReplyForm(9, false, "게임 방이 더이상 존재하지 않음");
						}
					}
					String monitorMessage9 = "[게임방 참가] " + received.getId() + "가" + received.getRoomName() + "방에 접속 시도,,, ";
					if (toSend.getResult() == true)
						monitorMessage9 += "입장 성공";
					else
						monitorMessage9 += "입장 실패";
					Start.mainMonitor.showRequest(monitorMessage9);
				break;
				
				case 10:
					// 게임방 입장 시 또는 유저의 정보 조회할 떄 요청 
					// 게임방 생성시 내 정보 요청해서 넣어놓기, 
					// 게임방 들어갈 때 상대 정보/내 정보 요청해서 넣어놓기
					// replyform의 특정 필드만 사용함. 
					// 입력받은 아이디는 무조건 존재하는 유저라고 가정하고 짬. 
					String[] parameters10 = {received.getId()};
					ResultSet result10 = Query.getResultSet("SELECT id, nickname, win, lose, draw, elo, email, phonenumber FROM userinfo WHERE id=?", 1, parameters10);
					try 
					{
						if (result10.next()) 
							{
								toSend = new LoginReplyForm(10, true, "유저의 정보 리턴함");
								toSend.setId(result10.getString("id"));
								toSend.setNickName(result10.getString("nickname"));
								String[][] rating = new String[1][4];
								rating[0][0] = result10.getString("win") + "승 " + result10.getString("draw") + "무 " + result10.getString("lose") + "패"; // 승무패
								rating[0][1] = "elo Rating : " + result10.getString("elo"); // 레이팅 
								rating[0][2] = result10.getString("phonenumber"); // 전번 
								rating[0][3] = result10.getString("email"); // 이메일 
								toSend.setSearchResult(rating);
								// 프사불러오기 
								try 
								{
									BufferedImage bufim = ImageIO.read(new File("profile/" + toSend.getId() + ".jpg"));
									toSend.setPicBlob(Blob.toByteArray(bufim, "jpg"));
								} catch (IOException e) {}
							}
						else toSend = new LoginReplyForm(10, false, "유저의 정보를 찾을 수 없음");
					}
					catch(SQLException e) {}
					String monitorMesage10 = "[유저정보 조회] " + received.getId() + "의 정보 조회 요청... ";
					if (toSend.getResult() == true)
						monitorMesage10 += "조회 성공";
					else
						monitorMesage10 += "조회 실패";
					Start.mainMonitor.showRequest(monitorMesage10);
				break;
				
				case 11:
					// 유저가 레이팅 정보 요청할 경우 
					String[] parameters11 = {};
					ResultSet result11 = Query.getResultSet("SELECT id, elo FROM userinfo ORDER BY elo DESC", 0, parameters11);
					try 
					{
						if (result11.next()) 
							{
								toSend = new LoginReplyForm(11, true, "레이팅 정보 리턴함");

								result11.last();
								String[][] ratings= new String[result11.getRow()][1];
								result11.beforeFirst(); 
								for (int i=0; i<ratings.length; i++)
								{
									result11.next();
									ratings[i][0] = "elo " + result11.getString("elo") + "  [" + result11.getString("id") + "]";
								}
								toSend.setSearchResult(ratings);
							}
						else toSend = new LoginReplyForm(11, false, "레이팅 정보를 불러올 수 없음.");
					}
					catch(SQLException e) {}
				break;
				
				case 12:
					// 방 닫기. 
					if (ChatServer.games.containsKey(received.getRoomName()))
						ChatServer.games.remove(received.getRoomName());
					toSend = new LoginReplyForm(12, true, "요청 처리함");
				break;
				
				case 13:
					// 유저가 관전자로 방에 들어가려고 하는 경우 -> 방의 id를 받음. 게스트의 id를 id필드에 넣어 전달함. 
					// 방이 아직 게임중이 아니거나 없어진 방이면 요청 실패. 
					if ((ChatServer.games.containsKey(received.getRoomName()) == true) && (ChatServer.games.get(received.getRoomName()).getOnGame() == true))
					{
						// 관전자 입장 
						ChatServer.games.get(received.getRoomName()).inviteSpector(received.getId());
						toSend = new LoginReplyForm(13, true, "관전 시작됨");
						toSend.setId(ChatServer.games.get(received.getRoomName()).getGuestId());
					}
					else
					{
						// 관전자 입장 실패 
						toSend = new LoginReplyForm(13, false, "관전 입장 실패");
					}
				break;
				
				default:
					toSend = new LoginReplyForm(0, false, "잘못된 요청");
				break;
			}
			SendObject.toClient(socket, toSend);
		}
	}
}
