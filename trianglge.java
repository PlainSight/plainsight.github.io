package Core;

import java.util.ArrayList;

public class Triangle {
	public Vertex[] vertices;
	
	public ArrayList<Triangle> adjacent = new ArrayList<Triangle>();

	public boolean pathable = true;
		
	public Triangle(Vertex[] vs) {
		vertices = vs;
		pathable = !(vs[0].obstacle == vs[1].obstacle && vs[1].obstacle == vs[2].obstacle && vs[0].obstacle != null);
	}
		
	public boolean HasVertex(Vertex other) {
		
		for(int i = 0; i < vertices.length; i++) {
			if(vertices[i].x == other.x && vertices[i].y == other.y) {
				return true;
			}
		}
		return false;
	}
			
	public ArrayList<Triangle> getValidAdjacent() {
		
		ArrayList<Triangle> validAdjacent = new ArrayList<Triangle>();
		
		for(int i = 0; i < adjacent.size(); i++) {
			Vertex[] gateway = sharedVertices(adjacent.get(i));
			
			if(gateway[0].distance2(gateway[1]) > 500) {
				validAdjacent.add(adjacent.get(i));
			}
		}

		return validAdjacent;
	}
	
	boolean pointInTraingle(int x, int y) {
		return vertexInTriangle(new Vertex(x, y));
	}
	
