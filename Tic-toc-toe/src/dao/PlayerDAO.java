package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.shape.Shape;
import model.*;

public class PlayerDAO {

	private static Connection getConnection() {
		return SqlUtil.getConnection();
	}

	public static boolean insertPlayer(PlayerInfo player) {
		String insert_player_query = "INSERT INTO player VALUES (?)";
		try (Connection conn = getConnection();
				PreparedStatement preparedStatement = conn.prepareStatement(insert_player_query);) {

			preparedStatement.setString(1, player.getNickname());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return false;
		}

		return true;
	}
	
	public static int getID(String name) {
		String get_ID_query = "SELECT * FROM name = ?";
		int ID = 0;
		try (Connection conn = getConnection();
				PreparedStatement preparedStatement = conn.prepareStatement(get_ID_query);) {

			preparedStatement.setString(1, name);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next()) {
				ID = rs.getInt("ID");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}

		return ID;
	}

	public static void main(String[] args) {
//        boolean successful_insert = insertPlayer(new PlayerInfo("tung", Shapes.TICK));
//        if(!successful_insert) {
//        	System.out.println("Exists");
//        }else {
//        	System.out.println("OK");
//        }
//        Shapes shape = getShape("tung");
//        System.out.println(shape.toString());
        
	}
}
