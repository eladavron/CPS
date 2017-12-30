package entity;

import java.util.ArrayList;

/**
 * A message object between client and server
 * @author Elad Avron
 * @author Aviad Bar-David
 */
public class Message {
    public enum MessageType { QUERY, CREATE, UPDATE, DELETE };
    public enum DataType { STRING, ORDER, USER, PARKING_LOT };

    private ArrayList<Object> _data;
    private MessageType _type;
    private DataType _dataType;

    public Message(MessageType type, DataType dataType, ArrayList<Object> data) {
        _type = type;
        _dataType = dataType;
        _data = data;
    }

    public ArrayList<Object> getData() {
        return _data;
    }

    public void setData(ArrayList<Object> _data) {
        this._data = _data;
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
}