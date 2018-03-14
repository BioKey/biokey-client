package com.biokey.client.manual;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineModelTest {

    private static final Logger log = LoggerFactory.getLogger(EngineModelTest.class);

    public static void main(String[] args) throws Exception {
        String modelJsonFilename = "/Users/connorgiles/Downloads/keras-model-test/ensemble_model.json";
        String weightsHdf5Filename = "/Users/connorgiles/Downloads/keras-model-test/ensemble_weights.h5";
        // String modelHdf5Filename = "PATH TO EXPORTED FULL MODEL HDF5 ARCHIVE";
        boolean enforceTrainingConfig = false;  //Controls whether unsupported training-related configs
        //will throw an exception or just generate a warning.

        /* Import VGG 16 model from separate model config JSON and weights HDF5 files.
         * Will not include loss layer or training configuration.
         */
        // Static helper from KerasModelImport
        ComputationGraph model = KerasModelImport.importKerasModelAndWeights(modelJsonFilename, weightsHdf5Filename);
        System.out.println(model.layerSize(0));

    }
}