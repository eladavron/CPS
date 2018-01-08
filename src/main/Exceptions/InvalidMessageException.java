package Exceptions;

/**
 * An exception to be thrown in case something is not yet implemented.
 * @author Aviad Bar-David
 */
public class InvalidMessageException extends RuntimeException {

    private long _sID;

    public InvalidMessageException(){
        super("Failed to parse incoming message");
    }

    public InvalidMessageException(String message) {
        super("Failed to parse incoming message:\n" + message);
    }

    public InvalidMessageException(String message, long sID){
        super("Processing of message ID " + sID + " failed");
    }

    public long getSID() {
        return _sID;
    }

    public void setSID(long _sID) {
        this._sID = _sID;
    }
}