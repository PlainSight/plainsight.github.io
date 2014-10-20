private ArrayList<Triangle> triangles = new ArrayList<Triangle>();

public void AddTriangle(Triangle t) {
	triangles.add(t);
}

public void RemoveTriangle(Triangle t) {
	triangles.remove(t);
}

public void getTriangleData(ArrayList<Integer[]> array) {
	for (Triangle t : triangles) {
		array.add(new Integer[] { 
			t.vertices[0].x, t.vertices[0].y, 
			t.vertices[1].x, t.vertices[1].y, 
			t.vertices[2].x, t.vertices[2].y,
			t.pathable ? 1 : 0,
			t.hashCode()
		});
	}
}


public Triangle getTriangle(int x, int y) {
	for (Triangle t : triangles) {
		if(t.vertexInTriangle(new Vertex(x, y, null))) {
			return t;
		}
	}
	return null;
}

public Engine engine;

public Map(Engine e, Obstacle[] obstacles, int width, int height, int midx, int midy) {
	
	engine = e;
	
	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	
	if(obstacles != null) {
		for(Obstacle o : obstacles) {
			for(Vertex p : o.getVertices()) {
				vertices.add(p);
			}
		}
	}
	
	int minx = midx - width/2;
	int maxx = midx + width/2;
	int miny = midy - height/2;
	int maxy = midy + height/2;
	
	Vertex v1 = new Vertex(minx, miny);
	Vertex v2 = new Vertex(minx, maxy);
	Vertex v3 = new Vertex(maxx, miny);
	Vertex v4 = new Vertex(maxy, maxy);
	
	Triangle t1 = new Triangle(new Vertex[] { v1, v2, v3 } );
	Triangle t2 = new Triangle(new Vertex[] { v2, v3, v4 } );
	
	t1.adjacent.add(t2);
	t2.adjacent.add(t1);
	
	triangles.add(t1);
	triangles.add(t2);
	
	for(Vertex v : vertices) {
		addVertex(v);
	}
}

public void addObstacle(Obstacle o) {
	for(Vertex v : o.getVertices()) {
		addVertex(v);
	}
}

public void addBuilding(Unit u) {
	Vertex v1 = new Vertex((int) (u.getX() - u.getR()), (int) (u.getY() - u.getR()), u);
	Vertex v2 = new Vertex((int) (u.getX() + u.getR()), (int) (u.getY() - u.getR()), u);
	Vertex v3 = new Vertex((int) (u.getX() - u.getR()), (int) (u.getY() + u.getR()), u);
	Vertex v4 = new Vertex((int) (u.getX() + u.getR()), (int) (u.getY() + u.getR()), u);
	
	addVertex(v1);
	addVertex(v2);
	addVertex(v3);
	addVertex(v4);
	
	buildings.put(u, new Vertex[] {v1, v2, v3, v4});
}

private void addVertex(Vertex v)
{
	cachedPaths.clear();
	
	System.out.println("Adding vertex " + v.toString());
	
	Triangle inside = null;
	Triangle edge = null;
	
	for(Triangle t: triangles)
	{
		if(t.vertexInTriangle(v))
		{
			inside = t;
		}
		
		if(t.vertexOnTriangleEdge(v)) {
			edge = t;
		}
		
		if(edge != null || inside != null) {
			break;
		}
	}
	
	if(edge != null) {
		Triangle[] newTriangles = edge.addVertexToEdge(v);
		for(int i = 0; i < newTriangles.length; i++) {
			if(!newTriangles[i].dead) {
				CheckAndFlip(newTriangles[i]);
			}
		}
		return;
	}
	
	if(inside != null)
	{
		Triangle[] newTriangles = inside.add(v);
		for(int i = 0; i < newTriangles.length; i++) {
			if(!newTriangles[i].dead) {
				CheckAndFlip(newTriangles[i]);
			}
		}
	}
}

public void RemoveBuilding(Unit u) {
	Vertex[] vertices = buildings.get(u);
	
	for(int i = 0; i < vertices.length; i++) {
		
		removeVertex(vertices[i]);
		
	}
}

