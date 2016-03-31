package com.fyber.metricsagent.grafanaclient;

import com.fyber.metricsagent.grafanaclient.api.GrafanaService;
import com.fyber.metricsagent.grafanaclient.model.gson.QueryBlock;
import com.fyber.metricsagent.grafanaclient.model.gson.StatElement;
import com.fyber.metricsagent.grafanaclient.parsers.StatDataManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GrafanaMetrics {
    public static final String API_BASE_URL = "http://grafana.prd.fyber.com";

    private GrafanaService       grafanaService;
    private OkHttpClient.Builder httpClient;
    private Retrofit.Builder     builder;
    private StatDataManager      statDataManager;
    private Consumer<String>     consumer;

    public GrafanaMetrics(Consumer<String> consumer) {
        this.consumer = consumer;
        httpClient = new OkHttpClient.Builder();
        builder = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());
        grafanaService = createService(GrafanaService.class);
        statDataManager = new StatDataManager(grafanaService);
        System.out.println("Grafana Metrics started");
    }

    private <S> S createService(Class<S> serviceClass) {
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    @SneakyThrows
    public void getStats(String endpointsJson) {
        List<StatElement> statElementList = new ArrayList<>();
        List<QueryBlock> queryBlockList = deserializeEndpointsJson(endpointsJson);
        CountDownLatch countDownLatch = new CountDownLatch(queryBlockList.size());

        queryBlockList.forEach(queryBlock -> {
            System.out.println("Endpoint: " + queryBlock);
            StatElement statElement = statDataManager.getData(queryBlock);
            if (!statElement.getBlockList().isEmpty() && !statElement.getFirstDataPointList().isEmpty() && !statElement.getSecondDataPointList().isEmpty()) {
                statElementList.add(statElement);
            } else {
                System.out.println("Not adding anything for graph " + statElement.getGraphName());
            }
            countDownLatch.countDown();
        });

        countDownLatch.await();
        if (consumer != null) {
            Gson gson = new Gson();
            consumer.accept(gson.toJson(statElementList));
        }
    }

    private List<QueryBlock> deserializeEndpointsJson(String endpointsJson) {
        Type listType = new TypeToken<ArrayList<QueryBlock>>() {}.getType();
        return new Gson().fromJson(endpointsJson, listType);
    }
}
