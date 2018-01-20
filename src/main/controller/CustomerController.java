package controller;

import Exceptions.CustomerAlreadyExists;
import Exceptions.CustomerNotificationFailureException;
import Exceptions.LastCarRemovalException;
import Exceptions.OrderNotFoundException;
import entity.*;

import java.sql.SQLException;
import java.util.*;

import static controller.Controllers.*;
import static controller.CustomerController.MailNotificationTemplates.*;
import static controller.CustomerController.SubscriptionOperationReturnCodes.*;
import static entity.Billing.priceList.*;
import static entity.Order.OrderStatus.*;
import static entity.Subscription.SubscriptionType.FULL;
import static entity.Subscription.SubscriptionType.REGULAR;


/**
 * Singleton Customer controller to be responsible over the methods of customer.<br>
 * This class is a Singleton.
 */
public class CustomerController {
    private static CustomerController instance;

    static {
        try {
            instance = new CustomerController();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Failure loading the customers.");
        }
    }

    /**
     * Returns the Singleton instance.
     * @return The Singleton instance.
     * @throws SQLException if something goes wrong with the SQL queries in the controller.
     */
    public static CustomerController getInstance() throws SQLException{
        return instance;
    }

    /**
     * Types of subscription operations return codes.
     */
    public enum SubscriptionOperationReturnCodes {
        FAILED,
        SUCCESS_ADDED,
        RENEWED,
        QUERY_RESPONSE
    }

    /**
     * A private constructor for the Singleton.
     * @throws SQLException if something goes wrong with the SQL queries in the controller.
     */

    /*
    *Used for specifying type of notification to be sent to the user.
    */
    public enum MailNotificationTemplates {
        LATE_FOR_PARKING,
        SUBSCRIPTION_EXPIRY_REMINDER,
        FULL_SUBSCRIPTION_MAX_PARK_TIME_BREACHED
    }

    private CustomerController() throws SQLException{
        getCustomersFromDb();
    }

    private Map<Integer, Customer> _customersList = new HashMap<>();

    /**
     * Searches for a specific Customer from the current Customer list.
     * @param customerID the ID of the customer which we are looking for.
     * @return the Customer if found, null otherwise.
     */
    public Customer getCustomer(Integer customerID)
    {
        return _customersList.getOrDefault(customerID, null);
    }

    /**
     * Searches for a specific Customer based on their Email address.
     * @param email The email address to search by.
     * @return The customer if found, null if none found.
     */
    public Customer getCustomerByEmail(String email) {
        for (Customer customer : _customersList.values()) {
            if (customer.getEmail().equals(email)) return customer;
        }
        return null;
    }

    /**
     * Private function that retrieves the Customers list form the DB, on startup.
     * @throws SQLException if something goes wrong with the SQL queries in the controller.
     */
    private void getCustomersFromDb()throws SQLException {
        setCustomersList(dbController.getCustomers());
    }

    /**
     * Contains the current list of Customers on our system.
     * @return the list.
     */
    public Collection<Customer> getCustomersList() {
        return _customersList.values();
    }

    /**
     * Private function used in order to convert the general user array into customer
     * and then map it into our customer list.
     * @param list - the customer list taken from the DB.
     */
    private void setCustomersList(ArrayList<User> list) {
        for (User user : list){
            Customer customer = (Customer) user;
            _customersList.put(customer.getUID(), customer);
            orderController.putAll(customer.getActiveOrders());
            subscriptionController.putAll(customer.getSubscriptionList());
        }
    }


    /**
     * function used by Server side in order to make a new Customer on Boundary's Action
     * @param customer to add to the System.
     * @return the Customer.
     * @throws CustomerAlreadyExists if the customer is already in the database.
     * @throws SQLException if something goes wrong.
     */
    public Customer addNewCustomer(Customer customer) throws CustomerAlreadyExists, SQLException {
        return addNewCustomer(customer.getUID(),
                customer.getName(),
                customer.getPassword(),
                customer.getEmail(),
                customer.getCarIDList());
    }

