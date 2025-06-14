package rumaps;

import java.util.*;

/**
 * This class represents the information that can be attained from the Rutgers University Map.
 * 
 * The RUMaps class is responsible for initializing the network, streets, blocks, and intersections in the map.
 * 
 * You will complete methods to initialize blocks and intersections, calculate block lengths, find reachable intersections,
 * minimize intersections between two points, find the fastest path between two points, and calculate a path's information.
 * 
 * Provided is a Network object that contains all the streets and intersections in the map
 * 
 * @author Vian Miranda
 * @author Anna Lu
 */
public class RUMaps {
    
    private Network rutgers;

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Constructor for the RUMaps class. Initializes the streets and intersections in the map.
     * For each block in every street, sets the block's length, traffic factor, and traffic value.
     * 
     * @param mapPanel The map panel to display the map
     * @param filename The name of the file containing the street information
     */
    public RUMaps(MapPanel mapPanel, String filename) {
        StdIn.setFile(filename);
        int numIntersections = StdIn.readInt();
        int numStreets = StdIn.readInt();
        StdIn.readLine();
        rutgers = new Network(numIntersections, mapPanel);
        ArrayList<Block> blocks = initializeBlocks(numStreets);
        initializeIntersections(blocks);

        for (Block block: rutgers.getAdjacencyList()) {
            Block ptr = block;
            while (ptr != null) {
                ptr.setLength(blockLength(ptr));
                ptr.setTrafficFactor(blockTrafficFactor(ptr));
                ptr.setTraffic(blockTraffic(ptr));
                ptr = ptr.getNext();
            }
        }
    }

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Overloaded constructor for testing.
     * 
     * @param filename The name of the file containing the street information
     */
    public RUMaps(String filename) {
        this(null, filename);
    }

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Overloaded constructor for testing.
     */
    public RUMaps() { 
        
    }

