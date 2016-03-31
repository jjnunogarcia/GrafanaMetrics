package com.fyber.metricsagent.grafanaclient.model.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class QueryBlock {
    @SerializedName("graphName") private String       graphName;
    @SerializedName("blockList") private List<String> blockList;
    @SerializedName("urlList") private   List<String> urlList;
}
