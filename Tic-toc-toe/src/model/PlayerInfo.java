package model;

import java.io.Serializable;

public class PlayerInfo implements Serializable{
	private String nickname;
	private Shapes shapes;
	
	public PlayerInfo(String nickname, Shapes shapes) {
		
		this.nickname = nickname;
		this.shapes = shapes;
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Shapes getShape() {
		return shapes;
	}
	public void setShape(Shapes shapes) {
		this.shapes = shapes;
	}
	
	public String toString() {
		return "{nickname: " + this.nickname + ", shapes: " + this.shapes.toString() + "}";
	}
	
}
