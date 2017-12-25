package cps.server;

import cps.entity.Employee;
import ocsf.server.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.sql.SQLException;

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
    private static DBController dbController;

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public CPSServer(int port)
    {
        super(port);
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
        System.out.println(client.getInetAddress()+ ": " + msg);
        String[] command = ((String) msg).split("\\s"); //Split command
        try{
            if (command.length == 0) //No command given. Fallback: Shouldn't happen!
            {
                client.sendToClient("Command not recognized!\n" + HELP_COMMANDS);
            }
            switch (command[0]){ //Check Command
                case "query":
                    if (command.length == 1) //No Params
                    {
                        client.sendToClient(dbController.ListTables());
                    }
                    else
                    {
                        client.sendToClient(dbController.GetData(command[1]));
                    }
                    break;
                case "create":
                    if (command.length != 5) //Not valid length
                    {
                        client.sendToClient("Error! Wrong number of parameters:\n" + HELP_CREATE);
                    }
                    else
                    {
                        switch (command[1]) { //Check what we're trying to create
                            case "employee":
                                Employee newEmployee = new Employee(command[2], command[3], command[4]);
                                if (dbController.InsertEmployee(newEmployee))
                                    client.sendToClient(String.format("Successfully created employee:\n%s", newEmployee.toString()));
                                else
                                    client.sendToClient("An error occurred adding the employee. See server for more details.");
                                break;
                            default:
                                client.sendToClient("Can only create employees at this time!");
                        }
                    }
                    break;
                case "help": //Display help message
                    client.sendToClient(HELP_COMMANDS);
                    break;
                default: //Unknown command
                    client.sendToClient(String.format("Command \"%s\" is not recognized!\n%s", command[0], HELP_COMMANDS));
            }
        } catch (IOException ex)
        {
            System.err.printf("Failed sending message to client at %s", client.getInetAddress().toString());
            ex.printStackTrace();
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
            formatter.printHelp("CPSClient ", options);
            System.exit(1);
            return;
        }

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
            dbController = new DBController(dbUrl, dbUsername, dbPwd);
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
