package form;

import java.io.Serializable;

public class ChatForm implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	private String roomId = null;
	private String id = null;
	private String nickName = null;
	private String msg = null;
	
	public ChatForm() {}
	
	public ChatForm(String room, String id, String nickName, String msg)
	{
		this.roomId = room;
		this.id = id;
		this.nickName = nickName;
		this.msg = msg;
	}
	
	public String getRoomId() {return this.roomId;}
	public String getId() {return this.id;}
	public String getNickName() {return this.nickName;}
	public String getMsg() {return this.msg;}
}
