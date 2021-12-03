package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlUtil {
	private static final String hostname = "localhost";
	private static final String dbName = "tic_toc_toe";
	private static final String userName = "root";
	private static final String password = "123456";

	static Connection getConnection() {
		String connectionURL = "jdbc:mysql://" + hostname + ":3306/" + dbName + "?useSSL=false";
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(connectionURL, userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
}
