package com.leecrafts.bowmaster.util;

import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;

public class NeuralNetworkUtil {

    public static BasicNetwork createNetwork(int inputSize, int outputSize) {
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(inputSize);
        pattern.addHiddenLayer(16);
        pattern.setOutputNeurons(outputSize);
        pattern.setActivationFunction(new ActivationTANH());

        BasicNetwork network = (BasicNetwork)pattern.generate();
        network.reset();
        return network;
    }

    public static double[] computeOutput(BasicNetwork network, double[] input) {
        MLData data = new BasicMLData(input);
        MLData output = network.compute(data);
        return output.getData();
    }

}
