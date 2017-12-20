package cps.server;

import cps.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class DBController {
        private static Connection db_conn;
        private static ArrayList<String> listTables = new ArrayList<String>();

    public DBController(String url, String username, String password) throws SQLException {
        try {
            db_conn = connect(url, username, password);
            DatabaseMetaData meta = db_conn.getMetaData();
            ResultSet res = meta.getTables(null, null, "%", null);
            while (res.next()) {
                listTables.add(res.getString("TABLE_NAME"));
            }
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
            if (!listTables.contains(table)) {
                String returnString;
                returnString = String.format("Unknown table \"%s\"!\n", table);
                returnString += ListTables();
                return returnString;
            }
            ResultSet rs = this.QueryEntireTable(table);
            switch (table) {
                case "employees":
                    String columnNamesEmployees = String.format("%3s %15s %20s %25s", "UID", "Name", "Email", "Creation Time");
                    ArrayList<String> rowsEmployees = new ArrayList<>();
                    while (rs.next()) {
                        String row = String.format("%3d %15s %20s %25s",
                                rs.getInt("UID"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getTimestamp("create_time"));
                        rowsEmployees.add(row);
                    }
                    return PrintTable(columnNamesEmployees, rowsEmployees);

                case "parking_lots":
                    String columnNamesParkingLots = String.format("%3s %15s %10s %15s", "UID", "Location", "Size", "Manager ID");
                    ArrayList<String> rowsParkingLots = new ArrayList<>();
                    while (rs.next()) {
                        String row = String.format("%3d %15s %10s %15d",
                                rs.getInt("UID"),
                                rs.getString("location"),
                                rs.getInt("rows") + "x" + rs.getInt("columns") + "x" + rs.getInt("depth"),
                                rs.getInt("manager_id"));
                        rowsParkingLots.add(row);

                    }
                    return PrintTable(columnNamesParkingLots, rowsParkingLots);
                default:
                    String returnString;
                    returnString = "Unknown table \"" + table + "\"!\n";
                    returnString += "Table Names are : parking_lots, employees";
                    return returnString;
            }
        } catch (SQLException e) {
            System.err.printf("Error occurred getting data from table \"%s\":\n%s", table, e.getMessage());
            return "Error occurred!\nSee server output for details.";
        }
    }

    private String PrintTable(String columnsString, ArrayList<String> rows)
    {
        String returnString =
                String.join("", Collections.nCopies(columnsString.length(),"-"))
                + "\n" + columnsString + "\n"
                + String.join("", Collections.nCopies(columnsString.length(),"-")) + "\n";
        for(String row : rows){
            returnString += row + "\n";
        }
        returnString += String.join("", Collections.nCopies(columnsString.length(),"-"));
        return returnString;
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

    /* Output Formatters */
    public static String ListTables()
    {
        String returnString = "Available tables:\n";
        for (String tableName : listTables) {
            returnString += "\t\t" + tableName + "\n";
        }
        return returnString;
    }
}
