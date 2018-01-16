package server;

import controller.Controllers;
import entity.Session;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class CPSServer extends AbstractServer {
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

    private static String dbUrl;
    private static String dbUsername;
    private static String dbPwd;

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public CPSServer(int port) {
        super(port);
    }

    /**
     * This method handles any messages received from the client.
     *
     * @param msg    The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client) {
        if (IS_DEBUG)
            System.out.println("\nRECIEVED (" + client.getInetAddress() + "): " + msg);
        try {
            MessageHandler.handleMessage((String) msg, client);
        } catch (IOException e) {
            MessageHandler.dropSession(client);
            e.printStackTrace();
        }
    }


    /**
     * This method overrides the one in the superclass.
     * Called when the server starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass.
     * Called when the server stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }


    /**
     * Starts the server.
     *
     * @param args override arguments (optional)
     */
    public static void main(String[] args) {
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

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("CPSServer ", options);
            System.exit(1);
            return;
        }

        IS_DEBUG = cmd.hasOption("debug");

        if (cmd.hasOption("database")) {
            if (!cmd.getOptionValue("database").matches("^((mysql):\\/)?\\/?([^:\\/\\s]+)(\\:\\d+)?((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$")) {
                System.err.printf("\"%s\" is not a valid MySQL url!\nMake sure the database URL is formatted as \"mysql://hostname:port/db_name\"", cmd.getOptionValue("database"));
                System.exit(1);
                return;
            }
        }

        /**
         * Override defaults if needed
         */

        int port = cmd.hasOption("port") ? Integer.valueOf(cmd.getOptionValue("port")) : DEFAULT_PORT;
        dbUsername = cmd.hasOption("username") ? cmd.getOptionValue("username") : DEFAULT_USERNAME;
        dbPwd = cmd.hasOption("password") ? cmd.getOptionValue("password") : DEFAULT_PWD;
        dbUrl = cmd.hasOption("database") ? cmd.getOptionValue("database") : DEFAULT_URL;

        /**
         * Connection Attempt
         */

        CPSServer sv = new CPSServer(port);

        try {
            Controllers.initDb(dbUrl, dbUsername, dbPwd);
            System.out.print("Please wait, setting up...");
            Controllers.init();
            System.out.println("Done!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            if (e.getMessage().contains("No suitable driver found")) {
                System.err.println("Make sure the database URL is formed as \"mysql://hostname:port/db_name\"");
            }
            System.exit(1);
            return;
        }

        /**
         * Start listening and handle messages.
         */
        try {
            sv.listen(); //Start listening for connections
            accept();
        } catch (Exception ex) {
            System.err.println("ERROR - Could not listen for clients!");
            System.exit(1);
        }
    }

    /**
     * This method waits for input from the console.
     */
    private static void accept() {
        BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while (true) {
            try{
                message = fromConsole.readLine().replaceAll("\\s+","");
                int size = MessageHandler.getSessionsMap().size();
                switch (message) {
                    case "refresh":
                        System.out.println("Please wait, refreshing all controllers...");
                        try {
                            Controllers.initDb(dbUrl, dbUsername, dbPwd);
                        } catch (SQLException e) {
                            System.err.println("An error occurred refreshing the database: ");
                            e.printStackTrace();
                            System.err.println("It is recommended that you restart the server!");
                        }
                        Controllers.init();
                        System.out.println("Refresh complete!");
                        break;
                    case "sessions":
                        System.out.println(String.format("Currently there are %d active sessions" + (size > 0 ? ":" : "."), size));
                        for (Session session : MessageHandler.getSessionsMap().values()) {
                            System.out.println("\t" + session);
                        }
                        break;
                    case "drop":
                        if (size == 0)
                        {
                            System.out.println("No sessions to drop.");
                            break;
                        }
                        System.out.print("Enter session ID to disconnect (or anything else to abort): ");
                        String input = fromConsole.readLine();
                        if (input.matches("\\d+")) //Matches a number
                        {
                            Session sessionToDrop = MessageHandler.getSession(Integer.valueOf(input));
                            if (sessionToDrop == null)
                            {
                                System.out.println("Session #" + input + " not found!");
                            }
                            else
                            {
                                MessageHandler.dropSession(sessionToDrop);
                                //TODO: Anything else you need to - like disconnect actual listening?
                                System.out.println("Session #" + sessionToDrop.getSid() + " dropped!");
                            }
                        }
                        else
                        {
                            System.out.println("Aborted.");
                        }
                        break;
                    case "purge":
                        if (size == 0)
                        {
                            System.out.println("No sessions to purge.");
                            break;
                        }
                        System.out.print("Purging all sessions...");
                        MessageHandler.getSessionsMap().clear();
                        System.out.println("Done!");
                        //TODO: Anything else you need to - like disconnect actual listening?
                        break;
                    case "exit":
                        //TODO: Handle orderly exit?
                        System.out.println("So long, and thanks for all the fish!");
                        System.exit(0);
                        break;
                    case "": //Empty string
                        break;
                    default:
                        System.out.println("Unknown command \"" + message +"\"");
                }
            } catch (IOException e) {
                System.err.println("An error occurred processing that command.");
                e.printStackTrace();
            }
        }
    }
}
