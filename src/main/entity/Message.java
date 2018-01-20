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

import static entity.Message.MessageType.CREATE;

/**
 * A Message object between client and server<br>
 * A Message has three essential parts: The{@link MessageType} which denotes what type of operation we're performing,
 * a {@link DataType} field which denotes what type of data we're making the operation on, and an ArrayList of Objects
 * which are the bulk of the data. The array list can be empty or transfer multiple types of different objects, and the
 * Message constructor and Message Handlers on both the client AND the server will know how to parse them according to
 * the combination of MessageType and DataType.
 * A Message object also has a Transaction ID, which allows the Server and Client to keep track which messages belong to
 * which request in cases of multiple requests made at the same time by the same client.
 */
public class Message {
    public enum MessageType {
        LOGIN,
        LOGOUT,
        QUERY, //Query messages will have the data type as the requested response, and following data: 0: UserID (int), 1: UserType (User.UserType)
        CREATE,
        UPDATE,
        DELETE,
        INIT,
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
        COMPLAINT_PRE_CUSTOMER,
        ALL_COMPLAINTS,
        USER,
        CARS,
        CUSTOMER,
        REPORT,
        FINAL_REPORT, //For reports only the company manager sees
        PARKING_LOT, //For a single parking lot
        PARKING_LOT_LIST, //For a list of parking lots
        PARKING_SPACE,
        PARKING_LOT_IMAGE,
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

    /**
     * Creates a message instance given a type and datatype.
     * @param type
     * @param dataType
     */
    public Message(MessageType type, DataType dataType)
    {
        Random rnd = new Random();
        _transactionID = rnd.nextLong();
        _type = type;
        _dataType = dataType;
        _data = new ArrayList<>();
    }

    /**
     * Creates a Message instance given a type, a data type and data.
     * @param type
     * @param dataType
     * @param data
     */
    public Message(MessageType type, DataType dataType, Object...data) {
        this(type, dataType);
        _data = new ArrayList<Object>();
        Collections.addAll(_data, data);
    }

    /**
     * Converts a Json String to a Message object based on its type and content.
     * @param json
     * @throws InvalidMessageException
     */
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
                    switch (_dataType)
                    {
                        case SINGLE_ORDER: //Userless with ID
                        case PARKING_LOT: //Userless with ID
                        case PARKING_LOT_IMAGE: //Userless with ID
                            _data.add(msg.getData().get(0)); //copy ID
                        case PARKING_LOT_LIST: //Userless
                        case FINAL_REPORT: //USerless
                        case ALL_COMPLAINTS: //Userless
                            break;
                        case REPORT: //Userless
                            Report.ReportType reportType = mapper.convertValue(msg.getData().get(1), Report.ReportType.class);
                            _data.add(msg.getData().get(0)); //Add User ID
                            _data.add(reportType);
                            _data.add(msg.getData().get(2)); //Copy parking lot ID;
                            break;
                        case ORDER:
                            _data.add(msg.getData().get(0)); //copy User ID
                            _data.add(mapper.convertValue(msg.getData().get(1), User.UserType.class)); //Copy User Type
                            if (msg.getData().size() >= 3) //If it's for a specific parking lot
                                _data.add(msg.getData().get(2)); //Copy Parking Lot ID
                            break;
                        default: //Other User queries
                            User.UserType userType = mapper.convertValue(msg.getData().get(1), User.UserType.class);
                            _data.add(msg.getData().get(0)); //Copy UserID
                            _data.add(userType);
                    }
                    break;
                case LOGIN:
                    String username = (String)msg.getData().get(0);
                    String password = (String)msg.getData().get(1);
                    ParkingLot parkingLot = mapper.convertValue(msg.getData().get(2), ParkingLot.class);
                    _data.add(0, username);
                    _data.add(1, password);
                    _data.add(2, parkingLot);
                    break;
                case UPDATE:
                    switch (_dataType)
                    {
                        case PARKING_SPACE:
                            Integer parkingLotID = (Integer) msg.getData().get(0);
                            _data.add(parkingLotID);
                            for (int i = 1; i < msg.getData().size(); i++){
                                ParkingSpace space = mapper.convertValue(msg.getData().get(i), ParkingSpace.class);
                                _data.add(space);
                            }
                            break;
                        case COMPLAINT_PRE_CUSTOMER:
                            _data.add(mapper.convertValue(msg.getData().get(0), Complaint.class));
                            break;
                        case PARKING_LOT:
                            _data.add(msg.getData().get(0)); //Copy Lot ID
                            _data.add(msg.getData().get(1)); //Copy new status
                            break;
                    }
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
                            case MANAGER:
                            case SUPERMAN:
                            case CUSTOMER_SERVICE:
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
                                        sub = mapper.convertValue(myData, RegularSubscription.class);
                                        break;
                                    case FULL:
                                        sub = mapper.convertValue(myData, FullSubscription.class);
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
                                    case SINGLE_ORDER:
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
                                        if (_type.equals(CREATE))
                                        {
                                            _data.add(dataObject); //It's just the ID
                                            break;
                                        }
                                    case PARKING_LOT_LIST:
                                        ParkingLot parkingLotQuery = mapper.convertValue(dataObject, ParkingLot.class);
                                        _data.add(parkingLotQuery);
                                        break;
                                    case PARKING_SPACE:
                                        ParkingSpace parkingSpace = mapper.convertValue(dataObject, ParkingSpace.class);
                                        _data.add(parkingSpace);
                                        break;
                                    case ALL_COMPLAINTS:
                                    case COMPLAINT_PRE_CUSTOMER:
                                        Complaint complaint = mapper.convertValue(dataObject, Complaint.class);
                                        _data.add(complaint);
                                        break;
                                    case FINAL_REPORT:
                                        FinalReport report = mapper.convertValue(dataObject, FinalReport.class);
                                        _data.add(report);
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

    /**
     * Extracts the Transaction ID from the Json for use in case of errors.
     * @param json The JSON string.
     * @return Transaction ID
     */
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

    /**
     * Returns the ArrayList representing the message data.
     * @return
     */
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

    /**
     * Returns a Json string representing the message.
     * @return
     * @throws JsonProcessingException
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(this);
        return json;
    }
}