package controller;

import Exceptions.CustomerAlreadyExists;
import Exceptions.CustomerNotificationFailureException;
import Exceptions.LastCarRemovalException;
import Exceptions.OrderNotFoundException;
import entity.*;

import java.sql.SQLException;
import java.util.*;

import static controller.Controllers.*;
import static controller.CustomerController.SubscriptionOperationReturnCodes.*;
import static entity.Billing.priceList.*;
import static entity.Order.OrderStatus.*;
import static entity.Subscription.SubscriptionType.FULL;
import static entity.Subscription.SubscriptionType.REGULAR;


/**
 * Singleton Customer controller to be responsible over the methods of customer.
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

    public static CustomerController getInstance() throws SQLException{
        return instance;
    }

    public enum SubscriptionOperationReturnCodes {
        FAILED,
        SUCCESS_ADDED,
        RENEWED,
        QUERY_RESPONSE
    }

    private CustomerController() throws SQLException{
        getCustomersFromDb();
    }

    private Map<Integer, Customer> _customersList = new HashMap<>();


    /**
     * Searches for a specific Customer from the current Customer list.
     * @param customerID
     * @return the Customer if found, null otherwise.
     */
    public Customer getCustomer(Integer customerID)
    {
        return _customersList.getOrDefault(customerID, null);
    }

    public Customer getCustomerByEmail(String email) {
        for (Customer customer : _customersList.values()) {
            if (customer.getEmail().equals(email)) return customer;
        }
        return null;
    }

    /**
     * Private function that retrieves the Customers list form the DB, on startup.
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
     */
    public Customer addNewCustomer(Customer customer) throws CustomerAlreadyExists, SQLException {
        return addNewCustomer(customer.getUID(),
                              customer.getName(),
                              customer.getPassword(),
                              customer.getEmail(),
                              customer.getCarIDList());
    }
    public Customer addNewCustomer(Integer uID, String name, String password, String email, ArrayList<Integer> carIDList) throws CustomerAlreadyExists,SQLException {
        /**
         * First make sure there are no duplicates!
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
     * @param customer Customer from which to delete the active order
     * @param orderID The order ID
     */
    public void removeOrderFromCustomerList(int customerID, int orderID) throws CustomerNotificationFailureException {
        try {
            _customersList.get(customerID).getActiveOrders().remove(orderID);
        } catch (Exception e) {
            if (Controllers.IS_DEBUG_CONTROLLER)
            {
                e.printStackTrace();
            }
            throw new CustomerNotificationFailureException("There was a problem with your request, please contact support");
        }
    }

    /**
     *  Given the the right params needed for a new Order...controller will add this new order into the active orders list.
     * @param customerID
     * @param carID
     * @param estimatedExitTime
     * @param parkingLotNumber
     */
    public Order addNewOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber) throws SQLException{
        Customer customer = getCustomer(customerID);
        Order newOrder = orderController.makeNewSimpleOrder(customerID, carID, estimatedExitTime,  parkingLotNumber);
        Map<Integer, Object> activeOrders = customer.getActiveOrders();
        activeOrders.put(newOrder.getOrderID(), newOrder);
        customer.setActiveOrders(activeOrders);
        return newOrder;
    }

    /**
     * OverLoading function for a given template of an Order.
     * @param newOrder
     */
    public Order addNewOrder(Order newOrder) throws SQLException{
        return addNewOrder(newOrder.getCostumerID(), newOrder.getCarID(), newOrder.getEstimatedExitTime(), newOrder.getParkingLotNumber());
    }

    /**
     *  Overloaded function of the above just for PreOrder making.
     * @param customerID
     * @param carID
     * @param estimatedExitTime
     * @param parkingLotNumber
     * @param estimatedEntryTime
     * @return order
     */
    public Order addNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime)throws SQLException{
        Customer customer = getCustomer(customerID);
        Order newOrder = orderController.makeNewPreOrder(customerID, carID, estimatedExitTime,  parkingLotNumber, estimatedEntryTime);
        Map<Integer, Object> activeOrders = customer.getActiveOrders();
        activeOrders.put(newOrder.getOrderID(),newOrder);
        customer.setActiveOrders(activeOrders);
        return newOrder;
    }

    /**
     * OverLoading function for a given template of a PreOrder.
     * @param newPreOrder
     * @return Order new PreOrder
     */
    public Order addNewPreOrder(PreOrder newPreOrder) throws SQLException{
        return addNewPreOrder(newPreOrder.getCostumerID(), newPreOrder.getCarID(), newPreOrder.getEstimatedExitTime(), newPreOrder.getParkingLotNumber(), newPreOrder.getEstimatedEntryTime());
    }

    /**
     *  Delete an un-wanted order.
     */
    public Order removeOrder(Customer customer, Integer orderID)throws SQLException{
        Order removedOrder = orderController.deleteOrder(orderID);
        return removedOrder;
    }

    /**
     * Calls DBController to update order status to be in progress + actual entry time. to be used when fulfilling a preorder
     * @param orderToUpdate
     * @throws SQLException
     */
    public void updatePreOrderEntranceToParking(Order orderToUpdate) throws SQLException {
        dbController.changeOrderToInProgress(orderToUpdate.getOrderID(),orderToUpdate.getActualEntryTime());
    }

    /**
     * Finish an order (exiting with the car)
     * Throws order not found if we cant find it.
     * Return: the price for the Customer to pay (by calling BillingController)
     */
    public double finishOrder(Customer customer,Integer orderID, double finalPrice) throws OrderNotFoundException, SQLException, CustomerNotificationFailureException {
        Map<Integer, Object> activeOrders = customer.getActiveOrders();
        Order orderToFinish;
        if (activeOrders.containsKey(orderID)) {
            orderToFinish = (Order) activeOrders.get(orderID);
            // The order was found...need to check the client's cost for this order.
            Integer carID = orderToFinish.getCarID();
            orderController.finishOrder(orderToFinish.getOrderID(), finalPrice);
            removeOrderFromCustomerList(customer.getUID(), orderID);
            return orderToFinish.getPrice();
        }
        else{ throw new OrderNotFoundException(orderID);}
    }

    /**
     *  Private method to be used in finishOrder method, will check if the user has any subscription THAT MATCHES this order.
     * @param customerID
     * @param orderToFinish
     * @return
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
     * private method used in order to make sure this order actualy fits the subscription regularly 1 time entrance.
     * @param currentOrder
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
     *  Add another car to this customer.
     * @param customer
     * @param carID
     */
    public void addCar(Customer customer, Integer carID) throws SQLException
    {
        customer.getCarIDList().add(carID);
        dbController.addCarToCustomer(customer.getUID(), carID);
    }

    /**
     * @param carID     * Remove carID from this customer, will fail if this is the last car on his list!
     * @param customer

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
     *   Given the the right params needed for a new Regular Subscription
     *   the controller will add subscribe it and put it into the the customer's subscriptionList.
     * @param subs - Regular Subscription
     * @return CustomerController.SubscriptionOperationReturnCodes Return Code
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
     *  Same as addNewRegularSubscription just on FullSubscription this time.
     * @param fSubs - Full Subscription
     * @return CustomerController.SubscriptionOperationReturnCodes Return Code
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

    public boolean sendEntryTimeBreachedNotification(PreOrder preOrder)
    {
        Customer lateCustomer = getCustomer(preOrder.getCostumerID());
        System.out.println("Order #" + preOrder.getOrderID() + " is late. Parking reminder for " + lateCustomer.getName() + " was sent to " + lateCustomer.getEmail());
        return true; //Assuming mailing always works
    }
}