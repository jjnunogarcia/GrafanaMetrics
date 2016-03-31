package com.fyber.metricsagent;

import com.fyber.metricsagent.googleclient.SheetsUpdater;
import com.fyber.metricsagent.googleclient.exceptions.SheetsException;
import com.fyber.metricsagent.grafanaclient.GrafanaMetrics;

import lombok.SneakyThrows;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        final String spreadsheetIdOrUrl = System.getProperty("SPREADSHEET_ID_OR_URL") != null ? System.getProperty("SPREADSHEET_ID_OR_URL") : System.getenv("SPREADSHEET_ID_OR_URL");
        final String sheetName = System.getProperty("SHEET_NAME") != null ? System.getProperty("SHEET_NAME") : System.getenv("SHEET_NAME");

        if (spreadsheetIdOrUrl == null || spreadsheetIdOrUrl.isEmpty()) {
            throw new SheetsException("Spreadsheet id not found");
        }
        if (sheetName == null || sheetName.isEmpty()) {
            throw new SheetsException("Sheet name not found");
        }

        System.out.println("Spreadsheet id/url found: " + spreadsheetIdOrUrl);
        System.out.println("Sheet name found: " + sheetName);

        SheetsUpdater sheetsUpdater = new SheetsUpdater();
        String endpointsJson = sheetsUpdater.getEndpointsToQuery(spreadsheetIdOrUrl, sheetName);

        GrafanaMetrics grafanaMetrics = new GrafanaMetrics(responseJson -> sheetsUpdater.updateSpreadsheet(spreadsheetIdOrUrl, responseJson));
        grafanaMetrics.getStats(endpointsJson);
    }
}
