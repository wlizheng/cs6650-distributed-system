package server;

import com.zaxxer.hikari.HikariDataSource;
import service_interface.ImageMetaData;
import service_interface.Profile;

import java.sql.*;

public class AlbumDao {
    private final HikariDataSource dataSource;

    public AlbumDao(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
    public ImageMetaData createAlbum(Profile profile, byte[] imageBytes) {
        String insertAlbum = "INSERT INTO albums (artist, title, year, image) VALUES (?, ?, ?, ?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(insertAlbum,
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, profile.getArtist());
            preparedStatement.setString(2, profile.getTitle());
            preparedStatement.setString(3, profile.getYear());
            preparedStatement.setBytes(4, imageBytes);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    String albumID = resultSet.getString(1);
                    String imageSize = String.valueOf(imageBytes.length);
                    ImageMetaData imageMetaData = new ImageMetaData(albumID, imageSize);
                    return imageMetaData;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        return null;
    }

    public Profile getAlbum(String albumID) {
        String getAlbum = "SELECT artist, title, year FROM albums WHERE albumID = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(getAlbum);
            preparedStatement.setString(1, albumID);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String artist = resultSet.getString("artist");
                String title = resultSet.getString("title");
                String year = resultSet.getString("year");
                return new Profile(artist, title, year);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }

        return null;
    }

    private void closeResources(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

