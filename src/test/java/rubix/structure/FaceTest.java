package rubix.structure;

import static rubix.structure.Face.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FaceTest {
	
	public static Face buildFace(
	int order, Direction normal) {
		Face face = new Face.Builder()
			.setOrder(order)
			.setNormal(normal)
			.build();
		return face;
	}
	
	@Test
	public void testAllocation() {
		buildFace(10, Direction.REAR);
		buildFaceWithSides(20, Direction.LEFT);
	}
	
	public void checkComposition(Face face) {
		Direction normal = face.getNormal();
		Direction[][] dirs = Face
			.getPlaneDirs(normal);
			
		Cube.Block p = face.getCorner(dirs[X][POS]), q;
		Color color = Color.getColor(face
			.getDef().get(normal));
		
		for(; p != null;
			p = p.getBlock(dirs[Y][NEG])) {
			
			for(q = p; q != null; 
				q = q.getBlock(dirs[X][POS])) {
				
				Color faceColor = q.getColor(normal);
				assertEquals(color, faceColor);
			}
		}
	}
	
	@Test
	public void testComposition() 
		{ checkComposition(buildFace(10, Direction.LEFT)); }
	
	@Test
	public void testPutEdge() {
		int order = 5;
		Direction normal = Direction.TOP;
		
		Face face = buildFace(order, normal);
		
		Edge edge = new Edge.Builder()
			.setEnds(Corner.New(), Corner.New())
			.setAxialDirection(Direction.TOP, Direction.RIGHT)
			.setAxialDirection(Direction.REAR, Direction.LEFT)
			.setLength(order)
			.build();
		
		face.putEdge(edge, Direction.REAR);
	}
	
	public static Face buildFaceWithSides(
	int order, Direction normal) {
		Corner[] corners = new Corner[Face.N_SIDES];
		for(int i = 0; i < corners.length; i++)
			corners[i] = Corner.New();
			
		Direction[] walls = Direction.getPlane(normal);
		Face face = buildFace(order, normal);
		
		Edge[] edges = new Edge[Face.N_SIDES];
		for(int i = 0; i < edges.length; i++) {
			int next = (i+1) % Face.N_SIDES;
			Corner start = corners[i];
			Corner end = corners[next];
			Direction axialD = walls[next];
			edges[i] = new Edge.Builder()
				.setEnds(start, end)
				.setAxialDirection(normal, axialD)
				.setAxialDirection(walls[i],
					Direction.getReverse(axialD))
				.setLength(order)
				.build();
			face.putEdge(edges[i], walls[i]);
		} 
		
		return face;
	}
}
