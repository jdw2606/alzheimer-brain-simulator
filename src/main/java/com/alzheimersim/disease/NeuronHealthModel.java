package com.alzheimersim.disease;

import java.util.Random;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.network.NeuronPopulation;
import com.alzheimersim.network.SynapseGraph;

public class NeuronHealthModel {
    public static void step(NeuronPopulation pop, SynapseGraph graph, SimulationConfig config, Random rng) {
        for (int i = 0; i < pop.size; i ++) {
            if (!pop.alive[i] || !pop.functional[i]) { continue; }
            double initialStrength = graph.initialStrength(i);
            double currentStrength = graph.totalWeight(i);
            double lostFraction;
            if (initialStrength > 0) { lostFraction = Math.max(0, Math.min(1, 1 - currentStrength / initialStrength)); }
            else { lostFraction = 0; }

            double decline = config.healthDecayRate * (pop.tau[i] + config.synapticLossHealthWeight * lostFraction) * pop.vulnerability[i];
            pop.health[i] = Math.max(0, Math.min(1, pop.health[i] - decline));

            if (pop.health[i] < config.nonfunctionalHealthThreshold) {
                double severity = config.nonfunctionalHealthThreshold - pop.health[i];
                double hazard = sigmoid(config.nonfunctionalHazardSteepness * (severity - config.nonfunctionalHazardMidpoint)) 
                * Math.max(0, Math.min(1, (pop.vulnerability[i] / config.vulnerabilityMax)));
                if (rng.nextDouble() < hazard) {
                    pop.functional[i] = false;
                    pop.activation[i] = 0;
                    pop.fadeCountdown[i] = config.fadeSteps;
                    pop.fadeAlpha[i] = 1.0;
                }
            }
        }
        for (int i = 0; i < pop.size; i ++) {
            if (!pop.alive[i] || pop.functional[i]) { continue; }
            pop.fadeCountdown[i] --;
            pop.fadeAlpha[i] = Math.max(0, pop.fadeCountdown[i] / (double) config.fadeSteps);
            if (pop.fadeCountdown[i] <= 0) {
                pop.alive[i] = false;
                pop.fadeAlpha[i] = 0;
            }
        }
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
}
