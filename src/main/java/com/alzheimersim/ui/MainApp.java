package com.alzheimersim.ui;

import java.nio.file.Path;
import java.util.Random;

import com.alzheimersim.config.SimulationConfig;
import com.alzheimersim.engine.SimulationEngine;
import com.alzheimersim.memory.MemoryPattern;
import com.alzheimersim.metrics.ExperimentRunner;
import com.alzheimersim.metrics.MetricsCalculator;
import com.alzheimersim.metrics.MetricsSnapshot;
import com.alzheimersim.network.Region;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
    private static final int EXPERIMENT_STEPS = 500;

    private SimulationEngine engine;
    private SimulationEngine healthyShadow;
    private NetworkCanvas canvas;
    private ControlPanel controls;
    private InspectorPanel inspector;
    private Timeline timeline;
    private int selectedNeuron = -1;

    private final XYChart.Series<Number, Number> healthyRecall = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> diseasedRecall = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> ecTau = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> hipTau = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> cortexTau = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        SimulationConfig config = new SimulationConfig();
        engine = new SimulationEngine(config, true);
        healthyShadow = new SimulationEngine(config.copy(), false);

        canvas = new NetworkCanvas();
        controls = new ControlPanel();
        inspector = new InspectorPanel();

        StackPane canvasHolder = new StackPane(canvas);
        canvasHolder.setMinSize(200, 200);
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener((obs, o, n) -> canvas.render());
        canvas.heightProperty().addListener((obs, o, n) -> canvas.render());

        ScrollPane rightScroll = new ScrollPane(new VBox(controls, inspector));
        rightScroll.setFitToWidth(true);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        healthyRecall.setName("Healthy control");
        diseasedRecall.setName("Diseased");
        ecTau.setName("Entorhinal cortex");
        hipTau.setName("Hippocampus");
        cortexTau.setName("Cortex");

        LineChart<Number, Number> recallChart = buildChart("Memory recall accuracy", "Recall accuracy", 1.0,
                "recall-chart", healthyRecall, diseasedRecall);
        LineChart<Number, Number> tauChart = buildChart("Tau pathology by region (diseased network)", "Average tau", 1.0,
                "tau-chart", ecTau, hipTau, cortexTau);
        HBox charts = new HBox(8, recallChart, tauChart);
        charts.setPadding(new Insets(4, 8, 8, 8));
        HBox.setHgrow(recallChart, Priority.ALWAYS);
        HBox.setHgrow(tauChart, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(canvasHolder);
        root.setRight(rightScroll);
        root.setBottom(charts);

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/ui.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Alzheimer's Disease Progression Simulation");
        stage.show();

        canvas.setEngine(engine);
        canvas.setOnSelect(i -> {
            selectedNeuron = i;
            inspector.show(i, engine.population);
        });

        wireControls();
        rebuildTimeline();
        encodeMemory();
        refresh(true);
    }

    private LineChart<Number, Number> buildChart(String title, String yLabel, double yMax, String styleClass,
                                                 XYChart.Series<Number, Number>... series) {
        NumberAxis x = new NumberAxis();
        x.setLabel("Timestep");
        x.setForceZeroInRange(true);
        NumberAxis y = new NumberAxis(0, yMax, yMax / 5);
        y.setLabel(yLabel);
        LineChart<Number, Number> chart = new LineChart<>(x, y);
        chart.setTitle(title);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.getStyleClass().add(styleClass);
        chart.setLegendSide(Side.RIGHT);
        chart.setPrefHeight(270);
        chart.setMinHeight(240);
        chart.getData().addAll(series);
        return chart;
    }

    private void wireControls() {
        controls.playPauseButton.setOnAction(e -> togglePlay());
        controls.stepButton.setOnAction(e -> doStep());
        controls.resetButton.setOnAction(e -> rebuildNetwork());
        controls.speedSlider.valueProperty().addListener((obs, o, n) -> rebuildTimeline());

        controls.tauSpreadSlider.valueProperty().addListener((obs, o, n) -> engine.config.tauSpreadRate = n.doubleValue());
        controls.synapticDamageSlider.valueProperty().addListener((obs, o, n) -> engine.config.synapticDamageRate = n.doubleValue());
        controls.vulnerabilitySlider.valueProperty().addListener((obs, o, n) -> engine.config.vulnerabilityMax = n.doubleValue());
        controls.neuronCountCombo.setOnAction(e -> {
            applyNeuronCount(controls.neuronCountCombo.getValue());
            rebuildNetwork();
        });

        controls.ecVisibleCheck.selectedProperty().addListener((o, ov, nv) -> canvas.setRegionVisible(Region.ENTORHINAL_CORTEX, nv));
        controls.hippVisibleCheck.selectedProperty().addListener((o, ov, nv) -> canvas.setRegionVisible(Region.HIPPOCAMPUS, nv));
        controls.cortexVisibleCheck.selectedProperty().addListener((o, ov, nv) -> canvas.setRegionVisible(Region.CORTEX, nv));
        controls.synapsesVisibleCheck.selectedProperty().addListener((o, ov, nv) -> canvas.setSynapsesVisible(nv));
        controls.overlayCombo.valueProperty().addListener((o, ov, nv) -> canvas.setOverlay(overlayFromLabel(nv)));
        controls.resetViewButton.setOnAction(e -> canvas.resetView());

        controls.encodeMemoryButton.setOnAction(e -> {
            encodeMemory();
            refresh(false);
        });
        controls.recallButton.setOnAction(e -> doRecall());
        controls.exportCsvButton.setOnAction(e -> exportCsv());
    }

    private void togglePlay() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.pause();
            controls.playPauseButton.setText("Play");
        } else {
            timeline.play();
            controls.playPauseButton.setText("Pause");
        }
    }

    private void rebuildTimeline() {
        boolean wasPlaying = timeline != null && timeline.getStatus() == Animation.Status.RUNNING;
        if (timeline != null) timeline.stop();
        double stepsPerSecond = controls.speedSlider.getValue();
        timeline = new Timeline(new KeyFrame(Duration.millis(1000.0 / Math.max(0.1, stepsPerSecond)), e -> doStep()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        if (wasPlaying) timeline.play();
    }

    private void doStep() {
        engine.stepOnce();
        healthyShadow.stepOnce();
        refresh(true);
    }

    private void rebuildNetwork() {
        timeline.pause();
        controls.playPauseButton.setText("Play");

        engine.reset();
        healthyShadow = new SimulationEngine(engine.config.copy(), false);
        canvas.setEngine(engine);
        selectedNeuron = -1;
        inspector.showNothingSelected();
        controls.memoryCombo.getItems().clear();
        controls.recallAccuracyLabel.setText("Recall test: -");

        healthyRecall.getData().clear();
        diseasedRecall.getData().clear();
        ecTau.getData().clear();
        hipTau.getData().clear();
        cortexTau.getData().clear();

        encodeMemory();
        refresh(true);
    }

    private void applyNeuronCount(int total) {
        int ec = Math.round(total * 0.15f);
        int hipp = Math.round(total * 0.25f);
        engine.config.entorhinalCount = ec;
        engine.config.hippocampusCount = hipp;
        engine.config.cortexCount = total - ec - hipp;
    }

    private void encodeMemory() {
        String label = "M" + (engine.memoryStore.patterns().size() + 1);
        MemoryPattern pattern = engine.memoryStore.createRandomPattern(label, engine.population, engine.config, engine.rng());
        engine.memoryStore.encode(pattern, engine.population, engine.graph, engine.config);

        MemoryPattern copy = new MemoryPattern(pattern.label, pattern.neuronIndices.clone(), pattern.targetActivation.clone());
        healthyShadow.memoryStore.encode(copy, healthyShadow.population, healthyShadow.graph, healthyShadow.config);

        controls.memoryCombo.getItems().add(label);
        controls.memoryCombo.getSelectionModel().select(label);
    }

    private void doRecall() {
        String label = controls.memoryCombo.getValue();
        if (label == null) return;
        MemoryPattern pattern = findPattern(label);
        if (pattern == null) return;

        double cue = controls.cueFractionSlider.getValue();
        double accuracy = engine.memoryStore.recall(pattern, cue, engine.population, engine.graph, engine.config, new Random());
        controls.recallAccuracyLabel.setText(String.format("Recall test (%s, %.0f%% cue given): accuracy %.3f", label, cue * 100, accuracy));
        controls.overlayCombo.getSelectionModel().select("Activity");
        canvas.render();
    }

    private MemoryPattern findPattern(String label) {
        for (MemoryPattern p : engine.memoryStore.patterns()) {
            if (p.label.equals(label)) return p;
        }
        return null;
    }

    private void exportCsv() {
        controls.exportCsvButton.setDisable(true);
        controls.exportStatusLabel.setText("Running experiment...");
        SimulationConfig cfg = engine.config.copy();
        Path path = Path.of("experiment.csv").toAbsolutePath();
        Thread worker = new Thread(() -> {
            String message;
            try {
                ExperimentRunner.runPaired(cfg, EXPERIMENT_STEPS, path);
                message = "Wrote " + path;
            } catch (Exception ex) {
                message = "Export failed: " + ex.getMessage();
            }
            String finalMessage = message;
            Platform.runLater(() -> {
                controls.exportStatusLabel.setText(finalMessage);
                controls.exportCsvButton.setDisable(false);
            });
        });
        worker.setDaemon(true);
        worker.start();
    }

    private NetworkCanvas.Overlay overlayFromLabel(String label) {
        if (label == null) return NetworkCanvas.Overlay.PATHOLOGY;
        return switch (label) {
            case "Activity" -> NetworkCanvas.Overlay.ACTIVITY;
            case "Synaptic" -> NetworkCanvas.Overlay.SYNAPTIC;
            default -> NetworkCanvas.Overlay.PATHOLOGY;
        };
    }

    private void refresh(boolean addChartPoint) {
        MetricsSnapshot hm = MetricsCalculator.compute(healthyShadow);
        MetricsSnapshot dm = MetricsCalculator.compute(engine);

        if (addChartPoint) {
            int step = engine.step;
            healthyRecall.getData().add(new XYChart.Data<>(step, hm.avgRecallAccuracy));
            diseasedRecall.getData().add(new XYChart.Data<>(step, dm.avgRecallAccuracy));
            if (livingIn(Region.ENTORHINAL_CORTEX) > 0) ecTau.getData().add(new XYChart.Data<>(step, dm.ecTauBurden));
            if (livingIn(Region.HIPPOCAMPUS) > 0) hipTau.getData().add(new XYChart.Data<>(step, dm.hippocampusTauBurden));
            if (livingIn(Region.CORTEX) > 0) cortexTau.getData().add(new XYChart.Data<>(step, dm.cortexTauBurden));
        }

        boolean allLost = dm.deadCount >= engine.population.size;
        controls.stepLabel.setText("Step: " + engine.step + (allLost ? "  (all neurons lost)" : ""));
        controls.statesLabel.setText(String.format(
                "Neurons: %d healthy, %d mild, %d significant, %d severe, %d nonfunctional, %d lost",
                dm.healthyCount, dm.mildCount, dm.significantCount, dm.severeCount, dm.nonfunctionalCount, dm.deadCount));
        controls.comparisonRecallLabel.setText(String.format("Recall accuracy: healthy %.3f / diseased %.3f",
                hm.avgRecallAccuracy, dm.avgRecallAccuracy));
        controls.comparisonTauLabel.setText(String.format("Average tau: healthy %.3f / diseased %.3f",
                hm.avgTauBurden, dm.avgTauBurden));
        controls.comparisonAffectedLabel.setText(String.format("Nonfunctional + lost neurons: healthy %d / diseased %d",
                hm.nonfunctionalCount + hm.deadCount, dm.nonfunctionalCount + dm.deadCount));
        controls.comparisonSynapseLabel.setText(String.format("Synapses: %d left, %d lost; average strength healthy %.3f / diseased %.3f",
                dm.functionalSynapseCount, dm.lostSynapseCount, hm.avgSynapticStrength, dm.avgSynapticStrength));

        if (selectedNeuron >= 0) inspector.show(selectedNeuron, engine.population);
        canvas.render();

        if (allLost && timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.pause();
            controls.playPauseButton.setText("Play");
        }
    }

    private int livingIn(Region region) {
        int n = 0;
        for (int i = 0; i < engine.population.size; i++) {
            if (engine.population.region[i] == region && engine.population.alive[i]) n++;
        }
        return n;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
