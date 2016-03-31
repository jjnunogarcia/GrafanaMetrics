package com.fyber.metricsagent.grafanaclient.model.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ParsedStatElement {
    @SerializedName("target") private     String             self;
    @SerializedName("datapoints") private List<List<Double>> dataPoints;
}
