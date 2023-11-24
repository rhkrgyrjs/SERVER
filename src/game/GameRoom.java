package game;

public class GameRoom 
{
	private boolean onGame = false;
	private String roomName = null;
	private String hostId = null;
	private String guestId = null;
	
	public GameRoom(String roomName, String hostId)
	{
		this.roomName = roomName;
		this.hostId = hostId;
	}
	
	public void setGuestId(String guestId)
	{
		this.guestId = guestId;
	}
	
	public void setOnGame()
	{
		this.onGame = true;
	}
	
	public String getHostId() {return this.hostId;}
	public String getRoomName() {return this.roomName;}
	public boolean getOnGame() {return this.onGame;}
	
}
