package Exceptions;

/**
 * An exception to be thrown in case something is not yet implemented.
 * @author Elad Avron
 */
public class NotImplementedException extends RuntimeException {

    public NotImplementedException(){
        super("This method or operation is not yet implemented.");
    }

    public NotImplementedException(String message) {
        super("This method or operation is not yet implemented:\n" + message);
    }
}