    /**
     * Initializes all blocks, given a number of streets.
     * the file was opened by the constructor - use StdIn to continue reading the file
     * @param numStreets the number of streets
     * @return an ArrayList of blocks
     */
    public ArrayList<Block> initializeBlocks(int numStreets) {
        ArrayList<Block> blocks = new ArrayList<>();

        for(int i = 0; i < numStreets; i++) {
            String streetName = StdIn.readLine();
            int numBlocks = StdIn.readInt();
            StdIn.readLine();

            for(int j = 0; j < numBlocks; j++) {
                int blockNum = StdIn.readInt();
                int numPts = StdIn.readInt();
                double roadSize = StdIn.readDouble();
                StdIn.readLine();
                Block block = new Block(roadSize, streetName, blockNum);

                for(int k = 0; k < numPts; k++) {
                    int x = StdIn.readInt();
                    int y = StdIn.readInt();
                    Coordinate coordinate = new Coordinate(x, y);

                    if(k == 0) {
                        block.startPoint(coordinate);
                    } else {
                        block.nextPoint(coordinate);
                    }
                    StdIn.readLine();
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * This method traverses through each block and finds
     * the block's start and end points to create intersections. 
     * 
     * It then adds intersections as vertices to the "rutgers" graph if
     * they are not already present, and adds UNDIRECTED edges to the adjacency
     * list.
     * 
     * Note that .addEdge(__) ONLY adds edges in one direction (a -> b). 
     */
    public void initializeIntersections(ArrayList<Block> blocks) {
        for(Block block: blocks) {
            Coordinate start = block.getCoordinatePoints().get(0);
            Coordinate end = block.getCoordinatePoints().get(block.getCoordinatePoints().size() -1);
            int startIndex = rutgers.findIntersection(start);

            if(startIndex == -1) {
                Intersection startIntersection = new Intersection(start);
                rutgers.addIntersection(startIntersection);
                block.setFirstEndpoint(startIntersection);
            } else {
                Intersection startIntersction = rutgers.getIntersections()[startIndex];
                block.setFirstEndpoint(startIntersction);
            }
            int endIndex = rutgers.findIntersection(end);

            if(endIndex == -1) {
                Intersection endIntersection = new Intersection(end);
                rutgers.addIntersection(endIntersection);
                block.setLastEndpoint(endIntersection);
            } else {
                Intersection endIntersection = rutgers.getIntersections()[endIndex];
                block.setLastEndpoint(endIntersection);
            }

            int updateStart = rutgers.findIntersection(start);
            int updateEnd = rutgers.findIntersection(end);

            Block forward = block.copy();
            Block backward = block.copy();

            forward.setFirstEndpoint(block.getFirstEndpoint());
            forward.setLastEndpoint(block.getLastEndpoint());
            backward.setFirstEndpoint(block.getLastEndpoint());
            backward.setLastEndpoint(block.getFirstEndpoint());

            rutgers.addEdge(updateStart, forward);
            rutgers.addEdge(updateEnd, backward);
        }
    }

    /**
     * Calculates the length of a block by summing the distances between consecutive points for all points in the block.
     * 
     * @param block The block whose length is being calculated
     * @return The total length of the block
     */
    public double blockLength(Block block) {
        double totalLength = 0.0;
        ArrayList<Coordinate> points = block.getCoordinatePoints();

        for(int i = 0; i < points.size() -1; i++) {
            Coordinate a = points.get(i);
            Coordinate b = points.get(i + 1);
            totalLength += coordinateDistance(a, b);
        }
        return totalLength;
    }

    /**
     * Use a DFS to traverse through blocks, and find the order of intersections
     * traversed starting from a given intersection (as source).
     * 
     * Implement this method recursively, using a helper method.
     */
    public ArrayList<Intersection> reachableIntersections(Intersection source) {
        ArrayList<Intersection> result = new ArrayList<>();
        Set<Intersection> visited = new HashSet<>();
        dfsHelper(source, visited, result);
        return result;
    }
    private void dfsHelper(Intersection curr, Set<Intersection> visited, ArrayList<Intersection> result) {
        if(curr == null || visited.contains(curr)) {
            return;
        }
        visited.add(curr);
        result.add(curr);

        int currIndex = rutgers.findIntersection(curr.getCoordinate());
        Block block = rutgers.getAdjacencyList()[currIndex];

        while(block != null) {  
            Intersection neighbor = block.getLastEndpoint();
            dfsHelper(neighbor, visited, result);
            block = block.getNext();
        }
    }

    /**
     * Finds and returns the path with the least number of intersections (nodes) from the start to the end intersection.
     * 
     * - If no path exists, return an empty ArrayList.
     * - This graph is large. Find a way to eliminate searching through intersections that have already been visited.
     * 
     * @param start The starting intersection
     * @param end The destination intersection
     * @return The path with the least number of turns, or an empty ArrayList if no path exists
     */
    public ArrayList<Intersection> minimizeIntersections(Intersection start, Intersection end) {
        Map<Intersection, Intersection> edgeTo = new HashMap<>();
        Set<Intersection> visited = new HashSet<>();
        Queue<Intersection> queue = new Queue<>();

        visited.add(start);
        queue.enqueue(start);

        while(!queue.isEmpty()) {
            Intersection curr = queue.dequeue();

            if(curr.equals(end)){
                break;
            }
            int currIndex = rutgers.findIntersection(curr.getCoordinate());
            Block block = rutgers.getAdjacencyList()[currIndex];

            while(block != null) {
                Intersection neighbor = block.getLastEndpoint();

                if(!visited.contains(neighbor)){
                    visited.add(neighbor);
                    edgeTo.put(neighbor, curr);
                    queue.enqueue(neighbor);
                }
                block = block.getNext();

            }
        }
        ArrayList<Intersection> path = new ArrayList<>();
        Intersection v = end;

        while(v != null && edgeTo.containsKey(v)){
            path.add(v);
            v = edgeTo.get(v);
        }
        if(v != null && v.equals(start)){
            path.add(start);
            Collections.reverse(path);
            return path;
        }
        return new ArrayList<>();
    }

    /**
     * Finds the path with the least traffic from the start to the end intersection using a variant of Dijkstra's algorithm.
     * The traffic is calculated as the sum of traffic of the blocks along the path.
     * 
     * What is this variant of Dijkstra?
     * - We are using traffic as a cost - we extract the lowest cost intersection from the fringe.
     * - Once we add the target to the done set, we're done. 
     * 
     * @param start The starting intersection
     * @param end The destination intersection
     * @return The path with the least traffic, or an empty ArrayList if no path exists
     */
    public ArrayList<Intersection> fastestPath(Intersection start, Intersection end) {
        Map<Intersection, Double> cost = new HashMap<>();
        Map<Intersection, Intersection> pred = new HashMap<>();
        Set<Intersection> done = new HashSet<>();
        ArrayList<Intersection> fringe = new ArrayList<>();

        cost.put(start, 0.0);
        fringe.add(start);

        while(!fringe.isEmpty()){
            Intersection curr = fringe.get(0);
            for(int i = 0; i < fringe.size(); i++) {
                Intersection candidate = fringe.get(i);

                if(cost.get(candidate) < cost.get(curr)){
                    curr = candidate;
                }
            }
            fringe.remove(curr);
            done.add(curr);

            if(curr.equals(end)){
                break;
            }
            int currIndex = rutgers.findIntersection(curr.getCoordinate());
            Block block = rutgers.getAdjacencyList()[currIndex];

            while(block != null) {
                Intersection neighbor = block.getLastEndpoint();
                double edgeWeight = block.getTraffic();

                if(done.contains(neighbor)){
                    block = block.getNext();
                    continue;
                }
                double newCost = cost.get(curr) + edgeWeight;

                if(!cost.containsKey(neighbor) || newCost < cost.get(neighbor)){
                    cost.put(neighbor, newCost);
                    pred.put(neighbor, curr);
                    if(!fringe.contains(neighbor)){
                        fringe.add(neighbor);
                    }
                }
                block = block.getNext();
            }
        }
        ArrayList<Intersection> path = new ArrayList<>();
        Intersection v = end;
        
        while(v != null && pred.containsKey(v)){
            path.add(v);
            v = pred.get(v);
        }
        if(v != null && v.equals(start)){
            path.add(start);
            Collections.reverse(path);
            return path;
        }
        return new ArrayList<>();
    }

    /**
     * Calculates the total length, average experienced traffic factor, and total traffic for a given path of blocks.
     * 
     * You're given a list of intersections (vertices); you'll need to find the edge in between each pair.
     * 
     * Compute the average experienced traffic factor by dividing total traffic by total length.
     *  
     * @param path The list of intersections representing the path
     * @return A double array containing the total length, average experienced traffic factor, and total traffic of the path (in that order)
     */
    public double[] pathInformation(ArrayList<Intersection> path) {
        double totalLength = 0.0;
        double totalTraffic = 0.0;

        if(path == null || path.size() < 2) {
            return new double[]{0,0,0};
        }
        for(int i = 0; i < path.size() -1; i++){
            Intersection curr = path.get(i);
            Intersection next = path.get(i + 1);
            int currIndex = rutgers.findIntersection(curr.getCoordinate());
            Block block = rutgers.getAdjacencyList()[currIndex];

            while(block != null) {
                if(block.getLastEndpoint().equals(next)){
                    totalLength += block.getLength();
                    totalTraffic += block.getTraffic();
                    break;
                }
                block = block.getNext();
            }
        }
        double avgTraffic = totalLength == 0?0: totalTraffic / totalLength;
        return new double[]{totalLength, avgTraffic, totalTraffic};
    }

    /**
     * Calculates the Euclidean distance between two coordinates.
     * PROVIDED - do not modify
     * 
     * @param a The first coordinate
     * @param b The second coordinate
     * @return The Euclidean distance between the two coordinates
     */
    private double coordinateDistance(Coordinate a, Coordinate b) {
        // PROVIDED METHOD

        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Calculates and returns a randomized traffic factor for the block based on a Gaussian distribution.
     * 
     * This method generates a random traffic factor to simulate varying traffic conditions for each block:
     * - < 1 for good (faster) conditions
     * - = 1 for normal conditions
     * - > 1 for bad (slower) conditions
     * 
     * The traffic factor is generated with a Gaussian distribution centered at 1, with a standard deviation of 0.2.
     * 
     * Constraints:
     * - The traffic factor is capped between a minimum of 0.5 and a maximum of 1.5 to avoid extreme values.
     * 
     * @param block The block for which the traffic factor is calculated
     * @return A randomized traffic factor for the block
     */
    public double blockTrafficFactor(Block block) {
        double rand = StdRandom.gaussian(1, 0.2);
        rand = Math.max(rand, 0.5);
        rand = Math.min(rand, 1.5);
        return rand;
    }

    /**
     * Calculates the traffic on a block by the product of its length and its traffic factor.
     * 
     * @param block The block for which traffic is being calculated
     * @return The calculated traffic value on the block
     */
    public double blockTraffic(Block block) {
        // PROVIDED METHOD
        
        return block.getTrafficFactor() * block.getLength();
    }

    public Network getRutgers() {
        return rutgers;
    }
}
