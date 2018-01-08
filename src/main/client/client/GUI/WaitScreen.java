package client.GUI;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WaitScreen extends AnchorPane implements Initializable {

    @FXML
    private Button btnCancel;

    @FXML
    private Label lblMessage;

    @FXML
    private FlowPane paneButtons;

    @FXML
    private BorderPane paneContent;

    @FXML
    private Label lblTitle;

    private AnchorPane _root;

    private Thread worker;
    private Timer timer;
    private Runnable _onClose;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               hide();
            }
        });
    }


    public WaitScreen() throws IOException {
        _root = CPSClientGUI.getPageRoot();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WaitScreen.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
    }

    /**
     * @param task Task to run while displaying waiting window
     * @param timeout In seconds!
     */
    public void run(Task<?> task, int timeout)
    {
        this.bindMessageProperty(task.messageProperty());
        this.show();
        worker = new Thread(task);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                worker.interrupt();
            }
        }, TimeUnit.SECONDS.toMillis(timeout));
        worker.start();
    }

    public void bindMessageProperty(ReadOnlyStringProperty sp) {
        lblMessage.textProperty().bind(sp);
    }

    public void show()
    {
        _root.getChildren().add(this);
        this.toFront();
        AnchorPane.setRightAnchor(this,0.0);
        AnchorPane.setLeftAnchor(this,0.0);
        AnchorPane.setBottomAnchor(this,0.0);
        AnchorPane.setTopAnchor(this,0.0);
    }

    public void hide()
    {
        _root.getChildren().remove(this);
        this.reset();
        if (_onClose != null)
        {
            _onClose.run();
        }
    }

    public void hideIn(int seconds)
    {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        hide();
                    }
                });
            }
        }, TimeUnit.SECONDS.toMillis(seconds));
    }

    public void setTitle(String title) {
        this.lblTitle.setText(title);
    }

    public void showError(String title, String message, int secondsToShow)
    {
        cancelTimeout();
        lblTitle.setText(title);
        lblTitle.setTextFill(Color.RED);

        lblMessage.textProperty().unbind();
        lblMessage.setText(message);
        lblMessage.setTextFill(Color.RED);

        btnCancel.setText("Ok");
        if (secondsToShow > 0)
            hideIn(secondsToShow);
    }

    public void showSuccess(String title, String message, int secondsToShow)
    {
        cancelTimeout();

        lblTitle.setText(title);
        lblTitle.setTextFill(Color.GREEN);

        lblMessage.textProperty().unbind();
        lblMessage.setText(message);
        lblMessage.setTextFill(Color.BLACK);

        btnCancel.setText("Ok");
        if (secondsToShow > 0)
            hideIn(secondsToShow);
    }

    public void cancelTimeout()
    {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public void reset()
    {
        lblTitle.setText("");
        lblTitle.setTextFill(Color.BLACK);

        lblMessage.textProperty().unbind();
        lblMessage.setText("");
        lblMessage.setTextFill(Color.BLACK);

        btnCancel.setText("Cancel");

        if (timer != null)
        {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (worker != null)
        {
            worker.interrupt();
            worker = null;
        }
    }

    public void setOnClose(Runnable action)
    {
        _onClose = action;
    }
}
