package com.leecrafts.bowmaster.util;

import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeuralNetworkUtil {

    // TODO do not use absolute path
    private static final String MODEL_DIRECTORY_PATH = "/Users/wlee2019/Downloads/mod repos/skeleton bow master/src/main/java/com/leecrafts/bowmaster/util/models";
//    private static final String MODEL_DIRECTORY_PATH = "src/main/java/com/leecrafts/bowmaster/util/models";
    private static final String MODEL_BASE_NAME = "model";
    private static final int INPUT_SIZE = 9;
    private static final int OUTPUT_SIZE = 10;

    public static BasicNetwork createNetwork() {
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(INPUT_SIZE);
        pattern.addHiddenLayer(32); // TODO add more layers?
        pattern.setOutputNeurons(OUTPUT_SIZE);
        pattern.setActivationFunction(new ActivationSoftMax()); // output of softmax is (0, 1)

        BasicNetwork network = (BasicNetwork) pattern.generate();
        network.reset();
        return network;
    }

    public static double[] computeOutput(BasicNetwork network, double[] input) {
        MLData data = new BasicMLData(input);
        MLData output = network.compute(data);
        return output.getData();
    }

    public static void updateNetwork(
            BasicNetwork network,
            ArrayList<double[]> states,
            ArrayList<double[]> actionsLogProbs,
            ArrayList<Double> rewards,
            double learningRate,
            double gamma) {
        // cumulative rewards if not already provided
        double[] cumulativeRewards = new double[rewards.size()];
        double cumulative = 0;
        for (int i = rewards.size() - 1; i >= 0; i--) {
            cumulative = rewards.get(i) + cumulative * gamma;
            cumulativeRewards[i] = cumulative;
        }

        // Initialize gradient storage for each weight
        double[] weightGradients = new double[network.getFlat().getWeights().length];

        // Process each experience tuple
        for (int t = 0; t < states.size(); t++) {
            double[] state = states.get(t);
            double[] logProbs = actionsLogProbs.get(t);
            double reward = cumulativeRewards[t];

            // Forward pass to compute outputs
            double[] outputArray = computeOutput(network, state);

            // Calculate policy gradients for the output layer
            double[] outputGradients = new double[outputArray.length];
            for (int i = 0; i < outputArray.length; i++) {
                outputGradients[i] = reward * (Math.exp(logProbs[i]) - outputArray[i]);
            }

            // Backpropagate the error
            for (int layer = network.getLayerCount() - 1; layer > 0; layer--) {
                int layerSize = network.getLayerNeuronCount(layer);
                int previousLayerSize = network.getLayerNeuronCount(layer - 1);

                double[] newDeltas = new double[previousLayerSize];

                int weightIndex = 0;
                for (int l = 1; l < layer; l++) {
                    weightIndex += network.getLayerNeuronCount(l - 1) * network.getLayerNeuronCount(l);
                }
                for (int j = 0; j < previousLayerSize; j++) {
                    for (int k = 0; k < layerSize; k++) {
                        weightGradients[weightIndex] += outputGradients[k] * network.getLayerOutput(layer - 1, j);
                        newDeltas[j] += outputGradients[k] * network.getFlat().getWeights()[weightIndex];
                        weightIndex++;
                    }
                }
                outputGradients = newDeltas;
            }
        }

        // Update weights using the gradients
        double[] weights = network.getFlat().getWeights();
        for (int i = 0; i < weights.length; i++) {
            weights[i] -= learningRate * weightGradients[i]; // Apply gradient descent
        }
    }

    public static void saveModel(BasicNetwork network) {
        EncogDirectoryPersistence.saveObject(file(getNewestModelNumber() + 1), network);
    }

    public static BasicNetwork loadOrCreateModel() {
        return loadOrCreateModel(getNewestModelNumber());
    }

    public static BasicNetwork loadOrCreateModel(int modelNumber) {
        File file = file(modelNumber);
        if (file.exists()) {
            System.out.println("existing model found (" + modelNumber + ")");
            return (BasicNetwork) EncogDirectoryPersistence.loadObject(file);
        }
        else {
            System.out.println("no model found, so creating new one");
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
