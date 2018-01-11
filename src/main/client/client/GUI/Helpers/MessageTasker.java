package client.GUI.Helpers;

import Exceptions.InvalidMessageException;
import client.GUI.CPSClientGUI;
import entity.Message;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.net.SocketException;
import java.util.concurrent.TimeoutException;

/**
 * A custom <code>Task</code> class that sends a message to the server with easy-to-set error and success screens.
 * Note that for the {@link #onFailedProperty()} to execute an exception must be thrown. If it's run manually without
 * an exception, the task will still be considered a "success".
 */
public class MessageTasker extends Task<Message> {

    private String _sendingMessage;
    private String _queuedMessage ;
    private String _successMessage;
    private String _failedMessage;
    private Message _message;
    private MessageRunnable _onSuccess;
    private MessageRunnable _onFailure;

    /**
     * Basic default Constructor.
     * Sets up the tasker object with basic default parameters.
     * @param message The Message object sending to the server.
     * @param onSuccess The action to perform when succeeding.
     * @param onFailure The action to perform when failing.
     */
    public MessageTasker(Message message, MessageRunnable onSuccess, MessageRunnable onFailure) {
        this("Sending...",
                "Queued...",
                "Success!",
                "Failed!",
                message, onSuccess, onFailure);
    }

    /**
     * Extended constructor.
     * Sets messages other than default ones.
     * @param sendingMessage The message to display while sending request.
     * @param queuedMessage The message to display when request is queued.
     * @param successMessage The message to display when the request succeeds.
     * @param failedMessage The message to display if the request fails.
     * @param message The Message object sending to the server.
     * @param onSuccess The action to perform when succeeding.
     * @param onFailure The action to perform when failing.
     */
    public MessageTasker(String sendingMessage,
                         String queuedMessage,
                         String successMessage,
                         String failedMessage,
                         Message message,
                         MessageRunnable onSuccess,
                         MessageRunnable onFailure) {
        this._sendingMessage = sendingMessage;
        this._queuedMessage = queuedMessage;
        this._successMessage = successMessage;
        this._failedMessage = failedMessage;
        this._message = message;
        this._onSuccess = onSuccess;
        this._onFailure = onFailure;
        this.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                event.getSource().getException().printStackTrace();
                _onFailure.setException(event.getSource().getException());
                if (getException() instanceof InterruptedException) {
                    _onFailure.setException(new TimeoutException("The operation timed out."));
                }
                if (getException() instanceof SocketException)
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
        long sid = _message.getSID();
        while (CPSClientGUI.getMessageQueue(sid) == null) //Wait for incoming message
        {
            if (Thread.currentThread().isInterrupted()) //I'm not actually sure it works from here, but just in case let's leave it in.
            {
                updateMessage("Timed out!");
                _onFailure.setMessage(_message);
                Platform.runLater(_onFailure);
            }
        }

        while(true){
            Message incoming = CPSClientGUI.popMessageQueue(sid);
            if (incoming != null)
            {
                switch (incoming.getType())
                {
                    case QUEUED:
                        updateMessage(_queuedMessage);
                        break;
                    case FAILED:
                        updateMessage(_failedMessage);
                        _onFailure.setMessage(incoming);
                        throw new InvalidMessageException();
                    case FINISHED:
                        updateMessage(_successMessage);
                        _onSuccess.setMessage(incoming);
                        return null;
                    default:
                        throw new InvalidMessageException("Unexpected response type: " + incoming.getType().toString());
                }
            }
            else
            {
                Thread.sleep(500); //Busywaits are terrible.
            }
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

    public String getQueuedMessage() {
        return _queuedMessage;
    }

    public void setQueuedMessage(String queuedMessage) {
        this._queuedMessage = queuedMessage;
    }

    public String getSuccessMessage() {
        return _successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this._successMessage = successMessage;
    }

    public String getFailedMessage() {
        return _failedMessage;
    }

    public void setFailedMessage(String failedMessage) {
        this._failedMessage = failedMessage;
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

    //endregion
}
