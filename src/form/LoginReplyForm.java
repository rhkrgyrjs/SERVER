package form;

import java.io.Serializable;

public class LoginReplyForm implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	private int reqType = 0;
	private boolean reqSuc = false;
	private String msg = null;
	private String[][] searchResult = null;
	private String id = null;
	private String nickName = null;
	
	public LoginReplyForm(int type, boolean bool, String msg)
	{
		this.reqType = type;
		this.reqSuc = bool;
		this.msg = msg;
	}
	
	public int getReqType() {return this.reqType;}
	public boolean getResult() {return this.reqSuc;}
	public String getMsg() {return this.msg;}
	public String[][] getSearchResult() {return this.searchResult;}
	public String getId() {return this.id;}
	public String getNickName() {return this.nickName;}
	
	public void setSearchResult(String[][] res) {this.searchResult = res;}
	public void setId(String id) {this.id = id;}
	public void setNickName(String nickName) {this.nickName = nickName;}

}
 