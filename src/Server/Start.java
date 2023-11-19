package Server;

import chat.ChatServer;
import login.LoginServer;

public class Start 
{
	public static void main(String[] args)
	{
		LoginServer loginServer = LoginServer.getInstance();
		ChatServer chatServer = ChatServer.getInstance();
		
		loginServer.start();
		chatServer.start();
	}
}
