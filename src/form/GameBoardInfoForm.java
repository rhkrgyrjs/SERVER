package form;

import java.io.Serializable;
import java.util.List;

public class GameBoardInfoForm  implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	// 유저 덱
	private Card AFront = null;
	private Card BFront = null;
	private Card CFront = null;
	private Card DFront = null;
	private int hostDeckCount;
	private int guestDeckCount;
	private String command = null;
	
	// 게임 끝났는지 여부, 승자 id 
	private boolean isGameEnd = false;
	private String winnerId = null;
	
	public GameBoardInfoForm(Card A, Card B, Card C, Card D, int hostCount, int guestCount)
	{
		this.AFront = A;
		this.BFront = B;
		this.CFront = C;
		this.DFront = D;
		
		this.hostDeckCount = hostCount;
		this.guestDeckCount = guestCount;
	}
	
	public Card getAFront() {return this.AFront;}
	public Card getBFront() {return this.BFront;}
	public Card getCFront() {return this.CFront;}
	public Card getDFront() {return this.DFront;}
	public int getHostDeckCount() {return this.hostDeckCount;}
	public int getGuestDeckCount() {return this.guestDeckCount;}
	public String getCommand() {return this.command;}
	
	public void setIsGameEnd() {this.isGameEnd = true;}
	public void setWinnerId(String id) {this.winnerId = id;}
	public void setCommand(String com) {this.command = com;}
	
	

}
