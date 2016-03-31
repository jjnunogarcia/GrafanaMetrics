package com.fyber.metricsagent.googleclient.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Extracts the id from a google spreadsheet with its url
     *
     * @param url The url of the google spreadsheet
     * @return the id
     */
    public static String getSpreadsheetIdFromUrl(String url) {
        Pattern regex = Pattern.compile("(?<=/d/)[^.!/]+");
        Matcher regexMatcher = regex.matcher(url);
        if (regexMatcher.find()) {
            return regexMatcher.group();
        }
        return url;
    }

}
