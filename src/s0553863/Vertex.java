package s0553863;

public class Vertex {

	int id;
	double g; // distance
	double h; // heuristic
	double f;
	int prevVertex = Integer.MAX_VALUE;
	
	public Vertex(int id, double distance){
		this.id = id;
		this.g = distance;
	}
	
	public int getId(){
		return id;
	}

	public void setDistance(double dist){
		this.g = dist;
	}

	public double getDistance(){
		return g;
	}
	
	public void setHeuristic(double h){
		this.h = g + h;
	}
	
	public double getHeuristic(){
		return h;
	}
	
	public void setF(){
		this.f = g + h;
	}
	
	public double getF(){
		return f;
	}
	
	public void setNewPrevVertex(int newPrevVertex){
		prevVertex = newPrevVertex;
	}
	
	public int getPrevVertex(){
		return prevVertex;
	}
	
}
