package controller;

import entity.Complaint;

public class CustomerServiceController {
    private static CustomerServiceController ourInstance = new CustomerServiceController();

    public static CustomerServiceController getInstance() {
        return ourInstance;
    }

    private CustomerServiceController() {
    }

    public void handleComplaint(Complaint complaint) {

    }
}
