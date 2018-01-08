package server;

import Exceptions.InvalidMessageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import ocsf.server.ConnectionToClient;

import java.io.IOException;

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
                    break;
                case CREATE:
                    handleCreation(msg);
                    break;
                case DELETE:
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
        }
        return true;
    }

    private static void handleLogin(Message msg, ConnectionToClient client) throws IOException {
        Message loginResponse;
        String username =(String) msg.getData().get(0);
        String pwd =(String) msg.getData().get(1);
        if (username.equals("username") && pwd.equals("password")){
            User LoginUser = new User(666, username, pwd);
            loginResponse = new Message(Message.MessageType.FINISHED, Message.DataType.USER, LoginUser);
        }else{
            loginResponse = new Message(Message.MessageType.FAILED, Message.DataType.STRING, "Wrong Username or Password");
        }
        loginResponse.setSID(msg.getSID());
        sendToClient(loginResponse, client);
    }


    private static boolean handleQueries(Message query)
    {
        return true;
    }

    private static boolean handleCreation(Message creation)
    {
        switch(creation.getDataType())
        {
            case USER:
                User user = new User(707070, "dum-dum-dummy", "weCallYou@DontCallUs.com");
                break;
            case ORDER:
                Order order = mapper.convertValue(creation.getData().get(0),Order.class);
                Message orderReply = new Message(Message.MessageType.FINISHED, Message.DataType.ORDER, order);
                break;
            case PREORDER:
                PreOrder preorder = mapper.convertValue(creation.getData().get(0), PreOrder.class);
                //TODO: return pre-order from DB
                Message preorderReply = new Message(Message.MessageType.FINISHED, Message.DataType.PREORDER, preorder);
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
        return true;
    }

    private static boolean handleUpdate(Message update)
    {
        return true;
    }

    private static boolean handleDelete(Message delete)
    {
        return true;
    }
    }
