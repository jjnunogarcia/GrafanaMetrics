package com.fyber.metricsagent.grafanaclient.model.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StatElement {
    @SerializedName("graphName") private           String       graphName;
    @SerializedName("blockList") private           List<String> blockList;
    @SerializedName("firstDataPointList") private  List<Double> firstDataPointList;
    @SerializedName("secondDataPointList") private List<Double> secondDataPointList;

    public StatElement() {
        blockList = new ArrayList<>();
        firstDataPointList = new ArrayList<>();
        secondDataPointList = new ArrayList<>();
    }

    public void addBlock(String block) {
        blockList.add(block);
    }

    public void addFirstDataPoint(Double firstDataPoint) {
        firstDataPointList.add(firstDataPoint);
    }

    public void addSecondDataPoint(Double secondDataPoint) {
        secondDataPointList.add(secondDataPoint);
    }
}
