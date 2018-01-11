package Exceptions;

public class LastCarRemovalException extends RuntimeException{
    public LastCarRemovalException() {
        super("Cannot remove last Car from your Cars list!");
    }

    public LastCarRemovalException(String message) {
        super("Cannot remove last Car from your Cars list." +'\n' + message);
    }

}
