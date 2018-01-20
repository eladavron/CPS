package Exceptions;


public class CustomerAlreadyExists extends RuntimeException{
    public CustomerAlreadyExists() {
        super("A customer with that email is already registered!");
    }

    public CustomerAlreadyExists(String message) {
        super(message);
    }
}
