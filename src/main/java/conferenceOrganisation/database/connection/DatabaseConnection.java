package conferenceOrganisation.database.connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.ejb.Singleton;

@Singleton
public class DatabaseConnection {

	public static Statement getStatement() throws SQLException, IOException {

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("No driver!");
		}

		Connection connection = null;

		try {
			Properties prop = new Properties();
			InputStream input = null;

			input = new FileInputStream("config.properties");

			prop.load(input);

			String databaseUrl = String.format("jdbc:mysql://%s:%s/%s", prop.getProperty("databaseNetworkAddress"),
					prop.getProperty("databasePort"), prop.getProperty("databaseName"));

			connection = DriverManager.getConnection(databaseUrl, prop.getProperty("databaseUserAccount"),
					prop.getProperty("databasePassword"));
		} catch (SQLException e) {
			System.out.println("Connection failed");
		}

		if (connection != null) {
			System.out.println("Connected successfuly to the database.");
		} else {
			System.out.println("Connection failed.");
		}

		return connection.createStatement();
	}

}