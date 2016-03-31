# GrafanaMetrics
Updates metrics for grafana

Create a `gradle.properties` file in the root folder and add:

    spreadsheetIdOrUrl=<spreadsheet url/id>
    sheetName=<sheet name within the spreadsheet>

The `spreadsheetIdOrUrl` should look like `https://docs.google.com/spreadsheets/d/91aTj8gv6dEk6wG2SO7-yR7ygvhKYHWL7yx0xIwo8pEe/edit` or `91aTj8gv6dEk6wG2SO7-yR7ygvhKYHWL7yx0xIwo8pEe`.
The `sheetName` should look like `Sheet1`.

Remember to enable the Google Apps Script Execution API as described here:

    https://developers.google.com/apps-script/guides/rest/quickstart/java

The link to the Google Apps Script is the following:

    https://script.google.com/a/fyber.com/d/1ArAZOjFnGNvxSpCslkk-evOwzEtoMvuzFpV_oYXTmEH5us4B1wXxSycF/edit?usp=drive_web
