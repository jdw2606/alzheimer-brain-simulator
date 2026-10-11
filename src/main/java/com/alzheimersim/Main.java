package com.alzheimersim;

import java.nio.file.Path;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.metrics.ExperimentRunner;

public class Main {
    public static void main(String[] args) throws Exception {
        Path output = Path.of("experiment.csv");
        ExperimentRunner.runPaired(new SimulationConfig(), 500, output);
        System.out.println("Wrote " + output.toAbsolutePath());
    }
}
