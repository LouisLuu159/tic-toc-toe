package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.*;

public class SessionDAO {
	
   private static Connection getConnection() {
	   return SqlUtil.getConnection();
   }
	
   public static boolean insertSession(SessionInfo sessionInfo) {
	   String insert_session = "INSERT INTO session VALUES (?,?,?,?,?); ";
	   String insert_move = "INSERT INTO move VALUES (?,?,?,?,?);";
	   PreparedStatement preparedStatement;
	   try(Connection conn = getConnection();){
		   preparedStatement = conn.prepareStatement(insert_session);
		   preparedStatement.setString(1, sessionInfo.getId());
		   preparedStatement.setInt(2, sessionInfo.getNumberOfPlayersAllowed());
		   preparedStatement.setInt(3, sessionInfo.getNumberOfRows());
		   preparedStatement.setInt(4, sessionInfo.isDraw());
		   preparedStatement.setString(5, sessionInfo.getWinner());
		   preparedStatement.executeUpdate();
		   
		   preparedStatement = conn.prepareStatement(insert_move);
		   List<PlayerInfo> players = sessionInfo.getPlayers_info();
		   Shapes board[][] = sessionInfo.getBoard();
		   int rows = sessionInfo.getNumberOfRows();
		   for(int i = 0; i < rows; i++) {
			   for(int j = 0; j < rows; j++) {
				   for(PlayerInfo player : players) {
					   if(Shapes.equals(board[i][j], player.getShape())) {
						   preparedStatement.setInt(1, i);
						   preparedStatement.setInt(2, j);
						   preparedStatement.setString(3, sessionInfo.getId());
						   preparedStatement.setInt(4, PlayerDAO.getID(player.getNickname()));
						   preparedStatement.setString(5, player.getShape().toString());
						   preparedStatement.executeUpdate();
					   }
				   }
				   
			   }
		   }
		   preparedStatement.close();
		   
	   } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	   }
	   return true;
   }
   
//   public static List<SessionInfo> getSession(String name) {
//	   List<SessionInfo> session_list = new ArrayList<>();
//	   String get_session_by_ID = "SELECT * FROM session WHERE ID = ? ";
//	   String get_move_by_playerName = "SELECT DISTINCT sessionID FROM move WHERE playerName = ?";
//	   String get_move_by_sessionID= "SELECT * FROM move WHERE sessionID = ?";
//	   PreparedStatement preparedStatement;
//	   try(Connection conn = getConnection();){
//		   
//		   // Get list of ID's session which player joined
//		   preparedStatement = conn.prepareStatement(get_move_by_playerName);
//		   preparedStatement.setString(1, name);
//		   ResultSet rs = preparedStatement.executeQuery();
//		   List<String> sessionIDs = new ArrayList();
//		   while(rs.next()) {
//			   sessionIDs.add(rs.getString("sessionID"));
//		   }
//		   
//		   //By each ID, Get info of rows, players, isDraw, winner for session
//		   preparedStatement = conn.prepareStatement(get_session_by_ID);
//		   for(String id : sessionIDs) {
//			   preparedStatement.setString(1, id);
//			   rs = preparedStatement.executeQuery();
//			   while(rs.next()) {
//				   int numberOfRows = rs.getInt("numberOfRows");
//				   int numberOfPlayers = rs.getInt("numberOfPlayers");
//				   boolean isDraw = (rs.getInt("isDraw") == 1);
//				   String winner = rs.getString("winner");
//				   session_list.add(new SessionInfo(id, numberOfRows, numberOfPlayers, winner, isDraw));
//			   }
//		   }
//		   
//		   // Set players_info and board for each session
//		   PlayerInfo[][] board;
//		   Shapes[][] board1;
//		   for(SessionInfo session : session_list) {
//			   board = new PlayerInfo[session.getNumberOfRows()][session.getNumberOfRows()];
//			   preparedStatement = conn.prepareStatement(get_move_by_sessionID);
//			   preparedStatement.setString(1, session.getId());
//			   rs = preparedStatement.executeQuery();
//			   ArrayList<PlayerInfo> player_list = new ArrayList<>();
//			   while(rs.next()) {
//				   int row = rs.getInt("row");
//				   int col = rs.getInt("col");
//				   String playerName = rs.getString("playerName");
//				   PlayerInfo player = new PlayerInfo(playerName, PlayerDAO.getShape(playerName));
//				   player_list.add(player);
//                   board[row][col] = player;
//			   }
//			   int rows = session.getNumberOfRows();
//			   board1 = new Shapes[rows][rows];
//			   for(int i = 0; i < rows; i++) {
//				   for(int j = 0; j < rows; j++) {
//					   board1[i][j] = board[i][j].getShape();
//				   }
//			   }
//			   session.setPlayers_info(player_list);
//			   session.setBoard(board1);
//		   }
//		   preparedStatement.close();
//		   
//	   } catch (SQLException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	   }
//	   return session_list;		   
//   }
}
