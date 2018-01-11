package server;

import Exceptions.InvalidMessageException;
import Exceptions.NotImplementedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import ocsf.server.ConnectionToClient;
import utils.TimeUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

/**
 * Handles messages from client GUI:
 *  1. Query
 *  2. Create
 *  3. Update
 *  4. Delete
 *  @author Aviad Bar-David
 */
public class MessageHandler {
    //TODO:: add validation for message
    private static ObjectMapper mapper = new ObjectMapper();

    public static void sendToClient(Message message, ConnectionToClient client) throws IOException {
        String json = mapper.writeValueAsString(message);
        if (CPSServer.IS_DEBUG)
        {
            System.out.println("SENT (" + client.getInetAddress() + "): " + json);
        }
        client.sendToClient(json);
    }

    public static boolean handleMessage(String json, ConnectionToClient client) throws IOException {
        try {
            Message msg = new Message(json);
            Message.MessageType msgType = msg.getType();

            if (true) {//TODO: IMPORTANT: Validate
                Message replyOnReceiveMsg = new Message(Message.MessageType.QUEUED, Message.DataType.STRING, "tempString");
                replyOnReceiveMsg.setSID(msg.getSID());
                sendToClient(replyOnReceiveMsg, client);
            }else{
                throw new InvalidMessageException("Failed to process message " + msg.getSID(), msg.getSID());
            }
            switch(msgType)
            {
                case LOGIN:
                    handleLogin(msg, client);
                    break;
                case QUERY:
                    handleQueries(msg, client);
                    break;
                case CREATE:
                    handleCreation(msg, client);
                    break;
                case DELETE:
                    //TODO: implement ASAP :)
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
            Message replyInvalid = new Message(Message.MessageType.FAILED, Message.DataType.STRING, e.getMessage());
            replyInvalid.setSID(e.getSID());
            sendToClient(replyInvalid, client);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void handleLogin(Message msg, ConnectionToClient client) throws IOException {
        Message loginResponse;
        String username =(String) msg.getData().get(0);
        String pwd =(String) msg.getData().get(1);
        ParkingLot parkingLot = (ParkingLot) msg.getData().get(2);
        if (username.equals("username") && pwd.equals("password")){ //TODO: Validate Actual User Login!!!
            Session session = new Session();
            User user = new User(666, username, pwd); //TODO: Get Actual User from db!
            session.setUser(user);
            session.setParkingLot(parkingLot);
            session.setSid(new Random().nextInt()); //TODO: Or however you get session IDs
            //TODO: Save session locally
            loginResponse = new Message(Message.MessageType.FINISHED, Message.DataType.SESSION, session.getSid(), session.getUser(), session.getParkingLot());
        }else{
            loginResponse = new Message(Message.MessageType.FAILED, Message.DataType.STRING, "Wrong Username or Password");
        }
        loginResponse.setSID(msg.getSID());
        sendToClient(loginResponse, client);
    }

    private static boolean handleQueries(Message queryMsg, ConnectionToClient client) throws IOException {

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
                //TODO: Query DB for all orders for this users.
                //In the meantime, here's a dummy response:
                PreOrder dummyOrder1 = new PreOrder(user.getUID(), 1234567, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 3), 0, 0.0, new Date());
                PreOrder dummyOrder2 = new PreOrder(user.getUID(), 7654321, TimeUtils.addTime(new Date(), TimeUtils.Units.DAYS, 4), 1, 0.0, new Date());
                //TODO: IMPORTANT return order ID from DB
                dummyOrder1.setOrderID(11);
                dummyOrder2.setOrderID(22);
                response.addData(dummyOrder1, dummyOrder2);
                break;

            case PARKING_LOT:
                //TODO: Query DB for available parking lots.
                //In the meantime, here's a dummy response:
                ParkingLot dummyLot1 = new ParkingLot(1,1,1,"Haifa");
                ParkingLot dummyLot2 = new ParkingLot(2,2,2,"Tel-Aviv");
                ParkingLot dummbyLot3 = new ParkingLot(3,3,3,"Petah-Tikva");
                response.addData(dummyLot1, dummyLot2, dummbyLot3);
                break;
            case STRING:
                break;
            case ORDER:
                Order dummy1 = new Order(user.getUID(),1234567,TimeUtils.addTime(new Date(), TimeUtils.Units.HOURS, 1) ,0);
                Order dummy2 = new Order(user.getUID(),7654321,TimeUtils.addTime(new Date(), TimeUtils.Units.HOURS, 1) ,1);
                //TODO: IMPORTANT return order ID from DB.
                dummy1.setOrderID(11);
                dummy2.setOrderID(22);
                response.addData(dummy1, dummy2);
                break;
            case USER:
                break;
            default:
                throw new NotImplementedException("Unknown data type: " + queryMsg.getDataType().toString());

        }
        response.setType(Message.MessageType.FINISHED);
        response.setSID(queryMsg.getSID());
        sendToClient(response, client);
        return true;
    }

    private static boolean handleCreation(Message createMsg, ConnectionToClient client) throws IOException {
        Message reply = new Message();
        switch(createMsg.getDataType())
        {
            case USER:
                User user = new User(707070, "dum-dum-dummy", "weCallYou@DontCallUs.com"); //TODO: Get from server!
                break;
            case ORDER:
                //TODO: return order from DB
                //Meanwhile, here's a dummy
                Order order = mapper.convertValue(createMsg.getData().get(0),Order.class);
                order.setOrderID(new Random().nextInt()); //TODO: Get from server! This is a dummy response!
                reply = new Message(Message.MessageType.FINISHED, Message.DataType.ORDER, order);
                break;
            case PREORDER:
                PreOrder preorder = mapper.convertValue(createMsg.getData().get(0), PreOrder.class);
                Order newPreOrder = controller.OrderController.getInstance().makeNewPreOrder(preorder);
                reply = new Message(Message.MessageType.FINISHED, Message.DataType.PREORDER, newPreOrder);
                break;
            case STRING:
                break;
            case PARKING_LOT:
                ParkingLot dummy1 = new ParkingLot();
                ParkingLot dummy2 = new ParkingLot();
                ParkingLot dummy3 = new ParkingLot();
                Message parkingLotReply = new Message(Message.MessageType.FINISHED, Message.DataType.PARKING_LOT, dummy1,dummy2,dummy3);
                break;
            default:
                return false;
        }
        reply.setSID(createMsg.getSID());
        sendToClient(reply, client);
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
