package login;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.*;

import javax.imageio.ImageIO;

import Server.Start;
import chat.ChatServer;
import form.LoginReplyForm;
import form.LoginRequestForm;
import image.Blob;
import image.PicResize;

import java.net.*;
import java.awt.image.BufferedImage;
import java.io.*;

import socket.*;

import window.MainMonitor;

import db.Query;

/*
 * 로그인 서버 : 로그인 / 회원가입 관련 처리 함. 
 * 1. 로그인 요청 처리 : DB에 ID PW 조회해보고 맞으면 로그인, 아니면 실패. 
 * 2. 회원가입 요청 처리 : 유저가 전송한 정보를 DB에 저장.
 *  2.1. ID 중복체크 : DB에 ID가 존재하나 조회.
 *  2.2. 프로필 사진 전송받기 : 유저에게 사진 파일 전송받아 저장.
 *  2.3. 우편번호 찾기 : 우편번호 검색 결과를 유저에게 전송. 
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
					String[] parameters2 = {received.getId(), received.getPw(), received.getNickName(), received.getPhoneNumber(), received.getEmail(), received.getZipcode(), received.getPw(), received.getAddress()};
					boolean result2 = Query.execute("INSERT INTO userinfo (id, pw, nickname, phonenumber, email, zipcode, used_pw, address)" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 8, parameters2);
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
					// 로그아웃 요청 
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
				
				default:
					toSend = new LoginReplyForm(0, false, "잘못된 요청");
				break;
			}
			SendObject.toClient(socket, toSend);
		}
	}
}
