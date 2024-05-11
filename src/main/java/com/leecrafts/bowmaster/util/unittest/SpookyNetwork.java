package com.leecrafts.bowmaster.util.unittest;

import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.freeform.FreeformLayer;
import org.encog.neural.freeform.FreeformNetwork;
import org.encog.neural.freeform.FreeformNeuron;

// I created this class just so that outputs can be calculated for multiple output layers
// FreeformNetwork can only store one output layer, so I had to take matters into my own hands
public class SpookyNetwork {

    private final FreeformNetwork network;
    private final FreeformLayer inputLayer;
    private final FreeformLayer lookDirectionLayer;
    private final FreeformLayer rightClickLayer;

    public SpookyNetwork() {
        this.network = new FreeformNetwork();
        this.inputLayer = this.network.createInputLayer(2);

        FreeformLayer hiddenLayer = this.network.createLayer(2);
        this.network.ConnectLayers(this.inputLayer, hiddenLayer, new ActivationTANH());

        this.lookDirectionLayer = this.network.createOutputLayer(2); // tanh
        this.network.ConnectLayers(hiddenLayer, this.lookDirectionLayer, new ActivationTANH());

        this.rightClickLayer = this.network.createOutputLayer(2); // softmax
        this.network.connectLayers(hiddenLayer, this.rightClickLayer, new ActivationSoftMax(), 0, false);

        this.network.reset();
    }

    public FreeformNetwork getNetwork() {
        return this.network;
    }

    public FreeformLayer getInputLayer() {
        return this.inputLayer;
    }

    public FreeformLayer getLookDirectionLayer() {
        return this.lookDirectionLayer;
    }

    public FreeformLayer getRightClickLayer() {
        return this.rightClickLayer;
    }

    // specialized method for calculating outputs from multiple output layers
    public MLData computeAllOutputs(MLData input) {
        // Allocate result
        final MLData result = new BasicMLData(4);

        // Copy the input
        for (int i = 0; i < input.size(); i++) {
            this.inputLayer.setActivation(i, input.getData(i));
        }

        for (int i = 0; i < this.lookDirectionLayer.size(); i++) {
            final FreeformNeuron outputNeuron = this.lookDirectionLayer.getNeurons()
                    .get(i);
            outputNeuron.performCalculation();
            result.setData(i, outputNeuron.getActivation());
        }
        for (int i = 0; i < this.rightClickLayer.size(); i++) {
            final FreeformNeuron outputNeuron = this.rightClickLayer.getNeurons()
                    .get(i);
            outputNeuron.performCalculation();
            result.setData(i, outputNeuron.getActivation());
        }

//        this.network.updateContext();

        return result;

    }

}
