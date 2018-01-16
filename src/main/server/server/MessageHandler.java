package server;

import Exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.CustomerController;
import entity.*;
import ocsf.server.ConnectionToClient;
import utils.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static controller.Controllers.*;
import static controller.CustomerController.SubscriptionOperationReturnCodes.FAILED;
import static controller.CustomerController.SubscriptionOperationReturnCodes.QUERY_RESPONSE;
import static entity.Message.DataType.PRIMITIVE;
import static entity.Message.MessageType;

/**
 * Handles messages from client GUI:
 *  1. Query
 *  2. Create
 *  3. Update
 *  4. Delete
 */
public class MessageHandler {
    private static ObjectMapper mapper = new ObjectMapper();
    private static HashMap<Long, Order> messagesThatNeedPayment = new HashMap<>();

    public static HashMap<ConnectionToClient, Session> getSessionsMap() {
        return _sessionsMap;
    }

    /**
     * Sessions
     */
    private static HashMap<ConnectionToClient, Session> _sessionsMap = new HashMap<ConnectionToClient, Session>();

    /**
     * Get a session from the map by SID.
     * @param SID Session ID to search
     * @return Session if found, null if not.
     */
    public static Session getSession(int SID)
    {
        for (Session session : _sessionsMap.values())
        {
            if (session.getSid() == SID)
            {
                return session;
            }
        }
        return null;
    }

    public static Session getSession(ConnectionToClient clientConnection){
        return _sessionsMap.get(clientConnection);
    }

    public static void saveSession(ConnectionToClient clientConnection, Session session){
        _sessionsMap.put(clientConnection, session);
        System.out.println("Session added: " + session);
    }

    public static void dropSession(ConnectionToClient clientConnection)
    {
        _sessionsMap.remove(clientConnection);
    }

    public static void dropSession(Session session)
    {
        for (ConnectionToClient connection : _sessionsMap.keySet())
        {
            if (_sessionsMap.get(connection).equals(session))
                dropSession(connection);
        }
    }

