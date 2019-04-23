 package c2g2.geometry;

import java.util.HashMap;
import java.lang.Math;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;


/* Computes quadrics of vertices and cost of edges */
public class GarlandMeasurer extends Measurer {

    /* Create measurer for the given mesh.
     * Stores quadrics in quadMap using vertex ids as keys.
     */
    public GarlandMeasurer(HalfEdgeMesh mesh) {
        this.mesh = mesh;
        this.quadMap = new HashMap<>();
    }

    // call before simplification
    public void init() {
        quadMap.clear();
        for (Vertex v : mesh.getVertices()) updateQuadric(v);
    }

    /* TODO (part 2):
     *   Compue the collapse cost of an edge (v0, v1) and store
     *   the optimal position in newV.
     *
     *   This is step (3) of algorithm in the Garland and Heckbert paper.
     *   Returns the cost of newV.
     */
    public float collapseCost(HalfEdge he, Vertex newV) {
        // Hint: Our representation of a quadric has the form
        //      m = [Q b
        //           0 d]
        //   with inverse
        //   m^-1 = [Q^-1  -(1/d)(Q^-1)*b
        //             0        1/d      ]
        //   so the quadric is invertible <=> Q is invertible
        
        // student code goes here 

        return 0;
    }

    /* Update quadric of newV from v0's and v1's */ 
    public void edgeCollapsed(Vertex v0, Vertex v1, Vertex newV) {
        int i1 = v0.getId();
        int i2 = v1.getId();
        assert (quadMap.containsKey(i1) && quadMap.containsKey(i2));
        Quadric q = new Quadric();
        quadMap.get(i1).add(quadMap.get(i2), q);
        quadMap.remove(i1);
        quadMap.remove(i2);
        quadMap.put(newV.getId(), q);
    }

    /* TODO (part 2): 
     *   Compute quadric at vertex v. 
     *
     *   Use face normal (see Face.getNormal()) and orgin (newV) to find 
     *   (a,b,c,d) to build the fundamental error quadric K for each
     *   triangle around newV.
     *   Note that the bottom row is zero except for the last value.
     */
    private void updateQuadric(Vertex v) {
        // student code goes here

    }
  
}