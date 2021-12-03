package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Player {
	private String nickname;
	private Shapes shapes;
	private ObjectInputStream fromPlayer; //  input stream to receive player message
	private ObjectOutputStream toPlayer; // output stream to send message to player
	
	public Player() {
		
	}
	
	public Player(String nickname, ObjectInputStream fromPlayer, ObjectOutputStream toPlayer) {
		
		this.nickname = nickname;
		this.fromPlayer = fromPlayer;
		this.toPlayer = toPlayer;
	}
	
	public void sendObject(Object obj) {
		try {
			this.toPlayer.writeObject(obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Object readObject() {
		try {
			return this.fromPlayer.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	
	public void closeStream() {
		try {
			this.toPlayer.close();
			this.fromPlayer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