    private static ArrayList<Object> getDummyOrders(ConnectionToClient clientConnection) {
        //TODO: Query DB for all orders for this users.
        //In the meantime, here's a dummy response:
        Order dummyOrder1 = new Order(getSession(clientConnection).getUser().getUID(), 1234567, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 3), 0);
        Order dummyOrder2 = new Order(getSession(clientConnection).getUser().getUID(), 7654321, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 4), 1);
        //TODO: IMPORTANT return order ID from DB
        dummyOrder1.setOrderID(11);
        dummyOrder2.setOrderID(22);
        ArrayList<Object> dummies = new ArrayList<Object>();
        dummies.add(dummyOrder1);
        dummies.add(dummyOrder2);
        return dummies;
    }

    private static ArrayList<Object> getDummyPreOrders(ConnectionToClient clientConnection) {
        //TODO: Query DB for all orders for this users.
        //In the meantime, here's a dummy response:
        PreOrder dummyOrder1 = new PreOrder(getSession(clientConnection).getUser().getUID(), 1234567, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 3), 0, 0.0, new Date());
        PreOrder dummyOrder2 = new PreOrder(getSession(clientConnection).getUser().getUID(), 7654321, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 4), 1, 0.0, new Date());
        //TODO: IMPORTANT return order ID from DB
        dummyOrder1.setOrderID(11);
        dummyOrder2.setOrderID(22);
        ArrayList<Object> dummies = new ArrayList<Object>();
        dummies.add(dummyOrder1);
        dummies.add(dummyOrder2);
        return dummies;
    }

    public static void sendToClient(Message message, ConnectionToClient clientConnection) throws IOException {
        String json = message.toJson();
        if (CPSServer.IS_DEBUG)
        {
            System.out.println("SENT (" + clientConnection.getInetAddress() + "): " + json);
        }
        clientConnection.sendToClient(json);
    }

    public static boolean handleMessage(String json, ConnectionToClient clientConnection) throws IOException {
        try {
            Message msg = new Message(json);
            MessageType msgType = msg.getType();

            if (msgType == MessageType.LOGOUT)
            {
                handleLogout(msg,clientConnection);
                return true;
            }

            Message replyOnReceiveMsg = new Message(MessageType.QUEUED, PRIMITIVE, "tempString");
            replyOnReceiveMsg.setTransID(msg.getTransID());
            sendToClient(replyOnReceiveMsg, clientConnection);

            switch(msgType)
            {
                case LOGIN:
                    handleLogin(msg, clientConnection);
                    break;
                case QUERY:
                    handleQueries(msg, clientConnection);
                    break;
                case CREATE:
                    handleCreation(msg, clientConnection);
                    break;
                case PAYMENT:
                    handlePayment(msg, clientConnection);
                    break;
                case END_PARKING:
                    handleEndParking(msg, clientConnection);
                    break;
                case DELETE:
                    handleDeletion(msg, clientConnection);
                    break;
                case UPDATE:
                    break;
                default:
                    return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidMessageException e) {
            Message replyInvalid = new Message(MessageType.FAILED, PRIMITIVE, e.getMessage());
            Long SID = Message.getSidFromJson(json);
            if (SID != null)
                replyInvalid.setTransID(SID);
            sendToClient(replyInvalid, clientConnection);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void handleEndParking(Message endParkingMsg, ConnectionToClient clientConnection) throws IOException {

        Message endParkingResponse = new Message();
        endParkingResponse.setTransID(endParkingMsg.getTransID());
        Order order = (Order)endParkingMsg.getData().get(0);
        Customer departingCustomer = customerController.getCustomer(order.getCostumerID());

        try{

            Double neededPayment = customerController.finishOrder(departingCustomer,order.getOrderID());
            if (neededPayment.equals((double)0))
            {
                endParkingResponse.setType(MessageType.FINISHED);
                endParkingResponse.setDataType(PRIMITIVE);
            }
            else
            {
                getSession(clientConnection).setOrderInNeedOfPayment(order);
                endParkingResponse.setType(MessageType.NEED_PAYMENT);
                endParkingResponse.setDataType(PRIMITIVE);
                endParkingResponse.addData(neededPayment);
            }

            sendToClient(endParkingResponse, clientConnection);

        }catch (OrderNotFoundException e){
            endParkingResponse.setType(MessageType.FAILED);
            endParkingResponse.setDataType(PRIMITIVE);
            endParkingResponse.addData("Order ID " + order.getOrderID() + " was not found, please contact CPS personnel");
            sendToClient(endParkingResponse, clientConnection);
        }

    }

    private static void handlePayment(Message paymentNeededMsg, ConnectionToClient clientConnection) throws IOException {
        Message response = new Message();
        response.setTransID(paymentNeededMsg.getTransID());
        Order orderInNeedOfPayment = getSession(clientConnection).getOrderInNeedOfPayment();
        Customer payingCustomer = getSession(clientConnection).getCustomer();
        if (orderInNeedOfPayment != null){
            customerController.finishOrder(payingCustomer,orderInNeedOfPayment.getOrderID());
            getSession(clientConnection).setOrderInNeedOfPayment(null);
        }
        response.setType(MessageType.FINISHED);
        sendToClient(response, clientConnection);
    }

    private static void handleLogout(Message msg, ConnectionToClient clientConnection) throws IOException {
        Message logoutResponse = new Message();
        ArrayList<Object> data = new ArrayList<Object>();
        logoutResponse.setType(MessageType.LOGOUT);
        logoutResponse.setTransID(msg.getTransID());
        logoutResponse.setDataType(PRIMITIVE);
        logoutResponse.addData("So long, and thanks for all the fish");
        dropSession(clientConnection);
    }

    private static void handleLogin(Message msg, ConnectionToClient clientConnection) throws IOException {
        Message loginResponse;
        String email =(String) msg.getData().get(0);
        String pwd =(String) msg.getData().get(1);
        ParkingLot parkingLot = (ParkingLot) msg.getData().get(2);

        /*
        Now we check if user is an employee or not
         */
        User loggingUser;
        loggingUser = employeeController.getEmployeeByEmail(email);

        try
        {
            if (loggingUser != null && parkingLot.getParkingLotID() == -1) //Employee trying to log in remotely
            {
                throw new LoginException("Employees can only log in to their workplace!");
            }

            if (loggingUser == null) //Not an employee...
            {
                loggingUser = customerController.getCustomerByEmail(email);
            }

            if (loggingUser == null || !loggingUser.getPassword().equals(pwd)) //User not found or wrong password.
            {
                throw new LoginException("Wrong Username or Password");
            }

            if (isUserAlreadyLoggedIn(email)) //login ok - checking if already logged in and not superuser
            {
                throw new LoginException(loggingUser.getName() + " is already logged on!");
            }
            // If you reached here, login is ok!
            Session session = new Session(clientConnection.hashCode(),
                    loggingUser, loggingUser.getUserType(), email, parkingLot);

            saveSession(clientConnection,session);
            loginResponse = new Message(MessageType.FINISHED, Message.DataType.SESSION, session.getSid(), session.getParkingLot(), session.getUserType(), session.getUser());
        }
        catch (LoginException le)
        {
            loginResponse = new Message(MessageType.FAILED, Message.DataType.PRIMITIVE, le.getMessage());
        }
        loginResponse.setTransID(msg.getTransID());
        sendToClient(loginResponse, clientConnection);
    }

    private static boolean isUserAlreadyLoggedIn(String email) {
        for (Session session : getSessionsMap().values())
        {
            if (session.getEmail().equals(email))
            {
                return true;
            }
        }
        return false;
    }

    private static void handleUserQueries(Message queryMsg, Message response)
    {
        int userID = (int) queryMsg.getData().get(0);
        switch (queryMsg.getDataType())
        {
            case PREORDER:
                response.setDataType(Message.DataType.PREORDER);
                response.setData(customerController.getCustomersPreOrders(userID));
                break;
            case ORDER:
                response.setDataType(Message.DataType.ORDER);
                response.setData(customerController.getCustomersActiveOrders(userID));
                break;
            case CARS:
                for (Integer car : (customerController.getCustomer((userID))).getCarIDList())
                {
                    response.addData(car);
                }
                break;
            case SUBSCRIPTION:
                response.setDataType(Message.DataType.SUBSCRIPTION);
                response.addData(QUERY_RESPONSE);
                for (Object sub : customerController.getCustomer(userID).getSubscriptionList())
                {
                    response.addData(sub);
                }
                break;
            case SESSION:
                break;
            default:
                throw new NotImplementedException("Unknown data type: " + queryMsg.getDataType().toString());
        }
    }

    private static boolean handleQueries(Message queryMsg, ConnectionToClient clientConnection) throws IOException {

        /**
         * Important!
         * In all queries, the first data object in the request will be the user requesting the query.
         */

        Message response = new Message();
        response.setDataType(queryMsg.getDataType());

        User user = new User();
        if (queryMsg.getData().get(0) != null && queryMsg.getData().get(1) != null) {
            int userID = (int) queryMsg.getData().get(0);
            User.UserType type = (User.UserType) queryMsg.getData().get(1);
            switch (type)
            {
                case CUSTOMER:
                    user = customerController.getCustomer(userID);
                    break;
            }
        }

        if (queryMsg.getDataType().equals(Message.DataType.PARKING_LOT)) //Parking lot queries are userless
        {
            ArrayList<Object> parkingLots = parkingController.getParkingLots();
            response.setDataType(Message.DataType.PARKING_LOT);
            response.setData(parkingLots);
        }
        else if (queryMsg.getData().get(0) != null)
        {
            handleUserQueries(queryMsg, response);
        }

        response.setType(MessageType.FINISHED);
        response.setTransID(queryMsg.getTransID());
        sendToClient(response, clientConnection);
        return true;
    }

    private static void handleDeletion(Message deleteMsg, ConnectionToClient clientConnection) throws IOException {

        Message response = new Message();
        switch(deleteMsg.getDataType())
        {
            case CARS:
                Integer uID = (Integer)deleteMsg.getData().get(0);
                Integer carToDelete = (Integer)deleteMsg.getData().get(1);
                response.setDataType(deleteMsg.getDataType());
                try{
                    if (customerController.removeCar(customerController.getCustomer(uID),carToDelete))
                    {//success
                        response.setType(MessageType.FINISHED);
                    }
                    else
                    {
                        response.setType(MessageType.FAILED);
                        response.setDataType(PRIMITIVE);
                        response.addData("Car removal from DB failed");
                    }
                }catch(LastCarRemovalException e)
                {
                    response.setType(MessageType.FAILED);
                    response.setDataType(PRIMITIVE);
                    response.addData("It is required to have at least 1 registered car");
                }

                break;
            case PREORDER:
                PreOrder preOrderToDelete = (PreOrder) deleteMsg.getData().get(0);
                Customer customer = customerController.getCustomer(preOrderToDelete.getCostumerID());
                Order removedOrder = customerController.removeOrder(customer, preOrderToDelete.getOrderID());
                if (removedOrder == null)
                { //TODO: Add support for payment required && order not found exception
                    response.setType(MessageType.FAILED);
                }
                else
                {
                    response = new Message(MessageType.FINISHED, Message.DataType.PREORDER, removedOrder);
                }
                break;
        }
        response.setTransID(deleteMsg.getTransID());
        sendToClient(response,clientConnection);
    }

    private static boolean handleCreation(Message createMsg, ConnectionToClient clientConnection) throws IOException {
        Message createMsgResponse;
        switch(createMsg.getDataType())
        {
            case CUSTOMER:
                Customer customer = mapper.convertValue(createMsg.getData().get(0),Customer.class);
                Customer newCustomer = customerController.addNewCustomer(customer);
                createMsgResponse = new Message(MessageType.FINISHED, Message.DataType.CUSTOMER, newCustomer);
                break;
            case ORDER:
                Order order = mapper.convertValue(createMsg.getData().get(0),Order.class);
                Order newOrder = customerController.addNewOrder(order);
                createMsgResponse = new Message(MessageType.FINISHED, Message.DataType.ORDER, newOrder);
                break;
            case PREORDER:
                PreOrder preorder = mapper.convertValue(createMsg.getData().get(0), PreOrder.class);
                Order newPreOrder = customerController.addNewPreOrder(preorder);
                createMsgResponse = new Message(MessageType.FINISHED, Message.DataType.PREORDER, newPreOrder);
                break;
            case CARS:
                Integer uID = (Integer)createMsg.getData().get(0);
                Integer carToAdd = (Integer)createMsg.getData().get(1);
                Customer customerToAddTo = customerController.getCustomer(uID);
                if (customerToAddTo != null)
                {
                    customerController.addCar(customerToAddTo, carToAdd);
                }
                createMsgResponse = new Message(MessageType.FINISHED, Message.DataType.CARS, carToAdd);
                break;
            case SUBSCRIPTION:
                Subscription subscription = (Subscription) createMsg.getData().get(0);
                Subscription.SubscriptionType subType = subscription.getSubscriptionType();
                CustomerController.SubscriptionOperationReturnCodes rc = FAILED;
                switch(subType)
                {
                    case REGULAR:
                    case REGULAR_MULTIPLE:
                        rc = customerController.addNewRegularSubscription((RegularSubscription) subscription);
                        break;
                    case FULL:
                        rc = customerController.addNewFullSubscription((FullSubscription) subscription);
                        break;
                    default:
                        throw new NotImplementedException("Subscription type does not exists: " + subType.toString());
                }

                if (rc != FAILED)
                {
                    createMsgResponse = new Message(MessageType.FINISHED,
                            Message.DataType.SUBSCRIPTION,
                            rc,
                            subscription);
                }else
                {
                    createMsgResponse = new Message(MessageType.FAILED,
                            Message.DataType.SUBSCRIPTION);
                }
                break;
            default:
                createMsgResponse = new Message(MessageType.FAILED, PRIMITIVE,"Unknown Type: " + createMsg.getDataType().toString());
                return false;
        }
        createMsgResponse.setTransID(createMsg.getTransID());
        sendToClient(createMsgResponse, clientConnection);
        return true;
    }

    private static boolean handleUpdate(Message updateMsg)
    {
        return true;
    }
}
