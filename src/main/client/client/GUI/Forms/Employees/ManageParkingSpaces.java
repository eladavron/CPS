package client.GUI.Forms.Employees;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.ParkingLotViewController;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.GUIController;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Refreshable;
import entity.Message;
import entity.ParkingLot;
import entity.ParkingSpace;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static entity.Message.DataType.PARKING_LOT;
import static entity.Message.DataType.PARKING_SPACE;
import static entity.Message.MessageType.QUERY;
import static entity.Message.MessageType.UPDATE;

/**
 * A GUI controller for the Employee "Manage Parking Spaces" screen.<br>
 * Uses the {@link ParkingLotViewController} custom controller for actually displaying the parking lot,
 * this is only the outer shell.
 */
public class ManageParkingSpaces extends GUIController implements Initializable, Refreshable {

    @FXML
    private Button btnApply;

    @FXML
    private Button btnRevert;

    @FXML
    private Button btnBack;

    @FXML
    private ToggleButton toggleFull;

    @FXML
    private TabPane tabMain;

    private SimpleBooleanProperty _dirty = new SimpleBooleanProperty(false);

    private ParkingLotViewController _controller;

    private ManageParkingSpaces _this;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _this = this;
        btnApply.disableProperty().bind(_dirty.not());
        btnRevert.disableProperty().bind(_dirty.not());
        toggleFull.selectedProperty().addListener((observable, oldValue, newValue) ->
                toggleFull.setText("\"Full\" Sign: " + (newValue ? "On" : "Off")));
        Platform.runLater(this::queryParkingLot);
    }

    /**
     * Queries the server for all parking spaces in the logged on to parking lot.
     */
    private void queryParkingLot(){
        WaitScreen waitScreen = new WaitScreen();
        Message queryParkingLot = new Message(QUERY, PARKING_LOT, CPSClientGUI.getSession().getParkingLot().getParkingLotID());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                tabMain.getTabs().clear();
                ParkingLot parkingLot = (ParkingLot) getMessage().getData().get(0);
                toggleFull.setSelected(parkingLot.getIsFullState());
                _controller = new ParkingLotViewController(parkingLot,_this);

                waitScreen.setOnClose(()->Platform.runLater(()->initController()));
                waitScreen.hide();
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.setGoBackOnClose(true);
                waitScreen.showError("Couldn't get Parking Space form server!", getErrorString());
            }
        };
        MessageTasker taskQuery = new MessageTasker(queryParkingLot, onSuccess, onFailure);
        waitScreen.run(taskQuery);
    }

    /**
     * Initializes all the displays by querying the server and constructing the custom {@link ParkingLotViewController}
     * controller.
     */
    private void initController(){
        WaitScreen waitScreen = new WaitScreen();
        Task init = new Task() {
            @Override
            protected Object call() throws Exception {
                updateMessage("Getting Parking Space Information...");
                Platform.runLater(()->_controller.init());
                return null;
            }
        };
        init.setOnFailed((value)-> {
            waitScreen.setGoBackOnClose(true);
            waitScreen.showDefaultError(init.getException().getMessage());
        });
        init.setOnSucceeded((value)->waitScreen.hide());
        waitScreen.run(init);
    }


    /**
     * Handles the "Apply" button click event.
     * @param event The Click Event.
     */
    @FXML
    void applyChanges(ActionEvent event) {
        WaitScreen waitScreen = new WaitScreen();
        Message updateMessage = new Message(UPDATE, PARKING_SPACE, _controller.getParkingLot().getParkingLotID());
        for (ParkingSpace space : _controller.getChangedSpaces())
        {
            updateMessage.addData(space);
        }
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.setOnClose(()->Platform.runLater(()->queryParkingLot()));
                waitScreen.showSuccess("Parking Lot Saved!", "Your changes have been saved to the server.");
            }
        };
        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker updateTask = new MessageTasker(updateMessage, onSuccess, onFailed, "Saving to server...");
        waitScreen.run(updateTask);
    }

    /**
     * Handles the "Toggle Full" button press.
     * Sends the server a message to change the parking lot status to whatever the new status of the button is.
     * This is an immediate action - doesn't require refreshing.
     * @param event the toggling event.
     */
    @FXML
    void toggleFull(ActionEvent event) {
        boolean isFull = toggleFull.isSelected();
        WaitScreen waitScreen = new WaitScreen();
        Message toggleFullMessage = new Message(UPDATE, PARKING_LOT, _controller.getParkingLot().getParkingLotID(), isFull);
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _controller.getParkingLot().setIsFullState(isFull);
                waitScreen.hide();
            }
        };
        MessageRunnable onFailure = new MessageRunnable() {
            @Override
            public void run() {
                toggleFull.setSelected(false);
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker taskUpdate = new MessageTasker(toggleFullMessage,onSuccess,onFailure,"Toggling Sign...");
        waitScreen.run(taskUpdate);
    }

    /**
     * Reverts the controller to display the parking spaces as they are in the server.
     * @param event The button click event.
     */
    @FXML
    void revertChanges(ActionEvent event) {
        if (isDirty())
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Are you sure you want to refresh?");
            alert.setContentText("Any unsaved changes will be lost!");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.OK))
            {
                queryParkingLot();
            }
        }
        else
        {
            queryParkingLot();
        }
    }

    /**
     * Makes the controller know there were changes made to the parking spaces.
     * @param dirty whether or not changes were made to the parking spaces.
     */
    public void setDirty(boolean dirty)
    {
        _dirty.set(dirty);
    }

    /**
     * Checks whether or not changes were made to the parking spaces.
     * @return whether or not changes were made to the parking spaces.
     */
    public boolean isDirty()
    {
        return _dirty.get();
    }

    /**
     * Adds a "Floor" tab to the TabView controller.<br>
     * Called on by the custom {@link ParkingLotViewController} controller.
     * @param tab The new floor tab to add.
     */
    public void addTab(Tab tab)
    {
        tabMain.getTabs().add(tab);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        queryParkingLot();
    }

    /**
     * Goes back to the previous screen. The name is remnant of an older GUI scheme.
     * @param event the click event.
     */
    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(isDirty());
    }

}
