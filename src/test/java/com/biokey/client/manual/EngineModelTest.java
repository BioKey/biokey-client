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
import org.nd4j.linalg.factory.Nd4j;
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
                Integer i1 = Integer.parseInt((String) o1);
                Integer i2 = Integer.parseInt((String) o2);
                return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
            }
        });
        return sortedKeys;
    }

    public static void main(String[] args) throws Exception {

        // Load in Computation Graph
        String modelJsonFilename = "/Users/connorgiles/Downloads/keras-model-test/ensemble_model.json";
        String weightsHdf5Filename = "/Users/connorgiles/Downloads/keras-model-test/ensemble_weights.h5";
        ComputationGraph model = KerasModelImport.importKerasModelAndWeights(modelJsonFilename, weightsHdf5Filename);

        System.out.println(model.getNumInputArrays());

        try {
            // Read in JSON Files
            JSONParser parser = new JSONParser();
            JSONObject gaussian = (JSONObject)parser.parse(new FileReader("/Users/connorgiles/Downloads/keras-model-test/test-gaussian.json"));
            JSONObject xRaw = (JSONObject)parser.parse(new FileReader("/Users/connorgiles/Downloads/keras-model-test/raw_frames_test.json"));
            JSONObject x40 = (JSONObject)parser.parse(new FileReader("/Users/connorgiles/Downloads/keras-model-test/windowed_frames_40_test.json"));
            JSONObject x100 = (JSONObject)parser.parse(new FileReader("/Users/connorgiles/Downloads/keras-model-test/windowed_frames_100_test.json"));

            // Add feature index to gaussian profile
            List<Object> features = new ArrayList<Object>();
            features.addAll(gaussian.keySet());
            Collections.sort(features, (o1, o2) -> ((String)o1).compareTo((String)o2));
            for(int i = 0; i < features.size(); i++) {
                ((JSONObject)gaussian.get(features.get(i))).put("index", i);
            }

            // Create input arrays
            int[] arrayShape = {xRaw.keySet().size(), gaussian.keySet().size()};
            INDArray x_raw = Nd4j.zeros(arrayShape);
            INDArray x_40 = Nd4j.zeros(arrayShape).add(0.5);
            INDArray x_100 = Nd4j.zeros(arrayShape).add(0.5);

            // Populate Arrays
            for (Object i : xRaw.keySet()) {
                JSONObject frame = (JSONObject) xRaw.get(i);
                JSONArray xArray =((JSONArray)frame.get("x"));
                for(int j = 0; j < xArray.size(); j++) {
                    x_raw.putScalar(Integer.parseInt((String)i), j, (double)xArray.get(j));
                }

            }

            for (Object i : x40.keySet()) {
                JSONObject frame = (JSONObject) x40.get(i);
                JSONObject x = (JSONObject)frame.get("x");
                for (Object k : x.keySet()) {
                    JSONObject stats = (JSONObject)gaussian.get(k);
                    int timeStep = Integer.parseInt((String)i);
                    int fIndex = (int)stats.get("index");
                    x_40.putScalar(timeStep, fIndex, (double)((JSONObject)x.get(k)).get("mean"));
                }
            }

            for (Object i : x100.keySet()) {
                JSONObject frame = (JSONObject) x100.get(i);
                JSONObject x = (JSONObject)frame.get("x");
                for (Object k : x.keySet()) {
                    JSONObject stats = (JSONObject)gaussian.get(k);
                    int timeStep = Integer.parseInt((String)i);
                    int fIndex = (int)stats.get("index");
                    x_100.putScalar(timeStep, fIndex, (double)((JSONObject)x.get(k)).get("mean"));
                }
            }

            System.out.println(model.getConfiguration().getNetworkInputs());

            int errors = 0;
            int num = 10000;
            for (int i = 0; i < num; i++) {
                model.rnnClearPreviousState();
                INDArray[] inputs = {x_100.getRow(i), x_40.getRow(i), x_raw.getRow(i)};
                Object actual = ((JSONObject) xRaw.get(i + "")).get("y");
                double pred = model.rnnTimeStep(inputs)[0].getDouble(0);

                double thresh = 0.5;
                int predicted = 0;
                if (pred > thresh) predicted = 1;

                System.out.println(i+ "\t" + pred  + "\t" + actual);

                if (predicted != ((Long)actual).intValue()) errors++;

            }

            System.out.println(1-(1.0*errors)/num);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}