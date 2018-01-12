package Exceptions;

public class PaymentRequiredException extends RuntimeException {
    public PaymentRequiredException()
    {
        super("Payment is required to continue this operation.");
    }

    public PaymentRequiredException(double amount)
    {
        super("A payment of " + amount + " NIS is required to continue this operation.");
    }
}
