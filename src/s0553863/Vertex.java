package s0553863;

public class Vertex {

	int id;
	double g; // distance
	double h; // heuristic
	double f;
	int prevVertex;
	
	public Vertex(int id){
		this.id = id;
	}
	
	public void setDistance(double dist){
		this.g = dist;
	}

	public void setHeuristic(double h){
		this.h = g + h;
	}
	
	public void setF(){
		this.f = g + h;
	}
	
	public void setNewPrevVertex(int newPrevVertex){
		prevVertex = newPrevVertex;
	}
	
}
