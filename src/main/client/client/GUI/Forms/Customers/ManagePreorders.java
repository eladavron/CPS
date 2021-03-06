package client.GUI.Forms.Customers;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.PreorderCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.Order;
import entity.PreOrder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static entity.Order.OrderStatus.PRE_ORDER;

public class ManagePreorders implements Initializable {

    @FXML
    private ListView<PreOrder> listViewOrder;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnNew;

    @FXML
    private Button btnRefresh;

    @FXML
    private TitledPane rootOrders;

    private ObservableList<PreOrder> _listPreorders = FXCollections.observableArrayList();

    private ManagePreorders _this;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        _this = this;
        listViewOrder.setCellFactory(new Callback<ListView<PreOrder>, ListCell<PreOrder>>() {
            @Override
            public ListCell<PreOrder> call(ListView<PreOrder> param) {
                return new PreorderCell(_this);
            }
        });
        listViewOrder.setItems(_listPreorders);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                queryPreorders();
            }
        });
    }

    public void queryPreorders()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryOrdersMsg = new Message(Message.MessageType.QUERY, Message.DataType.PREORDER, CPSClientGUI.getLoggedInUserID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _listPreorders.clear();
                ArrayList preorders = getMessage().getData();
                for (Object preOrder : preorders)
                {
                    if (((Order)preOrder).getOrderStatus() == PRE_ORDER)
                        _listPreorders.add((PreOrder)preOrder);
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
        MessageTasker queryOrders = new MessageTasker(queryOrdersMsg, onSuccess, onFailed, "Looking for you orders...");
        waitScreen.run(queryOrders);
    }

    @FXML
    void refreshList(ActionEvent event) {
        queryPreorders();
    }

    @FXML
    void createOrder(ActionEvent event) {
        CPSClientGUI.changeGUI(CPSClientGUI.NEW_PREORDER);
    }

    @FXML
    void returnToMain(ActionEvent event) {
        CPSClientGUI.goBack(false);
    }
}
