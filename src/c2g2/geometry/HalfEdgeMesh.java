package c2g2.geometry;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector3f;


import c2g2.engine.graph.Mesh;

/*
 * A mesh represented by HalfEdge data structure
 */
public class HalfEdgeMesh {

	private ArrayList<HalfEdge> halfEdges;
	private ArrayList<Vertex> vertices;
	private boolean DEBUG = true;

	/* TODO (part 1):
	 *   Build HalfEdge mesh from a triangle mesh.
	 *
	 *   Before beginning, take a look at the Vertex, Face and HalfEdge
	 *   classes to see the fields you will need to set.
	 */
	public HalfEdgeMesh(Mesh mesh) {  
		float[] pos = mesh.getPos();
		float[] norms = mesh.getNorms();
		int[] inds = mesh.getInds();

		halfEdges = new ArrayList<>();
		vertices = new ArrayList<>();

		// Hint: You will need multiple passes of the arrays to set up 
		// each geometry object. A suggestion is to:
		//   (1) Create vertex list
		//   (2) Create half edge list and set faces
		//   (3) Set flip edges

		// student code starts here

		//   (1) Create vertex list
		for (int i = 0; i < pos.length / 3; ++i) {
			// Create new vertex object
			Vector3f vPos = new Vector3f(pos[3*i], pos[3*i+1], pos[3*i+2]);
			Vector3f vNorm = new Vector3f(norms[3*i], norms[3*i+1], norms[3*i+2]);
			Vertex v = new Vertex(i, vPos, vNorm); 

			if (DEBUG)
				System.out.println("Vertex " + (i+1) + " pos " + vPos + " norm " + vNorm);

			vertices.add(v);
		}



		//   (2) Create half edge list and set faces
		for (int i = 0; i < inds.length / 3; ++i) {
			
			if (DEBUG)
				System.out.println("Face " + (i+1));
			// Loop through the 3 edges of the face 
			for (int j = 0; j < 3; ++j) {
				int idx1 = inds[3*i+j];
				int idx2 = j == 2 ? inds[3*i] : inds[3*i+j+1];

				// if (DEBUG)
				// 	System.out.println("        Edge " + inds[3*i+j] + "-" + inds[3*i+j+1]);

				// // Correct for zero-indexing
				// // --idx1;
				// // --idx2;

				// Vertex v1 = vertices.get(idx1);
				// Vertex v2 = vertices.get(idx2);
			}
		}
	}

	/* TODO (part 1):
	 *   Convert this HalfEdgeMesh into an indexed triangle mesh. 
	 *   This index triangle mesh will be used by the OpenGL engine 
	 *   (implemented in c2g2.game.DummyGame) to render the mesh on
	 *   the screen.
	 *
	 *   Note that this HalfEdgeMesh data structure is not intended 
	 *   to store texture coordinates, so to create a new Mesh instance
	 *   one can just pass an array of zeros. 
	 */
	public Mesh toMesh() {
		// student code starts here


		// Return a Mesh object instead of null
		return null;
	}

	/* Remove the first vertex from the HalfEdgeMesh. 
	 * Requires implementation of removeVertex() below.
	 */
	public void removeFirstVertex(){
		if (halfEdges.isEmpty()) return;

		Vertex vertex = halfEdges.get(0).getNextV();
		removeVertex(vertex);
	}

	/* Collapse the first edge from the HalfEdgeMesh. 
	 * Requires implementation of collapseEdge() below.
	 */
	public void collapseFirstEdge(){
		if (halfEdges.isEmpty()) return;

		HalfEdge edge = halfEdges.get(0);
		Vertex v = edge.getNextV();
		Vertex u = edge.getFlipE().getNextV();
		Vertex newV = u.getAverage(v);
		newV.getNorm().normalize();
		collapseEdge(edge, newV);
	}

	/* TODO (part 1):
	 *   Remove the given vertex, and modify the half-edge data structure
	 *   accordingly to ensure the data structure remains valid.
	 * 
	 *   See the specification for the detailed requirement.
	 */
	public void removeVertex(Vertex vtx) {
		// Vertex that will inherit all edges adjacent to vtx
		Vertex inheritor = vtx.getEdge().getNextV();
		HalfEdge start = vtx.getEdge();
		Face f1 = start.getlFace();
		Face f2 = start.getFlipE().getlFace();

		// student code starts here

		// Remember to update array lists with removed edges and vertices.
	}

	/* TODO (part 1):
	 *   Collapse the given edge into a point.
	 *   All edges connected to either end of edge are  connected
	 *   to newV after collapse.
	 *
	 *   See the specification for the detailed requirement.
	 */
	public void collapseEdge(HalfEdge edge, Vertex newV) {
		// Hint: collapseHalfTriFan(...) will be useful here.

		// student code starts here


		// Remember to update array lists with removed edges and vertices.
		// newV already has a unique id and it will replace one of the 
		// removed vertices.
	}


	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public ArrayList<HalfEdge> getEdges() {
		return halfEdges;
	}


	/* For a half edge pointing from u to v, redirects all half edges
	 * pointing at v (a triangle fan) to point at newV.
	 */
	private void collapseHalfTriFan(HalfEdge start, Vertex newV) {
		HalfEdge he = start.getNextE();
		while (he != start.getFlipE()) {
			he.getFlipE().setNextV(newV);
			he = he.getFlipE().getNextE();
		}
	}
}
