package controller;

import entity.FinalReport;
import entity.Report;

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

    public String generateReport(Report.ReportType reportType, Integer reportManagerID, Integer parkingLotID) throws SQLException{
        return dbController.makeReportFromDB(reportType,reportManagerID, parkingLotID);
    }

    public ArrayList<FinalReport> getAllReports() throws SQLException {
        return dbController.queryAllReports();
    }
}
