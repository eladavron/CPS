package cps.server;

import cps.*;
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

    /* Query Parsers */
    public String GetData(String table)
    {
        try {
            String returnString;
            ResultSet rs = this.QueryEntireTable(table);
            switch (table) {
                case "employees":
                    returnString = "UID\t\tName\t\t\tEmail\t\t\tCreation Date\n";
                    while (rs.next()) {
                        returnString += rs.getInt("UID") +"\t\t" + rs.getString("name") + "\t\t" + rs.getString("email") + "\t" + rs.getTimestamp("create_time") + "\n";
                    }
                    return returnString;
                case "parking_lots":
                    returnString = "UID\t\tLocation\t\tSize\t\tManager ID\n";
                    while (rs.next()) {
                        returnString += rs.getInt("UID") +"\t\t"
                                + rs.getString("location") + "\t"
                                + rs.getInt("rows") + "x" + rs.getInt("columns") + "x" + rs.getInt("depth") + "\t"
                                + rs.getTimestamp("create_time") + "\n";
                    }
                    return returnString;
                default:
                    return "Unknown table \"" + table + "\"!";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.printf("Error occurred getting data from table \"%s\":\n%s", table, e.getMessage());
            return "Error occurred!\nSee server output for details.";
        }
    }

    /* Query Performers */
    private ResultSet QueryEntireTable(String table)
    {
        ResultSet result;
        try {
            Statement stmt = db_conn.createStatement();
            result = stmt.executeQuery(String.format("SELECT * FROM %s",table));
        } catch (SQLException e) {
            System.err.printf("An error occured querying table %s:\n%s\n", table, e.getMessage());
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /* Insertion */
    public boolean InsertEmployee(Employee employee) {
        try {
            Statement stmt = db_conn.createStatement();
            Date creationDate;
            int uid = -1;
            stmt.executeUpdate(String.format("INSERT INTO employees VALUES ('%s', '%s', '%s')", employee.get_name(), employee.get_email(), employee.get_password()), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                uid = rs.getInt(1);
            else
                throw new SQLException("Couldn't get auto-generated UID");

            rs = stmt.executeQuery(String.format("SELECT * FROM employees WHERE ID=%d", uid));
            if (rs.next())
                creationDate = new Date(rs.getTimestamp("create_time").getTime());
            else
                throw new SQLException("Something went wrong retrieving the employee just inserted!");

            return true;
        } catch (SQLException e) {
            System.err.printf("An error occured inserting employee %s:\n%s\n", employee, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