    /**
     * Adds a new Customer to the dataabase
     * @param uID The User ID (will be replaced by the DB Controller once inserted)
     * @param name The Full Name of the Customer
     * @param password The Customer's Password
     * @param email The Customer's Email Address (must be unique)
     * @param carIDList The list of cars in the Customer's account upon creation.
     * @return The customer with the modified uID generated by the DB.
     * @throws CustomerAlreadyExists If the customer tries registering with an Email address already in the Database.
     * @throws SQLException if something goes wrong with the SQL queries in the controller.
     */
    public Customer addNewCustomer(Integer uID, String name, String password, String email, ArrayList<Integer> carIDList) throws CustomerAlreadyExists,SQLException {
        /*
          First make sure there are no duplicates!
         */
        if (customerController.getCustomerByEmail(email) != null || employeeController.getEmployeeByEmail(email) != null) //Make sure no double registrations!
        {
            throw new CustomerAlreadyExists(String.format("A user is already registered with the email \"%s\"!", email));
        }
        Customer newCustomer = new Customer(uID, name, password, email, carIDList);
        if (!dbController.insertCustomer(newCustomer))
            return null;
        _customersList.put(newCustomer.getUID(),newCustomer);
        return newCustomer;
    }

    /**
     * Gets all the preorders by a specific customer.
     * @param customerID The ID of the customer to query preorders for.
     * @return An ArrayList of all preorders by the customer. Uses the {@link Object} type for better use with the {@link Message} class.
     * @throws SQLException if something goes wrong with the SQL queries in the controller.
     */
    public ArrayList<Object> getCustomersPreOrders(int customerID) throws SQLException{
        ArrayList<Object> returnList = new ArrayList<>();
        for (Object obj : dbController.getOrdersByUserID(customerID).values())
        {
            if (obj instanceof Order && ((Order) obj).getOrderStatus().equals(PRE_ORDER))
            {
                returnList.add(obj);
            }
        }
        return returnList;
    }

    /**
     * Returns a list of all "Active Orders" (meaning: currently parked) for a specific customer in a specific parking lot.
     * @param customerID The customer ID to search for.
     * @param parkingLotID The parking lot to search in.
     * @return An ArrayList of all parking sessions by the customer in the given lot. Uses the {@link Object} type for better use with the {@link Message} class.
     * @throws SQLException if something goes wrong with the SQL queries in the controller.
     */
    public ArrayList<Object> getCustomersActiveOrdersInLot(int customerID, int parkingLotID) throws SQLException {
        ArrayList<Object> returnList = new ArrayList<>();
        for (Object obj : dbController.getOrdersByUserID(customerID).values())
        {
            if (obj instanceof Order
                    && ((Order) obj).getOrderStatus().equals(IN_PROGRESS)
                    && ((Order) obj).getParkingLotNumber().equals(parkingLotID))
            {
                returnList.add(obj);
            }
        }
        return returnList;
    }

    /**
     * Deletes an order from active orders list
     * @param customerID Customer from which to delete the active order
     * @param orderID The order ID
     * @throws CustomerNotificationFailureException if an error occurs deleting the order.
     */
    public void removeOrderFromCustomerList(int customerID, int orderID) throws CustomerNotificationFailureException {
        try {
            _customersList.get(customerID).getActiveOrders().remove(orderID);
        } catch (Exception e) {
            if (Controllers.IS_DEBUG_CONTROLLER) {
                e.printStackTrace();
            }
            throw new CustomerNotificationFailureException("There was a problem with your request, please contact support");
        }
    }

