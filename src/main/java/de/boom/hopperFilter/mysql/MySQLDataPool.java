package de.boom.hopperFilter.mysql;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLDataPool {

    private final BasicDataSource connectionPool;

    public MySQLDataPool(String host, int port, String database, String username, String password) throws ClassNotFoundException, SQLException {
        this.connectionPool = new BasicDataSource();
        Class.forName("com.mysql.jdbc.Driver");
        connectionPool.setDriverClassName("com.mysql.jdbc.Driver");
        connectionPool.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        connectionPool.setUsername(username);
        connectionPool.setPassword(password);
        connectionPool.setInitialSize(3);
        connectionPool.setAccessToUnderlyingConnectionAllowed(false);
        connectionPool.setPoolPreparedStatements(true);

        setupTables();
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public void close() throws SQLException {
        connectionPool.close();
    }

    private void setupTables() throws SQLException {
        String sql = "create table if not exists hopper (" +
                    "uuid varchar(36) not null primary key, " +
                    "creatorUUID varchar(36) not null, " +
                    "enabled tinyint(1) null, " +
                    "whitelist tinyint(1) null);";

        try (PreparedStatement st = connectionPool.getConnection().prepareStatement(sql) ) {
            st.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new SQLException(ex.getCause());
        }

        sql = "create table if not exists items (hopper_id varchar(36) not null, " +
            "id int auto_increment primary key, " +
            "material varchar(36), " +
            "foreign key (hopper_id) references hopper (uuid) on delete cascade);";
        try (PreparedStatement st = connectionPool.getConnection().prepareStatement(sql) ) {
            st.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new SQLException(ex.getCause());
        }
    }


}

