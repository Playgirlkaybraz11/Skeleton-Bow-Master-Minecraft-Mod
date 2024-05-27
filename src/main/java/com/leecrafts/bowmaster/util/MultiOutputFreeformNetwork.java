package com.leecrafts.bowmaster.util;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.freeform.*;

import java.util.*;

public class MultiOutputFreeformNetwork extends FreeformNetwork {

    private FreeformLayer inputLayer;
    private List<FreeformLayer> hiddenLayers;
    private List<FreeformLayer> discreteOutputLayers;
    private List<FreeformLayer> continuousOutputLayers;

    public MultiOutputFreeformNetwork() {
//        super();
        this.hiddenLayers = new ArrayList<>();
        this.discreteOutputLayers = new ArrayList<>();
        this.continuousOutputLayers = new ArrayList<>();
    }

    @Override
    public FreeformLayer createInputLayer(int neuronCount) {
        if (neuronCount < 1) {
            throw new FreeformNetworkError(
                    "Input layer must have at least one neuron.");
        }
        this.inputLayer = createLayer(neuronCount);
        return this.inputLayer;
    }

    @Override
    public FreeformLayer createLayer(int neuronCount) {
        FreeformLayer layer = super.createLayer(neuronCount);
        this.hiddenLayers.add(layer);
        return layer;
    }

    public List<FreeformLayer> getAllLayers() {
        List<FreeformLayer> list = new ArrayList<>();
        list.add(this.inputLayer);
        list.addAll(this.hiddenLayers);
        list.addAll(this.discreteOutputLayers);
        list.addAll(this.continuousOutputLayers);
        return list;
    }

    @Override
    public MLData compute(final MLData input) {
        // Set activations in the input layer
        for (int i = 0; i < input.size(); i++) {
            this.inputLayer.setActivation(i, input.getData(i));
        }

        // Propagate the input through the network layers
        propagateInput();

        // Calculate the output size based on discrete and continuous layers
        int totalOutputSize = getTotalOutputCount();  // Assume method is defined as before
        MLData result = new BasicMLData(totalOutputSize);
        int index = 0;

        // Collect outputs from discrete output layers
        for (FreeformLayer layer : this.discreteOutputLayers) {
            for (FreeformNeuron neuron : layer.getNeurons()) {
                neuron.performCalculation();
                result.setData(index++, neuron.getActivation());
            }
        }

        // Collect outputs from continuous output layers
        for (FreeformLayer layer : this.continuousOutputLayers) {
            for (FreeformNeuron neuron : layer.getNeurons()) {
                neuron.performCalculation();
                result.setData(index++, neuron.getActivation());
            }
        }

        return result;
    }

    // Method to propagate input through all layers in the network
    private void propagateInput() {
        List<FreeformLayer> layers = new ArrayList<>();
        layers.add(this.inputLayer);
        // Add hidden layers in order
        layers.addAll(this.hiddenLayers);  // Assuming you have a List<FreeformLayer> for hidden layers
        layers.addAll(this.discreteOutputLayers);
        layers.addAll(this.continuousOutputLayers);

        for (FreeformLayer layer : layers) {
            for (FreeformNeuron neuron : layer.getNeurons()) {
                neuron.performCalculation();
            }
        }
    }

    // Method to add output layers separately
    public void addDiscreteOutputLayer(int neuronCount) {
        FreeformLayer layer = this.createOutputLayer(neuronCount);
        this.discreteOutputLayers.add(layer);
    }

    public void addContinuousOutputLayer(int neuronCount) {
        FreeformLayer layer = this.createOutputLayer(neuronCount);
        this.continuousOutputLayers.add(layer);
    }

    // You'll need to handle connections from the last hidden layer to multiple output layers
    public void connectToDiscreteOutputLayers(FreeformLayer lastLayer, ActivationFunction af) {
        for (FreeformLayer layer : this.discreteOutputLayers) {
            this.connectLayers(lastLayer, layer, af, 1.0, false);
        }
    }

    public void connectToContinuousOutputLayers(FreeformLayer lastLayer, ActivationFunction af) {
        for (FreeformLayer layer : this.continuousOutputLayers) {
            this.connectLayers(lastLayer, layer, af, 1.0, false);
        }
    }

    // Additional helper methods as necessary...
    private int getTotalOutputCount() {
        int totalOutputCount = 0;

        // Sum up all the neurons in the discrete output layers
        for (FreeformLayer layer : this.discreteOutputLayers) {
            totalOutputCount += layer.sizeNonBias(); // Assuming 'size()' method returns the number of neurons in the layer
        }

        // Sum up all the neurons in the continuous output layers
        for (FreeformLayer layer : this.continuousOutputLayers) {
            totalOutputCount += layer.sizeNonBias(); // Same assumption as above
        }

        return totalOutputCount;
    }

}
