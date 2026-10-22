/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
import java.util.*;
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] outgoing = Graphs.slidesFrom(s);
        int[][] incoming = Graphs.slidesInto(s);

        boolean[] seen = new boolean[n];
        int[] nextEdge = new int[n];
        int[] stack = new int[n];
        int[] finish = new int[n];
        int finished = 0;

        // Первый проход: записываем порядок завершения DFS.
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;

            int top = 0;
            stack[top] = start;
            seen[start] = true;

            while (top >= 0) {
                int cell = stack[top];

                if (nextEdge[cell] < outgoing[cell].length) {
                    int edge = outgoing[cell][nextEdge[cell]++];
                    int next = s.to()[edge];

                    if (!seen[next]) {
                        seen[next] = true;
                        stack[++top] = next;
                    }
                } else {
                    finish[finished++] = cell;
                    top--;
                }
            }
        }

        // Второй проход: идём по обратным рёбрам.
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zoneCount = 0;

        for (int i = finished - 1; i >= 0; i--) {
            int start = finish[i];
            if (zoneOf[start] != -1) continue;

            int size = 0;
            stack[size++] = start;
            zoneOf[start] = zoneCount;

            while (size > 0) {
                int cell = stack[--size];

                for (int edge : incoming[cell]) {
                    int next = s.from()[edge];

                    if (zoneOf[next] == -1) {
                        zoneOf[next] = zoneCount;
                        stack[size++] = next;
                    }
                }
            }

            zoneCount++;
        }

        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        ArrayList<Map<Integer, Integer>> graph = new ArrayList<>();

        for (int i = 0; i < zoneCount; i++) {
            graph.add(new HashMap<>());
        }

        // Между каждой парой зон оставляем самый дешёвый переход.
        for (int i = 0; i < s.count(); i++) {
            int from = zoneOf[s.from()[i]];
            int to = zoneOf[s.to()[i]];

            if (from != to) {
                graph.get(from).merge(to, s.time()[i], Math::min);
            }
        }

        long infinity = Long.MAX_VALUE / 4;
        long[] distance = new long[zoneCount];
        Arrays.fill(distance, infinity);
        distance[zoneOf[0]] = 0;

        int[] stages = new int[zoneCount];
        Arrays.fill(stages, 1);
        long flowStages = 0;

        // Номера зон уже идут в топологическом порядке.
        for (int zone = 0; zone < zoneCount; zone++) {
            flowStages = Math.max(flowStages, stages[zone]);

            for (Map.Entry<Integer, Integer> edge
                    : graph.get(zone).entrySet()) {

                int next = edge.getKey();
                int cost = edge.getValue();

                stages[next] = Math.max(
                        stages[next], stages[zone] + 1
                );

                if (distance[zone] != infinity) {
                    distance[next] = Math.min(
                            distance[next], distance[zone] + cost
                    );
                }
            }
        }

        return new long[]{
                flowStages,
                distance[zoneOf[s.n() - 1]]
        };

    }
}