    /**
     *  Given the the right params needed for a new "Active Order" (Parking Session), controller will add this new order into the active orders list.
     * @param customerID The customer ID of the customer creating the order.
     * @param carID The Car ID user for the order.
     * @param estimatedExitTime The time the customer estimates they will leave the lot.
     * @param parkingLotNumber The parking lot into which the customer is entering.
     * @throws SQLException if something goes wrong with the DB operation.
     * @throws CustomerNotificationFailureException if something goes wrong with the Controller operation.
     * @return The new order with the OrderID and times filled in by the DB.
     */
    public Order addNewOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber) throws SQLException, CustomerNotificationFailureException {
        Customer customer = getCustomer(customerID);
        Order newOrder = orderController.makeNewSimpleOrder(customerID, carID, estimatedExitTime,  parkingLotNumber);
        Map<Integer, Object> activeOrders = customer.getActiveOrders();
        activeOrders.put(newOrder.getOrderID(), newOrder);
        customer.setActiveOrders(activeOrders);
        return newOrder;
    }

    /**
     * Overload for {@link #addNewOrder(Order)} using an {@link Order} object.
     * @param newOrder The order to get info from.
     * @throws SQLException if something goes wrong with the DB operation.
     * @throws CustomerNotificationFailureException if something goes wrong with the Controller operation.
     * @return The new order with the OrderID and times filled in by the DB.
     */
    public Order addNewOrder(Order newOrder) throws SQLException, CustomerNotificationFailureException {
        return addNewOrder(newOrder.getCostumerID(), newOrder.getCarID(), newOrder.getEstimatedExitTime(), newOrder.getParkingLotNumber());
    }

    /**
     *  Overloaded function of the above just for PreOrder making.
     * @param customerID The ID of the customer making the preorder.
     * @param carID The Car ID the customer supplied.
     * @param estimatedExitTime The time and date the customer estimates they'll exit the parking lot.
     * @param parkingLotNumber The parking lot they're making the order in.
     * @param estimatedEntryTime The time they estimate they will arrive at the parking lot.
     * @return order The saved order with the order ID and price.
     * @throws SQLException if something goes wrong with the DB operation.
     * @throws CustomerNotificationFailureException if something goes wrong with the Controller operation.
     */
    public Order addNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime) throws SQLException, CustomerNotificationFailureException {
        Customer customer = getCustomer(customerID);
        Order newOrder = orderController.makeNewPreOrder(customerID, carID, estimatedExitTime,  parkingLotNumber, estimatedEntryTime);
        Map<Integer, Object> activeOrders = customer.getActiveOrders();
        activeOrders.put(newOrder.getOrderID(),newOrder);
        customer.setActiveOrders(activeOrders);
        return newOrder;
    }

    /**
     * OverLoading function for a given template of a PreOrder.
     * @param newPreOrder The Preorder to create.
     * @return Order The created Preorder (with the ID and price filled in).
     * @throws SQLException if something goes wrong with the DB operation.
     * @throws CustomerNotificationFailureException if something goes wrong with the Controller operation.
     */
    public Order addNewPreOrder(PreOrder newPreOrder) throws SQLException, CustomerNotificationFailureException {
        return addNewPreOrder(newPreOrder.getCostumerID(), newPreOrder.getCarID(), newPreOrder.getEstimatedExitTime(), newPreOrder.getParkingLotNumber(), newPreOrder.getEstimatedEntryTime());
    }

    /**
     * Delete an order from the controller.
     * @param customer The customer whose order we are deleting.
     * @param orderID The order ID to delete.
     * @return The order that was deleted if successful.
     * @throws SQLException If something went wrong.
     */
    public Order removeOrder(Customer customer, Integer orderID)throws SQLException{
        Order removedOrder = orderController.deleteOrder(orderID);
        return removedOrder;
    }

    /**
     * Calls DBController to update order status to be in progress + actual entry time. to be used when fulfilling a preorder
     * @param orderToUpdate The order to update the status of.
     * @throws SQLException If something goes wrong.
     */
    public void updatePreOrderEntranceToParking(Order orderToUpdate) throws SQLException {
        dbController.changeOrderToInProgress(orderToUpdate.getOrderID(),orderToUpdate.getActualEntryTime());
    }

    /**
     * Finish an "Active Order" (parking session) when the customer exits.
     * @param customer The customer exiting.
     * @param orderID The order ID we're removing.
     * @param finalPrice The final price he paid for parking.
     * @return The price he paid for the session.
     * @throws OrderNotFoundException If the order is not found.
     * @throws SQLException If something went wrong in the SQL operation.
     * @throws CustomerNotificationFailureException If something went wrong with the controller operation.
     */
    public double finishOrder(Customer customer,Integer orderID, double finalPrice) throws OrderNotFoundException, SQLException, CustomerNotificationFailureException {
        Map<Integer, Object> activeOrders = customer.getActiveOrders();
        Order orderToFinish;
        if (activeOrders.containsKey(orderID)) {
            orderToFinish = (Order) activeOrders.get(orderID);
            // The order was found...need to check the client's cost for this order.
            orderController.finishOrder(orderToFinish.getOrderID(), finalPrice);
            removeOrderFromCustomerList(customer.getUID(), orderID);
            return orderToFinish.getPrice();
        }
        else{ throw new OrderNotFoundException(orderID);}
    }

    /**
     *  Check if the user has any subscription THAT MATCHES his order/parking session and calculate the type of price
     *  scheme for his order accordingly.
     * @param customerID The customer ID to calculate for.
     * @param orderToFinish The relevant order.
     * @return The type of price scheme he will be charged according to.
     */
    public Billing.priceList getHourlyParkingCost(Integer customerID, Order orderToFinish){
        Billing.priceList price;
        Map<Integer, Subscription> subscriptionsList = customerController.getCustomer(customerID).getSubscriptionMap();
        price = PRE_ORDER_ONE_TIME_PARKING;
        if (!(orderToFinish instanceof PreOrder))
        {
            price = ONE_TIME_PARKING;
        }
        ArrayList<Integer> existingSubscriptionIDs = SubscriptionController.getInstance()
                .findSubscriptionsByCarID(subscriptionsList, orderToFinish.getCarID());

        if (existingSubscriptionIDs.size() > 0)
        {
            for (Integer subscriptionID : existingSubscriptionIDs)
            {
                Subscription current = subscriptionsList.get(subscriptionID);
                if (current.getSubscriptionType().equals(FULL))
                    return NO_CHARGE_DUE_TO_SUBSCRIPTION;
                else
                // There is a a subscription listed on this car but need to check if its of the same parkingLot as the order's.
                {
                    RegularSubscription regularSubscription = (RegularSubscription) current;
                    if (orderToFinish.getParkingLotNumber().equals(regularSubscription.getParkingLotNumber())
                            && validateRegularTimes(orderToFinish))
                    {
                        // Then we have a RegularSubscription!
                        price = NO_CHARGE_DUE_TO_SUBSCRIPTION;
                    }
                }
            }
        }
        return price;
    }

    /**
     * private method used in order to make sure this order actually fits the subscription regularly 1 time entrance.
     * @param currentOrder the order to calculate for.
     * @return true if it does, false otherwise.
     */
    private boolean validateRegularTimes(Order currentOrder) {
        //First we check if this Subscription has not been used today.
        Customer customer = _customersList.get(currentOrder.getCostumerID());
        for (Object order : customer.getActiveOrders().values()){
            if (((Order)order).getCarID().equals(currentOrder.getCarID())
                    && ((Order)order).getOrderStatus().equals(FINISHED))
            { //Then this car has entered the parking Lot today!
                return false;
            }
        }
        //Then we check if this order is within working days.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentOrder.getActualEntryTime());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek <= 5;
    }

    /**
     * Add another car to this customer's account.
     * @param customer The customer to add the car to.
     * @param carID The car ID to add.
     * @throws SQLException if something goes wrong with the DB operation.
     */
    public void addCar(Customer customer, Integer carID) throws SQLException
    {
        customer.getCarIDList().add(carID);
        dbController.addCarToCustomer(customer.getUID(), carID);
    }

    /**
     * Remove the car from this customer's account.
     * @param carID The car to remove.
     * @param customer The customer to remove the car from.
     * @return True if successful, false if not.
     * @throws LastCarRemovalException If user tries to remove the last car from their account.
     * @throws SQLException if something goes wrong in the SQL operation.
     */
    public boolean removeCar(Customer customer, Integer carID) throws LastCarRemovalException, SQLException{
        ArrayList<Integer> carList = customer.getCarIDList();
        if (carList.contains(carID)) {
            if (carList.size() > 1) {
                if(dbController.removeCarFromCustomer(customer.getUID(), carID)){
                    customer.getCarIDList().remove(carID);
                    return true;
                }else{
                    return false;
                }
            }
            else { // Trying to remove the customer's last car!
                throw new LastCarRemovalException("Customer : " + customer.getName() + "Car ID:" + carID);
            }
        }else{
            return false;
        }
    }

    /**
     * Given the the right params needed for a new Regular Subscription
     * the controller will add subscribe it and put it into the the customer's subscriptionList.
     * @param subs Regular Subscription
     * @return CustomerController.SubscriptionOperationReturnCodes Return Code
     * @throws SQLException if something goes wrong in the SQL operation.
     */
    public SubscriptionOperationReturnCodes addNewRegularSubscription(RegularSubscription subs) throws SQLException
    {
        Customer cust = customerController.getCustomer(subs.getUserID());
        Map<Integer, Subscription> subscriptionList = cust.getSubscriptionMap();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getSubscriptionType().equals(REGULAR)
                    && subscription.getCarsID().get(0).equals(subs.getCarsID().get(0)))
            {
                RegularSubscription regularSub = (RegularSubscription) subscription;
                if (regularSub.getParkingLotNumber().equals(subs.getParkingLotNumber())) //then this is just a renewal of an existing subscription!
                {
                    regularSub.setRegularExitTime(subs.getRegularExitTime()); // in case times has changed
                    if (subscriptionController.renewSubscription(regularSub)) //just renewing it's expiration date does the job.
                        return RENEWED;
                    else
                        return FAILED;
                }
            }
        }

        if (subscriptionController.addRegularSubscription(subs) != -1) {
            cust.getSubscriptionMap().put(subs.getSubscriptionID(), subs);
            return SUCCESS_ADDED;
        }else
            return FAILED;
    }

    /**
     * Same as {@link #addNewRegularSubscription(RegularSubscription)} just with a {@link FullSubscription}
     * @param fSubs Full Subscription
     * @return CustomerController.SubscriptionOperationReturnCodes Return Code
     * @throws SQLException if something goes wrong in the SQL operation.
     */
    public SubscriptionOperationReturnCodes addNewFullSubscription(FullSubscription fSubs) throws SQLException
    {
        Customer cust = customerController.getCustomer(fSubs.getUserID());
        Map<Integer, Subscription> subscriptionList = cust.getSubscriptionMap();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getSubscriptionType().equals(FULL)
                    && subscription.getCarsID().get(0).equals(fSubs.getCarsID().get(0))){
                subscriptionController.renewSubscription(subscription); //just renewing it's expiration date does the job.
            }
        }
        //Has no Full Subscription over this car
        if (subscriptionController.addFullSubscription(fSubs) != -1) {
            cust.getSubscriptionMap().put(fSubs.getSubscriptionID(), fSubs);
            return SUCCESS_ADDED;
        }else
            return FAILED;
    }

    /**
     * send late for parking reminder to customer
     * @param preOrder the preorder
     * @return true on success, false on failure
     */
    public boolean sendEntryTimeBreachedNotification(PreOrder preOrder)
    {
        Customer lateCustomer = getCustomer(preOrder.getCostumerID());
        System.out.println("Order #" + preOrder.getOrderID() + " is late. Parking reminder for " + lateCustomer.getName() + " was sent to " + lateCustomer.getEmail());
        mailCustomerNotification(lateCustomer,LATE_FOR_PARKING);
        return true;
    }

    /**
     * Send max park time breached notification
     * @param order the order
     */
    public void sendMaxParkTimeBreachedTowingNotification(Order order)
    {
        Customer forgetfulCustomer = getCustomer(order.getCostumerID());
        System.out.println("Order #" + order.getOrderID() + ": Car is in the parking lot for over 14 day. " +
                "Towing notification for " + forgetfulCustomer.getName() + " was sent to " + forgetfulCustomer.getEmail());
        orderTowingTruckForPickUpCar(order.getCostumerID(), order.getCarID(), order.getParkingLotNumber());
        mailCustomerNotification(forgetfulCustomer, FULL_SUBSCRIPTION_MAX_PARK_TIME_BREACHED);

         //Assuming order a towing truck and mailing the customer always work
    }

    /**
     * sends subscription expiration notifications to customer
     * @param subscription expiring subscription
     */
    public void sendSubscriptionUpcomingExpiryNotification(Subscription subscription)
    {
        Customer customer = getCustomer(subscription.getUserID());
        System.out.println( "Subscription #" + subscription.getSubscriptionID() + ": Subscription will expire in 1 week."
                + "Renewal reminder for " + customer.getName() + " was sent to " + customer.getEmail());
        mailCustomerNotification(customer,SUBSCRIPTION_EXPIRY_REMINDER);
    }


    /**
     * Orders a towing truck for full subscriber who's car is in the parking lot for the max the
     * @param customerID the customer's Id
     * @param carID the car ID
     * @param parkingLotNumber the number of the parking lot.
     */
    private void orderTowingTruckForPickUpCar(Integer customerID, Integer carID, Integer parkingLotNumber) {
        //assuming always works
    }

    /**
     * Mails the customer notification according to params
     * @param customer customer ID
     * @param mailNotificationTemplate Notification template
     */
    private void mailCustomerNotification(Customer customer, MailNotificationTemplates mailNotificationTemplate) {
        switch (mailNotificationTemplate)
        {//assuming sending notification never fails
            case LATE_FOR_PARKING:
                //send late for parking notification
                break;
            case SUBSCRIPTION_EXPIRY_REMINDER:
                //send expiry reminder to customer
                break;
            case FULL_SUBSCRIPTION_MAX_PARK_TIME_BREACHED:
                //send notification about towing the car
                break;
        }
    }
}