package client.GUI.Controls;

import Exceptions.NotImplementedException;
import client.GUI.Forms.Employees.ManageParkingSpaces;
import client.GUI.Helpers.ErrorHandlers;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.Order;
import entity.ParkingLot;
import entity.ParkingSpace;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.ArrayList;

import static entity.Message.DataType.SINGLE_ORDER;
import static entity.Message.MessageType.QUERY;


public class ParkingLotViewController {

    private ParkingLot _parkingLot;
    private ManageParkingSpaces _parent;

    private ArrayList<ParkingSpace> _changedSpaces = new ArrayList<ParkingSpace>();

    public ParkingLotViewController(ParkingLot parkingLot, ManageParkingSpaces parent) {
        this._parent = parent;
        this._parkingLot = parkingLot;
    }

    public void init(){
        for (int h = 1; h <= _parkingLot.getHeight(); h++) //Get all spaces in a floor
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ParkingLotFloor.fxml"));
                Tab newTab = loader.load();
                newTab.setText("Floor " + h);
                GridPane parkingGrid = (GridPane) loader.getNamespace().get("gridFloor");
                for (int w = 1; w <= _parkingLot.getWidth(); w++) {
                    for (int d = 1; d <= _parkingLot.getDepth(); d++) {
                        ParkingSpace thisSpace = _parkingLot.getParkingSpaceMatrix()[d][w][h];
                        Node thisNode = parkingSpaceNode(thisSpace);
                        parkingGrid.add(thisNode, w, d); //TODO: Might have to flip this, check!
                        GridPane.setHgrow(thisNode, Priority.ALWAYS);
                        GridPane.setVgrow(thisNode, Priority.ALWAYS);
                    }
                }
                _parent.addTab(newTab);
            } catch (IOException e) {
                ErrorHandlers.GUIError(e, false);
            }
        }
        _parent.setDirty(false);
    }

    private BorderPane parkingSpaceNode(ParkingSpace item) {
        BorderPane pane = new BorderPane();
        if (item == null || item.getStatus() == null)
            pane.setCenter(new Label("Null"));
        else {
            switch (item.getStatus()) {
                case OCCUPIED:
                    Hyperlink occupied = new Hyperlink();
                    occupied.setText("Occupied");
                    occupied.setOnAction((event) -> showOrder(item.getOccupyingOrderID()));
                    pane.setCenter(occupied);
                    break;
                case ORDERED:
                    Hyperlink ordered = new Hyperlink();
                    ordered.setText("Ordered");
                    ordered.setOnAction((event) -> showOrder(item.getOccupyingOrderID()));
                    pane.setCenter(ordered);
                    break;
                case UNAVAILABLE:
                    ComboBox<String> comboUnavailable = new ComboBox<String>();
                    comboUnavailable.getItems().addAll("Free", "Unavailable");
                    comboUnavailable.getSelectionModel().select("Unavailable");
                    setListener(comboUnavailable, item);
                    pane.setCenter(comboUnavailable);
                    break;
                case FREE:
                    ComboBox<String> comboFree = new ComboBox<String>();
                    comboFree.getItems().addAll("Free", "Unavailable");
                    comboFree.getSelectionModel().select("Free");
                    setListener(comboFree, item);
                    pane.setCenter(comboFree);
                    break;
                default:
                    throw new NotImplementedException("Unexpected Parking Space Status: " + item.getStatus());
            }
        }
        pane.getStyleClass().add("card");
        return pane;
    }

    private void showOrder(int orderID)
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryOrder = new Message(QUERY, SINGLE_ORDER, orderID);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                Order order = (Order) getMessage().getData().get(0);
                waitScreen.showSuccess("Order No." + order.getOrderID(), order.toGUIString());
            }
        };

        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker showOrder = new MessageTasker(queryOrder, onSuccess, onFailed, "Getting message details...");
        waitScreen.run(showOrder);
    }

    private void setListener(ComboBox<String> comboBox, ParkingSpace item)
    {
        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.equals(oldValue)) //Only if it was actually changed
                {
                    switch (newValue) {
                        case "Free":
                            changeStatus(item, ParkingSpace.ParkingStatus.FREE);
                            break;
                        case "Unavailable":
                            changeStatus(item, ParkingSpace.ParkingStatus.UNAVAILABLE);
                            break;
                    }
                }
            }
        });
    }

    private void changeStatus(ParkingSpace space, ParkingSpace.ParkingStatus newStatus)
    {
        ParkingSpace originalSpace = _parkingLot.getParkingSpaceMatrix()[space.getDepth()][space.getWidth()][space.getHeight()];
        originalSpace.setStatus(newStatus);
        if (!_changedSpaces.contains(originalSpace))
        {
            _changedSpaces.add(originalSpace);
        }
        _parent.setDirty(true);
    }

    public ParkingLot getParkingLot() {
        return _parkingLot;
    }

    public ArrayList<ParkingSpace> getChangedSpaces() {
        return _changedSpaces;
    }
}
