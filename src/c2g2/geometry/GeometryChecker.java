package c2g2.geometry;

import java.util.HashMap;
import java.util.HashSet;

import org.joml.Vector3f;


/* Checks "niceness" of mesh geometry after and before
 * edge collapse
 */
public class GeometryChecker {

    private final float TRI_FAIRNESS_MIN_THRESH = 0.45f;
    private HalfEdgeMesh mesh;

    public GeometryChecker(HalfEdgeMesh meshh) {
        this.mesh = mesh;
    }

    public boolean passPreCheck(HalfEdge he) {
        return !willDegenerate(he);
    }

    public boolean passPostCheck(HalfEdge he, Vertex v) {
	boolean flag2 = !willDegenerate(he);
	// System.out.println("done");
	boolean flag = checkFairness(he, v);
	return flag && flag2;
        // return !willDegenerate(he) && checkFairness(he, v);
    }

    // Return true if deleting he will produce a non-manifold
    private boolean willDegenerate(HalfEdge he) {
        Vertex v0 = he.getFlipE().getNextV();
        Vertex va = he.getNextE().getNextV();
        Vertex vb = he.getFlipE().getNextE().getNextV();

        HashSet<Vertex> ns = new HashSet<>();
        // store neighbourhood of he.getNextV() except for v0, va, vb
	// System.out.println(he.getNextE().getId() + " Start at " + he.getNextE().toString());
	// System.out.println(he.getFlipE().getId() + " Terminate at " + he.getFlipE().toString());
	// boolean printed = false;
	// int cntr = 0;

	for (HalfEdge curr = he.getNextE(); curr != he.getFlipE(); 
			curr = curr.getFlipE().getNextE()) {
		// if (cntr++ > 20 && cntr < 30 && !printed) {
		// 	// printed = true;
		// 	System.out.println(curr.getId() + " " + curr.toString());
		// }
		
		
		if (curr.getNextV() != v0 &&
				curr.getNextV() != va &&
				curr.getNextV() != vb)
			ns.add(curr.getNextV()); 
	}

        for (HalfEdge curr = he.getFlipE().getNextE(); curr != he; 
            curr = curr.getFlipE().getNextE()) {
            if (ns.contains(curr.getNextV())) {
                return true;
            }
        }

        return false;

    }

    private boolean checkFairness(HalfEdge he, Vertex v) {
        HalfEdge h0 = he;
        HalfEdge lastHe = h0.getNextE();
        for (HalfEdge h = lastHe.getFlipE().getNextE(); h != h0.getFlipE(); 
            lastHe = h, h = lastHe.getFlipE().getNextE())
            if (triangleFairness(v.getPos(), h.getNextV().getPos(), 
                lastHe.getNextV().getPos()) < TRI_FAIRNESS_MIN_THRESH)
                return false;

        h0 = h0.getFlipE();
        lastHe = h0.getNextE();
        for (HalfEdge h = lastHe.getFlipE().getNextE(); h != h0.getFlipE(); 
            lastHe = h, h = lastHe.getFlipE().getNextE())
            if (triangleFairness(v.getPos(), h.getNextV().getPos(), 
                lastHe.getNextV().getPos()) < TRI_FAIRNESS_MIN_THRESH)
                return false;

        return true;

    }

    // preserve triangle "niceness" (not in paper)
    private float triangleFairness(Vector3f p0, Vector3f p1, Vector3f p2) {
        Vector3f v1 = new Vector3f(p1).sub(p0);
        Vector3f v2 = new Vector3f(p2).sub(p0);
        Vector3f cross = new Vector3f(v1).cross(v2);
        float area = 0.5f * cross.length();
        float denom = p0.distanceSquared(p1)+p1.distanceSquared(p2)+p0.distanceSquared(p2);
        return 4f*((float)Math.sqrt(3))*area/denom;
    }

}
