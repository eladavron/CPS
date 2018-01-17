package entity;

import Exceptions.InvalidMessageException;
import Exceptions.NotImplementedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.CustomerController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A message object between client and server
 * @author Elad Avron
 * @author Aviad Bar-David
 */
public class Message {
    public enum MessageType {
        LOGIN,
        LOGOUT,
        QUERY, //Query messages will have the data type as the requested response, and following data: 0: UserID (int), 1: UserType (User.UserType)
        CREATE,
        UPDATE,
        DELETE,
        END_PARKING,
        NEED_PAYMENT, //Indicates the customer needs to pay. Accompany with a double containing the amount.
        PAYMENT, //The message of the customer paying through the GUI. Accompany with a double containing the amount.
        QUEUED,
        FINISHED,
        FAILED,
        ERROR_OCCURRED,
    };
    public enum DataType {
        PRIMITIVE, //Any data type native to Java, such as String, Double, Integer, etc.
        SINGLE_ORDER, //For getting a single order from the server
        ORDER, //For getting ALL orders (that match a query) from the server
        PREORDER,
        COMPLAINT,
        USER,
        CARS,
        CUSTOMER,
        PARKING_LOT,
        SUBSCRIPTION,
        SESSION
    };

    private long _transactionID;
    private ArrayList<Object> _data;
    private MessageType _type;
    private DataType _dataType;

    public Message(){
        _data = new ArrayList<>();
    }

    public Message(MessageType type, DataType dataType)
    {
        Random rnd = new Random();
        _transactionID = rnd.nextLong();
        _type = type;
        _dataType = dataType;
        _data = new ArrayList<>();
    }

    public Message(MessageType type, DataType dataType, Object...data) {
        this(type, dataType);
        _data = new ArrayList<Object>();
        Collections.addAll(_data, data);
    }

