package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Controls.PreorderCell;
import client.GUI.Controls.WaitScreen;
import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
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
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ViewPreorders implements Initializable {

    @FXML
    private ListView<PreOrder> listViewOrder;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnNew;

    @FXML
    private Button btnRefresh;

    private ObservableList<PreOrder> _listPreorders = FXCollections.observableArrayList();

    private ViewPreorders _this;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                queryPreorders();
            }
        });
        _this = this;
    }

    public void queryPreorders()
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryOrdersMsg = new Message(Message.MessageType.QUERY, Message.DataType.PREORDER, CPSClientGUI.getSession().getUser().getUID(), CPSClientGUI.getSession().getUserType());
        MessageRunnable onSuccess = new MessageRunnable() {
            @Override
            public void run() {
                _listPreorders.clear();
                listViewOrder.setItems(null);
                ArrayList preorders = getMessage().getData();
                for (Object preOrder : preorders)
                {
                    _listPreorders.add((PreOrder)preOrder);
                }
                listViewOrder.setItems(_listPreorders);
                listViewOrder.setCellFactory(new Callback<ListView<PreOrder>, ListCell<PreOrder>>() {
                    @Override
                    public ListCell<PreOrder> call(ListView<PreOrder> param) {
                        return new PreorderCell(_this);
                    }
                });
                waitScreen.hide();
            }
        };

        MessageRunnable onFailed = new MessageRunnable() {
            @Override
            public void run() {
                waitScreen.showDefaultError(getErrorString());
            }
        };
        MessageTasker queryOrders = new MessageTasker("Connecting...",
                "Looking for you orders...",
                "Orders found!",
                "No orders to display",
                queryOrdersMsg,
                onSuccess,
                onFailed);
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
