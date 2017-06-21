package s0553863;
import java.awt.geom.Point2D;

public class Vertex {

	int id;
	double g; // cost
	double h; // heuristic
	double f;
	int prevVertex = Integer.MAX_VALUE;
	Point2D position;
	
	public Vertex(int id, Point2D position, double cost){
		this.id = id;
		this.position = position;
		this.g = cost;	
	}
	
	public int getId(){
		return id;
	}
	
	public Point2D getPosition(){
		return position;
	}

	public void setCost(double cost){
		this.g = cost;
	}

	public double getCost(){
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
