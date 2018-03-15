package com.biokey.client.manual;

import java.util.*;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.modelimport.keras.KerasModel;
import org.deeplearning4j.nn.modelimport.keras.KerasModel.*;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class EngineModelTest {

    private static final Logger log = LoggerFactory.getLogger(EngineModelTest.class);

    public static List<Integer> getSortedKeys(JSONObject obj) {
        List<Integer> sortedKeys = new ArrayList<Integer>(obj.keySet().size());
        sortedKeys.addAll(obj.keySet());
        Collections.sort(sortedKeys, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                Integer i1 = Integer.parseInt((String)o1);
                Integer i2 = Integer.parseInt((String)o2);
                return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
            }
        });
        return sortedKeys;
    }

    public static void main(String[] args) throws Exception {
        String modelJsonFilename = "/Users/connorgiles/Downloads/keras-model-test/ensemble_model.json";
        String weightsHdf5Filename = "/Users/connorgiles/Downloads/keras-model-test/ensemble_weights.h5";

        ComputationGraph model = KerasModelImport.importKerasModelAndWeights(modelJsonFilename, weightsHdf5Filename);
        System.out.println(model.layerSize(0));


        JSONParser parser = new JSONParser();


        try {
            Object raw = parser.parse(new FileReader("/Users/connorgiles/Downloads/keras-model-test/windowed_frames_40_test.json"));
            JSONObject rawJSON = (JSONObject) raw;
            System.out.println(getSortedKeys(rawJSON));

            System.out.println((((JSONObject)rawJSON.get("40"))));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}