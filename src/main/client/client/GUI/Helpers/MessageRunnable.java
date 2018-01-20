package client.GUI.Helpers;

import entity.Message;

/**
 * Implements a {@link Runnable} with a built in {@link Message} and {@link Throwable} as optional parameters.<br>
 * Used for handling incoming messages form the server during {@link MessageTasker tasks}.<br>
 * Both parameters need to be set manually (which the {@link MessageTasker} does).
 */
public abstract class MessageRunnable implements Runnable {
    private Message _message;
    private Throwable _exception;

    /**
     * Set the {@link Message} object associated with this Runnable.
     * @param message An incoming Message object.
     */
    public void setMessage(Message message)
    {
        _message = message;
    }

    /**
     * Returns the {@link Message} object associated with this runner.
     * @return A message object and such.
     */
    public Message getMessage() {
        return _message;
    }

    /**
     * Returns a string compiled of any errors supplied by the server and any exceptions associated with the runnable.
     * @return the compiled error string.
     */
    public String getErrorString()
    {
        String returnString = "";
        if (getMessage() != null
                && getMessage().getMessageType() == Message.MessageType.FAILED
                && getMessage().getData() != null
                && getMessage().getData().size() > 0
                && getMessage().getData().get(0) != null)
        {
            returnString += (String)getMessage().getData().get(0) +"\n";
        }
        else if (getException() != null)
            returnString += getException().getMessage();
        return returnString;
    }

    /**
     * Return any exception associated with this runnable.
     * @return any exceptions associated with the Runnable.
     */
    public Throwable getException() {
        return _exception;
    }

    /**
     * Sets the exception associated with this runnable.
     * @param exception An Exception.
     */
    public void setException(Throwable exception) {
        this._exception = exception;
    }
}
