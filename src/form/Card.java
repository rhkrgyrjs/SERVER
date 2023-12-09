package form;

import java.io.Serializable;

public class Card implements Serializable
{
	// 직렬화 처리 
	private static final long serialVersionUID = 1L;
	
	// fruit : 0~4 사이의 값, 0-빈카드 1-바나나, 2-라임, 3-딸기, 4-자두 
	// number : 0~5 사이의 값 (0은 빈카드) 
	private int fruit;
	private int number;
	
	public Card(int f, int n)
	{
		this.fruit = f;
		this.number = n;
	}
	
	public int getFruit() {return this.fruit;}
	public int getNumber() {return this.number;}
}