	Vertex[] sharedVertices(Triangle other) {
		
		Vertex[] shared = new Vertex[2];
		
		int numfound = 0;
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(vertices[i].x == other.vertices[j].x && vertices[i].y == other.vertices[j].y) {
					shared[numfound++] = vertices[i];
				}
			}
		}
		
		return shared;
	}
	
	Vertex uniqueVertex(Triangle other) {
		
		for(int i = 0; i < 3; i++) {
			boolean equal = false;
			for(int j = 0; j < 3; j++) {
				if(vertices[i].x == other.vertices[j].x && vertices[i].y == other.vertices[j].y) {
					equal = true;
				}
			}
			if(!equal) {
				return vertices[i];
			}
		}
		
		return null;
	}
	
	boolean vertexOnTriangleEdge(Vertex v) {
		
		for(int i = 0; i < 3; i++) {
			
			Vertex a = vertices[i];
			Vertex b = vertices[(i+1)%3];
			
			boolean on = onLine(v, a, b);
			
			if(on) {
				System.out.println("Vertex on Edge of Triangle");
				System.out.println(Integer.toHexString(this.hashCode()));
				System.out.println("v: " + v.x + " " + v.y);
				System.out.println("a: " + a.x + " " + a.y);
				System.out.println("b: " + b.x + " " + b.y);

				return true;
			}
		}
		return false;
	}
	
	private boolean onLine(Vertex v, Vertex a, Vertex b) {
		int m_numerator = b.x - a.x;
		int m_denominator = b.y - a.y;
		
		boolean onLine = false;
		
		if(m_denominator == 0) {
			onLine = a.y == v.y;
		} else {
			onLine = m_denominator * (v.y - a.y) ==  m_numerator * (v.x - a.x);
		}
		
		if(v.x < Math.min(a.x, b.x) || v.x > Math.max(a.x, b.x) || v.y < Math.min(a.y, b.y) || v.y > Math.max(a.y, b.y)) {
			onLine = false;
		}	
		
		return onLine;
	}
	
	Triangle[] addVertexToEdge(Vertex v) {
									
		Triangle oldOne = this;
		Triangle oldTwo = null;
		
		for(Triangle tempAdj : oldOne.adjacent) {
			if(tempAdj.vertexOnTriangleEdge(v)){
				oldTwo = tempAdj;
				break;
			}
		}
		
		Vertex uniqueVertexOne = oldOne.uniqueVertex(oldTwo);
		Vertex uniqueVertexTwo = oldTwo.uniqueVertex(oldOne);
		Vertex[] sharedVertices = oldOne.sharedVertices(oldTwo);
		
		Triangle t1 = new Triangle(new Vertex[] { v, uniqueVertexOne, sharedVertices[0] });
		Triangle t2 = new Triangle(new Vertex[] { v, uniqueVertexOne, sharedVertices[1] });
		Triangle t3 = new Triangle(new Vertex[] { v, uniqueVertexTwo, sharedVertices[0] });
		Triangle t4 = new Triangle(new Vertex[] { v, uniqueVertexTwo, sharedVertices[1] });
		
		oldOne.adjacent.remove(oldTwo);
		oldTwo.adjacent.remove(oldOne);
		
		for(Triangle t : oldOne.adjacent) {
			t.adjacent.remove(oldOne);
			if(t1.share2points(t)) {
				t.adjacent.add(t1);
				t1.adjacent.add(t);
			}
			if(t2.share2points(t)) {
				t.adjacent.add(t2);
				t2.adjacent.add(t);
			}
			if(t3.share2points(t)) {
				t.adjacent.add(t3);
				t3.adjacent.add(t);
			}
			if(t4.share2points(t)) {
				t.adjacent.add(t4);
				t4.adjacent.add(t);
			}
		}
		
		for(Triangle t : oldTwo.adjacent) {
			t.adjacent.remove(oldTwo);
			if(t1.share2points(t)) {
				t.adjacent.add(t1);
				t1.adjacent.add(t);
			}
			if(t2.share2points(t)) {
				t.adjacent.add(t2);
				t2.adjacent.add(t);
			}
			if(t3.share2points(t)) {
				t.adjacent.add(t3);
				t3.adjacent.add(t);
			}
			if(t4.share2points(t)) {
				t.adjacent.add(t4);
				t4.adjacent.add(t);
			}
		}
		
		t1.adjacent.add(t2);
		t1.adjacent.add(t3);
		
		t2.adjacent.add(t1);
		t2.adjacent.add(t4);
		
		t3.adjacent.add(t1);
		t3.adjacent.add(t4);
		
		t4.adjacent.add(t2);
		t4.adjacent.add(t3);
		
		Engine.map.AddTriangle(t1);
		Engine.map.AddTriangle(t2);
		Engine.map.AddTriangle(t3);
		Engine.map.AddTriangle(t4);
		
		Engine.map.RemoveTriangle(oldOne);
		Engine.map.RemoveTriangle(oldTwo);
		
		Engine.map.engine.ProduceErrorAppearance();
		
		return new Triangle[] { t1, t2, t3, t4 };
	}
	
	Triangle[] (Vertex v) {
		
		Triangle t1 = new Triangle(new Vertex[] {vertices[0], vertices[1], v});
		Triangle t2 = new Triangle(new Vertex[] {vertices[1], vertices[2], v});
		Triangle t3 = new Triangle(new Vertex[] {vertices[0], vertices[2], v});
		//note first edge is only edge capable of being illegal
		
		for(Triangle t : adjacent) {
			t.adjacent.remove(this);
			if(t1.share2points(t)) {
				t.adjacent.add(t1);
				t1.adjacent.add(t);
			}
			if(t2.share2points(t)) {
				t.adjacent.add(t2);
				t2.adjacent.add(t);
			}
			if(t3.share2points(t)) {
				t.adjacent.add(t3);
				t3.adjacent.add(t);
			}
		}
		
		t1.adjacent.add(t2);
		t1.adjacent.add(t3);
		
		t2.adjacent.add(t1);
		t2.adjacent.add(t3);
		
		t3.adjacent.add(t1);
		t3.adjacent.add(t2);
						
		Engine.map.AddTriangle(t1);
		Engine.map.AddTriangle(t2);
		Engine.map.AddTriangle(t3);
		
		//remove this triangle
		Engine.map.RemoveTriangle(this);
		
		return new Triangle[] { t1, t2, t3 };
	}
	
	private boolean hasPointInCircle(Vertex p) {
		
		Vertex one = vertices[0];
		Vertex two = vertices[1];
		Vertex three = vertices[2];
		
		//ensure points are arranged counter clockwise
		if(!isLeft(new Vertex[] { three, one }, two)) {
			Vertex temp = one;
			one = two;
			two = temp;
		}
					
		long a = one.x - p.x;
		long b = one.y - p.y;
		long c = (one.x*one.x - p.x*p.x) + (one.y*one.y - p.y*p.y);
		long d = two.x - p.x;
		long e = two.y - p.y;
		long f = (two.x*two.x - p.x*p.x) + (two.y*two.y - p.y*p.y);
		long g = three.x - p.x;
		long h = three.y - p.y;
		long i = (three.x*three.x - p.x*p.x) + (three.y*three.y - p.y*p.y);
		
		long det = (a*(e*i - f*h)) - (b*(d*i - f*g)) + (c*(d*h - e*g));
		
		return det > 0;
	}
	
	//determines whether vertex c is left of line a->b
	private Boolean isLeft(Vertex[] ab, Vertex c){
  	     return ((ab[1].x - ab[0].x)*(c.y - ab[0].y) - (ab[1].y - ab[0].y)*(c.x - ab[0].x)) >= 0;
  	}
	
	boolean hasPointInCircle(Triangle circleTri) {
		
		return hasPointInCircle(circleTri.uniqueVertex(this));
	}
	
	boolean share2points(Triangle other) {
		
		int count = 0;
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(vertices[i].x == other.vertices[j].x && vertices[i].y == other.vertices[j].y) {
					count++;
				}
			}
		}
		
		return 2 == count;
	}
}
