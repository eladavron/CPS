package cps.server;

import ocsf.server.*;

import java.io.IOException;
import java.sql.ResultSet;

import static cps.Helpers.RetryScanner;

public class CPSServer extends AbstractServer
{
    //Default Params
    final public static int DEFAULT_PORT = 5555;

    //Instance Parameters
    private static DBController dbController;

    /**
     * Constructs an instance of the echo cps.server.
     *
     * @param port The port number to connect on.
     */
    public CPSServer(int port)
    {
        super(port);
    }

    /**
     * This method handles any messages received from the cps.client.
     *
     * @param msg The message received from the cps.client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
    {
        System.out.println(client.getInetAddress()+ ": " + msg);
        String[] command = ((String) msg).split("\\s"); //Split command
        try{
            switch (command[0]){
                case "query":
                    if (command.length != 2) //Not valid
                    {
                        client.sendToClient("Error! Missing table name to query");
                    }
                    else
                    {
                        client.sendToClient(dbController.GetData(command[1]));
                    }
                    break;
                default: //Unknown command
                    client.sendToClient(String.format("Command \"%s\" is not recognized!", command[0]));
            }
        } catch (IOException ex)
        {
            System.err.printf("Failed sending message to client at %s", client.getInetAddress().toString());
            ex.printStackTrace();
        }
    }


    /**
     * This method overrides the one in the superclass.  Called
     * when the cps.server starts listening for connections.
     */
    protected void serverStarted()
    {
        System.out.println
                ("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the cps.server stops listening for connections.
     */
    protected void serverStopped()
    {
        System.out.println
                ("Server has stopped listening for connections.");
    }

    //Class methods ***************************************************

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT; //Port to listen on

        String dbUsername= "";
        String dbPwd = "";
        String dbUrl = "";

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
                        dbUrl = RetryScanner(String.format("Input DB URL (default \"%s\"): ", dbUrl),"^((mysql):\\/)?\\/?([^:\\/\\s]+)(\\:\\d+)?((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$");
                        dbUsername = RetryScanner(String.format("Input DB Username (default \"%s\"): ", dbUsername), "^\\S+$");
                        dbPwd = RetryScanner("Input DB Password (default hidden): ", "^\\S+$");
                        break;
                    case "--port":
                        port = Integer.valueOf(RetryScanner("Input port number for CPS system (default 5555): ", "^\\d+$"));
                        break;
                    default:
                        System.out.println("Unknown parameter \"arg\"!\nUse either \"--db\" to override database connection settings or \"--port\" to override server port.");
                        System.exit(1);
                }
            }
        }
        CPSServer sv = new CPSServer(port);
        dbController = new DBController(dbUsername, dbPwd, dbUrl);
        try
        {
            sv.listen(); //Start listening for connections
        }
        catch (Exception ex)
        {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }
}
