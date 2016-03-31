package com.fyber.metricsagent.googleclient;

import com.fyber.metricsagent.googleclient.utils.Utils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

public class SheetsUpdater {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Grafana Metrics";

    /**
     * The script id to run.
     */
    private static final String SCRIPT_ID = "MIyl9b3Yf8LGAm4IVK-4idqNU9xyyaxvm";

    /**
     * Directory to store user credentials for this application.
     */
    private static final File DATA_STORE_DIR = new File("../.credentials/metrics-agent-auth.json");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this class.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/metrics-agent-auth.json
     */
    private static final List<String> SCOPES = new ArrayList<>();

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }

        SCOPES.addAll(Collections.singletonList("https://www.googleapis.com/auth/drive"));
        SCOPES.addAll(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = SheetsUpdater.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Create a HttpRequestInitializer from the given one, except set
     * the HTTP read timeout to be longer than the default (to allow
     * called scripts time to execute).
     *
     * @param requestInitializer the initializer to copy and adjust; typically a Credential object.
     * @return an initializer with an extended read timeout.
     */
    private HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
        return httpRequest -> {
            requestInitializer.initialize(httpRequest);
            // This allows the API to call (and avoid timing out on) functions that take up to 6 minutes to complete (the maximum allowed script run time), plus a little overhead.
            httpRequest.setReadTimeout(380000);
        };
    }

    /**
     * Build and return an authorized Script client service.
     *
     * @return an authorized Script client service
     */
    private Script getScriptService() throws IOException {
        Credential credential = authorize();
        return new Script.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(credential))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Interpret an error response returned by the API and return a String
     * summary.
     *
     * @param op the Operation returning an error response
     * @return summary of error response, or null if Operation returned no
     * error
     */
    private String getScriptError(Operation op) {
        if (op.getError() == null) {
            return null;
        }

        // Extract the first (and only) set of error details and cast as a Map. The values of this map are the script's 'errorMessage' and
        // 'errorType', and an array of stack trace elements (which also need to be cast as Maps).
        Map<String, Object> detail = op.getError().getDetails().get(0);
        List<Map<String, Object>> stacktrace = (List<Map<String, Object>>) detail.get("scriptStackTraceElements");

        java.lang.StringBuilder sb = new StringBuilder("\nScript error message: ");
        sb.append(detail.get("errorMessage"));
        sb.append("\nScript error type: ");
        sb.append(detail.get("errorType"));

        if (stacktrace != null) {
            // There may not be a stacktrace if the script didn't start
            // executing.
            sb.append("\nScript error stacktrace:");
            for (Map<String, Object> elem : stacktrace) {
                sb.append("\n  ");
                sb.append(elem.get("function"));
                sb.append(":");
                sb.append(elem.get("lineNumber"));
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    @SneakyThrows
    public String getEndpointsToQuery(String spreadsheetIdOrUrl, String sheetName) {
        String spreadsheetId = Utils.getSpreadsheetIdFromUrl(spreadsheetIdOrUrl);
        Script service = getScriptService();

        // Create an execution request object.
        List<Object> functionParameters = new ArrayList<>();
        functionParameters.add(spreadsheetId);
        functionParameters.add(sheetName);
        ExecutionRequest request = new ExecutionRequest().setDevMode(true).setFunction("getEndpoints").setParameters(functionParameters);

        // Make the API request.
        Operation op = service.scripts().run(SCRIPT_ID, request).execute();

        if (op != null) {
            if (op.getError() != null) {
                System.out.println(getScriptError(op));
            } else {
                String endpointList = (String) (op.getResponse().get("result"));
                if (endpointList != null) {
                    System.out.println("Endpoint list obtained successfully");
                    return endpointList;
                } else {
                    System.out.println("There was an error retrieving the endpoint list. Please run the script again.");
                }
            }
        } else {
            System.out.println("Error: Operation is null");
        }

        return null;
    }

    /**
     * Update google drive spreadsheet
     *
     * @param spreadsheetIdOrUrl the id or url of the spreadsheet to update
     */
    @SneakyThrows
    public void updateSpreadsheet(String spreadsheetIdOrUrl, String statsJson) {
        String spreadsheetId = Utils.getSpreadsheetIdFromUrl(spreadsheetIdOrUrl);
        Script service = getScriptService();

        // Create an execution request object.
        List<Object> functionParameters = new ArrayList<>();
        functionParameters.add(spreadsheetId);
        functionParameters.add(statsJson);
        ExecutionRequest request = new ExecutionRequest().setDevMode(true).setFunction("updateStats").setParameters(functionParameters);

        // Make the API request.
        Operation op = service.scripts().run(SCRIPT_ID, request).execute();

        // Print results of request.
        if (op != null) {
            if (op.getError() != null) {
                // The API executed, but the script returned an error.
                System.out.println(getScriptError(op));
            } else {
                // The result provided by the API needs to be cast into the correct type, based upon what types the Apps Script function returns. Here, the function returns
                // an Apps Script Object with boolean value, so must be cast into a boolean.
//                boolean metricsFile = (boolean) (op.getResponse().get("result"));
//                if (metricsFile) {
//                    System.out.println("All rows introduced successfully");
//                } else {
//                    System.out.println("Some row was not introduced properly, you might want to run the script again.");
//                }
                System.out.println("Task finished successfully");
            }
        } else {
            System.out.println("Error: Operation is null");
        }
    }
}
