package client.GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import javax.lang.model.element.Element;
import java.io.IOException;

public class LoginScreenController {

    private CPSClientGUI parentGUI;

    @FXML
    private TextField txtLoginUsr;

    @FXML
    private Button btnLogin;

    @FXML
    private TitledPane paneLogin;

    @FXML
    private Accordion loginRoot;

    @FXML
    private TitledPane paneRegister;

    @FXML
    private PasswordField txtLoginPwd;


    @FXML
    void initialize() {
        assert paneLogin != null : "fx:id=\"paneLogin\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert loginRoot != null : "fx:id=\"loginRoot\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        assert paneRegister != null : "fx:id=\"paneRegister\" was not injected: check your FXML file 'LoginScreen.fxml'.";
        loginRoot.setExpandedPane(paneLogin);
    }

    @FXML
    void attemptLogin(ActionEvent event) throws IOException {
        //TODO: Attempt Login
        if (!loginValidation())
            return;
        CPSClientGUI.getInstance().changeGUI("CustomerScreen.fxml");
    }

    /**
     * Validates the login form.
     * Also pops up a tooltip next the offending field.
     * @return True if valid, false otherwise.
     */
    boolean loginValidation()
    {
        boolean finalResult = true;
        if (txtLoginUsr.getText().length() < 4)
        {
           Helpers.showTooltip(txtLoginUsr, "User ID must be at least 4 characters long!");
           finalResult = false;
        }
        if (txtLoginPwd.getText().length() < 4)
        {
            Helpers.showTooltip(txtLoginPwd, "Password must be at least 4 characters long!");
            finalResult = false;
        }
        return finalResult;
    }

}
