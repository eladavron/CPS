package client.GUI;

import client.ClientController;
import client.GUI.Helpers.ErrorHandlers;
import entity.Message;
import entity.Session;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Stack;

/**
 * The main GUI application
 * Also acts as a controller for the main GUI root FXML.
 * @author Elad Avron
 */
public class CPSClientGUI extends Application{
    /**
     * FXML paths
     */
    public final static String CUSTOMER_SCREEN = "Forms/CustomerScreen.fxml";
    public final static String ENTER_PARKING = "Forms/EnterParking.fxml";
    public final static String LOGIN_SCREEN = "Forms/LoginScreen.fxml";
    public final static String NEW_PREORDER = "Forms/NewPreorder.fxml";
    public final static String VIEW_PREORDERS = "Forms/ViewPreorders.fxml";

    /**
     * Public Finals
     */
    public final static int DEFAULT_TIMEOUT = 10;
    public final static int DEFAULT_WAIT_TIME = 3;

    public static boolean IS_DEBUG;

    public enum ConnectionStatus { CONNECTED, DISCONNECTED, RESET };
    private static ConnectionStatus _connectionStatus = ConnectionStatus.DISCONNECTED;

    private static Stack<Node> _history = new Stack<Node>();

    /**
     * FXML decelerations
     */
    @FXML
    private AnchorPane pageRoot;

    @FXML
    private AnchorPane guiRoot;

    @FXML
    private Label lblStatus;

    @FXML
    private Button btnExit;

    @FXML
    private Button btnLogout;



    /**
     * Static Accessors
     */
    private static AnchorPane _pageRoot;
    private static AnchorPane _guiRoot;
    private static Label _lblStatus;
    private static Button _btnExit;
    private static Button _btnLogout;
    private static Stage _primaryStage;

    /**
     * The Active Client connection
     */
    private static ClientController _client;


    /**
     * A two-dimensional message queue.
     * The first dimension identifies the SID of the message.
     * The second dimension is a queue for all messages with that SID.
     * When a message is received, it is stored in the queue matching its SID.
     */
    private static HashMap<Long, LinkedList<Message>> _incomingMessages = new HashMap<Long, LinkedList<Message>>();

    /**
     * The current session.
     */
    private static Session _session;

    /**
     * The main method of the application.
     * @param args Command line arguments. Supports "debug" for debugging.
     */
    public static void main(String[] args) {
        Options options = new Options();

        Option optDebug = new Option("d", "debug", false, "Run the program in Debug mode");
        optDebug.setRequired(false);
        options.addOption(optDebug);

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

        IS_DEBUG = cmd.hasOption("debug");

        launch(args);
    }

    //region GUI Methods
    /**
     * Starts the GUI session.
     * @param primaryStage The primary stage for the application to display its GUI in.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            URL rootURL = getClass().getResource("GUIRoot.fxml");
            FXMLLoader loader = new FXMLLoader(rootURL);
            loader.setController(this);
            AnchorPane rootPane = loader.load();
            Scene scene = new Scene(rootPane);

            _pageRoot = pageRoot;
            _guiRoot = guiRoot;
            _btnExit = btnExit;
            _lblStatus = lblStatus;
            _btnLogout = btnLogout;

            _primaryStage = primaryStage;
            changeGUI(LOGIN_SCREEN);

            _primaryStage.setScene(scene);
            _primaryStage.setMinHeight(rootPane.getPrefHeight());
            _primaryStage.setMinWidth(rootPane.getPrefWidth());

            _btnExit.setOnAction(event ->
                    _primaryStage.getOnCloseRequest().handle(
                            new WindowEvent(_primaryStage,WindowEvent.WINDOW_CLOSE_REQUEST)));

            _primaryStage.setOnCloseRequest((WindowEvent event) -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Are you sure you want to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    System.exit(0);
                } else {
                    event.consume();
                }
            });
            _primaryStage.show();
        } catch (IOException io)
        {
            ErrorHandlers.GUIError(io,true);
        }
    }

    /**
     * Replaces the inner gui page with the supplied filename.
     * @param filename a local fxml filename to load.
     */
    public static void changeGUI(String filename) {
        try {
            URL guiURL = CPSClientGUI.class.getResource(filename);
            Node guiRoot = FXMLLoader.load(guiURL);
            if (_pageRoot.getChildren().size() > 0)
                _history.push(_pageRoot.getChildren().get(0));
            _pageRoot.getChildren().clear();
            _pageRoot.getChildren().add(guiRoot);
            _btnLogout.setVisible(!filename.equals(LOGIN_SCREEN) && getSession() != null);
            AnchorPane.setTopAnchor(guiRoot, 0.0);
            AnchorPane.setBottomAnchor(guiRoot, 0.0);
            AnchorPane.setLeftAnchor(guiRoot, 0.0);
            AnchorPane.setRightAnchor(guiRoot, 0.0);
        } catch (IOException io)
        {
            ErrorHandlers.GUIError(io, false);
        }
    }

