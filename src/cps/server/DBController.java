package cps.server;

import cps.*;
import java.sql.*;

public class DBController {
        private Connection db_conn;

    public DBController(String url, String username, String password) throws SQLException {
        try {
            db_conn = connect(url, username, password);
        }
        catch (SQLException ex)
        {
            throw ex;
        }
    }

    public static Connection connect(String url, String username, String password) throws SQLException {
        try
        {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            Connection conn = DriverManager.getConnection("jdbc:" + url, username, password);
            System.out.println("Database connected!");
            return conn;
        }
        catch (SQLException ex)
        {
            throw ex;
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
                        returnString += rs.getInt("UID") +"\t" + rs.getString("name") + "\t\t" + rs.getString("email") + "\t" + rs.getTimestamp("create_time") + "\n";
                    }
                    rs.close();
                    return returnString;
                case "parking_lots":
                    returnString = "UID\tLocation\t\tSize\t\tManager ID\n";
                    while (rs.next()) {
                        returnString += rs.getInt("UID") +"\t"
                                + rs.getString("location") + "\t\t"
                                + rs.getInt("rows") + "x" + rs.getInt("columns") + "x" + rs.getInt("depth") + "\t\t"
                                + rs.getInt("manager_id") + "\n";

                    }
                    rs.close();
                    return returnString;
                default:
                    returnString = "Unknown table \"" + table + "\"!\n";
                    returnString += "Table Names are : parking_lots, employees"
                    return returnString;
            }
        } catch (SQLException e) {
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
            stmt.executeUpdate(String.format("INSERT INTO employees (name, email, password) VALUES ('%s', '%s', '%s')", employee.get_name(), employee.get_email(), employee.get_password()), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                uid = rs.getInt(1);
            else
                throw new SQLException("Couldn't get auto-generated UID");

            rs = stmt.executeQuery(String.format("SELECT * FROM employees WHERE UID=%d", uid));
            if (rs.next())
                creationDate = new Date(rs.getTimestamp("create_time").getTime());
            else
                throw new SQLException("Something went wrong retrieving the employee just inserted!");

            employee.set_uid(uid);
            employee.set_creationDate(creationDate);

            return true;
        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", employee, e.getMessage());
            return false;
        }
    }
}
