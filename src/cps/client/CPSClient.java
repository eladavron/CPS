package cps.client;

import java.io.*;

public class CPSClient
{
    /**
     * The default port to connect on.
     */
    final public static int DEFAULT_PORT = 5555;

    ClientController client;

    /**
     * Constructs an instance of the cps.client.CPSClient UI.
     *
     * @param host The host to connect to.
     * @param port The port to connect on.
     */
    public CPSClient(String host, int port)
    {
        try
        {
            client = new ClientController(host, port);
        }
        catch(IOException exception)
        {
            System.out.println("Error: Can't setup connection! Terminating cps.client.");
            System.exit(1);
        }
    }

    /**
     * This method waits for input from the console. Once it is received, it sends it to the cps.client's message handler.
     */
    public void accept()
    {
        try
        {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true)
            {
                message = fromConsole.readLine();
                client.handleMessageFromClientUI(message);
            }
        }
        catch (Exception ex)
        {
            System.out.println
                    ("Unexpected error while reading from console!");
        }
    }

    //Class methods ***************************************************

    /**
     * This method is responsible for the creation of the Client UI.
     *
     * @param args The host to connect to.
     */
    public static void main(String[] args)
    {
        String host = "";
        int port = 0;  //The port number

        try
        {
            host = args[0];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            host = "localhost";
        }
        CPSClient chat = new CPSClient(host, DEFAULT_PORT);
        System.out.println("Connected to cps.server at " + host);
        chat.accept();  //Wait for console data
    }
}
//End of ConsoleChat class
