package Exceptions;

public class LoginException extends RuntimeException
{
    public LoginException() {
        super("A login exception occurred!");
    }

    public LoginException(String message) {
        super(message);
    }
}
