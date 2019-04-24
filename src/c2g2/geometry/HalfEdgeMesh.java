package c2g2.geometry;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.joml.Vector3f;


import c2g2.engine.graph.Mesh;

/*
 * A mesh represented by HalfEdge data structure
 */
public class HalfEdgeMesh {

	private ArrayList<HalfEdge> halfEdges;
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	private boolean DEBUG = false;

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
		faces = new ArrayList<>();

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
				System.out.println("Vertex " + (v.getId() + 1) + " pos " + vPos + " norm " + vNorm);

			vertices.add(v);
		}

		//   (2) Create half edge list and set faces
		HashMap<String, ArrayList<HalfEdge>> edges = new HashMap<>();

		for (int i = 0; i < inds.length / 3; ++i) {
			
			if (DEBUG)
				System.out.println("Face " + (i+1));

			Face face = new Face();
			faces.add(face);

			// Loop through the 3 edges of the face 
			for (int j = 0; j < 3; ++j) {
				int idx1 = inds[3*i+j];
				int idx2 = j == 2 ? inds[3*i] : inds[3*i+j+1];

				// NOTE: Debug message is corrected for 1-index of OBJ files
				if (DEBUG)
					System.out.println("        Edge " + (idx1 + 1) + "-" + (idx2 + 1));

				Vertex v1 = vertices.get(idx1);
				Vertex v2 = vertices.get(idx2);

				// Create new half edge
				HalfEdge he = new HalfEdge();
				he.setNextV(v2);
				he.setlFace(face);
				halfEdges.add(he);

				// Add to map of edges (for connecting half edge pairs)
				String edge = idx1 < idx2 ? idx1 + "/" + idx2 : idx2 + "/" + idx1; // Sort by lowest

				if (edges.containsKey(edge)) {
					if (DEBUG)
						System.out.println("Found pair for " + edge);
					edges.get(edge).add(he);
				}
				else {
					ArrayList<HalfEdge> list = new ArrayList<>();
					list.add(he);
					edges.put(edge,list);
				}

				// Update vertex and face
				v1.setEdge(he);
				face.setEdge(he);
			}

			// Build counter-clockwise "cycle" for each face
			for (int j = 0; j < 3; ++j) {
				int idx1 = 3 * i + j;
				int idx2 = 3 * i + ((j + 1) % 3);
				HalfEdge he = halfEdges.get(idx1);
				HalfEdge next = halfEdges.get(idx2);

				he.setNextE(next);
			}
		}
		
		//   (3) Set flip edges
		for (Map.Entry<String, ArrayList<HalfEdge>> entry : edges.entrySet()) {
			ArrayList<HalfEdge> pair = entry.getValue();
			if (pair.size() == 2) {
				HalfEdge hf1 = pair.get(0);
				HalfEdge hf2 = pair.get(1);
				hf1.setFlipE(hf2);
				hf2.setFlipE(hf1);
			}
			// Error, there should NOT be <> 2 half edges per edge
			else
				System.out.println("Mesh is disconnected OR more than two faces touch a given edge");
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
		float[] positions = new float[vertices.size() * 3];
		float[] textCoords = new float[vertices.size() * 2];
		float[] normals = new float[vertices.size() * 3];
		int[] indices = new int[faces.size() * 3];

		// Set positions and normals
		for (int i = 0; i < vertices.size(); ++i) {
			Vector3f vPos = vertices.get(i).getPos();
			Vector3f vNorm = vertices.get(i).getNorm();

			positions[3*i] = vPos.x;
			positions[3*i+1] = vPos.y;
			positions[3*i+2] = vPos.z;

			normals[3*i] = vNorm.x;
			normals[3*i+1] = vNorm.y;
			normals[3*i+2] = vNorm.z;
		}

		// Set faces
		for (int i = 0; i < faces.size(); ++i) {
			HalfEdge he = faces.get(i).getEdge();

			Vertex v1 = he.getNextV();
			Vertex v2 = he.getNextE().getNextV();
			Vertex v3 = he.getNextE().getNextE().getNextV();

			indices[3*i] = v1.getId();
			indices[3*i+1] = v2.getId();
			indices[3*i+2] = v3.getId();
		}

		Mesh mesh = new Mesh(positions, textCoords, normals, indices);

		// Return a Mesh object instead of null
		return mesh;
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
		// Keep references to half edges for fixing the mesh later
		HalfEdge topLeft = start.getNextE().getFlipE();
		HalfEdge botLeft = topLeft.getFlipE().getNextE();

		HalfEdge botRight = start.getFlipE().getNextE();
		HalfEdge topRight = botRight.getNextE().getFlipE();

		// Delete faces
		faces.remove(start.getlFace());
		faces.remove(start.getFlipE().getlFace());

		// Fix referenced faces
		botLeft.setlFace(topLeft.getlFace());
		botRight.setlFace(topRight.getlFace());

		topLeft.getlFace().setEdge(botLeft);
		topRight.getlFace().setEdge(botRight);

		// Fix vertices' referenced edges
		inheritor.setEdge(botLeft.getFlipE());
		botLeft.getFlipE().getNextV().setEdge(botLeft);
		botRight.getNextV().setEdge(botRight.getFlipE());
		
		// Collapse edges around vtx into inheritor
		collapseHalfTriFan(start.getFlipE(), inheritor);

		// Fix next edge references
		botLeft.setNextE(topLeft.getNextE());
		topLeft.getNextE().getNextE().setNextE(botLeft);

		botRight.setNextE(topRight.getNextE());
		topRight.getNextE().getNextE().setNextE(botRight);

		// Delete vertices and half edges
		vertices.remove(vtx);
		halfEdges.remove(start.getFlipE());
		halfEdges.remove(start);
		halfEdges.remove(topLeft.getFlipE());
		halfEdges.remove(topLeft);
		halfEdges.remove(topRight.getFlipE());
		halfEdges.remove(topRight);

		// Remember to update array lists with removed edges and vertices.
		resetVertexIds();
	}

	// Check if a vertex exists in our half edge mesh
	private int checkIds(Vertex v) {
		int cnt = 0;
		for (HalfEdge he : halfEdges) {
			if (he.getNextV().getId() == v.getId())
				cnt++;
		}
		return cnt;
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


		// newV.setId(9999);

		int count = checkIds(edge.getFlipE().getNextV());
		if (DEBUG)
			System.out.println("BEFORE: " + count);

		System.out.println("Collapsing " + edge.getNextV().getId() + " and " + edge.getFlipE().getNextV().getId() + " into " + newV.getId());

		// Make all edges that point to v1 point INSTEAD to newV
		Vertex v1 = edge.getNextV();
		collapseHalfTriFan(edge, newV);


		edge.setNextV(newV);

		vertices.add(newV);
		// vertices.remove(v1);


		// count = checkIds(edge.getFlipE().getNextV());
		// System.out.println("MIDDLE: " + count);


		// Store half edges that need to be fixed
		// Image reference (Figure 1)
		HalfEdge topLeft = edge.getFlipE().getNextE().getFlipE();
		HalfEdge botLeft = topLeft.getFlipE().getNextE();

		HalfEdge botRight = edge.getNextE();
		HalfEdge topRight = botRight.getNextE().getFlipE();



		// Fix affected vertices
		botLeft.getFlipE().getNextV().setEdge(botLeft);
		botRight.getNextV().setEdge(botRight.getFlipE());
		newV.setEdge(botRight);

		// Fix affected faces
		faces.remove(botLeft.getlFace());
		faces.remove(botRight.getlFace());

		botLeft.setlFace(topLeft.getlFace());
		botRight.setlFace(topRight.getlFace());

		botLeft.getlFace().setEdge(botLeft);
		botRight.getlFace().setEdge(botRight);




		// Collapse v2 into newV
		Vertex v2 = edge.getFlipE().getNextV();
		collapseHalfTriFan(edge.getFlipE(), newV);




		// Fix affected half edges
		botLeft.setNextE(topLeft.getNextE());
		topLeft.getNextE().getNextE().setNextE(botLeft);

		botRight.setNextE(topRight.getNextE());
		topRight.getNextE().getNextE().setNextE(botRight);
		
		// Remove affected half edges and vertices
		// vertices.remove(v2);

		halfEdges.remove(topLeft.getFlipE());
		halfEdges.remove(topLeft);
		halfEdges.remove(topRight.getFlipE());
		halfEdges.remove(topRight);
		halfEdges.remove(edge.getFlipE());
		halfEdges.remove(edge);


		count = checkIds(edge.getFlipE().getNextV());
		if (DEBUG)
			System.out.println("AFTER: " + count);

		newV.setId(edge.getFlipE().getNextV().getId());

		// Clean up
		// resetVertexIds();

	}

	private void resetVertexIds() {
		int id = 0;

		for (Vertex v : vertices)
			v.setId(id++);
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
