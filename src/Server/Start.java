package Server;

import chat.ChatServer;
import login.LoginServer;

import window.MainMonitor;
import window.UserDBWindow;
import window.UserInfo;

public class Start 
{
	public static MainMonitor mainMonitor = MainMonitor.getInstance();
	public static UserInfo userInfo = UserInfo.getInstance();
	public static UserDBWindow userDBWindow = UserDBWindow.getInstance();
	
	public static void main(String[] args)
	{
		LoginServer loginServer = LoginServer.getInstance();
		ChatServer chatServer = ChatServer.getInstance();
		
		mainMonitor.setUserDBWindow(userDBWindow);
		userDBWindow.refresh();
		userDBWindow.setUserInfo(userInfo);
		
		loginServer.start();
		chatServer.start();
		
		mainMonitor.setInfoWindow(userInfo);
		mainMonitor.setVisible(true);
	}
}
