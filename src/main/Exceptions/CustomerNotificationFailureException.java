package Exceptions;

import entity.Customer;

public class CustomerNotificationFailureException extends Exception{

    public CustomerNotificationFailureException(String message) { super(message); }

}
