package Exceptions;

/**
 * An exception to be thrown in case something is not yet implemented.
 * @author Aviad Bar-David
 */
public class InvalidMessageException extends RuntimeException {

    private long _sID;

    public InvalidMessageException(){
        this("Failed to parse incoming message.");
    }

    /**
     * The main exception handler.
     * All others call this one.
     * @param message The error message to display.
     */
    public InvalidMessageException(String message)
    {
        super("Failed to parse incoming message.\n" + message);
        this.printStackTrace();
    }

    public InvalidMessageException(Exception ex) {
        this("Failed to parse incoming message.\n" + ex.getMessage());
        this.setStackTrace(ex.getStackTrace());
    }

    public InvalidMessageException(Exception ex, long SID)
    {
        this("Failed to parse incoming message with SID " + SID + ".\n" + ex.getMessage(), SID);
        this.setStackTrace(ex.getStackTrace());
    }

    public InvalidMessageException(String message, long SID)
    {
        this("Failed to parse incoming message with SID " + SID + ".\n" + message);
        this._sID = SID;
    }


    public long getSID() {
        return _sID;
    }

    public void setSID(long _sID) {
        this._sID = _sID;
    }
}