package client.GUI.Controls;

import client.GUI.Helpers.MessageRunnable;
import client.GUI.Helpers.MessageTasker;
import entity.Message;
import entity.Order;
import entity.ParkingSpace;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

import static entity.Message.DataType.SINGLE_ORDER;
import static entity.Message.MessageType.QUERY;

public class ParkingSpaceCell extends TableCell<TableView<ParkingSpace>, ParkingSpace> {

    @Override
    protected void updateItem(ParkingSpace item, boolean empty) {
        super.updateItem(item, empty);
        switch (item.getStatus())
        {
            case OCCUPIED:
                Hyperlink occupied = new Hyperlink();
                occupied.setText("Occupied");
                occupied.setOnAction((event)->showOrder(item.getOccupyingOrderID()));
            case ORDERED:


        }
    }

    private void showOrder(int orderID)
    {
        WaitScreen waitScreen = new WaitScreen();
        Message queryOrder = new Message(QUERY, SINGLE_ORDER, orderID);
        MessageRunnable onSucces = new MessageRunnable() {
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
        MessageTasker showOrder = new MessageTasker(queryOrder, onSucces, onFailed, "Getting message details...");
    }
}
