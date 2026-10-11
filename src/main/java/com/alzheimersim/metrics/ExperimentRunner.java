package com.alzheimersim.metrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.engine.SimulationEngine;
import com.alzheimersim.memory.MemoryPattern;

public class ExperimentRunner {
    public static void runPaired(SimulationConfig config, int steps, Path outputCsv) throws IOException {
        SimulationEngine healthy = new SimulationEngine(config.copy(), false);
        SimulationEngine diseased = new SimulationEngine(config.copy(), true);

        MemoryPattern healthyPattern = healthy.memoryStore.createRandomPattern("M1", healthy.population, healthy.config, healthy.rng());
        healthy.memoryStore.encode(healthyPattern, healthy.population, healthy.graph, config);

        MemoryPattern diseasedPattern = new MemoryPattern(healthyPattern.label, healthyPattern.neuronIndices.clone(), healthyPattern.targetActivation.clone());
        diseased.memoryStore.encode(diseasedPattern, diseased.population, diseased.graph, config);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsv.toFile()))) {
            writer.write("step,healthy_avg_tau,diseased_avg_tau,healthy_recall,diseased_recall,diseased_nonfunctional,diseased_dead\n");
            for (int s = 0; s <= steps; s++) {
                MetricsSnapshot hm = MetricsCalculator.compute(healthy);
                MetricsSnapshot dm = MetricsCalculator.compute(diseased);
                writer.write(s + "," + hm.avgTauBurden + "," + dm.avgTauBurden + "," + hm.avgRecallAccuracy + ","
                        + dm.avgRecallAccuracy + "," + dm.nonfunctionalCount + "," + dm.deadCount + "\n");
                if (s < steps) {
                    healthy.stepOnce();
                    diseased.stepOnce();
                }
            }
        }
    }
}
