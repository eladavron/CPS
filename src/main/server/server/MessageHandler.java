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
import java.util.Random;
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

    private static ArrayList<Object> getDummyOrders(ConnectionToClient clientConnection) {
        //TODO: Query DB for all orders for this users.
        //In the meantime, here's a dummy response:
        Order dummyOrder1 = new Order(_sessionsMap.get(clientConnection).getUser().getUID(), 1234567, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 3), 0);
        Order dummyOrder2 = new Order(_sessionsMap.get(clientConnection).getUser().getUID(), 7654321, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 4), 1);
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
        PreOrder dummyOrder1 = new PreOrder(_sessionsMap.get(clientConnection).getUser().getUID(), 1234567, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 3), 0, 0.0, new Date());
        PreOrder dummyOrder2 = new PreOrder(_sessionsMap.get(clientConnection).getUser().getUID(), 7654321, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 4), 1, 0.0, new Date());
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
        //TODO: Check if order ows money
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
        _sessionsMap.remove(clientConnection);
    }

    private static void handleLogin(Message msg, ConnectionToClient clientConnection) throws IOException {
        Message loginResponse;
        String username =(String) msg.getData().get(0);
        String pwd =(String) msg.getData().get(1);
        ParkingLot parkingLot = (ParkingLot) msg.getData().get(2);
        if (username.equals("u") && pwd.equals("p")){ //TODO: Validate Actual User Login!!!
            Session session = new Session();
            User user = new User(2, username, pwd, User.UserType.USER); //TODO: Get Actual User from db!
            user.setName("Lucifer");
            user.setEmail("something@hateful.edu");
            session.setUser(user);
            session.setParkingLot(parkingLot);
            session.setSid(new Random().nextInt()); //TODO: Or however you get session IDs
            _sessionsMap.put(clientConnection,session);
            loginResponse = new Message(Message.MessageType.FINISHED, Message.DataType.SESSION, session.getSid(), session.getUser(), session.getParkingLot());
        }else{
            loginResponse = new Message(Message.MessageType.FAILED, Message.DataType.PRIMITIVE, "Wrong Username or Password");
        }
        loginResponse.setSID(msg.getSID());
        sendToClient(loginResponse, clientConnection);
    }

    private static boolean handleQueries(Message queryMsg, ConnectionToClient clientConnection) throws IOException {

        /**
         * Important!
         * In all queries, the first data object in the request will be the user requesting the query.
         */

        Message response = new Message();
        response.setDataType(queryMsg.getDataType());
        User user = (User)queryMsg.getData().get(0);

        switch (queryMsg.getDataType())
        {
            case PREORDER:
                response.setDataType(Message.DataType.PREORDER);
                response.setData(getDummyPreOrders(clientConnection));
                break;

            case PARKING_LOT:
                //TODO: Query DB for available parking lots.
                //In the meantime, here's a dummy response:
                ArrayList<Object> parkingLots = dbController.getParkingLots();
                response.setDataType(Message.DataType.PARKING_LOT);
                response.setData(parkingLots);
                break;
            case PRIMITIVE:
                break;
            case ORDER:
                response.setData(getDummyOrders(clientConnection));
                break;
            case USER:
                break;
            case SESSION:
                break;
            default:
                throw new NotImplementedException("Unknown data type: " + queryMsg.getDataType().toString());

        }
        response.setType(Message.MessageType.FINISHED);
        response.setSID(queryMsg.getSID());
        sendToClient(response, clientConnection);
        return true;
    }

    private static void handleDeletion(Message deleteMsg, ConnectionToClient clientConnection) {

//        Message deleteMsgResponse = new Message();
//        switch(deleteMsg.getDataType())
//        {
//            case
//
//        }

    }

    private static boolean handleCreation(Message createMsg, ConnectionToClient clientConnection) throws IOException {
        Message createMsgResponse;
        switch(createMsg.getDataType())
        {
            case USER:
                User user = new User(707070, "dum-dum-dummy", "weCallYou@DontCallUs.com", User.UserType.USER); //TODO: Get from server!
                createMsgResponse = new Message();
                break;
            case CUSTOMER:
                Customer customer = mapper.convertValue(createMsg.getData().get(0),Customer.class);
                Customer newCustomer = customerController.addNewCustomer(customer);
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.CUSTOMER, newCustomer);
                break;
            case ORDER:
                //TODO: return order from DB
                //Meanwhile, here's a dummy
                Order order = mapper.convertValue(createMsg.getData().get(0),Order.class);
                order.setOrderID(new Random().nextInt()); //TODO: Get from server! This is a dummy response!
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.ORDER, order);
                break;
            case PREORDER:
                PreOrder preorder = mapper.convertValue(createMsg.getData().get(0), PreOrder.class);
                Order newPreOrder = orderController.makeNewPreOrder(preorder);
                createMsgResponse = new Message(Message.MessageType.FINISHED, Message.DataType.PREORDER, newPreOrder);
                break;
            case PRIMITIVE:
                createMsgResponse = new Message();
                break;
            case PARKING_LOT:
//                TODO: Add create ParkingLot? when gui supports. do we even need this?
//                ArrayList<Object> parkingLots = dbController.getParkingLots();
//                Message parkingLotReply = new Message(Message.MessageType.FINISHED, Message.DataType.PARKING_LOT, parkingLots);
                ParkingLot dummy1 = new ParkingLot();
                ParkingLot dummy2 = new ParkingLot();
                ParkingLot dummy3 = new ParkingLot();
                Message parkingLotReply = new Message(Message.MessageType.FINISHED, Message.DataType.PARKING_LOT, dummy1,dummy2,dummy3);
                createMsgResponse = new Message();
                break;
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
