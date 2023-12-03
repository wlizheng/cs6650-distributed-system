package server.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void createTables() {
        try (Connection connection = DatabaseConnection.getDataSource().getConnection()) {
            createSchema(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createSchema(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            // Drop the schema if it exists (optional)
            statement.execute("DROP SCHEMA IF EXISTS AlbumStore");

            // Create the AlbumStore schema
            statement.execute("CREATE SCHEMA AlbumStore");

            // Use the AlbumStore schema
            statement.execute("USE AlbumStore");

            // Create the albums table
            statement.execute("CREATE TABLE IF NOT EXISTS albums ("
                    + "albumID INT AUTO_INCREMENT PRIMARY KEY,"
                    + "artist VARCHAR(255) NOT NULL,"
                    + "title VARCHAR(255) NOT NULL,"
                    + "year VARCHAR(4) NOT NULL,"
                    + "image LONGBLOB NOT NULL)");

            // Create the reviews table
            statement.execute("CREATE TABLE IF NOT EXISTS reviews ("
                    + "reviewID INT AUTO_INCREMENT PRIMARY KEY,"
                    + "albumID INT NOT NULL,"
                    + "likeOrNot VARCHAR(255) NOT NULL)");

//            System.out.println("Schema and tables created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
