package com.alzheimersim.disease;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.Region;
import com.alzheimersim.network.SynapseGraph;

public class TauPathologyModel {
    public static void seed(NeuronPopulation pop, SimulationConfig config, Random rng) {
        List<Integer> ec = new ArrayList<>();
        for (int i = 0; i < pop.size; i ++) {
            if (pop.region[i] == Region.ENTORHINAL_CORTEX) {
                ec.add(i);
            }
        }
        Collections.shuffle(ec, rng);
        int seedCount = (int) Math.round(config.tauSeedFraction * ec.size());
        for (int i = 0; i < seedCount && i < ec.size(); i ++) {
            int idx = ec.get(i);
            pop.tau[idx] = config.tauSeedMin + rng.nextDouble() * (config.tauSeedMax - config.tauSeedMin);
        }
    }

    public static void step(NeuronPopulation pop, SynapseGraph graph, SimulationConfig config) {
        double[] laplacian = graph.laplacianApply(pop.tau);
        for (int i = 0; i < pop.size; i ++) {
            if (!pop.alive[i]) { continue; }
            double growth = config.tauAccumulationRate * pop.tau[i] * (1 - pop.tau[i]) * pop.vulnerability[i];
            double spread = -config.tauSpreadRate * laplacian[i];
            double newTau = pop.tau[i] + growth + spread;
            pop.tau[i] = Math.max(0, Math.min(1, newTau));
        } 
    }
}
