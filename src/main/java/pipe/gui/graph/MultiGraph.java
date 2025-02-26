package pipe.gui.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiGraph extends AbstractGraph {
    private final Map<String, Map<String, Graph>> multiGraphMap;
    private final Map<String, Double> multiGraphGlobalAvgMap;

    public MultiGraph(String name, String xAxisLabel, String yAxisLabel, String buttonText) {
        this.name = name;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.buttonText = buttonText;
        multiGraphMap = new HashMap<>(); 
        multiGraphGlobalAvgMap = new HashMap<>();
    }

    public void addGraph(String observation, String property, Graph graph) {
        multiGraphMap.computeIfAbsent(observation, k -> new HashMap<>())
                     .put(property, graph);
    }

    public void addGlobalAvg(String key, double value) {
        multiGraphGlobalAvgMap.put(key, value);
    }

    @Override
    public boolean isEmpty() {
        return multiGraphMap.values().stream().allMatch(Map::isEmpty);
    }

    public Map<String, Map<String, Graph>> getMultiGraphMap() {
        return multiGraphMap;
    }

    public Map<String, Double> getMultiGraphGlobalAvgMap() {
        return multiGraphGlobalAvgMap;
    }

    public List<Graph> getGraphs() {
        return multiGraphMap.entrySet().stream()
            .flatMap(entry -> entry.getValue().entrySet().stream()
                .map(propertyEntry -> {
                    Graph graph = propertyEntry.getValue();
                    graph.setName(entry.getKey() + " - " + propertyEntry.getKey());
                    return graph;
                }))
            .collect(Collectors.toList());
    }

    public MultiGraph copy() {
        MultiGraph copy = new MultiGraph(
            this.name,
            this.xAxisLabel,
            this.yAxisLabel,
            this.buttonText
        );

        multiGraphMap.forEach((observation, propertyMap) -> 
            propertyMap.forEach((property, graph) -> 
                copy.addGraph(observation, property, graph)
            )
        );

        multiGraphGlobalAvgMap.forEach(copy::addGlobalAvg);

        return copy;
    }
}
