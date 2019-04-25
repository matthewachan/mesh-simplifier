package c2g2.geometry;

import java.util.ArrayList;
import java.lang.Comparable;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;


public class MeshSimplifier {
    private final float EP = 1.0e-6f;
    // good ratios - bunny: 0.005, sphere: 0.5
    private final float STOP_RATIO;

    private HalfEdgeMesh mesh;
    private Measurer measurer;
    private GeometryChecker checker;
    private PriorityQueue<EdgeRecord> pque;
    private HashMap<Integer, EdgeRecord> validPairs;
    private int startNumOfFaces;
    private boolean DEBUG = false;

    /* Helper class containing information about a valid edge inserted
     * in the minheap.
     */
    private class EdgeRecord implements Comparable<EdgeRecord> {
        int id; // should match halfedge id
        /* note that Java PQ does not support an update functionality
         * which ideally is implemented in O(log n). A work-around is
         * to reinsert the element with updated cost into the heap and
         * ignore any old instances already in it.
         */
        boolean mostRecent;
        HalfEdge he;
        float cost;
        Vertex v; // new vertex after collapseCost()

        EdgeRecord(HalfEdge he) {
            this.mostRecent = true;
            this.he = he; 
            this.v = new Vertex(he.getFlipE().getNextV());
        }

        EdgeRecord(EdgeRecord rec) {
            this.mostRecent = true;
            this.he = rec.he;
            this.v = rec.v;
            this.id = rec.id;
        }

        @Override
        public int compareTo(EdgeRecord o) {
            return (int)((this.cost - o.cost)/EP);
        }
    }


    public MeshSimplifier(HalfEdgeMesh mesh, float ratio) {
        this.mesh = mesh;
        this.STOP_RATIO = ratio;
        this.measurer = new GarlandMeasurer(mesh);
        this.checker = new GeometryChecker(mesh);
        this.pque = new PriorityQueue<>();
        this.validPairs = new HashMap<>();
        this.startNumOfFaces = mesh.getEdges().size()/3;
    }

    /* TODO (part 2):
     *   Garland simplification algorithm:
     *   1. For each edge
     *      - If edge passes pre-check, then it is a valid pair, 
     *         - Create EdgeRecord with unique id
     *         - Add it to validPairs
     *   2. Place all valid pairs in the priority queue
     *   3. Pop edges from the PQ while shouldStop() == False
     *      - If edge passes post-check
     *         - Collapse edge and update costs
     */
    public HalfEdgeMesh simplify() {
        // init PQ
        measurer.init();
        ArrayList<HalfEdge> edges = mesh.getEdges();
        startNumOfFaces = edges.size()/3;

        // use currId as the id of a newly created record
        // make sure to also set the id of the respective
        // edge to be the same
        int currId = 0;

        // Hint: Make sure to only use most recent record in the PQ
        
        /* student code goes here */

        // 1. For each edge
        //    - If edge passes pre-check, then it is a valid pair, 
        //       - Create EdgeRecord with unique id
        //       - Add it to validPairs
        // 2. Place all valid pairs in the priority queue
	for (HalfEdge he : edges) {
		if (checker.passPreCheck(he) && he.getId() == -1) {
			EdgeRecord eRec = new EdgeRecord(he);
			he.setId(currId);

			// Generate a new vertex 
			Vertex v = he.getNextV();
			Vertex u = he.getFlipE().getNextV();
			Vertex newV = u.getAverage(v);
			newV.getNorm().normalize();

			// Check vertex IDs
			// if (DEBUG)
			// 	System.out.println("Pair " + he.getNextV().getId() + " and " + he.getFlipE().getNextV().getId() + " into " + newV.getId());

			// Compute the cost of collapsing this edge pair
			float cost = measurer.collapseCost(he, newV);

			// Fill out EdgeRecord 
			eRec.id = currId;
			eRec.cost = cost;
			eRec.v = newV;

			validPairs.put(currId, eRec);
			pque.add(eRec);

			++currId;
		}
	}

        // 3. Pop edges from the PQ while shouldStop() == False
        //    - If edge passes post-check
        //       - Collapse edge and update costs
	// for (int i = 0; i < 15; ++i) {
	
	while (!shouldStop()) {
		EdgeRecord eRec = pque.poll();
		HalfEdge he = eRec.he;
		Vertex newV = eRec.v;
		
		if (!eRec.mostRecent)
			continue;

		// System.out.println("DEBUG: Pulled " + he.getId() + " " + he.toString() + " from the heap (" + eRec.mostRecent + ")");

		// System.out.println("DEBUG: Pulled " + he.toString());
		if (edgeInMesh(he) && checker.passPostCheck(he, newV)) { 
			// if (DEBUG) 
			// 	System.out.println("DEBUG: Collapse edge " + he.getId());
			int id = collapseEdge(he, newV, currId++);
		}

	}


        /* student code ends here */

        validPairs.clear();
        pque.clear();
        System.out.println("New simplified mesh of " + mesh.getEdges().size()/3 + " faces from " + startNumOfFaces);

        return mesh;
    }

    // Check if a given half edge exists in the mesh
    private boolean edgeInMesh(HalfEdge edge) {
	    for (HalfEdge he : mesh.getEdges()) {
		    if (edge == he)
			    return true;
	    }
	    return false;
    }

