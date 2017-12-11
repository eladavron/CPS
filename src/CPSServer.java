import ocsf.server.*;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CPSServer extends AbstractServer
{
    //Default Params
    final public static int DEFAULT_PORT = 5555;
    final private static String DB_URL = "mysql://mysql.eladavron.com:3306/cps_prototype";
    final private static String DB_USERNAME = "swe";
    final private static String DB_PWD = "6R1Csn4B";

    //Instance Parameters
    static Connection db_conn;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public CPSServer(int port)
    {
        super(port);
    }


    //Instance methods ************************************************

    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
    {
        System.out.println("Message received: " + msg + " from " + client);
    }


    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted()
    {
        System.out.println
                ("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    protected void serverStopped()
    {
        System.out.println
                ("Server has stopped listening for connections.");
    }

    //Class methods ***************************************************

    public static void main(String[] args)
    {
        int port = 0; //Port to listen on

        String dbUsername= DB_USERNAME;
        String dbPwd = DB_PWD;
        String dbUrl = DB_URL;

        /**
         * Command Line Parameters
         */
        if (args.length > 0)
        {
            for (String arg : args)
            {
                switch (arg)
                {
                    case "--db":
                        dbUrl = Helpers.RetryScanner(String.format("Input DB URL (default \"%s\"): ", dbUrl),"^((mysql):\\/)?\\/?([^:\\/\\s]+)(\\:\\d+)?((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$");
                        dbUsername = Helpers.RetryScanner(String.format("Input DB Username (default \"%s\"): ", dbUsername), "^\\S+$");
                        dbPwd = Helpers.RetryScanner("Input DB Password (default hidden): ", "^\\S+$");
                }
            }
        }
        CPSServer sv = new CPSServer(port);

        dbUrl = "jdbc:" + dbUrl;

        try
        {
            db_conn = connect(dbUrl, dbUsername, dbPwd);
            sv.listen(); //Start listening for connections
        }
        catch (Exception ex)
        {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }

    public static Connection connect(String url, String username, String password) {
        try
        {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            Connection conn = DriverManager.getConnection(url, username, password);
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
//End of CPSServer class
