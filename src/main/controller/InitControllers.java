package controller;

public class InitControllers {
    public static DBController             dbController             = DBController.getInstance();
    public static OrderController          orderController          = OrderController.getInstance();
    public static BillingController        billingController        = BillingController.getInstance();
    public static CustomerController       customerController       = CustomerController.getInstance();
    public static SubscriptionController   subscriptionController   = SubscriptionController.getInstance();
}