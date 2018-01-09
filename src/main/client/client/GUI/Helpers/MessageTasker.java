package client.GUI.Helpers;

import Exceptions.LoginException;
import client.GUI.CPSClientGUI;
import entity.Message;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class MessageTasker extends Task<Message> {

    private String _sendingMessage = "Sending...";
    private String _queuedMessage = "Queued...";
    private String _successMessage = "Success!";
    private String _failedMessage = "Failed!";
    private Message _message;
    private RunnableWithMessage _onSuccess;
    private RunnableWithMessage _onFailure;

    /**
     * Basic default Constructor.
     * Sets up the tasker object with basic parameters.
     * @param _message
     * @param _onSuccess
     * @param _onFailure
     */
    public MessageTasker(Message _message, RunnableWithMessage _onSuccess, RunnableWithMessage _onFailure) {
        this._message = _message;
        this._onSuccess = _onSuccess;
        this._onFailure = _onFailure;
    }

    /**
     * Extended constructor.
     * Sets messages other than default ones.
     * @param _sendingMessage The message to display while sending request.
     * @param _queuedMessage The message to display when request is queued.
     * @param _successMessage The message to display when the request succeeds.
     * @param _failedMessage The message to display if the request fails.
     * @param _message The Message object sending to the server.
     * @param _onSuccess The action to perform when succeeding.
     * @param _onFailure The action to perform when failing.
     */
    public MessageTasker(String _sendingMessage,
                         String _queuedMessage,
                         String _successMessage,
                         String _failedMessage,
                         Message _message,
                         RunnableWithMessage _onSuccess,
                         RunnableWithMessage _onFailure) {
        this._sendingMessage = _sendingMessage;
        this._queuedMessage = _queuedMessage;
        this._successMessage = _successMessage;
        this._failedMessage = _failedMessage;
        this._message = _message;
        this._onSuccess = _onSuccess;
        this._onFailure = _onFailure;
    }

    @Override
    protected Message call() throws Exception {
        updateMessage(_sendingMessage);
        CPSClientGUI.sendToServer(_message);
        long sid = _message.getSID();
        while (CPSClientGUI.getMessageQueue(sid) == null) //Wait for incoming message
        {
            if (Thread.currentThread().isInterrupted())
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
                        Platform.runLater(_onFailure);
                        return null;
                    case FINISHED:
                        updateMessage(_successMessage);
                        _onSuccess.setMessage(incoming);
                        Platform.runLater(_onSuccess);
                        return null;
                    default:
                        throw new LoginException("Unexpected response type: " + incoming.getType().toString());
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

    public void setOnSuccess(RunnableWithMessage onSuccess) {
        this._onSuccess = onSuccess;
    }

    public Runnable getOnFailure() {
        return _onFailure;
    }

    public void setOnFailure(RunnableWithMessage onFailure) {
        this._onFailure = onFailure;
    }

    //endregion
}
