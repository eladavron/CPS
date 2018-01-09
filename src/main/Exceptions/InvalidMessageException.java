package Exceptions;

/**
 * An exception to be thrown in case something is not yet implemented.
 * @author Aviad Bar-David
 */
public class InvalidMessageException extends RuntimeException {

    private long _sID;

    public InvalidMessageException(){
        super("Failed to parse incoming message.");
    }

    public InvalidMessageException(String message)
    {
        super("Failed to parse incoming message.\n" + message);
    }

    public InvalidMessageException(Exception ex) {
        super("Failed to parse incoming message.\n" + ex.getMessage());
    }

    public InvalidMessageException(Exception ex, long SID)
    {
        super("Failed to parse incoming message with SID " + SID + ".\n" + ex.getMessage());
        this._sID = SID;
    }

    public InvalidMessageException(String message, long SID)
    {
        super("Failed to parse incoming message with SID " + SID + ".\n" + message);
        this._sID = SID;
    }


    public long getSID() {
        return _sID;
    }

    public void setSID(long _sID) {
        this._sID = _sID;
    }
}