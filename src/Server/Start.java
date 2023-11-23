package Server;

import chat.ChatServer;
import login.LoginServer;

import window.MainMonitor;
import window.UserInfo;

public class Start 
{
	public static MainMonitor mainMonitor = MainMonitor.getInstance();
	public static UserInfo userInfo = UserInfo.getInstance();
	
	public static void main(String[] args)
	{
		LoginServer loginServer = LoginServer.getInstance();
		ChatServer chatServer = ChatServer.getInstance();
		
		loginServer.start();
		chatServer.start();
		
		mainMonitor.setInfoWindow(userInfo);
		mainMonitor.setVisible(true);
	}
}
