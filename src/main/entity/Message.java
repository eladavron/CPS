package entity;

import Exceptions.InvalidMessageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * A message object between client and server
 * @author Elad Avron
 * @author Aviad Bar-David
 */
public class Message {
    public enum MessageType {
        LOGIN,
        LOGOUT,
        QUERY,
        CREATE,
        UPDATE,
        DELETE,
        END_PARKING,
        NEED_PAYMENT, //Indicates the customer needs to pay. Accompany with a double containing the amount.
        PAYMENT, //The message of the customer paying through the GUI. Accompany with a double containing the amount.
        QUEUED,
        FINISHED,
        FAILED
    };
    public enum DataType {
        PRIMITIVE, //Any data type native to Java, such as String, Double, Integer, etc.
        ORDER,
        PREORDER,
        USER,
        CUSTOMER,
        PARKING_LOT,
        SESSION
    };

    private long _sID;
    private ArrayList<Object> _data;
    private MessageType _type;
    private DataType _dataType;

    public Message(){
        _data = new ArrayList<>();
    }

    public Message(MessageType type, DataType dataType)
    {
        Random rnd = new Random();
        _sID = rnd.nextLong();
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
            Message msg = mapper.readValue(json, Message.class);
            _sID =  msg.getSID();
            _type = msg.getType();
            _dataType = msg.getDataType();
            _data = new ArrayList<Object>();
            switch (_type)
            {
                case QUERY:
                    User sender = mapper.convertValue(msg.getData().get(0), User.class);
                    _data.add(sender);
                    break;
                case LOGIN:
                    String username = (String)msg.getData().get(0);
                    String password = (String)msg.getData().get(1);
                    ParkingLot parkingLot = mapper.convertValue(msg.getData().get(2), ParkingLot.class);
                    _data.add(0, username);
                    _data.add(1, password);
                    _data.add(2, parkingLot);
                    break;
                default:
                    if (_dataType == DataType.SESSION)
                    {
                        int sessionID = (int) msg.getData().get(0);
                        User sessionUser = mapper.convertValue(msg.getData().get(1), User.class);
                        ParkingLot sessionParkingLot = mapper.convertValue(msg.getData().get(2), ParkingLot.class);
                        Session session = new Session(0, sessionUser, sessionParkingLot);
                        _data.add(session);
                    }
                    else {
                        for (Object dataObject : msg.getData()) {
                            switch (_dataType) {
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
                                    Customer customer = mapper.convertValue(dataObject, Customer.class);
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
                                default:
                                    throw new InvalidMessageException("Unknown data type: " + msg.getDataType().toString());
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

    public MessageType getType() {
        return _type;
    }

    public void setType(MessageType _type) {
        this._type = _type;
    }

    public DataType getDataType() {
        return _dataType;
    }

    public void setDataType(DataType _dataType) {
        this._dataType = _dataType;
    }

    public long getSID() { return _sID; }

    public void setSID(long sID) { this._sID = sID ;}

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(this);
        return json;
    }
}