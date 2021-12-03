package model;

import java.io.Serializable;
import java.util.ArrayList;

public class SessionInfo implements Serializable{
    private String id; 
    private int numberOfRows;
    private int numberOfPlayersAllowed;
    private ArrayList<PlayerInfo> players_info;
    private Shapes[][] board;
    private String winner;
    private boolean isDraw;
    
    public SessionInfo() {
    	this.players_info = new ArrayList<>();
    }
    
	public SessionInfo(String id, int numberOfRows, int numberOfPlayersAllowed,
			ArrayList<PlayerInfo> players_info) {
		this.id = id;
		this.numberOfRows = numberOfRows;
		this.numberOfPlayersAllowed = numberOfPlayersAllowed;
		this.players_info = players_info;
	}
	
	
	
	public SessionInfo(String id, int numberOfRows, int numberOfPlayersAllowed, String winner, boolean isDraw) {
		super();
		this.id = id;
		this.numberOfRows = numberOfRows;
		this.numberOfPlayersAllowed = numberOfPlayersAllowed;
		this.winner = winner;
		this.isDraw = isDraw;
	}

	public String getId() {
		return id;
	}


	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

	public int getNumberOfPlayersAllowed() {
		return numberOfPlayersAllowed;
	}

	public void setNumberOfPlayersAllowed(int numberOfPlayersAllowed) {
		this.numberOfPlayersAllowed = numberOfPlayersAllowed;
	}

	public ArrayList<PlayerInfo> getPlayers_info() {
		return players_info;
	}

	public void setPlayers_info(ArrayList<PlayerInfo> players_info) {
		this.players_info = players_info;
	}
    
	public String toString() {
		String res =  
				"Rows: " + this.numberOfRows + 
				", numberOfPlayersAllowedAllowed: " + this.numberOfPlayersAllowed + 
				", NumberOfCurrentPlayer: " + this.players_info.size() + 
				"\nPlayer list: \n ";
		for(PlayerInfo info : players_info) {
			res += info.toString();
		}
		return res;
	}

	
	
	public void setBoard(Shapes[][] board) {
		this.board = board;
	}

	public String getWinner() {
		return winner;
	}

	public int isDraw() {
		if(isDraw)
			return 1;
		return 0;
	}

	public Shapes[][] getBoard() {
		return board;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
	}
	
	
	
    
}
