package com.alzheimersim.network;

import java.util.Random;

import com.alzheimersim.config.SimulationConfig;

public class NetworkBuilder {
    public static Network build(SimulationConfig config, Random rng) {
        int total = config.totalNeurons();
        NeuronPopulation pop = new NeuronPopulation(total);

        int ecStart = 0;
        int ecEnd = config.entorhinalCount;
        int hipStart = ecEnd;
        int hipEnd = hipStart + config.hippocampusCount;
        int cortStart = hipEnd;
        int cortEnd = total;

        for (int i = ecStart; i < ecEnd; i ++) {
            pop.region[i] = Region.ENTORHINAL_CORTEX;
        }
        for (int i = hipStart; i < hipEnd; i ++) {
            pop.region[i] = Region.HIPPOCAMPUS;
        }
        for (int i = cortStart; i < cortEnd; i ++) {
            pop.region[i] = Region.CORTEX;
        }

        for (int i = 0; i < total; i ++) {
            pop.vulnerability[i] = config.vulnerabilityMin + rng.nextDouble() * (config.vulnerabilityMax - config.vulnerabilityMin);
        }

        SynapseGraph graph = new SynapseGraph(total);
        buildSmallWorldRegion(graph, ecStart, ecEnd, config, rng);
        buildSmallWorldRegion(graph, hipStart, hipEnd, config, rng);
        buildSmallWorldRegion(graph, cortStart, cortEnd, config, rng);
        addInterRegionEdges(graph, ecStart, ecEnd, hipStart, hipEnd, config, rng);
        addInterRegionEdges(graph, hipStart, hipEnd, cortStart, cortEnd, config, rng);
        graph.captureInitialStrengths();
        return new Network(pop, graph);
    }

    private static void buildSmallWorldRegion(SynapseGraph graph, int start, int end, SimulationConfig config, Random rng) {
        int regionSize = end - start;
        int half = config.smallWorldK / 2;

        for (int local = 0; local < regionSize; local ++) {
            for (int offset = 1; offset <= half; offset ++) {
                int neighborLocal = (local + offset) % regionSize;
                int u = start + local;
                int v = start + neighborLocal;
                double randomWeight = config.initialWeightMin + rng.nextDouble() * (config.initialWeightMax - config.initialWeightMin);
                graph.addEdge(u, v, randomWeight);
            }
        }

        for (int local = 0; local < regionSize; local ++) {
            for (int offset = 1; offset <= half; offset ++) {
                if (rng.nextDouble() >= config.smallWorldRewireProb) continue;
                int u = start + local;
                int oldV = start + ((local + offset) % regionSize);
                int newLocal = -1;
                for (int attemps = 0; attemps < 50; attemps ++) {
                    int candidate = rng.nextInt(regionSize);
                    if (candidate != local && !graph.hasEdge(u, start + candidate)) {
                        newLocal = candidate;
                        break;
                    }
                }
                if (newLocal >= 0) {
                    double w = graph.getWeight(u, oldV);
                    graph.removeEdge(u, oldV);
                    graph.addEdge(u, start + newLocal, w);
                }
            }
        }
    }

    private static void addInterRegionEdges(SynapseGraph graph, int srcStart, int srcEnd, int dstStart, int dstEnd,
                                             SimulationConfig config, Random rng) {
        int numEdges = (int) Math.round(config.interRegionLinksPerNeuron * (srcEnd - srcStart));
        for (int i = 0; i < numEdges; i ++) {
            int u = srcStart + rng.nextInt(srcEnd - srcStart);
            int v = dstStart + rng.nextInt(dstEnd - dstStart);
            if (!graph.hasEdge(u, v)) {
                double randomWeight = config.initialWeightMin + rng.nextDouble() * (config.initialWeightMax - config.initialWeightMin);
                graph.addEdge(u, v, randomWeight);
            }
        }
    }
}
