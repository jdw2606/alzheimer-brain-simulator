package com.alzheimersim.metrics;

import java.util.List;
import java.util.Random;

import com.alzheimersim.engine.SimulationEngine;
import com.alzheimersim.memory.MemoryPattern;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.SynapseGraph;

public class MetricsCalculator {
    public static MetricsSnapshot compute(SimulationEngine engine) {
        MetricsSnapshot m = new MetricsSnapshot();
        m.step = engine.step;
        countNeuronStates(engine.population, m);
        computeTauBurden(engine.population, m);
        computeSynapseMetrics(engine.graph, m);
        computeRecall(engine, m);

        return m; 
    }

    private static void countNeuronStates(NeuronPopulation pop, MetricsSnapshot m) {
        for (int i = 0; i < pop.size; i ++) {
            switch (pop.stateOf(i)) {
                case HEALTHY -> m.healthyCount++;
                case MILD -> m.mildCount++;
                case SIGNIFICANT -> m.significantCount++;
                case SEVERE -> m.severeCount++;
                case NONFUNCTIONAL -> m.nonfunctionalCount++;
                case DEAD -> m.deadCount++;
            }
        }
    }

    private static void computeTauBurden(NeuronPopulation pop, MetricsSnapshot m) {
        double tauSum = 0;
        int aliveCount = 0;
        double ecSum = 0.0;
        double hcSum = 0.0;
        double coSum = 0.0;
        int ecCount = 0;
        int hcCount = 0;
        int coCount = 0;

        for (int i = 0; i < pop.size; i ++) {
            if (!pop.alive[i]) continue;
            tauSum += pop.tau[i];
            aliveCount ++;
            switch(pop.region[i]) {
                case ENTORHINAL_CORTEX -> {ecSum += pop.tau[i]; ecCount ++;}
                case HIPPOCAMPUS -> {hcSum += pop.tau[i]; hcCount ++;}
                case CORTEX -> {coSum += pop.tau[i]; coCount ++;}
            }
        }
        if (aliveCount > 0) {
            m.avgTauBurden = tauSum / aliveCount;
        } else {
            m.avgTauBurden = 0;
        }
        m.ecTauBurden = (ecCount > 0 ) ? ecSum / ecCount : 0;
        m.hippocampusTauBurden = (hcCount > 0 ) ? hcSum / hcCount : 0;
        m.cortexTauBurden = (coCount > 0 ) ? coSum / coCount : 0;
    }

    private static void computeSynapseMetrics(SynapseGraph graph, MetricsSnapshot m) {
        List<int[]> edges = graph.allEdges(); 
        m.functionalSynapseCount = edges.size();
        m.lostSynapseCount = graph.initialEdgeCount() - edges.size();

        double sum = 0;
        for (int[] edge : edges) {
            sum += graph.getWeight(edge[0], edge[1]);
        }
        if (edges.isEmpty()) {
                m.avgSynapticStrength = 0;
        } else{
            m.avgSynapticStrength = sum / edges.size();
        }
    }

    private static void computeRecall(SimulationEngine engine, MetricsSnapshot m) {
        Random rng = new Random(engine.config.randomSeed);
        List<MemoryPattern> patterns = engine.memoryStore.patterns();
        double total = 0;
        for (MemoryPattern pattern : patterns) {
            total += engine.memoryStore.recall(pattern, 0.5, engine.population, engine.graph, engine.config, rng);
        }
        if (patterns.isEmpty()) {
                m.avgRecallAccuracy = 0;
        } else{
            m.avgRecallAccuracy = total / patterns.size();
        }
    }
}