    /**
     * Go back in the gui history
     * @param prompt whether or not to prompt the user if he's sure.
     */
    public static void goBack(boolean prompt)
    {
        if (prompt) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Go back?");
            alert.setContentText("Any data you entered will be lost.");
            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
        }
        _pageRoot.getChildren().clear();
        Node guiRoot = _history.pop(); //Pop history.
        _pageRoot.getChildren().add(guiRoot);
        guiRoot.setDisable(false);
        AnchorPane.setTopAnchor(guiRoot, 0.0);
        AnchorPane.setBottomAnchor(guiRoot, 0.0);
        AnchorPane.setLeftAnchor(guiRoot, 0.0);
        AnchorPane.setRightAnchor(guiRoot, 0.0);
    }

    //endregion

    //region Connection and Message Sending

    /**
     * Attempt a connection to the server
     * @param host Hostname or ip to connect to
     * @param port TCP Port the server is listening to.
     * @throws IOException If the connection failed.
     */
    public static void connect(String host, int port) throws IOException {
        _client = new ClientController(host, port);
        _connectionStatus = ConnectionStatus.CONNECTED;
    }

    /**
     * Send a message object to the server.
     * The method extracts the JSON string and sends that.
     * @param message A message object to send.
     */
    public static void sendToServer(Message message) throws IOException {
        String json = message.toJson();
        if (IS_DEBUG)
        {
            System.out.println("SENT: " + json);
        }
        _client.sendToServer(json);
    }

    public static boolean isConnected()
    {
        if (_client != null && _client.isConnected())
        {
            _connectionStatus = ConnectionStatus.CONNECTED;
        }
        else
        {
            _connectionStatus = ConnectionStatus.DISCONNECTED;
        }
        return (_connectionStatus == ConnectionStatus.CONNECTED);
    }

    /**
     * To use in case of an error.
     * Resets the connection and all the session properties.
     */
    public static void resetConnection() {
        try {
            if (_client != null) {
                _client.closeConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            _connectionStatus = ConnectionStatus.RESET;
            _client = null;
            _session = null;
            changeGUI(LOGIN_SCREEN);
        }
    }

    public static ConnectionStatus getConnectionStatus()
    {
        return _connectionStatus;
    }


    @FXML
    void doLogout(ActionEvent event) throws IOException {
        Alert areYouSure = new Alert(Alert.AlertType.CONFIRMATION);
        areYouSure.setHeaderText("Are you sure you want to log out?");
        areYouSure.setContentText("Any unsaved changes will be lost!");
        areYouSure.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = areYouSure.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES)
        {
            Message logoutMessage = new Message();
            logoutMessage.setType(Message.MessageType.LOGOUT);
            logoutMessage.setDataType(Message.DataType.PRIMITIVE);
            logoutMessage.addData("So long, and thanks for all the fish");
            sendToServer(logoutMessage);
            setStatus("Logged out.", Color.BLACK);
            _session = null;
            changeGUI(LOGIN_SCREEN);
        }
    }

    //endregion

    //region Message Queue methods

    /**
     * Adds a received message to the message queue.
     * Separates messages according to SID.
     * @param message The message to add to the queue.
     * @see CPSClientGUI#_incomingMessages
     */
    public static void addMessageToQueue(Message message)
    {
        LinkedList<Message> list;
        long sid = message.getSID();
        if (!_incomingMessages.containsKey(sid)) //No queue exists for this sid
        {
            list = new LinkedList<Message>();
        }
        else { //This SID has a queue, just have to update it.
            list = _incomingMessages.get(sid);
        }
        list.push(message);
        _incomingMessages.put(sid,list);
    }

    /**
     * Pops the first message in a specific queue.
     * @param sid the message SID to pop the queue from.
     * @return The first message in that queue.
     * @see CPSClientGUI#_incomingMessages
     */
    public static Message popMessageQueue(long sid) {
        if (!_incomingMessages.containsKey(sid) || _incomingMessages.get(sid).isEmpty())
            return null;
        return _incomingMessages.get(sid).pop();
    }

    /**
     * Returns the queue for a specific SID.
     * @param sid Queue SID.
     * @return The <code>LinkedList&lt;Message&gt;</code> queue
     */
    public static LinkedList<Message> getMessageQueue(long sid)
    {
        return _incomingMessages.getOrDefault(sid, null);
    }

    //endregion

    //region getters and setters

    public static Session getSession()
    {
        return _session;
    }

    public static void setSession(Session session) {
        _session = session;
    }

    public static ClientController getClient() {
        return _client;
    }

    public static void setStatus(String status, Color color)
    {
        _lblStatus.setText(status);
        _lblStatus.setTextFill(color);
    }

    public static AnchorPane getPageRoot() {
        return _pageRoot;
    }

    //endregion

}
