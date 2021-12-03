package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import Exception.AddPlayerException;
import dao.PlayerDAO;
import dao.SessionDAO;
import model.Message;
import model.Player;
import model.PlayerInfo;
import model.SessionInfo;
import model.Shapes;

public class ServerSession implements Runnable{
	
//	 private HashMap<String, Player> players;
	 private ArrayList<Player> players;
	 private String id;
     private int currentActivePlayer_No;
     private int numberOfRows;
     private int numberOfPlayersAllowed;
     private int numberOfMoves;
     private int boardsize;
     private Shapes[][] board;
     private Shapes[] shape_list = {Shapes.CIRCLE, Shapes.LINE, Shapes.POLYGON, 
    		 Shapes.POLYGON, Shapes.TICK};
     
     
     public ServerSession() {
    	 this.players = new ArrayList<>();
    	 this.numberOfMoves = 0;
     }
     

	public ServerSession(int numberOfRows, int numberOfPlayersAllowed) {
		this.id = UUID.randomUUID().toString();
		this.numberOfRows = numberOfRows;
		this.numberOfPlayersAllowed = numberOfPlayersAllowed;
		this.players = new ArrayList<>();
		this.numberOfMoves = 0;
	}

	public void addPlayer(Player player) throws AddPlayerException {
		if(IsNickNameDuplicated(player.getNickname()) )
			throw new AddPlayerException(Message.NICKNAME_EXISTS.toString());
		
		if(players.size() < numberOfPlayersAllowed) {
			player.setShape(this.shape_list[players.size()]);
			players.add(player);

			SessionInfo session_info = this.getSessionInfo();
			for(Player p: players) {
				p.sendObject(session_info);
			}

			if(players.size() == numberOfPlayersAllowed)
				startGame();
		}else {
			throw new AddPlayerException(Message.SESSION_IS_FULL.toString());
		}
	}
	
	private void startGame() {
		System.out.println("Start game!");
		String move = null;
		String winner = null;
		Boolean isDraw = false;
		this.currentActivePlayer_No = 0;
		this.board = new Shapes[this.numberOfRows][this.numberOfRows];
		while(winner != null || isDraw != true) {
			Player currentActivePlayer = players.get(currentActivePlayer_No);
		    System.out.println("Player in turn: " + currentActivePlayer.getNickname());
			sendObjectToAllPlayer(currentActivePlayer.getNickname());// send msg who is in turn to all players
			move = (String)currentActivePlayer.readObject(); // get move for current player
			
			this.numberOfMoves += 1;
			this.currentActivePlayer_No += 1;
			if(this.currentActivePlayer_No == this.players.size())
				this.currentActivePlayer_No = 0;
			String[] array = move.split("-");
	        int rowValue = Integer.parseInt(array[0]);
	        int colValue = Integer.parseInt(array[1]);
	        this.board[rowValue][colValue] = currentActivePlayer.getShape();
	        
	        if(this.numberOfMoves == this.numberOfRows*this.numberOfRows) {
	        	isDraw = true;
	        	System.out.println("The game is draw.");
	        }
	        else if(checkWinningState(rowValue, colValue, currentActivePlayer.getShape())) {
	        	winner = currentActivePlayer.getNickname();
	        	System.out.println("The winner is " + winner + ". Game ended.");
	        }
	        Object moveAndResult = new Object[] {currentActivePlayer.getShape(),move,winner, isDraw};
	        sendObjectToAllPlayer(moveAndResult);
		}
//		this.saveSession(winner, isDraw);
		this.closeStreams();
	}
	
//	private void saveSession(String winner, boolean isDraw) {
//		SessionInfo session = getSessionInfo();
//		session.setBoard(board);
//		session.setWinner(winner);
//		session.setDraw(isDraw);
//		SessionDAO.insertSession(session);
//	}
	
	private boolean checkWinningState(int rowValue, int columnValue, Shapes shapes) {
        return rowCondition(rowValue, shapes) ||
                columnCondition(columnValue, shapes) ||
                diagonalCondition(rowValue, columnValue, shapes) ||
                antiDiagonalCondition(shapes);
    }

    private boolean rowCondition(int row, Shapes shapes) {
        for (int columnIndex = 0; columnIndex < this.numberOfRows; columnIndex++) {
            if (!(this.board[row][columnIndex] == shapes)) {
                return false;
            }
        }
        return true;
    }

    private boolean columnCondition(int column, Shapes shapes) {
        for (int rowIndex = 0; rowIndex < this.numberOfRows; rowIndex++) {
            if (!(this.board[rowIndex][column] == shapes)) {
                return false;
            }
        }
        return true;
    }

    private boolean diagonalCondition(int row, int column, Shapes shapes) {
        if (row == column) {
            for (int index = 0; index < this.numberOfRows; index++) {
                if (!(this.board[index][index] == shapes)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean antiDiagonalCondition(Shapes shapes) {
        for (int index = (this.numberOfRows - 1), i = 0; index >= 0; index--, i++) {
            if (!(this.board[i][index] == shapes)) {
                return false;
            }
        }
        return true;
    }
	
	private void sendObjectToAllPlayer(Object obj) {
		for(Player p: players) {
			p.sendObject(obj);
		}
	}
	
	private boolean IsNickNameDuplicated(String nickname) {
		for(Player player: players) {
			if(player.getNickname().equals(nickname)) {
				return true;
			}
		}
		return false;
	}
     
     public String getId() {
    	 return id;
     }

     public SessionInfo getSessionInfo() {
    	 ArrayList<PlayerInfo> players_info = new ArrayList<PlayerInfo>();
    	 for(Player player : players) {
    		 players_info.add(new PlayerInfo(player.getNickname(),player.getShape()));
    	 }
//    	 for(String nickname : players.keySet()) {
//    		 Player p = players.get(nickname);
//    		 players_info.add(new PlayerInfo(p.getNickname(), p.getShape()));
//    	 }
    	 SessionInfo sessionInfo = new SessionInfo(this.id,this.numberOfRows, this.numberOfPlayersAllowed, players_info);
    	 return sessionInfo;
     }

     private void closeStreams() {
    	 for(Player player:players) {
    		 player.closeStream();
    	 }
     }


	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
     
}
