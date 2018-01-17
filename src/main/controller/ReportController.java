package controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import entity.Employee;
import entity.Report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import static controller.Controllers.dbController;

public class ReportController {
    private static ReportController ourInstance = new ReportController();
    private Map<Integer, Report> _reportsList;

    public static ReportController getInstance() {
        return ourInstance;
    }

    private ReportController() {
    }

    /**
     * Private function used in order to convert the general obj array into report
     * and then map it into our reports list.
     * @param list - the reports list taken from the DB.
     */
    private void setReportsList(ArrayList<Object> list) {
        for (Object reportObj : list)
        {
            Report report = (Report) reportObj;
            _reportsList.put(report.getReportID(), report);
        }
    }

    public  Report getReportByID(Integer reportID){
        return this._reportsList.get(reportID);
    }

    public void printReport(Report.ReportType reportType, Integer reportManagerID) throws FileNotFoundException,
                                                                                          DocumentException,
                                                                                          SQLException
    {
        Document doc = new Document();
        //The pdf file will be created and stored in the same project folder.
        PdfWriter.getInstance(doc, new FileOutputStream("ReportPrint.pdf"));
        String reportString = generateReport(reportType,reportManagerID);
        doc.open();
        doc.add(new Paragraph(reportString));
        doc.close();

    }

    public String generateReport(Report.ReportType reportType, Integer reportManagerID) throws SQLException{
        String generatedReport = dbController.makeReportFromDB(reportType,reportManagerID);
        return generatedReport;
    }
}
