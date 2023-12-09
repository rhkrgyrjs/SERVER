package form;

import java.io.Serializable;

public class ChatForm implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	// reqType : 1-일반적인 채팅 요청, 2-게임 관련 요청 
	private int reqType = 0;
	private String roomId = null;
	private String id = null;
	private String nickName = null;
	private String msg = null;

	private GameBoardInfoForm gameBoardInfo = null;
	
	public ChatForm() {}
	
	public ChatForm(int reqType, String room, String id, String nickName, String msg)
	{
		this.reqType = reqType;
		this.roomId = room;
		this.id = id;
		this.nickName = nickName;
		this.msg = msg;
	}
	
	public void setBoardInfo(GameBoardInfoForm info) {this.gameBoardInfo = info;}
	
	public int getReqType() {return this.reqType;}
	public String getRoomId() {return this.roomId;}
	public String getId() {return this.id;}
	public String getNickName() {return this.nickName;}
	public String getMsg() {return this.msg;}
	public GameBoardInfoForm getBoardInfo() {return this.gameBoardInfo;}
}
