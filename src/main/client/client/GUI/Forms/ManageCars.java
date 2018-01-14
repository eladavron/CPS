package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.CarCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import client.GUI.Helpers.Validation;
import entity.Customer;
import entity.Message;
import entity.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageCars implements Initializable{
    @FXML
    private Button btnBack;

    @FXML
    private ListView<Integer> listViewCars;

    @FXML
    private Button btnNew;

    @FXML
    private Button btnRefresh;

    private ObservableList<Integer> _listCars = FXCollections.observableArrayList();

    private ManageCars _this;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _this = this;
        listViewCars.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> param) {
                return new CarCell(_this);
            }
        });
        listViewCars.setItems(_listCars);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                queryServerForCars();
            }
        });
    }

    public void queryServerForCars()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryMessage = new Message(Message.MessageType.QUERY, Message.DataType.CARS, CPSClientGUI.getSession().getUser().getUID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _listCars.clear();
                for (Object car : getMessage().getData())
                {
                    _listCars.add((Integer) car);
                }
                if (CPSClientGUI.getSession().getUserType() == User.UserType.CUSTOMER) //Update the session carlist
                {
                    ArrayList<Integer> customerCarList = new ArrayList<>(_listCars);
                    ((Customer)CPSClientGUI.getSession().getUser()).setCarIDList(customerCarList);
                }
                waitScreen.hide();
            }
        };

        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };

        MessageTasker tasker = new MessageTasker("Fetching car list...",
                "Receiving...",
                "Car List Received!",
                "Failed to get car list!",
                queryMessage, onSuccess, onFailed);

        waitScreen.run(tasker);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(false);
    }

    @FXML
    void refreshList(ActionEvent event) {
        queryServerForCars();
    }

    @FXML
    void newCar(ActionEvent event) {
        TextInputDialog newCarDialog = new TextInputDialog();
        newCarDialog.setTitle("Add New Car");
        newCarDialog.setHeaderText("Add A New Car");
        newCarDialog.setContentText("Please enter a new car number: ");
        newCarDialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {
                TextInputDialog source = (TextInputDialog)event.getSource();
                TextField editor = source.getEditor();
                String result = source.getResult();
                if (result != null && ( !Validation.carNumber(result) || _listCars.contains(Integer.valueOf(result)))  ) {
                        Validation.showError(source.getEditor(), "Not a valid car number!");
                        event.consume();
                    }
                }
            });
        Optional<String> result = newCarDialog.showAndWait();
        result.ifPresent(s -> {
            WaitScreen waitScreen = new WaitScreen();
            Message newCar = new Message(Message.MessageType.CREATE, Message.DataType.CARS, CPSClientGUI.getSession().getUser().getUID(), Integer.valueOf(s));
            MessageRunnable onSuccess = new MessageRunnable() {
                @Override
                public void run() {
                    waitScreen.hide();
                    Platform.runLater(()->queryServerForCars());
                }
            };
            MessageRunnable onFailure = new MessageRunnable() {
                @Override
                public void run() {
                    waitScreen.showDefaultError();
                }
            };

            MessageTasker taskNewCar = new MessageTasker("Adding Car...",
                    "Adding Car...",
                    "Car Added!",
                    "Something went wrong...",
                    newCar, onSuccess,onFailure);

            waitScreen.run(taskNewCar);
        });
    }

}
