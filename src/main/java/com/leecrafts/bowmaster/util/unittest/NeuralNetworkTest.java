package com.leecrafts.bowmaster.util.unittest;

import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;

import java.util.ArrayList;

public class NeuralNetworkTest {

    private static BasicNetwork createToyNetwork() {
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(2);
        pattern.addHiddenLayer(3);
        pattern.setOutputNeurons(2);
        pattern.setActivationFunction(new ActivationSoftMax()); // output of softmax is (0, 1)

        BasicNetwork network = (BasicNetwork) pattern.generate();
//        network.reset();
        double[] weights = network.getFlat().getWeights();
        double[] weightsToAssign = {0.4,1.8,3.2,0.8,0.8,2.6,2.8,1.6,1.5,2.5,1.3,1.1,0.6,0.6,2.0,0.5,0.5};
        System.arraycopy(weightsToAssign, 0, weights, 0, weights.length);
        return network;
    }

    private static SpookyNetwork createSpookyToyNetwork() {
//        FreeformNetwork network = new FreeformNetwork();
//        FreeformLayer inputLayer = network.createInputLayer(2);
//        FreeformLayer hiddenLayer = network.createLayer(2);
//        network.ConnectLayers(inputLayer, hiddenLayer, new ActivationTANH());
//        FreeformLayer lookDirectionLayer = network.createLayer(2); // tanh
//        network.ConnectLayers(hiddenLayer, lookDirectionLayer, new ActivationTANH());
//        FreeformLayer rightClickLayer = network.createLayer(2); // softmax
//        network.connectLayers(hiddenLayer, rightClickLayer, new ActivationSoftMax(), 0, false);
//        network.reset();

        return new SpookyNetwork();
    }

    public static void test1() {
        BasicNetwork network = createToyNetwork();
        ArrayList<double[]> states = new ArrayList<>();
        states.add(new double[] {-2, 1});
        states.add(new double[] {3, 5});
        ArrayList<double[]> actions = new ArrayList<>(); // log probabilities
        actions.add(new double[] {-0.2, -1.6});
        actions.add(new double[] {-0.1, -0.7});
        ArrayList<Double> rewards = new ArrayList<>();
        rewards.add(2.0);
        rewards.add(-1.0);
        double learningRate = 0.1;
        double gamma = 0.75;
        System.out.println(network.dumpWeightsVerbose());
        System.out.println(network.dumpWeights());
    }

    public static void test2() {
        SpookyNetwork network = createSpookyToyNetwork();
        double[] input = {0.2, -0.7};
        MLData inputData = new BasicMLData(input);
        MLData outputData = network.computeAllOutputs(inputData);
        System.out.println(outputData.size());
        for (double d: outputData.getData()) {
            System.out.println(d);
        }
        System.out.println("**");
        double[] lookDirectionOutput = new double[2];
        double[] rightClickOutput = new double[2];
        System.arraycopy(outputData.getData(), 0, lookDirectionOutput, 0, 2);
        System.arraycopy(outputData.getData(), 2, rightClickOutput, 0, 2);
        for (double d: lookDirectionOutput) {
            System.out.println(d);
        }
        for (double d: rightClickOutput) {
            System.out.println(d);
        }
    }

    public static void main(String[] args) {
        System.out.println("Running unit tests to verify correctness of REINFORCE algorithm");
//        test1();
        test2();
    }

}
