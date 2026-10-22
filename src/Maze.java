/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu=new DisjointSet(w.cells());
        int[] broken=new int[w.cells()-1];
        int count=0;
        for (int i = 0; i < w.count(); i++) {
            int a = w.a()[i];
            int b = w.b()[i];

            if (dsu.union(a, b)) {
                broken[count] = i;
                count++;
            }

            if (count == w.cells() - 1) {
                break;
            }
        }

        return broken;
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];

        for (int i = 0; i < w.count(); i++) {
            order[i] = i;
        }

        Arrays.sort(order, Comparator.comparingInt(i -> w.cost()[i]));

        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[w.cells() - 1];
        int count = 0;

        for (int i : order) {
            if (dsu.union(w.a()[i], w.b()[i])) {
                broken[count++] = i;
            }

            if (count == w.cells() - 1) {
                break;
            }
        }

        return broken;
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells();

        // У клетки прямоугольной сетки максимум четыре соседа.
        int[][] incident = new int[n][4];
        int[] degree = new int[n];

        for (int i = 0; i < w.count(); i++) {
            int a = w.a()[i];
            int b = w.b()[i];

            incident[a][degree[a]++] = i;
            incident[b][degree[b]++] = i;
        }

        boolean[] inTree = new boolean[n];

        PriorityQueue<Integer> queue = new PriorityQueue<>(
                Comparator.comparingInt(i -> w.cost()[i])
        );

        inTree[0] = true;
        int added = 1;
        long total = 0;

        for (int j = 0; j < degree[0]; j++) {
            queue.add(incident[0][j]);
        }

        while (!queue.isEmpty() && added < n) {
            int wall = queue.poll();
            int a = w.a()[wall];
            int b = w.b()[wall];

            if (inTree[a] && inTree[b]) {
                continue;
            }

            int cell = inTree[a] ? b : a;
            inTree[cell] = true;
            added++;
            total += w.cost()[wall];

            for (int j = 0; j < degree[cell]; j++) {
                int edge = incident[cell][j];
                int neighbour = w.a()[edge] == cell
                        ? w.b()[edge]
                        : w.a()[edge];

                if (!inTree[neighbour]) {
                    queue.add(edge);
                }
            }
        }
        return total;
    }
}
