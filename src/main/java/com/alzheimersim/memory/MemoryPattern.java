package com.alzheimersim.memory;

public class MemoryPattern{
    public final String label;
    public final int[] neuronIndices;
    public final double[] targetActivation;

    public MemoryPattern(String label, int[] neuronIndices, double[] targetActivation) {
        this.label = label;
        this.neuronIndices = neuronIndices;
        this.targetActivation = targetActivation;
    }
}
