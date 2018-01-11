package client.GUI.Forms;

import client.GUI.CPSClientGUI;
import client.GUI.Helpers.ErrorHandlers;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.SocketException;
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

    private Thread worker;
    private Timer timer;
    private Runnable _onClose;
    private Task _task;

    private static int timeToClose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               hide();
            }
        });
    }

    /**
     * Default constructor.
     */
    public WaitScreen(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("WaitScreen.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException io) {
            ErrorHandlers.GUIError(io, false);
        }
    }

    //region Worker Thread related methods
    /**
     * @param task Task to run while displaying waiting window
     */
    public void run(Task<?> task)
    {
        run(task, CPSClientGUI.DEFAULT_TIMEOUT);
    }

    /**
     * @param task Task to run while displaying waiting window
     * @param timeout In seconds!
     */
    public void run(Task<?> task, int timeout)
    {
        _task = task;
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
        worker.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showError("An unexpected error occurred:", e.getMessage());
                    }
                });
            }
        });
        worker.start();
    }

    /**
     * Checks if the main worker thread is currently active
     * @return true if it is, false if it is not.
     */
    public boolean isTaskRunning()
    {
        return (worker != null && worker.isAlive());
    }

    /**
     * Binds the window's status message to a given string property.
     * Use this to bind the window's message to a thread.
     * @param sp the <code>StringProperty</code> method.
     */
    public void bindMessageProperty(ReadOnlyStringProperty sp) {
        lblMessage.textProperty().bind(sp);
    }

    /**
     * Abort any time-out counter so that it doesn't interfere with error or success messages.
     */
    public void cancelTimeout()
    {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    //endregion

    //region Hide and Show methods
    /**
     * Show the wait screen and bring it to front.
     * Disables anything else in the current GUI.
     */
    public void show()
    {
        for (Node node : CPSClientGUI.getPageRoot().getChildren())
        {
            node.setDisable(true);
        }
        CPSClientGUI.getPageRoot().getChildren().add(this);
        this.toFront();
        AnchorPane.setRightAnchor(this,0.0);
        AnchorPane.setLeftAnchor(this,0.0);
        AnchorPane.setBottomAnchor(this,0.0);
        AnchorPane.setTopAnchor(this,0.0);
    }

    /**
     * Hides and resets the wait screen.
     * Re-enables anything below it.
     * If {@link #setOnClose(Runnable)} is used, will run whatever is set by it afterwards.
     */
    public void hide()
    {
        CPSClientGUI.getPageRoot().getChildren().remove(this);
        for (Node node : CPSClientGUI.getPageRoot().getChildren())
        {
            node.setDisable(false);
        }
        this.reset();
        if (_onClose != null)
        {
            _onClose.run();
        }
    }

    /**
     * Auto-hide the wait screen after a given amount of time.
     * Also updates the "ok/cancel" button with the number of seconds left before closing.
     * @param seconds number of seconds to close in.
     */
    public void hideIn(int seconds)
    {
        timeToClose = seconds;
        String ok = btnCancel.getText();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (timeToClose == 0)
                        {
                            hide();
                        }
                        else
                        {
                            btnCancel.setText(ok + "(" + timeToClose-- + ")");
                        }
                    }
                });
            }
        }, 0, 1000);
    }
    //endregion

    //region Error and Success messages
    /**
     * Show the default error message.
     */
    public void showDefaultError()
    {
        showDefaultError(-1);
    }

    /**
     * Show a self-closing default error message.
     * @param secondsToShow time to show error.
     */
    public void showDefaultError(int secondsToShow)
    {
        showDefaultError("", secondsToShow);
    }

    /**
     * Show the default error message with an additional text.
     * @param message The text to add to the default error message.
     */
    public void showDefaultError(String message)
    {
        showDefaultError(message, -1);
    }

    /**
     * Show a self-closing default error message with an additional text.
     * @param message The text to add to the default error message.
     * @param secondsToShow The amount of time in seconds to show the error for.
     */
    public void showDefaultError(String message, int secondsToShow)
    {
        showError("Something went wrong...", "Sorry, there was an error with your request.\n" + message, secondsToShow);
    }

    /**
     * Show an error message.
     * @param title The title of the error message.
     * @param message The message itself.
     */
    public void showError(String title, String message)
    {
        showError(title, message, -1);
    }

    /**
     * Show a self-closing error message.
     * @param title The title of the error message.
     * @param message The message itself.
     * @param secondsToShow The amount of time in seconds to show the error.
     */
    public void showError(String title, String message, int secondsToShow)
    {
        cancelTimeout();
        lblTitle.setText(title);
        lblTitle.setTextFill(Color.RED);

        lblMessage.textProperty().unbind();
        lblMessage.setText(message);
        lblMessage.setTextFill(Color.RED);

        btnCancel.setText("Ok");

        if (_task != null && _task.getException() instanceof SocketException)
        {
            this.setOnClose(new Runnable() {
                @Override
                public void run() {
                    CPSClientGUI.resetConnection();
                }
            });
        }
        if (secondsToShow > 0)
            hideIn(secondsToShow);
    }

    /**
     * Show a success message.
     * @param title Title of the success message.
     * @param message Message for success.
     */
    public void showSuccess(String title, String message)
    {
        showSuccess(title, message, -1);
    }

    /**
     * Show a self-closing success message.
     * @param title Title of the success message.
     * @param message For great success!
     * @param secondsToShow Time to show - in seconds!
     */
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
    //endregion

    //region Exit and Reset methods
    /**
     * Reset the wait window and any timers, properties, etc.
     */
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
        if (_task != null)
        {
            _task = null;
        }
    }

    /**
     * Set a custom action to execute when the window closes.
     * If you want it to happen AFTER the window closes, or just to be safe, use <code>Platform.runLater</code>
     * @param action
     */
    public void setOnClose(Runnable action)
    {
        _onClose = action;
    }

    /**
     * Redirect the GUI to a different screen when closing.
     * @param GUIScreen The fxml file to redirect to.
     */
    public void redirectOnClose(String GUIScreen)
    {
        _onClose = new Runnable() {
            @Override
            public void run() {
                CPSClientGUI.changeGUI(GUIScreen);
            }
        };
    }
    //endregion

    //region Getters and Setters
    public void setTitle(String title) {
        this.lblTitle.setText(title);
    }

    public String getTitle()
    {
        return this.lblTitle.getText();
    }
    //endregion
}
