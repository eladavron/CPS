package Exceptions;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(Integer orderID) {
        super("OrderID :" + orderID + "was not found under this client.");
    }
}
