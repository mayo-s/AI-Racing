package s0553863;

import java.awt.geom.Point2D;

public class Edge {
	
	int edgeId;
	Point2D target;
	float cost;
	
	Edge(int id, Point2D target, float cost){
		this.edgeId = id;
		this.target = target;
		this.cost = cost;		
	}
	
	public Point2D getTarget(){
		return target;
	}
	
	public float getCost(){
		return cost;
	}
}
