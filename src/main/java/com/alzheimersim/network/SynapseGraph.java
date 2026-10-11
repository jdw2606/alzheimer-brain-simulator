package com.alzheimersim.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynapseGraph {
    private final int size;

    private final Map<Integer, Double>[] map;

    double[] initialStrength;
    int initialEdgeCount;

    @SuppressWarnings("unchecked")
    public SynapseGraph(int size) {
        this.size = size;
        this.map = new HashMap[size];
        for (int i = 0; i < size; i ++) {
            map[i] = new HashMap<>();
        }
    }

    public int size() {
        return size;
    }

    public void addEdge(int i, int j, double weight) {
        if (i == j) return;
        map[i].put(j, weight);
        map[j].put(i, weight);
    }

    public boolean hasEdge(int i, int j) {
        return map[i].containsKey(j);
    }

    public double getWeight(int i, int j) {
        if (map[i].get(j) == null) {
            return 0.0;
        }
        Double weight = map[i].get(j);
        return weight;
    }

    public void setWeight(int i, int j, double weight) {
        map[i].put(j, weight);
        map[j].put(i, weight);
        }

    public void removeEdge(int i, int j) {
        map[i].remove(j);
        map[j].remove(i);
    }

    public Map<Integer, Double> neighbors(int i) {
        return Collections.unmodifiableMap(map[i]);
    }

    public int degree(int i) {
        return map[i].size();
    }

    public List<int[]> allEdges() {
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < map.length - 1; i ++) {
            for (int j : map[i].keySet()) {
                if (i < j) {
                    edges.add(new int[] {i, j});
                }
            }
        }
        return edges;
    }

    public int edgeCount() {
        return allEdges().size();
    }

    public double totalWeight(int i) {
        double sum = 0;
        for (double w : map[i].values()) {
            sum += w;
        }
        return sum;
    }

    public void captureInitialStrengths() {
        initialStrength = new double[size];
        for (int i = 0; i < size; i ++) {
            double total = 0;
            for (double w : map[i].values() ) {
                total += w;
            }
            initialStrength[i] = total;
        } 
        initialEdgeCount = edgeCount();
    }

    public double initialStrength(int i) {
        if (initialStrength == null) {
            return 0.0;
        }
        return initialStrength[i];
    }

    public double[] laplacianApply(double[] x) {
        double[] result = new double[size];
        for (int i = 0; i < size; i ++) {
            double sum = 0;
            for (Map.Entry<Integer, Double> e : map[i].entrySet()) {
                sum += e.getValue() * (x[i] - x[e.getKey()]);
            }
            result[i] = sum;
        }
        return result;
    }

    public int initialEdgeCount() {
        return initialEdgeCount;
    }
}
