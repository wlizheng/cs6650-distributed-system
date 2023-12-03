package server.database;

import com.zaxxer.hikari.HikariDataSource;
import service_interface.Review;

import java.sql.*;

public class ReviewDao {
    private final HikariDataSource dataSource;

    public ReviewDao(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Review createReview(String albumID, String likeOrNot) {
        String insertReview = "INSERT INTO reviews (albumID, likeOrNot) VALUES (?, ?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(insertReview,
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, albumID);
            preparedStatement.setString(2, likeOrNot);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    String reviewID = resultSet.getString(1);
                    return new Review(reviewID, albumID, likeOrNot);
                }
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

