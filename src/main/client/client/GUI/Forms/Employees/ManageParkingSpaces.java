package client.GUI.Forms.Employees;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.ParkingLotViewController;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
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

public class ManageParkingSpaces implements Initializable {

    @FXML
    private Button btnApply;

    @FXML
    private Button btnRevert;


    @FXML
    private Button btnBack;


    @FXML
    private TabPane tabMain;

    private SimpleBooleanProperty _dirty = new SimpleBooleanProperty(false);

    private ParkingLotViewController _controller;

    private ManageParkingSpaces _this;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _this = this;
        btnApply.disableProperty().bind(_dirty.not());
        btnRevert.disableProperty().bind(_dirty.not());
        Platform.runLater(this::queryParkingLot);
    }

    private void queryParkingLot(){
        WaitScreen waitScreen = new WaitScreen();
        Message queryParkingLot = new Message(QUERY, PARKING_LOT, CPSClientGUI.getSession().getParkingLot().getParkingLotID());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                tabMain.getTabs().clear();
                _controller = new ParkingLotViewController((ParkingLot) getMessage().getData().get(0),_this);
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

    public void setDirty(boolean dirty)
    {
        _dirty.set(dirty);
    }

    public boolean isDirty()
    {
        return _dirty.get();
    }

    public void addTab(Tab tab)
    {
        tabMain.getTabs().add(tab);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(isDirty());
    }


}
