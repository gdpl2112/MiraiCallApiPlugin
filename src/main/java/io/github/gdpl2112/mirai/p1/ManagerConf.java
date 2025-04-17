package io.github.gdpl2112.mirai.p1;

import java.sql.*;

/**
 * @author github.kloping
 */
public class ManagerConf {
    private static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:./conf/callApi/switch.db");
        return connection;
    }

    public static ManagerConf INSTANCE = new ManagerConf();

    public ManagerConf() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sw(touch VARCHAR(50),id VARCHAR(20),k BLOB);");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Boolean getStateByTouchAndIdDefault(String touch, String id, Boolean k) {
        Boolean a = query(String.format("SELECT k FROM sw WHERE touch='%s' AND id='%s'", touch, id));
        if (a == null) {
            update(String.format("INSERT INTO sw VALUES ('%s', '%s', %s);", touch, id, k));
            return k;
        }
        return a;
    }

    public void setStateByTouchAndId(String touch, String id, boolean k) {
        getStateByTouchAndIdDefault(touch, id, k);
        update(String.format("UPDATE sw SET k=%s WHERE touch='%s' AND id='%s';", k, touch, id));
    }

    public void setStateByTouch(String touch, boolean k) {
        update(String.format("UPDATE sw SET k=%s WHERE touch='%s';", k, touch));
    }

    public Integer update(String sql) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            int i = statement.executeUpdate(sql);
            statement.close();
            connection.close();
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Boolean query(String sql) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            Boolean k = null;
            if (rs.next()) {
                k = rs.getBoolean("k");
            }
            statement.close();
            connection.close();
            return k;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
