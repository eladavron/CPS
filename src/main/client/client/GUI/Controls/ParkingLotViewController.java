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
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.ArrayList;

import static entity.Message.DataType.SINGLE_ORDER;
import static entity.Message.MessageType.QUERY;


/**
 * A controller for the custom ParkingLot View.
 * This handles the populating and controlling of all the sub-elements in a TabView which represents the parking lot.
 * It is used in the {@link ManageParkingSpaces} screen for Employees to manage individual {@link ParkingSpace}s.
 * Parking Spaces with cars in them (or reserved) will show a link to see the active order that represents that parking
 * session. Otherwise shows a ComboBox allowing the Employee to set a single parking space as "Inactive".
 */
public class ParkingLotViewController {

    private ParkingLot _parkingLot;
    private ManageParkingSpaces _parent;

    private ArrayList<ParkingSpace> _changedSpaces = new ArrayList<ParkingSpace>();

    /**
     * The constructor requires the Parking Lot this element represents and the {@link ManageParkingSpaces} view that created it.
     * @param parkingLot The Parking Lot this element represents.
     * @param parent The {@link ManageParkingSpaces} view that created it.
     */
    public ParkingLotViewController(ParkingLot parkingLot, ManageParkingSpaces parent) {
        this._parent = parent;
        this._parkingLot = parkingLot;
    }

    /**
     * Inits the views by loading the FXML for a single Parking Lot Floor, then populating it with the
     * {@link ParkingSpace}s in it. Each Parking Space is initialized by {@link #parkingSpaceNode(ParkingSpace)}.
     */
    public void init(){
        for (int d = 1; d <= _parkingLot.getDepth(); d++) //Get all spaces in a floor
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ParkingLotFloor.fxml"));
                Tab newTab = loader.load();
                newTab.setText("Floor " + d);
                GridPane parkingGrid = (GridPane) loader.getNamespace().get("gridFloor");
                for (int w = 1; w <= _parkingLot.getWidth(); w++) {
                    for (int h = 1; h <= _parkingLot.getHeight(); h++) {
                        ParkingSpace thisSpace = _parkingLot.getParkingSpaceMatrix()[d][w][h];
                        Node thisNode = parkingSpaceNode(thisSpace);
                        parkingGrid.add(thisNode, w, h);
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

    /**
     * Represents a single {@link ParkingSpace} by returning a control with either a link to an active occupying order or
     * parking session, or a ComboBox for setting the parking space status manually.
     * @param item The {@link ParkingSpace} this node represent.
     * @return A {@link BorderPane} {@link Node} representing the cell.
     */
    private BorderPane parkingSpaceNode(ParkingSpace item) {
        BorderPane pane = new BorderPane();
        if (item == null || item.getStatus() == null)
            pane.setCenter(new Label("Null"));
        else {
            switch (item.getStatus()) {
                case OCCUPIED:
                    Hyperlink occupied = new Hyperlink();
                    occupied.setText("Occupied.\nOrder #" + item.getOccupyingOrderID());
                    occupied.setOnAction((event) -> showOrder(item.getOccupyingOrderID()));
                    occupied.setTextAlignment(TextAlignment.CENTER);
                    pane.setCenter(occupied);
                    break;
                case ORDERED:
                    Hyperlink ordered = new Hyperlink();
                    ordered.setText("Ordered.\nOrder #" + item.getOccupyingOrderID());
                    ordered.setOnAction((event) -> showOrder(item.getOccupyingOrderID()));
                    ordered.setTextAlignment(TextAlignment.CENTER);
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

    /**
     * Used to handle clicking on occupied cells. Queries the server to display the {@link Order} they represent.
     * @param orderID The {@link Order} ID.
     */
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

    /**
     * Sets the listener for the ComboBox representing the parking space.
     * The listener changes the status of the Parking Space using {@link #changeStatus(ParkingSpace, ParkingSpace.ParkingStatus)}
     * @param comboBox The {@link ComboBox in the parking space.}
     * @param item The Parking Space represented by the Combo Box.
     */
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

    /**
     * Change the status of a single Parking Space.
     * Also notifies the parent view that the list has been changed.
     * @param space The Parking Space whose status has changed.
     * @param newStatus The new {@link ParkingSpace.ParkingStatus}.
     */
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

    /**
     * Gets the Parking Lot represented by this control.
     * @return The Parking Lot Represented by this control.
     */
    public ParkingLot getParkingLot() {
        return _parkingLot;
    }

    /**
     * Gets the list of changed spaces.
     * @return An array list containing all the Parking Spaces whose statuses have changed.
     */
    public ArrayList<ParkingSpace> getChangedSpaces() {
        return _changedSpaces;
    }
}
