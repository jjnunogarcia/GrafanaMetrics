package com.fyber.metricsagent.grafanaclient.api;

import com.fyber.metricsagent.grafanaclient.model.gson.ParsedStatElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Defines the Grafana API endpoints
 */
public interface GrafanaService {
    @GET("/render")
    Call<List<ParsedStatElement>> getData(@Query("from") String from, @Query("until") String until, @Query("target") String target, @Query("format") String format);
}
