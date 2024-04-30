package com.leecrafts.bowmaster.util;

import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;

public class NeuralNetworkUtil {

    private static String MODEL_DIRECTORY_PATH = "/Users/wlee2019/Downloads/mod repos/skeleton bow master/src/main/java/com/leecrafts/bowmaster/util/models";
    private static String MODEL_BASE_NAME = "model";

    public static BasicNetwork createNetwork(int inputSize) {
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(inputSize);
        pattern.addHiddenLayer(32);
        pattern.setOutputNeurons(13);
        pattern.setActivationFunction(new ActivationTANH()); // output of tanh is (-1, 1)

        BasicNetwork network = (BasicNetwork)pattern.generate();
        network.reset();
        return network;
    }

    public static double[] computeOutput(BasicNetwork network, double[] input) {
        MLData data = new BasicMLData(input);
        MLData output = network.compute(data);
        return output.getData();
    }

    public static void saveModel(BasicNetwork network) {
        EncogDirectoryPersistence.saveObject(
                getFile(getNewestModelNumber()), network);
    }

    public static BasicNetwork loadModel() {
        return (BasicNetwork) EncogDirectoryPersistence.loadObject(
                getFile(getNewestModelNumber() - 1));
    }

    public static BasicNetwork loadModel(int modelNumber) {
        return (BasicNetwork) EncogDirectoryPersistence.loadObject(
                getFile(modelNumber));
    }

    private static int getNewestModelNumber() {
        int counter = 0;
        while (getFile(counter).exists()) {
            counter++;
        }
        return counter;
    }

    private static File getFile(int modelNumber) {
        return new File(String.format("%s/%s-%d.txt", MODEL_DIRECTORY_PATH, MODEL_BASE_NAME, modelNumber));
    }

}
