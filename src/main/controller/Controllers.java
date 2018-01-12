package controller;

public class Controllers {
    public static DBController             dbController;
    public static OrderController          orderController;
    public static BillingController        billingController;
    public static CustomerController       customerController;
    public static SubscriptionController   subscriptionController;

    public static void init(){
        dbController             = DBController.getInstance();
        orderController          = OrderController.getInstance();
        billingController        = BillingController.getInstance();
        customerController       = CustomerController.getInstance();
        subscriptionController   = SubscriptionController.getInstance();
    }
}