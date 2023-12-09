package window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import db.Query;

public class UserDBWindow extends JFrame
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	// 싱글톤 처리 
	private static UserDBWindow single_instance = null;
	public static UserDBWindow getInstance()
	{
		if (single_instance == null) single_instance = new UserDBWindow();
		return single_instance;
	}
	private JComboBox<String> searchVal = null;
	private JTextField searchInput = null;
	private JButton searchButton = null;
	
	private JTable userTable = null;
	private JScrollPane userScrollPane = null;
	private DefaultTableModel tableModel = null;
	
	String[] userinfo = {"id", "nickname", "email", "phonenumber", "zipcode", "address", "win", "lose", "draw", "elo"};
	String[] tableCol = {"ID", "Nickname", "Email", "Phonenumber", "Zipcode", "Address", "Win", "Lose", "Draw", "Elo"};
	String[][] db = null;
	
	private UserInfo userInfo = null;
	
	public void setUserInfo(UserInfo usif) {this.userInfo = usif;}
	
	private UserDBWindow()
	{
		// 기본적인 창 설정 
		setTitle("유저 데이터베이스");
		setResizable(false);
		setSize(1200, 600);
		setLayout(null);
		setLocationRelativeTo(null);
		
		searchVal = new JComboBox(userinfo);
		searchInput = new JTextField(20);
		searchButton = new JButton("검색");

		tableModel = new DefaultTableModel(db, tableCol);
		userTable= new JTable(tableModel) // 항목의 수정 가능 여부 관련 Override 
				{
					@Override
					public boolean isCellEditable(int row, int column) {return false;}
				};
		userTable.getColumn("ID").setPreferredWidth(25);
		userTable.getColumn("Nickname").setPreferredWidth(25);
		userTable.getColumn("Email").setPreferredWidth(45);
		userTable.getColumn("Phonenumber").setPreferredWidth(25);
		userTable.getColumn("Zipcode").setPreferredWidth(15);
		userTable.getColumn("Address").setPreferredWidth(25);
		userTable.getColumn("Win").setPreferredWidth(10);
		userTable.getColumn("Lose").setPreferredWidth(10);
		userTable.getColumn("Draw").setPreferredWidth(10);
		userTable.getColumn("Elo").setPreferredWidth(10);
		userScrollPane = new JScrollPane(userTable);
		
		searchVal.setBounds(300, 15, 150, 20);
		searchInput.setBounds(450, 15, 350, 20);
		searchButton.setBounds(795, 10, 80, 30);
		userScrollPane.setBounds(5, 50, 1190, 510);
		
		searchInput.addKeyListener(new Search(this));
		searchButton.addActionListener(new Search(this));
		userTable.addMouseListener(new EditUser(this));
		
		add(searchVal);
		add(searchInput);
		add(searchButton);
		add(userScrollPane);
		
		// 창 닫았을때 이벤트 리스너 
		this.addWindowListener(new WindowAdapter() 
		{
	        @Override
	        public void windowClosing(WindowEvent e) 
	        {setVisible(false);}});
		
		setVisible(false);
	}
	
	// DB에 있는 모든 유저 긁어오기
	public void refresh()
	{
		String[] dummy = {};
		ResultSet result = Query.getResultSet("SELECT id, nickname, email, phonenumber, zipcode, address, win, lose, draw, elo FROM userinfo", 0, dummy);
		
		try 
		{
			result.last();
			db = new String[result.getRow()][10];
			result.beforeFirst(); 
			for (int i=0; i<db.length; i++)
			{
				result.next();
				db[i][0] = result.getString("id");
				db[i][1] = result.getString("nickname");
				db[i][2] = result.getString("email");
				db[i][3] = result.getString("phonenumber");
				db[i][4] = result.getString("zipcode");
				db[i][5] = result.getString("address");
				db[i][6] = result.getString("win");
				db[i][7] = result.getString("lose");
				db[i][8] = result.getString("draw");
				db[i][9] = result.getString("elo");
			}
			tableModel.setRowCount(0);
			for (int i=0; i<db.length; i++)
			{
				tableModel.addRow(this.db[i]);
			}
		} 
		catch (SQLException e) {}
	}
	
    class EditUser extends MouseAdapter
    {
		UserDBWindow udbw = null;
		EditUser(UserDBWindow udbw) {this.udbw = udbw;}
		
    	@Override
    	public void mouseClicked(MouseEvent e) 
    	{
            if (e.getClickCount() == 2) 
            {
                int row = udbw.userTable.rowAtPoint(e.getPoint());
                String clickedValue = (String) udbw.userTable.getValueAt(row, 0);
                udbw.userInfo.loadUserInfo(clickedValue);
            }
    	}
    }
	
	class Search implements ActionListener, KeyListener
	{
		UserDBWindow udbw = null;
		Search(UserDBWindow udbw) {this.udbw = udbw;}
		
		public void search()
		{
			String sql = "SELECT id, nickname, email, phonenumber, zipcode, address, win, lose, draw, elo FROM userinfo WHERE ";
			if (searchInput.getText().equals(""))
			{
				refresh();
			}
			else
			{
				switch(searchVal.getSelectedIndex())
				{
					case 0:
						sql += "(id LIKE ?)";
					break;
					
					case 1:
						sql += "(nickname LIKE ?)";
					break;
					
					case 2:
						sql += "(email LIKE ?)";
					break;
					
					case 3:
						sql += "(phonenumber LIKE ?)";
					break;
					
					case 4:
						sql += "(zipcode LIKE ?)";
					break;
					
					case 5:
						sql += "(address LIKE ?)";
					break;
					
					case 6:
						sql += "win= ?";
					break;
					
					case 7:
						sql += "lose= ?";
					break;
						
					case 8:
						sql += "draw= ?";
					break;
						
					case 9:
						sql += "elo= ?";
					break;
					
					default:
					break;
				}
				String[] parameter = {searchInput.getText()};
				switch(searchVal.getSelectedIndex())
				{
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					ResultSet result = Query.getLikeResultSet(sql, 1, parameter);
					try 
					{
						if (result.next()) {
						result.last();
						db = new String[result.getRow()][10];
						result.beforeFirst(); 
						for (int i=0; i<db.length; i++)
						{
							result.next();
							db[i][0] = result.getString("id");
							db[i][1] = result.getString("nickname");
							db[i][2] = result.getString("email");
							db[i][3] = result.getString("phonenumber");
							db[i][4] = result.getString("zipcode");
							db[i][5] = result.getString("address");
							db[i][6] = result.getString("win");
							db[i][7] = result.getString("lose");
							db[i][8] = result.getString("draw");
							db[i][9] = result.getString("elo");
						}
						tableModel.setRowCount(0);
						for (int i=0; i<db.length; i++)
						{
							tableModel.addRow(db[i]);
						}
					} else {tableModel.setRowCount(0);} } 
					catch (SQLException a) {}
					break;
					
				case 6:
				case 7:
				case 8:
				case 9:
					ResultSet resulta = Query.getResultSet(sql, 1, parameter);
					try 
					{
						if (resulta.next()) {
						resulta.last();
						db = new String[resulta.getRow()][10];
						resulta.beforeFirst(); 
						for (int i=0; i<db.length; i++)
						{
							resulta.next();
							db[i][0] = resulta.getString("id");
							db[i][1] = resulta.getString("nickname");
							db[i][2] = resulta.getString("email");
							db[i][3] = resulta.getString("phonenumber");
							db[i][4] = resulta.getString("zipcode");
							db[i][5] = resulta.getString("address");
							db[i][6] = resulta.getString("win");
							db[i][7] = resulta.getString("lose");
							db[i][8] = resulta.getString("draw");
							db[i][9] = resulta.getString("elo");
						}
						tableModel.setRowCount(0);
						for (int i=0; i<db.length; i++)
						{
							tableModel.addRow(db[i]);
						}
					} else {tableModel.setRowCount(0);} }
					catch (SQLException a) {}
					break;
				}
				
			}
		}

		@Override
		public void keyTyped(KeyEvent e) 
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void keyPressed(KeyEvent e) 
		{
			// TODO Auto-generated method stub
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
				search();
		}

		@Override
		public void keyReleased(KeyEvent e) 
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			// TODO Auto-generated method stub
			search();
		}
		
	}
}
