package s0553863;
public class Vertex implements Comparable<Vertex>{

	public int vertex;
	public float cost;
	public int preVertex;

	public Vertex(int knoten, float cost, int preKnoten) {
		this.vertex = knoten;
		this.cost = cost;
		this.preVertex = preKnoten;
	}
	
	
	@Override
	public int compareTo(Vertex o) {
		return (int) Math.signum(cost - o.cost);
	}
	
	
}