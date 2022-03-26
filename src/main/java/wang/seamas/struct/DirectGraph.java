package wang.seamas.struct;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 有向图
 */
public class DirectGraph<T> {

    private Set<T> vertexes;
    private Map<T, Set<T>> edges;

    public DirectGraph() {
        vertexes = new HashSet<>();
        edges = new HashMap<>();
    }

    public void addVertex(T value) {
        vertexes.add(value);
    }

    public void addEdge(T from, T to) {
        addVertex(from);
        addVertex(to);
        edges.compute(from, (k, v) -> {
            if (v == null) {
                v = new HashSet<>();
            }
            v.add(to);
            return v;
        });
    }

    public Queue<T> topologicalSort() throws Exception {
        Queue<T> result = new LinkedList<>();
        Queue<T> queue = new LinkedList<>();

        Map<T, Integer> degreeMap = inDegreeMap();
        degreeMap.forEach((k, v) -> {
            if (v == 0) {
                queue.offer(k);
            }
        });

        while (!queue.isEmpty()) {
            T t = queue.poll();
            result.offer(t);
            degreeMap.remove(t);
            for (T end : edges.getOrDefault(t, new HashSet<>())) {
                if (degreeMap.compute(end, (k, v) -> --v) == 0) {
                    queue.offer(end);

                }
            }

        }

        if (degreeMap.size() > 0) {
            throw new Exception("Graph has circle");
        }
        return result;
    }


    private Map<T, Integer> inDegreeMap() {
        Map<T, Integer> initMap = vertexes.stream()
                .collect(Collectors.toMap(item -> item, item -> 0));
        edges.forEach((k, s) -> {
            s.forEach(item -> {
               initMap.compute(item, (kk, vv) -> ++vv);
            });
        });
        return initMap;
    }
}
