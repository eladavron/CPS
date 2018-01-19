package client.GUI.Helpers;

import Exceptions.InvalidMessageException;
import Exceptions.PaymentRequiredException;
import client.GUI.CPSClientGUI;
import client.GUI.Controls.WaitScreen;
import entity.Message;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static entity.Message.DataType.PRIMITIVE;
import static entity.Message.MessageType.PAYMENT;

/**
 * A custom <code>Task</code> class that sends a message to the server with easy-to-set error and success screens.
 * Note that for the {@link #onFailedProperty()} to execute an exception must be thrown. If it's run manually without
 * an exception, the task will still be considered a "success".
 */
public class MessageTasker extends Task<Message> {

    private String _sendingMessage;
    private Message _message;
    private MessageRunnable _onSuccess;
    private MessageRunnable _onFailure;
    private WaitScreen _waitScreen;

    /**
     * Basic default Constructor.
     * Sets up the tasker object with basic default parameters.
     * @param message The Message object sending to the server.
     * @param onSuccess The action to perform when succeeding.
     * @param onFailure The action to perform when failing.
     */
    public MessageTasker(Message message, MessageRunnable onSuccess, MessageRunnable onFailure) {
        this(message, onSuccess, onFailure, "Please hold...");
    }

    /**
     * Extended constructor.
     * Sets messages other than default ones.*
     * @param message The Message object sending to the server.
     * @param onSuccess The action to perform when succeeding.
     * @param onFailure The action to perform when failing.
     * @param customSendingMessage The message to display while sending request.
     */
    public MessageTasker(Message message, MessageRunnable onSuccess, MessageRunnable onFailure, String customSendingMessage) {
        this._sendingMessage = customSendingMessage;
        this._message = message;
        this._onSuccess = onSuccess;
        this._onFailure = onFailure;
        this.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                if (CPSClientGUI.IS_DEBUG)
                {
                    System.err.println("DEBUG - The following exception was caught and handled:");
                    event.getSource().getException().printStackTrace();
                }
                _onFailure.setException(event.getSource().getException());
                if (getException() instanceof InterruptedException) {
                    _onFailure.setException(new TimeoutException("The operation timed out."));
                }
                else if (getException() instanceof SocketException)
                {
                    _onFailure.setException(new SocketException("The connection to the server was lost!"));
                }
                _onFailure.run();
            }
        });
        this.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                _onSuccess.run();
            }
        });
    }

    @Override
    protected Message call() throws Exception {
        updateMessage(_sendingMessage);
        CPSClientGUI.sendToServer(_message);
        long sid = _message.getTransID();
        while (CPSClientGUI.getMessageQueue(sid) == null) //Wait for incoming message
        {
           Thread.sleep(100); //I hate busy-waits
        }

        while(true){
            Message incoming = CPSClientGUI.popMessageQueue(sid);
            if (incoming != null)
            {
                switch (incoming.getMessageType())
                {
                    case QUEUED:
                        updateMessage(_sendingMessage);
                        break;
                    case ERROR_OCCURRED:
                    case FAILED:
                        updateMessage("Operation failed!");
                        _onFailure.setMessage(incoming);
                        throw new Exception("The server responded with an error: " + incoming.getData().get(0));
                    case NEED_PAYMENT:
                        updateMessage("Payment is required before you can continue.");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    handlePayment(incoming);
                                } catch (Exception e) {
                                    _waitScreen.showError("Payment required!", "You must pay before you continue!");
                                }
                            }
                        });
                        break;
                    case FINISHED:
                        updateMessage("Operation Successful!");
                        _onSuccess.setMessage(incoming);
                        return null;
                    default:
                        throw new InvalidMessageException("Unexpected response type: " + incoming.getMessageType().toString());
                }
            }
            else
            {
                Thread.sleep(100); //Busywaits are terrible.
            }
        }
    }

    private void handlePayment(Message message) throws Exception {
        if (_waitScreen != null)
            _waitScreen.cancelTimeout();
        double amount = (double)message.getData().get(0);
        Alert requestPayment = new Alert(Alert.AlertType.INFORMATION);
        requestPayment.setTitle("Payment");
        requestPayment.setHeaderText("Please insert exactly " + amount + " NIS into the machine!");
        requestPayment.setContentText("No change, insert the exact amount!\nFake bills will be returned!");
        requestPayment.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = requestPayment.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            updateMessage("Sending payment...");
            Message payment = new Message(PAYMENT, PRIMITIVE, amount);
            payment.setTransID(message.getTransID());
            if (_waitScreen != null)
                _waitScreen.resetTimeout(CPSClientGUI.DEFAULT_TIMEOUT);
            CPSClientGUI.sendToServer(payment);
        }
        else
        {
            Message payment = new Message(PAYMENT, PRIMITIVE, 0.0);
            CPSClientGUI.sendToServer(payment);
            throw new PaymentRequiredException(amount);
        }
    }

    //region Getters and Setters

    public Message getMessageContent() {
        return _message;
    }

    public void setMessageContent(Message messageContent) {
        this._message = messageContent;
    }

    public String getSendingMessage() {
        return _sendingMessage;
    }

    public void setSendingMessage(String sendingMessage) {
        this._sendingMessage = sendingMessage;
    }

    public Runnable getOnSuccess() {
        return _onSuccess;
    }

    public void setOnSuccess(MessageRunnable onSuccess) {
        this._onSuccess = onSuccess;
    }

    public Runnable getOnFailure() {
        return _onFailure;
    }

    public void setOnFailure(MessageRunnable onFailure) {
        this._onFailure = onFailure;
    }

     public Message getTaskedMessage() {
        return _message;
    }

    public void setTaskedMessage(Message message) {
        this._message = message;
    }

    public WaitScreen getWaitScreen() {
        return _waitScreen;
    }

    public void setWaitScreen(WaitScreen waitScreen) {
        this._waitScreen = waitScreen;
    }

    //endregion
}
