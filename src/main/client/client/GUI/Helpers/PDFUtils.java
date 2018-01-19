package client.GUI.Helpers;

import client.GUI.CPSClientGUI;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFUtils {
    public static void createPDF(String content, boolean showAfter)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document", "*.pdf"));
        File file = fileChooser.showSaveDialog(CPSClientGUI.getPrimaryStage());
        if (file == null)
            return;
        Document doc = new Document();
        //The pdf file will be created and stored in the same project folder.
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            String reportString = content;
            doc.open();
            doc.add(new Paragraph(reportString));
            if (showAfter) {
                Desktop.getDesktop().open(file);
            }
            else
            {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setHeaderText("File Saved!");
                success.showAndWait();
            }
        } catch (FileNotFoundException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("The file you were trying to create was somehow not found?\nFull error was:" + e.getMessage());
            errorAlert.showAndWait();
            if (CPSClientGUI.IS_DEBUG)
            {
                e.printStackTrace();
            }
        } catch (DocumentException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("There was an error creating your PDF document.\n" + e.getMessage());
            errorAlert.showAndWait();
            if (CPSClientGUI.IS_DEBUG)
            {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            doc.close();
        }
    }
}