    public Message(String json) throws InvalidMessageException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Message msg = mapper.readValue(json, Message.class);
            _transactionID =  msg.getTransID();
            _type = msg.getMessageType();
            _dataType = msg.getDataType();
            _data = new ArrayList<Object>();
            switch (_type)
            {
                case QUERY:
                    _data.add(msg.getData().get(0));
                    User.UserType type = mapper.convertValue(msg.getData().get(1), User.UserType.class);
                    _data.add(type);
                    break;
                case LOGIN:
                    String username = (String)msg.getData().get(0);
                    String password = (String)msg.getData().get(1);
                    ParkingLot parkingLot = mapper.convertValue(msg.getData().get(2), ParkingLot.class);
                    _data.add(0, username);
                    _data.add(1, password);
                    _data.add(2, parkingLot);
                    break;
                default: //Not a special message type
                    if (_dataType == DataType.SESSION)
                    {
                        Session session = new Session();
                        int SID = (int)msg.getData().get(0);
                        ParkingLot sessionParkingLot = mapper.convertValue(msg.getData().get(1), ParkingLot.class);
                        User.UserType userType = mapper.convertValue(msg.getData().get(2), User.UserType.class);
                        User user;
                        switch (userType)
                        {
                            case CUSTOMER:
                                /*
                                 Once again the custom classes break the converter, have to create it manually.
                                 */
                                LinkedHashMap userMap = (LinkedHashMap) msg.getData().get(3);
                                user = new Customer(userMap);
                                break;
                            case EMPLOYEE:
                                user = mapper.convertValue(msg.getData().get(3), Employee.class);
                                break;
                            default: //Shouldn't happen
                                user = new User();
                        }
                        session.setSid(SID);
                        session.setUser(user);
                        session.setParkingLot(sessionParkingLot);
                        _data.add(session);
                    } else if (_dataType == DataType.SUBSCRIPTION){
                        if (msg.getData().size() > 0) {
                            if (_type == MessageType.FINISHED || _type == MessageType.FAILED) {
                                CustomerController.SubscriptionOperationReturnCodes rc;
                                rc = mapper.convertValue(msg.getData().get(0), CustomerController.SubscriptionOperationReturnCodes.class);
                                _data.add(rc);
                            }
                            for (int i = 1; i < msg.getData().size(); i++)
                            {
                                LinkedHashMap myData = (LinkedHashMap) msg.getData().get(i);
                                Subscription.SubscriptionType subType = mapper.convertValue(myData.get("subscriptionType"), Subscription.SubscriptionType.class);
                                Subscription sub;
                                switch (subType) {
                                    case REGULAR:
                                    case REGULAR_MULTIPLE:
                                        sub = new RegularSubscription(myData);
                                        break;
                                    case FULL:
                                        sub = new FullSubscription(myData);
                                        break;
                                    default:
                                        throw new NotImplementedException("Unimplemented subscription type: " + subType);
                                }
                                sub.setSubscriptionID((Integer) myData.get("subscriptionID"));
                                _data.add(sub);
                            }
                        }
                    } else {
                        if (msg.getData() != null && msg.getData().size() > 0) {
                            for (Object dataObject : msg.getData()) {
                                switch (_dataType) {
                                    case CARS: //Cars are Integers
                                    case PRIMITIVE:
                                        _data.add(dataObject);
                                        break;
                                    case PREORDER:
                                        PreOrder preOrder = mapper.convertValue(dataObject, PreOrder.class);
                                        _data.add(preOrder);
                                        break;
                                    case ORDER:
                                        Order order = mapper.convertValue(dataObject, Order.class);
                                        _data.add(order);
                                        break;
                                    case CUSTOMER:
                                        LinkedHashMap customerData = (LinkedHashMap) dataObject;
                                        Integer customerUID = (Integer) customerData.get("uid");
                                        String customerPwd = (String) customerData.get("password");
                                        String customerName = (String) customerData.get("name");
                                        String customerEmail = (String) customerData.get("email");
                                        ArrayList carList = (ArrayList) customerData.get("carIDList");
                                        Customer customer = new Customer(customerUID, customerName, customerPwd, customerEmail, carList);
                                        _data.add(customer);
                                        break;
                                    case USER:
                                        User user = mapper.convertValue(dataObject, User.class);
                                        _data.add(user);
                                        break;
                                    case PARKING_LOT:
                                        ParkingLot parkingLotQuery = mapper.convertValue(dataObject, ParkingLot.class);
                                        _data.add(parkingLotQuery);
                                        break;
                                    case COMPLAINT:
                                        Complaint complaint = mapper.convertValue(dataObject, Complaint.class);
                                        _data.add(complaint);
                                        break;
                                    default:
                                        throw new InvalidMessageException("Unknown data type: " + msg.getDataType().toString());
                                }
                            }
                        }
                    }
            }
        }
        catch (InvalidMessageException im)
        {
            throw im;
        }
        catch (Exception ex)
        {
            throw new InvalidMessageException(ex);
        }
    }

    public static Long getSidFromJson(String json)
    {
        Pattern pattern = Pattern.compile("\\\"transID\\\"\\:(\\-?\\d*)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find())
        {
            return Long.valueOf(matcher.group(1));
        }
        else
        {
            return null;
        }
    }

    public ArrayList<Object> getData() {
        return _data;
    }

    public void setData(ArrayList<Object> data)
    {
        this._data = data;
    }

    public void addData(Object...data)
    {
        Collections.addAll(this._data, data);
    }

    public MessageType getMessageType() {
        return _type;
    }

    public void setMessageType(MessageType _type) {
        this._type = _type;
    }

    public DataType getDataType() {
        return _dataType;
    }

    public void setDataType(DataType _dataType) {
        this._dataType = _dataType;
    }

    public long getTransID() { return _transactionID; }

    public void setTransID(long transactionID) { this._transactionID = transactionID ;}

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(this);
        return json;
    }
}