package com.alzheimersim.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ControlPanel extends VBox {
    public final Button playPauseButton = new Button("Play");
    public final Button stepButton = new Button("Step");
    public final Button resetButton = new Button("Reset");
    public final Slider speedSlider = new Slider(1, 60, 20);
    public final Label stepLabel = new Label("Step: 0");

    public final Slider tauSpreadSlider = new Slider(0.001, 0.03, 0.008);
    public final Slider synapticDamageSlider = new Slider(0.01, 0.2, 0.05);
    public final Slider vulnerabilitySlider = new Slider(1.0, 2.0, 1.3);
    public final ComboBox<Integer> neuronCountCombo = new ComboBox<>(FXCollections.observableArrayList(200, 500, 1000));

    public final CheckBox ecVisibleCheck = new CheckBox("Entorhinal cortex");
    public final CheckBox hippVisibleCheck = new CheckBox("Hippocampus");
    public final CheckBox cortexVisibleCheck = new CheckBox("Cortex");
    public final CheckBox synapsesVisibleCheck = new CheckBox("Synapses");
    public final ComboBox<String> overlayCombo = new ComboBox<>(FXCollections.observableArrayList("Pathology", "Activity", "Synaptic"));
    public final Button resetViewButton = new Button("Reset view");

    public final Button encodeMemoryButton = new Button("Encode another memory");
    public final ComboBox<String> memoryCombo = new ComboBox<>();
    public final Slider cueFractionSlider = new Slider(0.0, 1.0, 0.5);
    public final Button recallButton = new Button("Test recall");
    public final Label recallAccuracyLabel = new Label("Recall test: -");

    public final Label statesLabel = new Label();
    public final Label comparisonTauLabel = new Label();
    public final Label comparisonRecallLabel = new Label();
    public final Label comparisonAffectedLabel = new Label();
    public final Label comparisonSynapseLabel = new Label();

    public final Button exportCsvButton = new Button("Run 500-step experiment -> experiment.csv");
    public final Label exportStatusLabel = new Label();

    public ControlPanel() {
        setSpacing(8);
        setPadding(new Insets(10));
        setPrefWidth(340);

        ecVisibleCheck.setSelected(true);
        hippVisibleCheck.setSelected(true);
        cortexVisibleCheck.setSelected(true);
        synapsesVisibleCheck.setSelected(true);
        overlayCombo.getSelectionModel().selectFirst();
        neuronCountCombo.getSelectionModel().select(Integer.valueOf(500));
        memoryCombo.setPrefWidth(120);
        cueFractionSlider.setShowTickLabels(true);
        cueFractionSlider.setMajorTickUnit(0.5);

        for (Label l : new Label[]{statesLabel, comparisonTauLabel, comparisonRecallLabel,
                comparisonAffectedLabel, comparisonSynapseLabel, recallAccuracyLabel, exportStatusLabel}) {
            l.setWrapText(true);
            l.setMaxWidth(315);
        }
        exportCsvButton.setWrapText(true);
        exportCsvButton.setMaxWidth(315);

        Label title = new Label("Alzheimer's disease progression");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label subtitle = new Label("Tau spreads through a simplified entorhinal-hippocampus-cortex network and degrades memory recall. All parameters are simulation assumptions.");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(315);
        subtitle.setStyle("-fx-font-size: 10.5px; -fx-opacity: 0.75;");

        getChildren().addAll(
                title, subtitle,
                new Separator(),

                sectionLabel("Playback"),
                new HBox(6, playPauseButton, stepButton, resetButton),
                slider("Speed", speedSlider, "%.0f steps/sec"),
                stepLabel,
                new Separator(),

                sectionLabel("Live readout (healthy control vs. diseased)"),
                statesLabel, comparisonRecallLabel, comparisonTauLabel,
                comparisonAffectedLabel, comparisonSynapseLabel,
                new Separator(),

                sectionLabel("Disease parameters"),
                slider("Tau spread rate", tauSpreadSlider, "%.3f"),
                slider("Synaptic damage rate", synapticDamageSlider, "%.2f"),
                slider("Vulnerability spread (applies on Reset)", vulnerabilitySlider, "%.2f"),
                labeled("Neuron count (rebuilds the network)", neuronCountCombo),
                new Separator(),

                sectionLabel("View"),
                new HBox(10, ecVisibleCheck, hippVisibleCheck),
                new HBox(10, cortexVisibleCheck, synapsesVisibleCheck),
                labeled("Color neurons by", overlayCombo),
                resetViewButton,
                legendRow("#2ecc71", "Healthy"),
                legendRow("#f1c40f", "Mildly affected"),
                legendRow("#e67e22", "Significantly affected"),
                legendRow("#e74c3c", "Severely affected"),
                legendRow("#5a5f66", "Nonfunctional (fades out, then lost)"),
                new Separator(),

                sectionLabel("Memory"),
                new HBox(8, memoryCombo, encodeMemoryButton),
                slider("Cue given to the network", cueFractionSlider, "%.0f%%"),
                recallButton,
                recallAccuracyLabel,
                new Separator(),

                sectionLabel("Experiment"),
                exportCsvButton,
                exportStatusLabel
        );
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        return l;
    }

    private VBox labeled(String text, Node control) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-opacity: 0.8;");
        return new VBox(2, l, control);
    }

    private VBox slider(String name, Slider s, String format) {
        Label l = new Label();
        l.setStyle("-fx-font-size: 11px; -fx-opacity: 0.8;");
        boolean percent = format.contains("%%");
        l.textProperty().bind(Bindings.createStringBinding(
                () -> name + ": " + String.format(format, percent ? s.getValue() * 100 : s.getValue()),
                s.valueProperty()));
        return new VBox(2, l, s);
    }

    private HBox legendRow(String hex, String text) {
        Circle dot = new Circle(5, Color.web(hex));
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px;");
        HBox row = new HBox(8, dot, l);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
