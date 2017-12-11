package cps.server;

import java.sql.*;

public class DBController {
    final private static String DB_URL = "mysql://mysql.eladavron.com:3306/cps_prototype";
    final private static String DB_USERNAME = "swe";
    final private static String DB_PWD = "6R1Csn4B";

    private Connection db_conn;

    public DBController(String url, String username, String password){
        String conn_url = url.isEmpty() ? DB_URL : url;
        String conn_username = username.isEmpty() ? DB_USERNAME : username;
        String conn_password = username.isEmpty() ? DB_PWD : password;
        db_conn = connect(conn_url, conn_username, conn_password);
    }

    public static Connection connect(String url, String username, String password) {
        try
        {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            Connection conn = DriverManager.getConnection("jdbc:" + url, username, password);
            System.out.println("Database connected!");
            return conn;
        }
        catch (Exception ex)
        {
            System.err.println("Database connection error: " + ex.getMessage());
            return null;
        }
    }
}
