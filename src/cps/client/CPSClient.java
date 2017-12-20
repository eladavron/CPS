package cps.client;

import java.io.*;
import org.apache.commons.cli.*;

public class CPSClient
{
    /**
     * The default port and hostname.
     * Can be overridden from command line
     */
    final public static int DEFAULT_PORT = 5555;
    final public static String DEFAULT_HOST = "localhost";

    ClientController client;

    /**
     * Constructs an instance of the client and attempts connecting to server..
     *
     * @param host The host to connect to.
     * @param port The port to connect on.
     */
    public CPSClient(String host, int port)
    {
        try
        {
            System.out.println(String.format("Attempting to connect to %s on port %d...", host, port));
            client = new ClientController(host, port);
            System.out.println("Connected! Type \"help\" for a list of available commands.");
        }
        catch(IOException exception)
        {
            System.out.println("Error: Can't setup connection! Terminating client.");
            System.exit(1);
        }
    }

    /**
     * This method waits for input from the console.
     * Once it is received, it sends it to the client's message handler.
     */
    public void accept()
    {
        try
        {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;
            System.out.print("> ");

            while (true)
            {
                message = fromConsole.readLine();
                if (message.equals("exit")) //Exit command is handled client-side
                {
                    System.out.println("Thank you, come again!");
                    System.exit(0);
                    return;
                }
                else if (message.matches("^\\s*$")) //Empty string also handled client-side.
                {
                    System.out.print("> ");
                }
                else
                    client.handleMessageFromClientUI(message);
            }
        }
        catch (Exception ex)
        {
            System.err.println("Unexpected error while reading from console!");
        }
    }

    /**
     * The main client method.
     *
     * @param args arguments to override hard-coded settings.
     */
    public static void main(String[] args)
    {

        /**
         * Command Line Parser setup
         */
        Options options = new Options();

        Option optIP = new Option("h", "host", true, "Server hostname or IP address");
        optIP.setRequired(false);

        Option optPort = new Option("p", "port", true, "Server Port");
        optPort.setRequired(false);

        options.addOption(optIP);
        options.addOption(optPort);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        /**
         * Parse command line arguments
         */
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp("CPSClient ", options);
            System.exit(1);
            return;
        }

        //Override defaults if needed
        String host = cmd.hasOption("host") ? cmd.getOptionValue("host") : DEFAULT_HOST;
        int port = cmd.hasOption("port") ? Integer.valueOf(cmd.getOptionValue("port")) : DEFAULT_PORT;

        /**
         * Create the chat client and wait.
         */
        CPSClient chat = new CPSClient(host, port);
        chat.accept();  //Wait for console data
    }
}
