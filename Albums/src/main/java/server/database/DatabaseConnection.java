package server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://cs6650-database.clfmf0ji1qhj.us-west-2.rds.amazonaws.com:3306/AlbumStore";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "dbcs6650";
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMinimumIdle(50);
        config.setMaximumPoolSize(50);
        config.setConnectionTimeout(180000);
        config.setIdleTimeout(180000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}