    /* Collapse edge onto newV, update affected Quadrics,
     * update costs per vertex. Note that affected edges
     * are reinserted into the queue as new EdgeRecords.
     # Return new id after updated edges have been added.
     */
    private int collapseEdge(HalfEdge edge, Vertex newV, int id) {
        // store two vertices connected to the newly created edges
        Vertex va = edge.getNextE().getNextV();
        Vertex vb = edge.getFlipE().getNextE().getNextV();

        // remove edges asssociated to the two collapsed faces from valid pairs
        validPairs.remove(edge.getId());
        validPairs.remove(edge.getNextE().getId());
        validPairs.remove(edge.getNextE().getNextE().getId());
        validPairs.remove(edge.getFlipE().getNextE().getId());
        validPairs.remove(edge.getFlipE().getNextE().getNextE().getId());

        // collapse edge in the measurer and mesh instance
        // this updates the quadric map by adding sum of quadrics in contracted edge
	// Check that newV.ID and half edge vertices' IDs are correct
	// if (DEBUG) {
	// 	for (Map.Entry<Integer, EdgeRecord> entry : validPairs.entrySet()) {
	// 		EdgeRecord eRec = entry.getValue();
	// 		int id1 = eRec.he.getNextV().getId();
	// 		int id2 = eRec.he.getFlipE().getNextV().getId();
	// 		if (eRec.v.getId() != id1 && eRec.v.getId() != id2) {
	// 			eRec.v.setId(id2);
	// 		}
	// 	}
	// }
	if (edge.getNextV().getId() != newV.getId() && edge.getFlipE().getNextV().getId() != newV.getId())
		newV.setId(edge.getFlipE().getNextV().getId());

	System.out.println("DEBUG: Collapsing " + edge.toString() + " into " + newV.getId());

        measurer.edgeCollapsed(edge.getFlipE().getNextV(), edge.getNextV(), newV);
        mesh.collapseEdge(edge, newV);
	

        HashSet<HalfEdge> edgesToUpdate = new HashSet<>();
        HalfEdge he0 = newV.getEdge();

	// if (DEBUG && he0.getNextV().getId() == newV.getId())
	// 	System.out.println("no bueno");

        HalfEdge currHe = he0;
        // get set of vertices whose quadric is affected by deleting edge
        do {
            Vertex currV = currHe.getNextV();
            HalfEdge currHe0 = currV.getEdge();
            HalfEdge cCurrHe = currHe0;
            do {
                if (!edgesToUpdate.contains(cCurrHe.getFlipE()))
                    edgesToUpdate.add(cCurrHe);
                cCurrHe = cCurrHe.getFlipE().getNextE();
            } while(cCurrHe != currHe0);
            currHe = currHe.getFlipE().getNextE();
        } while (currHe != he0);

        // traverse possibly affected edges and update them
        for (HalfEdge newHe : edgesToUpdate) {
            boolean precheckPassed = checker.passPreCheck(newHe);
            Vertex head = newHe.getNextV();
            Vertex tail = newHe.getFlipE().getNextV();
            if ((head == newV && (tail == va || tail == vb)) ||
                (tail == newV && (head == va || head == vb))) {
                // newHe is the newly created edge
                if (precheckPassed) {
                    EdgeRecord rec = new EdgeRecord(newHe);
                    rec.cost = measurer.collapseCost(newHe, rec.v);
                    rec.id = id++;
                    newHe.setId(rec.id);
                    pque.add(rec);
                    validPairs.put(rec.id, rec);
                }
            } else {
                if (precheckPassed) {
                    if (validPairs.containsKey(newHe.getId())) {
                        // update cost of already valid pair
                        EdgeRecord recOld = validPairs.get(newHe.getId());
                        EdgeRecord recNew = new EdgeRecord(recOld);
                        recNew.cost = measurer.collapseCost(newHe, recNew.v);
                        recOld.mostRecent = false;
                        pque.add(recNew);
                        validPairs.put(recNew.id, recNew);
                    } else {
                        // not added into the queue before
                        EdgeRecord rec = new EdgeRecord(newHe);
                        rec.id = id++;
                        rec.cost = measurer.collapseCost(newHe, rec.v);
                        newHe.setId(rec.id);
                        pque.add(rec);
                        validPairs.put(rec.id, rec);
                    }
                } else {
                    validPairs.remove(newHe.getId());
                }
            }
        }

	// Check that all half edge cycles exit
	// for (HalfEdge he : mesh.getEdges()) {
	// 	if (he.getNextE().getId() == 239) {
	// 		System.out.println(he.getNextE().getId() + " Start at " + he.getNextE().toString());
	// 		System.out.println(he.getFlipE().getId() + " Terminate at " + he.getFlipE().toString());
	// 		// HalfEdge he = newV.getEdge().getFlipE();
	// 		int cntr = 0;
	// 		for (HalfEdge curr = he.getNextE(); curr != he.getFlipE(); 
	// 				curr = curr.getFlipE().getNextE()) {
	// 			System.out.println("Stuck " + curr.getFlipE().getNextE().toString() + " ID " + curr.getFlipE().getNextE().getId());
	// 			// if (cntr++ > 20) {
	// 			// 	System.out.println(curr.getFlipE().getNextE().getId() + " Stuck on : " + curr.getFlipE().getNextE().toString());
	// 			// 	break;
	// 			// }
	// 				}
	// 	}
	// }

        return id;
    }

    private boolean shouldStop() {
        return startNumOfFaces*STOP_RATIO > mesh.getEdges().size()/3;
    }
}
