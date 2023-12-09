package window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import chat.ChatServer;
import swing.ShowMessage;

public class MainMonitor extends JFrame
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	// 싱글톤 처리 
	private static MainMonitor single_instance = null;
	public static MainMonitor getInstance()
	{
		if (single_instance == null) single_instance = new MainMonitor();
		return single_instance;
	}
	
	private JLabel userListLabel = null;
	private JLabel requestListLabel = null;
	private JLabel chatListLabel = null;
	private JList userList = null;
	private JTextArea requestList = null;
	private JTextArea chatList = null;
	private JScrollPane userPane = null;
	private JScrollPane requestPane = null;
	private JScrollPane chatPane = null;
	private JButton userInfoButton = null;
	private JButton userKickButton = null;
	private JButton userSearchButton = null;
	private JButton userDBButton = null;
	
	private String selectedUser = null;
	
	private UserInfo infoWindow = null;
	private UserDBWindow userDBWindow = null;
	
	public void setInfoWindow(UserInfo infoWindow) {this.infoWindow = infoWindow;}
	public void setUserDBWindow(UserDBWindow udbw) {this.userDBWindow = udbw;}
	
	private MainMonitor()
	{
		// 기본적인 창 설정 
		setTitle("서버 모니터링 윈도우");
		setResizable(false);
		setSize(1200, 400);
		setLayout(null);
		setLocationRelativeTo(null);
		
		// 컴포넌트 생성 
		userListLabel = new JLabel("접속중 유저 ID 리스트");
		requestListLabel = new JLabel("요청 처리");
		chatListLabel = new JLabel("채팅"); // + 게임내용까지 할까? 
		userList = new JList();
		requestList = new JTextArea();
		chatList = new JTextArea();
		userPane = new JScrollPane(userList);
		requestPane = new JScrollPane(requestList);
		chatPane = new JScrollPane(chatList);
		userInfoButton = new JButton("유저정보");
		userKickButton = new JButton("강퇴");
		userSearchButton = new JButton("검색");
		userDBButton = new JButton("유저 DB");
		
		userListLabel.setBounds(10, 10, 170, 20);
		requestListLabel.setBounds(220, 10, 150, 20);
		chatListLabel.setBounds(650, 10, 150, 20);
		userPane.setBounds(10, 40, 200, 270);
		requestPane.setBounds(220 ,40, 400, 320);
		chatPane.setBounds(650, 40, 530, 320);
		userInfoButton.setBounds(75, 320, 60, 40);
		userKickButton.setBounds(135, 320, 60, 40);
		userSearchButton.setBounds(25, 320, 40, 40);	
		userDBButton.setBounds(130, 6, 80, 30);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		requestList.setEditable(false);
		chatList.setEditable(false);
		
		userList.addListSelectionListener(new UserClicked(this));
		userInfoButton.addActionListener(new ShowUserInfo(this));
		userKickButton.addActionListener(new KickUser(this));
		userSearchButton.addActionListener(new SearchUser(this));
		userDBButton.addActionListener(new ShowUserDB(this));
		
		add(userListLabel);
		add(requestListLabel);
		add(chatListLabel);
		add(userPane);
		add(requestPane);
		add(chatPane);
		add(userInfoButton);
		add(userKickButton);
		add(userSearchButton);
		add(userDBButton);
		
		// 창 닫을때 
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		userInfoButton.setEnabled(false);
		userKickButton.setEnabled(false);
		
		setVisible(false);
	}
	
	public void setUserList(Set<String> set)
	{
		// 받는 형태는 [유저명] : (아이디) 
		DefaultListModel listModel = new DefaultListModel();
		for (String key : set)
		{
			listModel.addElement(key);
		}
		this.userList.setModel(listModel);
	}
	
	public void showRequest(String requset)
	{
		this.requestList.append(requset);
		this.requestList.append("\n");
		try {this.requestList.setCaretPosition(this.chatList.getDocument().getLength());}
		catch (IllegalArgumentException e) {}
	}
	
	public void showChat(String chat)
	{
		this.chatList.append(chat);
		this.chatList.append("\n");
		try {this.chatList.setCaretPosition(this.chatList.getDocument().getLength());}	
		catch (IllegalArgumentException e) {}
	}
	
	class UserClicked implements ListSelectionListener
	{
		MainMonitor mm = null;
		UserClicked(MainMonitor mm) {this.mm = mm;}

		@Override
		public void valueChanged(ListSelectionEvent e) 
		{
			if (!e.getValueIsAdjusting()) 
			{
            // 클릭된 항목의 텍스트 가져오기
            String selectedText = (String) mm.userList.getSelectedValue();
            mm.selectedUser = selectedText;
            if (selectedText == null)
            {
            	// 버튼 비활성화 
            	mm.userInfoButton.setEnabled(false);
            	mm.userKickButton.setEnabled(false);
            }
            else
            {
            	// 버튼 활성화 
            	mm.userInfoButton.setEnabled(true);
            	mm.userKickButton.setEnabled(true);
            }
			}
		}
	}
	
	class SearchUser implements ActionListener
	{
		MainMonitor mm = null;
		SearchUser(MainMonitor mm) {this.mm = mm;}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			String id = ShowMessage.input("유저 검색", "정보를 찾아볼 유저의 ID를 입력하세요.");
			if (id!=null)
			{
				if (!(id.equals(""))) mm.infoWindow.loadUserInfo(id);
			}
		}
		
	}
	
	class ShowUserInfo implements ActionListener
	{
		MainMonitor mm = null;
		ShowUserInfo(MainMonitor mm) {this.mm = mm;}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if (selectedUser != null)
			{
				// 유저정보 보기 버
				mm.infoWindow.loadUserInfo(selectedUser);
			}
			
		}
	}
	
	class KickUser implements ActionListener
	{
		MainMonitor mm = null;
		KickUser(MainMonitor mm) {this.mm = mm;}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if (selectedUser != null)
			{
				// 유저 강퇴 (소켓 연결 끊어버리기)
				try 
				{
					ChatServer.users.get(mm.selectedUser).close();
					ChatServer.users.remove(mm.selectedUser);
				}
				catch (IOException a) {}
			}
		}
	}
	
	class ShowUserDB implements ActionListener
	{
		MainMonitor mm = null;
		ShowUserDB(MainMonitor mm) {this.mm = mm;}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			mm.userDBWindow.setVisible(true);
		}
		
	}
}
