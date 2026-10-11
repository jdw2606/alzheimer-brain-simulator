package com.alzheimersim.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.Region;
import com.alzheimersim.network.SynapseGraph;

public class MemoryStore {
    private final List<MemoryPattern> patterns = new LinkedList<>();
    public List<MemoryPattern> patterns() {
        return Collections.unmodifiableList(patterns);
    }

    public void clear() {
        patterns.clear(); 
    }
    public MemoryPattern createRandomPattern(String label, NeuronPopulation pop, SimulationConfig config, Random rng) {
        List<Integer> EC = new LinkedList<>();
        List<Integer> HC = new LinkedList<>();
        List<Integer> CO = new LinkedList<>();
        for (int i = 0; i < pop.size; i ++) {
            if (!pop.alive[i] || !pop.functional[i])
                continue;
            if (pop.region[i] == Region.ENTORHINAL_CORTEX) {EC.add(i);}
            else if (pop.region[i] == Region.HIPPOCAMPUS) {HC.add(i);}
            else {CO.add(i);}
        }

        int total = (int) Math.round(config.memoryPatternFraction * pop.size);
        int fromEC = (int) Math.round(total * 0.10);
        int fromHC = (int) Math.round(total * 0.40);
        int fromCO = total - fromEC - fromHC;

        List<Integer> chosen = new ArrayList<>();
        chosen.addAll(candidates(EC, fromEC, rng));
        chosen.addAll(candidates(HC, fromHC, rng));
        chosen.addAll(candidates(CO, fromCO, rng));
        
        int[] indices = new int[chosen.size()];

        for (int k = 0; k < indices.length; k ++) {
            indices[k] = chosen.get(k);
        }
        double[] target = new double[indices.length];
        for (int k = 0; k < target.length; k ++) {
            target[k] = config.memoryPatternActivation;
        }

        return new MemoryPattern(label, indices, target);
    }

    private List<Integer> candidates(List<Integer> pool, int count, java.util.Random rng) {
        List<Integer> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, rng);
        return copy.subList(0, Math.min(Math.max(count, 0), copy.size()));
    }

    public void encode(MemoryPattern pattern, NeuronPopulation pop, SynapseGraph graph, SimulationConfig config) {
        for (int step = 0; step < config.encodeSteps; step ++) {
            for (int i = 0; i < pop.size; i ++) {
                if (!pop.alive[i] || !pop.functional[i]) { 
                    pop.activation[i] = 0;
                    continue; 
                }
                pop.activation[i] = config.memoryBaselineActivation;
            }
            for (int k = 0; k < pattern.neuronIndices.length; k ++) {
                int inx = pattern.neuronIndices[k];
                if (!pop.alive[inx] || !pop.functional[inx]) { 
                    continue; 
                }
                pop.activation[inx] = pattern.targetActivation[k];
            }
            for (int[] edge : graph.allEdges()) {
                int i = edge[0];
                int j = edge[1];
                double delta = config.hebbianLearningRate * pop.activation[i] * pop.activation[j];
                double weight = Math.min(1.0, graph.getWeight(i, j) + delta);
                graph.setWeight(i, j, weight);
            }
        }
        for (int i = 0; i < pop.size; i ++) {
            if (!pop.alive[i] || !pop.functional[i]) { 
                pop.activation[i] = 0;
                continue; 
            }
            pop.activation[i] = config.memoryBaselineActivation;
        }
        patterns.add(pattern);
    }

    public double recall(MemoryPattern pattern, double cueFraction, NeuronPopulation pop, SynapseGraph graph,
                          SimulationConfig config, Random rng) {
        for (int i = 0; i < pop.size; i ++) { pop.activation[i] = 0; }
        List<Integer> working = new ArrayList<>();
        for (int i = 0; i < pattern.neuronIndices.length; i ++) {
            int idx = pattern.neuronIndices[i];
            if (pop.alive[idx] && pop.functional[idx]) {
                working.add(idx);
            }
        }
        Collections.shuffle(working, rng);
        int cueCount = (int) Math.round(cueFraction * pattern.neuronIndices.length);
        Set<Integer> cueSet = new HashSet<>(working.subList(0, Math.min(cueCount, working.size())));
        for (int idx : cueSet) {
            for (int k = 0; k < pattern.neuronIndices.length; k ++) {
                if (pattern.neuronIndices[k] == idx) {
                    pop.activation[idx] = pattern.targetActivation[k];
                    break;
                }
            }
        }

        for (int step = 0; step < config.recallSettleSteps; step ++) {
            double[] next = pop.activation.clone();
            for (int i = 0; i < pop.size; i ++) {
                if (cueSet.contains(i) || !pop.alive[i] || !pop.functional[i]) { continue; }
                Map<Integer, Double> neighbors = graph.neighbors(i);
                double target = 0;
                if (!neighbors.isEmpty()) {
                    double weightedSum = 0.0;
                    double weightedActSum = 0.0;
                    for (Map.Entry<Integer, Double> e : neighbors.entrySet()) {
                        weightedSum += e.getValue();
                        weightedActSum += e.getValue() * pop.activation[e.getKey()];
                    }
                    target = weightedActSum / weightedSum;
                }  
                double updated = (1 - config.recallLeak) * pop.activation[i] + config.recallLeak * target;
                next[i] = Math.max(0, Math.min(1, updated));
            }
            System.arraycopy(next, 0, pop.activation, 0, pop.size);
        }
        
        return cosineSimilarity(pattern, pop);
    }

    private static double cosineSimilarity(MemoryPattern pattern, NeuronPopulation pop) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < pattern.neuronIndices.length; i++) {
            double a = pop.activation[pattern.neuronIndices[i]];
            double b = pattern.targetActivation[i];
            dot += a * b;
            normA += a * a;
            normB += b * b; 
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
