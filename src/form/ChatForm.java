package form;

import java.io.Serializable;

public class ChatForm implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	private int reqType = 0;
	private String roomId = null;
	private String id = null;
	private String nickName = null;
	private String msg = null;
	
	public ChatForm() {}
	
	public ChatForm(int reqType, String room, String id, String nickName, String msg)
	{
		this.reqType = reqType;
		this.roomId = room;
		this.id = id;
		this.nickName = nickName;
		this.msg = msg;
	}
	
	public int getReqType() {return this.reqType;}
	public String getRoomId() {return this.roomId;}
	public String getId() {return this.id;}
	public String getNickName() {return this.nickName;}
	public String getMsg() {return this.msg;}
}
