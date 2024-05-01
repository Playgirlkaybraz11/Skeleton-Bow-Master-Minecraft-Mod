package com.leecrafts.bowmaster.util;

import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeuralNetworkUtil {

    private static final String MODEL_DIRECTORY_PATH = "/Users/wlee2019/Downloads/mod repos/skeleton bow master/src/main/java/com/leecrafts/bowmaster/util/models";
//    private static final String MODEL_DIRECTORY_PATH = "src/main/java/com/leecrafts/bowmaster/util/models";
    private static final String MODEL_BASE_NAME = "model";
    private static final int INPUT_SIZE = 9;
    private static final int OUTPUT_SIZE = 13;

    public static BasicNetwork createNetwork() {
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(INPUT_SIZE);
        pattern.addHiddenLayer(32);
        pattern.setOutputNeurons(OUTPUT_SIZE);
        pattern.setActivationFunction(new ActivationTANH()); // output of tanh is (-1, 1)

        BasicNetwork network = (BasicNetwork) pattern.generate();
        network.reset();
        return network;
    }

    public static double[] computeOutput(BasicNetwork network, double[] input) {
        MLData data = new BasicMLData(input);
        MLData output = network.compute(data);
        return output.getData();
    }

    public static void saveModel(BasicNetwork network) {
        EncogDirectoryPersistence.saveObject(file(getNewestModelNumber() + 1), network);
    }

    public static BasicNetwork loadOrCreateModel() {
        return loadOrCreateModel(getNewestModelNumber());
    }

    public static BasicNetwork loadOrCreateModel(int modelNumber) {
        // TODO what if there are no models in the directory?
        File file = file(modelNumber);
        if (file.exists()) {
            return (BasicNetwork) EncogDirectoryPersistence.loadObject(file);
        }
        else {
            return createNetwork();
        }
    }

    private static int getNewestModelNumber() {
        File[] files = new File(MODEL_DIRECTORY_PATH).listFiles();  // Get all files in the directory
        int maxNum = -1;
        if (files == null) {
            return maxNum;
        }

        Pattern pattern = Pattern.compile(String.format("^%s-(\\d+)\\.eg$", MODEL_BASE_NAME));  // Regex to find files
        for (File file : files) {
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    maxNum = Math.max(maxNum, Integer.parseInt(matcher.group(1)));
                }
            }
        }

        return maxNum;
    }

    private static File file(int modelNumber) {
        return new File(String.format("%s/%s-%d.eg", MODEL_DIRECTORY_PATH, MODEL_BASE_NAME, modelNumber));
    }

    public static void printWeights(BasicNetwork network) {
        for (int layer = 0; layer < network.getLayerCount() - 1; layer++) {
            int fromCount = network.getLayerTotalNeuronCount(layer);
            int toCount = network.getLayerNeuronCount(layer + 1);

            System.out.println("Weights from Layer " + layer + " to Layer " + (layer + 1) + ":");
            for (int fromNeuron = 0; fromNeuron < fromCount; fromNeuron++) {
                for (int toNeuron = 0; toNeuron < toCount; toNeuron++) {
                    double weight = network.getWeight(layer, fromNeuron, toNeuron);
                    System.out.println("Weight[" + fromNeuron + "][" + toNeuron + "]: " + weight);
                }
            }
        }
    }

}
