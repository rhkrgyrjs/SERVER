package window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import Server.Start;
import chat.ChatServer;
import db.Query;
import hash.SHA256;
import image.Blob;
import image.PicResize;
import parse.EndsWithImg;
import swing.ShowMessage;


public class UserInfo extends JFrame
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	// 싱글톤 처리 
	private static UserInfo single_instance = null;
	public static UserInfo getInstance()
	{
		if (single_instance == null) single_instance = new UserInfo();
		return single_instance;
	}
	
	private JLabel idLabel = null;
	private JLabel nickNameLabel = null;
	private JLabel pwLabel = null;
	private JLabel emailLabel = null;
	private JLabel phoneNumLabel = null;
	private JLabel addressLabel = null;
	private JLabel userIdLabel = null;
	private JLabel zipcodeLabel = null;
	private JLabel winLabel = null;
	private JLabel loseLabel = null;
	private JLabel drawLabel = null;
	private JLabel eloLabel = null;
	private JTextField nickNameInput = null;
	private JTextField pwInput = null;
	private JTextField emailInput = null;
	private JTextField phoneNumInput = null;
	private JTextField addressInput = null;
	private JButton profilPicButton = null;
	private JTextField winInput = null;
	private JTextField loseInput = null;
	private JTextField drawInput = null;
	private JTextField eloInput = null;
	JTextField zipcodeInput = null;
	private JButton updateButton = null;
	JButton cancelButton = null;
	private JFileChooser picChooser = null;
	BufferedImage bufim = null;
	boolean imModed = false;
	
	public void clear()
	{
		this.userIdLabel.setText("");
		this.nickNameInput.setText("");
		this.pwInput.setText("");
		this.emailInput.setText("");
		this.phoneNumInput.setText("");
		this.addressInput.setText("");
		this.bufim = null;
		imModed = false;
		// 우편번호 버튼 / 프사 버튼 초기화 루틴 넣기. 
	}
	
	private UserInfo()
	{
		// 기본적인 창 설정 
		setTitle("유저 정보 조회/수정");
		setResizable(false);
		setSize(375, 320);
		setLayout(null);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		

		idLabel = new JLabel("ID");
		nickNameLabel = new JLabel("닉네임");
		pwLabel = new JLabel("PW(변경)");
		emailLabel = new JLabel("이메일");
		phoneNumLabel = new JLabel("전화번호");
		addressLabel = new JLabel("상세주소");
		zipcodeLabel = new JLabel("우편번호");
		winLabel = new JLabel("승");
		loseLabel = new JLabel("패");
		drawLabel = new JLabel("무");
		eloLabel = new JLabel("레이팅");
		
		userIdLabel = new JLabel("");
		nickNameInput = new JTextField();
		pwInput = new JTextField();
		emailInput = new JTextField();
		phoneNumInput = new JTextField();
		addressInput = new JTextField();
		winInput = new JTextField();
		loseInput = new JTextField();
		drawInput = new JTextField();
		eloInput = new JTextField();
		
		profilPicButton = new JButton();
		zipcodeInput = new JTextField();
		updateButton = new JButton("수정");
		cancelButton = new JButton("취소");
		picChooser = new JFileChooser();

		idLabel.setBounds(15, 15, 50, 20);
		nickNameLabel.setBounds(15, 45, 50, 20);
		pwLabel.setBounds(15, 75, 50, 20);
		emailLabel.setBounds(15, 105, 50, 20);
		phoneNumLabel.setBounds(15, 135, 50, 20);
		addressLabel.setBounds(15, 165, 50, 20);
		zipcodeLabel.setBounds(295, 160, 50, 20);
		winLabel.setBounds(132, 195, 30, 20);
		loseLabel.setBounds(202, 195, 30, 20);
		drawLabel.setBounds(270, 195, 30, 20);
		eloLabel.setBounds(120, 225, 40, 20);
		
		userIdLabel.setBounds(70, 15, 200, 20);
		nickNameInput.setBounds(70, 45, 200, 20);
		pwInput.setBounds(70, 75, 200, 20);
		emailInput.setBounds(70, 105, 200, 20);
		phoneNumInput.setBounds(70, 135, 200, 20);
		addressInput.setBounds(70, 165, 200, 20);
		winInput.setBounds(80, 195, 50, 20);
		loseInput.setBounds(150, 195, 50, 20);
		drawInput.setBounds(218, 195, 50, 20);
		eloInput.setBounds(162, 225, 70, 20);
		
		profilPicButton.setBounds(280, 45, 80, 92);
		zipcodeInput.setBounds(280, 140, 80, 20);
		updateButton.setBounds(100, 260, 80, 25);
		cancelButton.setBounds(200, 260, 80, 25);

		picChooser.setFileFilter(new FileNameExtensionFilter("jpg", "jpg"));
		picChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		profilPicButton.addActionListener(new ChooseProfilePic(this));
		updateButton.addActionListener(new UpdateInfo(this));
		cancelButton.addActionListener(new CloseWindow(this));

		add(idLabel);
		add(nickNameLabel);
		add(pwLabel);
		add(emailLabel);
		add(phoneNumLabel);
		add(addressLabel);
		add(zipcodeLabel);
		add(winLabel);
		add(loseLabel);
		add(drawLabel);
		add(eloLabel);
		
		add(userIdLabel);
		add(nickNameInput);
		add(pwInput);
		add(emailInput);
		add(phoneNumInput);
		add(addressInput);
		add(winInput);
		add(loseInput);
		add(drawInput);
		add(eloInput);
		
		add(profilPicButton);
		add(zipcodeInput);
		add(updateButton);
		add(cancelButton);
		
		setVisible(false);
	}
	
	public void loadUserInfo(String id)
	{
		// 유저정보 로딩해오기.
		String sql = "SELECT id, nickname, email, phonenumber, address, zipcode, win, lose, draw, elo FROM userinfo WHERE id=?";
		String[] parameter = {id};
		ResultSet info = Query.getResultSet(sql, 1, parameter);
		try
		{
			info.next();
			this.userIdLabel.setText(info.getString("id"));
			this.nickNameInput.setText(info.getString("nickname"));
			this.emailInput.setText(info.getString("email"));
			this.phoneNumInput.setText(info.getString("phonenumber"));
			this.addressInput.setText(info.getString("address"));
			this.zipcodeInput.setText(info.getString("zipcode"));
			this.winInput.setText(info.getString("win"));
			this.loseInput.setText(info.getString("lose"));
			this.drawInput.setText(info.getString("draw"));
			this.eloInput.setText(info.getString("elo"));
			BufferedImage imgb = PicResize.getProfilePic("profile/" + info.getString("id") + ".jpg");
			ImageIcon img = new ImageIcon(imgb);
			this.profilPicButton.setIcon(img);
			this.setVisible(true);
		}
		catch (SQLException e) {ShowMessage.warning("오류", "유저의 정보를 찾을 수 없습니다.");}
		Query.close();
	}
	
	public boolean updateUserinfo()
	{
		String id = this.userIdLabel.getText();
		String nickName = this.nickNameInput.getText();
		String password = this.pwInput.getText();
		String email = this.emailInput.getText();
		String phoneNum = this.phoneNumInput.getText();
		String address = this.addressInput.getText();
		String zipcode = this.zipcodeInput.getText();
		String win = this.winInput.getText();
		String lose = this.loseInput.getText();
		String draw = this.drawInput.getText();
		String elo = this.eloInput.getText();

		boolean result = false;
		if (password == null || password.equals(""))
		{
			// pw 따로 입력 안했을 경우 
			String[] param1 = {nickName, email, phoneNum, address, zipcode, win, lose, draw, elo, id};
			String sql1 = "UPDATE userinfo SET nickname=?, email=?, phonenumber=?, address=?, zipcode=?, win=?, lose=?, draw=?, elo=? WHERE id=?";
			result = Query.execute(sql1, 10, param1);
		}
		else
		{
			// pw 따로 입력했을 경우 
			String hashed = SHA256.toString(password);
			String[] param2 = {hashed, nickName, email, phoneNum, address, zipcode, win, lose, draw, elo, id};
			String sql2 = "UPDATE userinfo SET pw=?, nickname=?, email=?, phonenumber=?, address=?, zipcode=?, win=?, lose=?, draw=?, elo=? WHERE id=?";
			result = Query.execute(sql2, 11, param2);
		}
		Query.close();
		if (result == true)
		{
			if (this.imModed == true)
			{
				File outputFile = new File("profile/" + this.userIdLabel.getText() + ".jpg");
				try{ImageIO.write(this.bufim, "jpg", outputFile);} catch (IOException a) {a.printStackTrace();}
			}
		}
		Start.mainMonitor.showRequest("[유저 정보 수정] " + id + "의 정보가 수정되었습니다.");
		try {ChatServer.users.get(id).close();}
		catch(Exception e) {}
		return result;
	}
	
	class UpdateInfo implements ActionListener
	{
		UserInfo ui = null;
		UpdateInfo(UserInfo ui) {this.ui = ui;}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if (ShowMessage.confirm("유저 정보 수정", "유저의 정보를 수정하시겠습니까? 유저의 접속이 종료됩니다."));
			{
				boolean result = ui.updateUserinfo();
				ui.clear();
				ui.setVisible(false);
				if (result == true)
					ShowMessage.information("유저 정보 수정됨", "유저 정보가 수정되었습니다.");
				else
					ShowMessage.warning("유저 정보 수정 실패", "유저 정보 수정 중 오류가 발생했습니다.");
			}
		}
	}

	class ChooseProfilePic implements ActionListener
	{
		UserInfo ui = null;
		ChooseProfilePic(UserInfo ui) {this.ui = ui;}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (ui.picChooser.showOpenDialog(null) == 0)
			{
				// 파일 불러오기 창 열림 
				// sw.picChooser.getSelectedFile(); // 대충 경로로 설정된 파일 열어오기 느낌일듯?
				// 파싱해서 확장자 png or jpg 아니면 컷하기 
				if (EndsWithImg.isJpg((ui.picChooser.getSelectedFile().getAbsolutePath())))
				{
					try 
					{
				    ui.bufim = ImageIO.read(new File(ui.picChooser.getSelectedFile().getAbsolutePath()));
					BufferedImage resized = PicResize.getProfilePic(ui.picChooser.getSelectedFile().getAbsolutePath());
					ImageIcon img = new ImageIcon(resized);
					ui.profilPicButton.setIcon(img);
					ui.imModed = true;
					} 
					catch (IOException a) {a.printStackTrace();}
				}
			}
		}
	}
	
	class CloseWindow implements ActionListener
	{
		UserInfo ui = null;
		CloseWindow(UserInfo ui) {this.ui = ui;}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ui.clear();
			ui.setVisible(false);
		}
	}
}
