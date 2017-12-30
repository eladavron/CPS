package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Message;
import entity.Order;

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

    public static boolean handleMessage(String message)
    {
//JSON from String to Object
        try {
            Message msg = mapper.readValue(message, Message.class);
            Message.MessageType msgType = msg.getType();
            switch(msgType)
            {
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
        }
        return true;
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
                break;
            case ORDER:
                Order order = mapper.convertValue(creation.getData().get(0),Order.class);
                break;
            case STRING:
                break;
            case PARKING_LOT:
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
