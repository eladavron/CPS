package client.GUI.Controls;

import client.GUI.Helpers.ErrorHandlers;
import client.GUI.Helpers.ReportUtils;
import entity.FinalReport;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * Represents a single Report in teh ListView of the Company Manager's {@link client.GUI.Forms.Employees.ManageAllReports} view.
 */
public class ReportCell extends ListCell<FinalReport>{

    @FXML
    private Button btnShow;

    @FXML
    private Button savePDF;

    @FXML
    private Label lblText;

    @FXML
    private AnchorPane paneRow;

    /**
     * Default Constructor.
     * Loads the FXML for the body of the row.
     */
    public ReportCell()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportCell.fxml"));
            loader.setController(this);
            loader.load();
        } catch(IOException io)
        {
            ErrorHandlers.GUIError(io, false);
        }

    }

    /**
     * Pops up a window displaying the report body in full (with an option to save as PDF).
     * @param event The Mouse Click event.
     */
    @FXML
    void showFullReport(ActionEvent event) {
        ReportUtils.showReportPopup(getItem().getFullContent(),getItem().getShortDescription());
    }

    /**
     * Saves the report as a PDF.
     * @param event The Mouse Click event.
     */
    @FXML
    void saveToPDF(ActionEvent event) {
        ReportUtils.createPDF(getItem().getFullContent(),true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateItem(FinalReport item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            lblText.setText("Report #" + item.getId() + "\n" + item.getShortDescription() + "\nCreated: " + item.getTimeCreated());
            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() == 2) //Double Click
                        ReportUtils.showReportPopup(getItem().getFullContent(), getItem().getShortDescription());
                }
            });
            setGraphic(paneRow);
        }
    }
}
