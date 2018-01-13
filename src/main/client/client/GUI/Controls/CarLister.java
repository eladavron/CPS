package client.GUI.Controls;

import client.GUI.Helpers.Validation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;

public class CarLister{

    private FlowPane _root;

    public CarLister(FlowPane root) {
        _root = root;
        _root.setHgap(10);
        _root.setOrientation(Orientation.VERTICAL);
        this.addCarField();
    }

    /**
     * Adds an "Enter Car" textfield to the form.
     * Also called upon when a car number is entered to allow adding others.
     */
    private void addCarField()
    {
        TextField newCarID = new TextField();
        if (_root.getChildren().size() > 0) //If _root is not the first field.
            newCarID.setPromptText("Add another...");
        newCarID.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue)
                {
                    if (!validateCarTextField(newCarID))
                        newCarID.requestFocus();
                }
            }
        });
        newCarID.setOnAction(value -> _root.requestFocus());
        _root.getChildren().add(newCarID);
    }

    public void addCar(Integer newNumber)
    {
        TextField carField = (TextField) _root.getChildren().get(_root.getChildren().size() - 1);
        carField.setText(newNumber.toString());
        validateCarTextField(carField);
    }

    /**
     * Validates a car textfield.
     * If valid, asks to add another to the form.
     * If empty, deletes the field (unless it's the last one, in which case it just empties it).
     * @param txtCar TextField to validate.
     * @return True if everything is valid, false otherwise.
     */
    private boolean validateCarTextField(TextField txtCar)
    {
        if (txtCar.getText().matches("^\\s*$")) //If new value is blank
        {
            if (_root.getChildren().indexOf(txtCar) != _root.getChildren().size() - 1) //If not the last one.
                _root.getChildren().remove(txtCar);
        }
        else if (!Validation.carNumber(txtCar.getText())) { //If it's not a valid car number
            Validation.showError(txtCar, "Please enter a valid car registration number!");
            return false;
        }
        else if (_root.getChildren().indexOf(txtCar) == _root.getChildren().size() - 1) //Valid, not last, not blank.
        {
            addCarField();
        }
        Validation.removeHighlight(txtCar);
        return true;
    }

    public ArrayList<Integer> getAllNumbers()
    {
        ArrayList<Integer> carList = new ArrayList<Integer>();
        for (Node carText: _root.getChildren())
        {
            if (carText instanceof TextField && !((TextField) carText).getText().isEmpty() && Validation.carNumber(((TextField) carText).getText()))
            {
                carList.add(Integer.valueOf(((TextField) carText).getText()));
            }
        }
        return carList;
    }

    public void setAllNumbers(Integer...numbers)
    {
        _root.getChildren().clear();
        for (Integer number : numbers)
        {
            this.addCar(number);
        }
    }
}
