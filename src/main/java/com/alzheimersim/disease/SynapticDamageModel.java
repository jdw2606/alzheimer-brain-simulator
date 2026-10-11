package com.alzheimersim.disease;

import java.util.List;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.SynapseGraph;

public class SynapticDamageModel {
    public static void step(SynapseGraph graph, NeuronPopulation pop, SimulationConfig config) {
        List<int[]> edges = graph.allEdges();
        for (int edge[] : edges) {
            int i = edge[0];
            int j = edge[1];
            double aveTau = (pop.tau[i] + pop.tau[j]) / 2;
            double newWeight = graph.getWeight(i, j) * (1 - config.synapticDamageRate * aveTau);
            if (newWeight < config.synapseLossThreshold) {
                graph.removeEdge(i, j);
            }
            else {
                graph.addEdge(i, j, newWeight);
            }
        }
    }
}
