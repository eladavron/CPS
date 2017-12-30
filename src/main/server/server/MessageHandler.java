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
    public static boolean handleMessage(String message)
    {
        ObjectMapper mapper = new ObjectMapper();

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
        }
        return true;
    }

    private static boolean handleQueries(Message query)
    {

        return true;
    }

    private static boolean handleCreation(Message create)
    {
        Message.DataType creation = create.getDataType();
        switch(creation)
        {
            case USER:
                break;
            case ORDER:
                for (Object orderObj : create.getData())
                {
                    Order order = (Order) orderObj;
                    System.out.println("Customer Name is " + order.getCustomerName());
                }
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
