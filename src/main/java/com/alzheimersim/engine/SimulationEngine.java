package com.alzheimersim.engine;

import java.util.Random;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.disease.NeuronHealthModel;
import com.alzheimersim.disease.SynapticDamageModel;
import com.alzheimersim.disease.TauPathologyModel;
import com.alzheimersim.memory.MemoryStore;
import com.alzheimersim.network.Network;
import com.alzheimersim.network.NetworkBuilder;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.SynapseGraph;

public class SimulationEngine {
    public final SimulationConfig config;
    public final boolean diseaseEnabled;
    public final MemoryStore memoryStore;

    public NeuronPopulation population;
    public SynapseGraph graph;
    public int step;

    private Random rng;
    public SimulationEngine(SimulationConfig config, boolean diseaseEnabled) {
        this.config = config;
        this.diseaseEnabled = diseaseEnabled;
        this.memoryStore = new MemoryStore();
        reset();
    }

    public SimulationEngine(SimulationConfig config) {
        this(config, true);
    }

    public void reset() {
        this.rng = new Random(config.randomSeed);
        Network net = NetworkBuilder.build(config, rng);
        this.population = net.population();
        this.graph = net.graph();
        this.step = 0;
        this.memoryStore.clear();
        if (diseaseEnabled) {
            TauPathologyModel.seed(population, config, rng);
        }
    }

    public void stepOnce() {
        if (diseaseEnabled) {
            TauPathologyModel.step(population, graph, config);
            SynapticDamageModel.step(graph, population, config);
            NeuronHealthModel.step(population, graph, config, rng);
        }
        step ++;
    }

    public Random rng() {
        return rng;
    }
}
