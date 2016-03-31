package com.fyber.metricsagent.grafanaclient.parsers;

import com.fyber.metricsagent.grafanaclient.api.GrafanaService;
import com.fyber.metricsagent.grafanaclient.model.gson.ParsedStatElement;
import com.fyber.metricsagent.grafanaclient.model.gson.QueryBlock;
import com.fyber.metricsagent.grafanaclient.model.gson.StatElement;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Response;

public class StatDataManager {
    public static final String FROM   = "from";
    public static final String UNTIL  = "until";
    public static final String TARGET = "target";
    public static final String FORMAT = "format";

    private final GrafanaService grafanaService;

    public StatDataManager(GrafanaService grafanaService) {
        this.grafanaService = grafanaService;
    }

    public StatElement getData(QueryBlock queryBlock) {
        StatElement statElement = new StatElement();
        statElement.setGraphName(queryBlock.getGraphName());
        statElement.setBlockList(queryBlock.getBlockList());

        queryBlock.getUrlList().forEach(url -> {
            Map<String, String> queryParams = splitQuery(url);
            Call<List<ParsedStatElement>> issueData = grafanaService.getData(queryParams.get(FROM), queryParams.get(UNTIL), queryParams.get(TARGET), queryParams.get(FORMAT));
            try {
                parseResponse(issueData.execute(), statElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("StatElement: " + statElement);
        return statElement;
    }

    private void parseResponse(Response<List<ParsedStatElement>> response, StatElement statElement) {
        if (response.isSuccess()) {
            System.out.println("Response successful!");
            if (!response.body().isEmpty()) {
                // it is a list, but it has only one element
                ParsedStatElement parsedStatElement = response.body().get(0);

                // we are only interested on the first and last elements from the list
                List<List<Double>> dataPoints = parsedStatElement.getDataPoints();

                if (!dataPoints.isEmpty()) {
                    List<Double> firstValue = dataPoints.get(0);
                    List<Double> lastValue = dataPoints.get(dataPoints.size() - 1);

                    // from the first and last pairs, take only first element
                    if (!firstValue.isEmpty() && !lastValue.isEmpty()) {
                        Double firstDataPoint = firstValue.get(0);
                        statElement.addFirstDataPoint(firstDataPoint != null ? firstDataPoint : 0);
                        Double secondDataPoint = lastValue.get(0);
                        statElement.addSecondDataPoint(secondDataPoint != null ? secondDataPoint : 0);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private Map<String, String> splitQuery(String url) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String[] pairs = url.split("&");
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            if (index != -1) {
                queryPairs.put(URLDecoder.decode(pair.substring(0, index), "UTF-8"), URLDecoder.decode(pair.substring(index + 1), "UTF-8"));
            }
        }
        return queryPairs;
    }
}
