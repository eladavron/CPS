package server;

import controller.Controllers;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import org.apache.commons.cli.*;
import static controller.Controllers.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class CPSServer extends AbstractServer
{
    /**
     * Default Params. Will not be hard-coded in final product.
     */
    final private static int DEFAULT_PORT = 5555;
    final private static String DEFAULT_URL = "mysql://mysql.eladavron.com:3306/cps_prototype";
    final private static String DEFAULT_USERNAME = "swe";
    final private static String DEFAULT_PWD = "6R1Csn4B";

    /**
     * Default Strings
     */
    final private static String HELP_COMMANDS = "Available commands:\n\tquery <table_name> (leave blank to get table names)\n\tcreate employee <name> <email> <password>\n\texit\n";
    final private static String HELP_CREATE = "Valid format for command is: create employee <name> <email> <password>\n";

    /**
     * Instance Parameters
     */
    public static boolean IS_DEBUG;

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public CPSServer(int port)
    {
        super(port);
    }

    public static void initDummies() {

        //Dummy customer
        ArrayList<Integer> dummyCarList = new ArrayList<Integer>();
        dummyCarList.add(1234567);
        dummyCarList.add(7654321);
        customerController.addNewCustomer(666, "Lucifer","666", "something@hateful.edu", dummyCarList);
    }


    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
    {
        if (IS_DEBUG)
            System.out.println("RECIEVED (" + client.getInetAddress()+ "): " + msg);
        try {
            MessageHandler.handleMessage((String)msg, client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method overrides the one in the superclass.
     * Called when the server starts listening for connections.
     */
    protected void serverStarted()
    {
        System.out.println("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass.
     * Called when the server stops listening for connections.
     */
    protected void serverStopped()
    {
        System.out.println("Server has stopped listening for connections.");
    }


    /**
     * Starts the server.
     * @param args override arguments (optional)
     */
    public static void main(String[] args)
    {
        /**
         * Command Line Parser Setup
         */

        Options options = new Options();

        Option optDB = new Option("db", "database", true, String.format("Database URL (default: %s)", DEFAULT_URL));
        optDB.setRequired(false);
        options.addOption(optDB);

        Option optUsername = new Option("u", "username", true, String.format("Database Username (default: %s)", DEFAULT_USERNAME));
        optUsername.setRequired(false);
        options.addOption(optUsername);

        Option optPWD = new Option("pwd", "password", true, "Database Password");
        optPWD.setRequired(false);
        options.addOption(optPWD);

        Option optPort = new Option("p", "port", true, String.format("Server Port (default: %d)", DEFAULT_PORT));
        optPort.setRequired(false);
        options.addOption(optPort);

        Option debug = new Option("d", "debug", false, "Debug flag");
        optPort.setRequired(false);
        options.addOption(debug);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        /**
         * Parse Command Line Arguments
         */

        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.err.println(e.getMessage());
            formatter.printHelp("CPSServer ", options);
            System.exit(1);
            return;
        }

        IS_DEBUG = cmd.hasOption("debug");

        if (cmd.hasOption("database"))
        {
            if (!cmd.getOptionValue("database").matches("^((mysql):\\/)?\\/?([^:\\/\\s]+)(\\:\\d+)?((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$"))
            {
                System.err.printf("\"%s\" is not a valid MySQL url!\nMake sure the database URL is formatted as \"mysql://hostname:port/db_name\"", cmd.getOptionValue("database"));
                System.exit(1);
                return;
            }
        }

        /**
         * Override defaults if needed
         */

        int port = cmd.hasOption("port") ? Integer.valueOf(cmd.getOptionValue("port")) : DEFAULT_PORT;
        String dbUsername = cmd.hasOption("username") ? cmd.getOptionValue("username") : DEFAULT_USERNAME;
        String dbPwd = cmd.hasOption("password") ? cmd.getOptionValue("password") : DEFAULT_PWD;
        String dbUrl = cmd.hasOption("database") ? cmd.getOptionValue("database") : DEFAULT_URL;

        /**
         * Connection Attempt
         */

        CPSServer sv = new CPSServer(port);

        try
        {
            Controllers.initDb(dbUrl, dbUsername, dbPwd);
            Controllers.init();
            initDummies();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to connect to database: " + e.getMessage());
            if (e.getMessage().contains("No suitable driver found"))
            {
                System.err.println("Make sure the database URL is formed as \"mysql://hostname:port/db_name\"");
            }
            System.exit(1);
            return;
        }

        /**
         * Start listening and handle messages.
         */
        try
        {
            sv.listen(); //Start listening for connections
        }
        catch (Exception ex)
        {
            System.err.println("ERROR - Could not listen for clients!");
            System.exit(1);
            return;
        }
    }
}