private void removeVertex(Vertex v) {
	
	ArrayList<Triangle> trianglesForReconstitution = new ArrayList<Triangle>();
	
	for(Triangle t : triangles) {
		
		for(int i = 0; i < 3; i++) {
			
			if(t.vertices[i] == v) {
				trianglesForReconstitution.add(t);
			}
		}
	}
	
	
	
	//remove adjacency referece from adjacent triangles
	for(Triangle t : trianglesForReconstitution) {
		for(Triangle tt : t.adjacent) {
			tt.adjacent.remove(t);
		}
	}
	
	//contains adjacent triangles to the ones being removed
	ArrayList<Triangle> adjacents = new ArrayList<Triangle>();
	
	for(Triangle t : trianglesForReconstitution) {
		for(Triangle tt : t.adjacent) {
			if(!adjacents.contains(tt)) {
				adjacents.add(tt);
			}
		}
	}
	
	triangles.removeAll(trianglesForReconstitution);

	}
}

private void CheckAndFlip(Triangle t) {
			
	for(int i = 0; i < t.adjacent.size(); i++) {
		Triangle tt = t.adjacent.get(i);
					
		if(t.hasPointInCircle(tt)) {
			
			Triangle[] newTriangles = flip(t, tt);
			CheckAndFlip(newTriangles[0]);
			if(!newTriangles[1].dead) {
				CheckAndFlip(newTriangles[1]);
			}
			return;
		}
	}
}
	
public Triangle[] flip(Triangle t1, Triangle t2) {
					
	Vertex[] common = t1.sharedVertices(t2);
	
	Vertex[] uncommon = new Vertex[2];
	uncommon[0] = t1.uniqueVertex(t2);
	uncommon[1] = t2.uniqueVertex(t1);
	
	//make new triangles
	Triangle t1n = new Triangle(new Vertex[] { uncommon[0], uncommon[1], common[0] });
	Triangle t2n = new Triangle(new Vertex[] { uncommon[0], uncommon[1], common[1] });
	
	ArrayList<Triangle> adjacents = new ArrayList<Triangle>();
	adjacents.addAll(t1.adjacent);
	adjacents.addAll(t2.adjacent);
	adjacents.remove(t1);
	adjacents.remove(t2);
	
	//remove old triangles from their neighbours adjacent lists
	
	for(Triangle t : t1.adjacent) {
		t.adjacent.remove(t1);
	}
	
	for(Triangle t : t2.adjacent) {
		t.adjacent.remove(t2);
	}
	
	for(Triangle t : adjacents) {
		
		if(t1n.share2points(t)) {
			t.adjacent.add(t1n);
			t1n.adjacent.add(t);
		}
		
		if(t2n.share2points(t)) {
			t.adjacent.add(t2n);
			t2n.adjacent.add(t);
		}	
	}
	
	t1n.adjacent.add(t2n);
	t2n.adjacent.add(t1n);
	
	if(t1n.adjacent.size() > 3) {
	
		ArrayList<Triangle> twoadj = new ArrayList<Triangle>();
		
		for(Triangle t : triangles) {
			
			if(t.adjacent.size() < 3) {
				twoadj.add(t);
			}
		}

		throw new ArrayIndexOutOfBoundsException(t1n.adjacent.size());
	}
	if(t2n.adjacent.size() > 3) {
		engine.ProduceErrorAppearance();
		
		ArrayList<Triangle> twoadj = new ArrayList<Triangle>();
		
		for(Triangle t : triangles) {
			
			if(t.adjacent.size() < 3) {
				twoadj.add(t);
			}
		}
		
		throw new ArrayIndexOutOfBoundsException(t2n.adjacent.size());
	}
	
	RemoveTriangle(t1);
	t1.dead = true;
	RemoveTriangle(t2);
	t2.dead = true;
	AddTriangle(t1n);
	AddTriangle(t2n);
			
	return new Triangle[] { t1n, t2n };
}
