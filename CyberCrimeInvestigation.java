package investigation;

import java.util.ArrayList;


/*  
 * This class represents a cyber crime investigation.  It contains a directory of hackers, which is a resizing
 * hash table. The hash table is an array of HNode objects, which are linked lists of Hacker objects.  
 * 
 * The class contains methods to add a hacker to the directory, remove a hacker from the directory.
 * You will implement these methods, to create and use the HashTable, as well as analyze the data in the directory.
 * 
 * @author Colin Sullivan
 */
public class CyberCrimeInvestigation {
       
    private HNode[] hackerDirectory;
    private int numHackers = 0; 

    public CyberCrimeInvestigation() {
        hackerDirectory = new HNode[10];
    }

    /**
     * Initializes the hacker directory from a file input.
     * @param inputFile
     */
    public void initializeTable(String inputFile) { 
        // DO NOT EDIT
        StdIn.setFile(inputFile);  
        while(!StdIn.isEmpty()){
            addHacker(readSingleHacker());
        }
    }

    /**
     * Reads a single hackers data from the already set file,
     * Then returns a Hacker object with the data, including 
     * the incident data.
     * 
     * StdIn.setFile() has already been called for you.
     * 
     * @param inputFile The name of the file to read hacker data from.
     */
     public Hacker readSingleHacker(){ 
        String name = StdIn.readLine();
        String ipAddyHash = StdIn.readLine();
        String location = StdIn.readLine();
        String os = StdIn.readLine();
        String webServer = StdIn.readLine();
        String date = StdIn.readLine();
        String urlHash = StdIn.readLine();

        Incident incident = new Incident(os, webServer, date, location, ipAddyHash, urlHash);
        Hacker hacker = new Hacker(name);
        hacker.addIncident(incident);

        return hacker; 
    }

    /**
     * Adds a hacker to the directory.  If the hacker already exists in the directory,
     * instead adds the given Hacker's incidents to the existing Hacker's incidents.
     * 
     * After a new insertion (NOT if a hacker already exists), checks if the number of 
     * hackers in the table is >= table length divided by 2. If so, calls resize()
     * 
     * @param toAdd
     */
    public void addHacker(Hacker toAdd) {
        int index = toAdd.hashCode() % hackerDirectory.length;

        HNode curr = hackerDirectory[index];

        if(curr == null) {
            hackerDirectory[index] = new HNode(toAdd);
            numHackers++;

            if(numHackers > (hackerDirectory.length/2)) {
                resize();
            }
            return;
        }
        HNode prev = null;

        while(curr != null) {
            if(curr.getHacker().getName().equals(toAdd.getName())){
                curr.getHacker().getIncidents().addAll(toAdd.getIncidents());
                return;
            }
            prev = curr;
            curr = curr.getNext();
        }
        prev.setNext(new HNode(toAdd));
        numHackers++;

        if(numHackers >= (hackerDirectory.length / 2)){
            resize();
        }
    }

    /**
     * Resizes the hacker directory to double its current size.  Rehashes all hackers
     * into the new doubled directory.
     */
    private void resize() {
        HNode[] temp = hackerDirectory;
        hackerDirectory = new HNode[temp.length * 2];
        numHackers = 0;

        for(int i = 0; i < temp.length; i++) {
            HNode node = temp[i];
            while(node != null){
                addHacker(node.getHacker());
                node = node.getNext();
            }
        }
    }

    /**
     * Searches the hacker directory for a hacker with the given name.
     * Returns null if the Hacker is not found
     * 
     * @param toSearch
     * @return The hacker object if found, null otherwise.
     */
    public Hacker search(String toSearch) {
        int index = Math.abs(toSearch.hashCode()) % hackerDirectory.length;
        HNode curr = hackerDirectory[index];

        while(curr != null) {
            if(curr.getHacker().getName().equals(toSearch)){
                return curr.getHacker();
            }
            curr = curr.getNext();
        }
        return null;
    }

