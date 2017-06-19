package s0553863;

public class Vertex {

	int id;
	double distance;
	double heuristic;
	double f;
	int prevVertex;
	
	public Vertex(int id, double g, double h, int prevVertex){
		this.id = id;
		this.distance = g;
		this.heuristic = h;
		this.f = g + h;
		this.prevVertex = prevVertex;
	}
	
}
