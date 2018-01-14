package server;

import Exceptions.InvalidMessageException;
import Exceptions.NotImplementedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import ocsf.server.ConnectionToClient;
import utils.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static controller.Controllers.*;

/**
 * Handles messages from client GUI:
 *  1. Query
 *  2. Create
 *  3. Update
 *  4. Delete
 *  @author Aviad Bar-David
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

    public static Session getSession(ConnectionToClient clientConnection){
        return _sessionsMap.get(clientConnection);
    }

    public static void saveSession(ConnectionToClient clientConnection, Session session){
        _sessionsMap.put(clientConnection, session);
    }

    public static void dropSession(ConnectionToClient clientConnection)
    {
        _sessionsMap.remove(clientConnection);
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
        String json = mapper.writeValueAsString(message);
        if (CPSServer.IS_DEBUG)
        {
            System.out.println("SENT (" + clientConnection.getInetAddress() + "): " + json);
        }
        clientConnection.sendToClient(json);
    }

    public static boolean handleMessage(String json, ConnectionToClient clientConnection) throws IOException {
        try {
            Message msg = new Message(json);
            Message.MessageType msgType = msg.getType();

            if (msgType == Message.MessageType.LOGOUT)
            {
                handleLogout(msg,clientConnection);
                return true;
            }

            Message replyOnReceiveMsg = new Message(Message.MessageType.QUEUED, Message.DataType.PRIMITIVE, "tempString");
            replyOnReceiveMsg.setSID(msg.getSID());
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
                    //TODO: Implement ASPS too
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
            Message replyInvalid = new Message(Message.MessageType.FAILED, Message.DataType.PRIMITIVE, e.getMessage());
            Pattern pattern = Pattern.compile("\\\"sid\\\"\\:(\\-?\\d*)");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find())
            {
                replyInvalid.setSID(Long.valueOf(matcher.group(1)));
            }
            sendToClient(replyInvalid, clientConnection);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void handleEndParking(Message msg, ConnectionToClient client) throws IOException {
        //TODO: IMPORTANT! THIS IS A DUMMY RESPONSE!!!
        Message endParkingResponse = new Message();
        Order order = (Order)msg.getData().get(0);
        endParkingResponse.setSID(msg.getSID());
        //TODO: Check if order owns money
        if (order.getOrderID() == 11) //DUMMY - end ok
        {
            endParkingResponse.setType(Message.MessageType.FINISHED);
        }
        if (order.getOrderID() == 22) //DUMMY - payment needed
        {
            messagesThatNeedPayment.put(msg.getSID(), order);
            endParkingResponse.setType(Message.MessageType.NEED_PAYMENT);
            endParkingResponse.setDataType(Message.DataType.PRIMITIVE);
            endParkingResponse.addData(33.45);
        }
        sendToClient(endParkingResponse, client);
    }

    private static void handlePayment(Message msg, ConnectionToClient client) throws IOException {
        Message response = new Message();
        response.setSID(msg.getSID());
        if (messagesThatNeedPayment.containsKey(msg.getSID()))
        {
            Order orderToUpdate = messagesThatNeedPayment.get(msg.getSID());
            //TODO: UPDATE THE DB THAT THIS ORDER PAID!!!
        }
        response.setType(Message.MessageType.FINISHED);
        sendToClient(response, client);
    }

    private static void handleLogout(Message msg, ConnectionToClient clientConnection) throws IOException {
        Message logoutResponse = new Message();
        ArrayList<Object> data = new ArrayList<Object>();
        logoutResponse.setType(Message.MessageType.LOGOUT);
        logoutResponse.setSID(msg.getSID());
        logoutResponse.setDataType(Message.DataType.PRIMITIVE);
        logoutResponse.addData("So long, and thanks for all the fish");
        dropSession(clientConnection);
    }

    private static void handleLogin(Message msg, ConnectionToClient clientConnection) throws IOException {
        Message loginResponse;
        String email =(String) msg.getData().get(0);
        String pwd =(String) msg.getData().get(1);
        ParkingLot parkingLot = (ParkingLot) msg.getData().get(2);
        User logginUser = customerController.getCustomerByEmail(email); //TODO: Get other types if it's not a user
        if (logginUser == null || !logginUser.getPassword().equals(pwd)) //no customer or wrong pwd
        {
            loginResponse = new Message(Message.MessageType.FAILED, Message.DataType.PRIMITIVE, "Wrong Username or Password");
        }
        else //login ok
        {
            Session session = new Session();
            session.setParkingLot(parkingLot);
            session.setUserType(logginUser.getUserType());
            String name = logginUser.getName();
            session.setUser(logginUser);
            session.setSid(clientConnection.hashCode());//hashCode is generated by a random hexa-string
            saveSession(clientConnection,session);
            loginResponse = new Message(Message.MessageType.FINISHED, Message.DataType.SESSION, session.getSid(), session.getParkingLot(), session.getUserType(), session.getUser());
        }
        loginResponse.setSID(msg.getSID());
        sendToClient(loginResponse, clientConnection);
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
                response.setData(customerController.getCustomer(userID).getSubscriptionList());
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
            ArrayList<Object> parkingLots = dbController.getParkingLots();
            response.setDataType(Message.DataType.PARKING_LOT);
            response.setData(parkingLots);
        }
        else if (queryMsg.getData().get(0) != null)
        {
            handleUserQueries(queryMsg, response);
        }

        response.setType(Message.MessageType.FINISHED);
        response.setSID(queryMsg.getSID());
        sendToClient(response, clientConnection);
        return true;
    }

    private static void handleDeletion(Message deleteMsg, ConnectionToClient clientConnection) throws IOException {

        Message response = new Message();
        response.setSID(deleteMsg.getSID());

        switch(deleteMsg.getDataType())
        {
            case CARS:
                Integer uID = (Integer)deleteMsg.getData().get(0);
                Integer carToDelete = (Integer)deleteMsg.getData().get(1);
                response.setDataType(deleteMsg.getDataType());
                if (customerController.removeCar(customerController.getCustomer(uID),carToDelete))
                {//success
                    response.setType(Message.MessageType.FINISHED);
                }
                else
                {
                    response.setType(Message.MessageType.FAILED);
                    response.setDataType(Message.DataType.PRIMITIVE);
                    response.addData("Car removal from DB failed");
                }
                break;
        }
        sendToClient(response,clientConnection);
    }

    private static boolean handleCreation(Message createMsg, ConnectionToClient clientConnection) throws IOException {
        Message createMsgResponse;
        switch(createMsg.getDataType())
        {
            case CUSTOMER:
                Customer customer = mapper.convertValue(createMsg.getData().get(0),Customer.class);
                Customer newCustomer = customerController.addNewCustomer(customer);
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.CUSTOMER, newCustomer);
                break;
            case ORDER:
                //TODO: return order from DB
                //Meanwhile, here's a dummy
                Order order = mapper.convertValue(createMsg.getData().get(0),Order.class);
//                order.setOrderID(new Random().nextInt()); //TODO: Get from server! This is a dummy response!
                Order newOrder = customerController.addNewOrder(order); //TODO: uncomment once fully supported and remove dummy response.
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.ORDER, newOrder);
                break;
            case PREORDER:
                PreOrder preorder = mapper.convertValue(createMsg.getData().get(0), PreOrder.class);
                Order newPreOrder = orderController.makeNewPreOrder(preorder);
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.PREORDER, newPreOrder);
                break;
            case CARS:
                Integer uID = (Integer)createMsg.getData().get(0);
                Integer carToAdd = (Integer)createMsg.getData().get(1);
                Customer customerToAddTo = customerController.getCustomer(uID);
                if (customerToAddTo != null)
                {
                    customerController.addCar(customerToAddTo, carToAdd);
                }
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.CARS, carToAdd);
                break;
//            case SUBSCRIPTION: // WIP
//                Customer subCustomer = customerController.getCustomer((Integer) createMsg.getData().get(0));
//                Subscription subscription = mapper.convertValue(createMsg.getData().get(1),Subscription.class);
//                switch(subscription.getSubscriptionType())
//                {
//                    case REGULAR:
//                        customerController.addNewRegularSubscription(subCustomer,subscription.getCarID(),)
//                        break;
//                    case FULL:
//                        break;
//                    default:
//                        throw new NotImplementedException("Subscription type does not exists: " + subscription.getSubscriptionType());
//                }
//
//                break;
            default:
                createMsgResponse = new Message(Message.MessageType.FAILED, Message.DataType.PRIMITIVE,"Unknown Type: " + createMsg.getDataType().toString());
                return false;
        }
        createMsgResponse.setSID(createMsg.getSID());
        sendToClient(createMsgResponse, clientConnection);
        return true;
    }

    private static boolean handleUpdate(Message updateMsg)
    {
        return true;
    }

    private static boolean handleDelete(Message deleteMsg)
    {
        return true;
    }
}