    /**
     * Removes a hacker from the directory.  Returns the removed hacker object.
     * If the hacker is not found, returns null.
     * 
     * @param toRemove
     * @return The removed hacker object, or null if not found.
     */
    public Hacker remove(String toRemove) {
        int index = Math.abs(toRemove.hashCode()) % hackerDirectory.length;
        HNode curr = hackerDirectory[index];
        HNode prev = null;

        while(curr != null) {
            if(curr.getHacker().getName().equals(toRemove)){
                Hacker removeHacker = curr.getHacker();

                if(prev == null) {
                    hackerDirectory[index] = curr.getNext();
                } else {
                    prev.setNext(curr.getNext());
                }
                numHackers --;
                return removeHacker;
            }
            prev = curr;
            curr = curr.getNext();
        }
        return null;
    } 

    /**
     * Merges two hackers into one based on number of incidents.
     * 
     * @param hacker1 One hacker
     * @param hacker2 Another hacker to attempt merging with
     * @return True if the merge was successful, false otherwise.
     */
    public boolean mergeHackers(String hacker1, String hacker2) {  
        Hacker hackerA = search(hacker1);
        Hacker hackerB = search(hacker2);

        if(hackerA == null || hackerB == null) {
            return false;
        }

        Hacker keeper;
        Hacker remover;

        if(hackerA.getIncidents().size() > hackerB.getIncidents().size()){
            keeper = hackerA;
            remover = hackerB;
        } else if(hackerA.getIncidents().size() < hackerB.getIncidents().size()){
            keeper = hackerB;
            remover = hackerA;
        } else {
            keeper = hackerA;
            remover = hackerB;
        }

        keeper.getIncidents().addAll(remover.getIncidents());
        keeper.getAliases().add(remover.getName());
        remove(remover.getName());

        return true;

    }

    /**
     * Gets the top n most wanted Hackers from the directory, and
     * returns them in an arraylist. 
     * 
     * You should use the provided MaxPQ class to do this. You can
     * add all hackers, then delMax() n times, to get the top n hackers.
     * 
     * @param n
     * @return Arraylist containing top n hackers
     */
    public ArrayList<Hacker> getNMostWanted(int n) {
        MaxPQ<Hacker> pq = new MaxPQ<>();
        ArrayList<Hacker> mostWanted = new ArrayList<>();

        for(int i = 0; i < hackerDirectory.length; i++) {
            HNode node = hackerDirectory[i];
            while(node != null) {
                pq.insert(node.getHacker());
                node = node.getNext();
            }
        }
        for(int i = 0; i < n && !pq.isEmpty(); i++) {
            mostWanted.add(pq.delMax());
        }

        return mostWanted;
    }

    /**
     * Gets all hackers that have been involved in incidents at the given location.
     * 
     * You should check all hackers, and ALL of each hackers incidents.
     * You should not add a single hacker more than once.
     * 
     * @param location
     * @return Arraylist containing all hackers who have been involved in incidents at the given location.
     */
    public ArrayList<Hacker> getHackersByLocation(String location) {
        ArrayList<Hacker> hackers = new ArrayList<>();

        for(int i = 0; i < hackerDirectory.length; i++) {
            HNode node = hackerDirectory[i];

            while(node != null){
                Hacker hacker = node.getHacker();
                ArrayList<Incident> incidents = hacker.getIncidents();

                for(Incident incident : incidents) {
                    if(incident.getLocation().equals(location)){
                        if(!hackers.contains(hacker)){
                            hackers.add(hacker);
                        }
                        break;
                    }
                }
                node = node.getNext();
            }
        }
        return hackers;
    }
  

    /**
     * PROVIDED--DO NOT MODIFY!
     * Outputs the entire hacker directory to the terminal. 
     */
     public void printHackerDirectory() { 
        System.out.println(toString());
    } 

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.hackerDirectory.length; i++) {
            HNode headHackerNode = hackerDirectory[i];
            while (headHackerNode != null) {
                if (headHackerNode.getHacker() != null) {
                    sb.append(headHackerNode.getHacker().toString()).append("\n");
                    ArrayList<Incident> incidents = headHackerNode.getHacker().getIncidents();
                    for (Incident incident : incidents) {
                        sb.append("\t" +incident.toString()).append("\n");
                    }
                }
                headHackerNode = headHackerNode.getNext();
            } 
        }
        return sb.toString();
    }

    public HNode[] getHackerDirectory() {
        return hackerDirectory;
    }
}
