package com.leecrafts.bowmaster.util.unittest;

import com.leecrafts.bowmaster.util.MultiOutputFreeformNetwork;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.freeform.FreeformConnection;
import org.encog.neural.freeform.FreeformLayer;
import org.encog.neural.freeform.FreeformNeuron;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;

import java.util.ArrayList;
import java.util.Random;

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
    private static MultiOutputFreeformNetwork createToyMultiOutputFreeformNetwork() {
        MultiOutputFreeformNetwork network = new MultiOutputFreeformNetwork();

        FreeformLayer inputLayer = network.createInputLayer(3);

        FreeformLayer hiddenLayer1 = network.addHiddenLayer(2);

        network.connectLayers(inputLayer, hiddenLayer1, new ActivationTANH(), 1.0, false);

        network.addContinuousOutputLayer(2); // Turn Head Action
        network.addDiscreteOutputLayer(3); // Strafe (left/right) Action
        network.addDiscreteOutputLayer(2); // Jump Action

        network.connectToContinuousOutputLayers(hiddenLayer1, new ActivationTANH()); // For continuous
        network.connectToDiscreteOutputLayers(hiddenLayer1, new ActivationSoftMax()); // For discrete

        Random r = new Random();
        for (FreeformNeuron f : inputLayer.getNeurons()) {
            for (FreeformConnection c : f.getOutputs()) {
                c.setWeight(r.nextFloat());
            }
        }
//        network.reset();
        return network;
    }

    private static void test1() {
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

    private static void test2() {
        MultiOutputFreeformNetwork network = createToyMultiOutputFreeformNetwork();
        double[] observations = new double[] {-.5, 0.7, 1};
        double[] outputs = NeuralNetworkUtil.computeOutput(network, observations);

        NeuralNetworkUtil.printWeights(network);
        for (int i = 0; i < outputs.length; i++) {
            System.out.println(outputs[i]);
        }
    }

    public static void main(String[] args) {
        System.out.println("Running unit tests to verify correctness of REINFORCE algorithm");
//        test1();
        test2();
    }

}
