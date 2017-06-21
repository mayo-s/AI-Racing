package s0553863;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
// import java.awt.geom.Area;
import java.awt.geom.Line2D;
import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.*;
import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class LosersInc extends AI {

	float maxVelocity = info.getMaxVelocity();
	float maxAngularVelocity = info.getMaxAngularVelocity();
	float maxAcceleration = info.getMaxAcceleration();
	float maxAngularAcceleration = info.getMaxAngularAcceleration();
	Polygon[] obstacles = info.getTrack().getObstacles();
	private ArrayList<Line2D> obstacleLines;
	private ArrayList<Point2D> points;
	private ArrayList<ArrayList<Edge>> edges;
	private Point prevCP;

	public LosersInc(Info info) {
		super(info);
		obstacleLines = new ArrayList<Line2D>();
		points = new ArrayList<Point2D>();
		edges = new ArrayList<ArrayList<Edge>>();
		System.out.println("get obstacles");
		getObstacleLines();
		System.out.println("create graph");
		createGraph();
	}

	@Override
	public String getName() {
		return "Numero Uno";
	}

	@Override
	public String getTextureResourceName() {
		return "/s0553863/car.png";
	}

	@Override
	public DriverAction update(boolean arg0) {
		Point currentCheckpoint = info.getCurrentCheckpoint();

		if (currentCheckpoint != prevCP) {
			Vertex v = astar(edges.size() - 2, edges.size() - 1);
			prevCP = currentCheckpoint;
		}

		float myCurrX = info.getX();
		float myCurrY = info.getY();
		float myCurrOrientation = info.getOrientation();
		float currAngularVelocity = info.getAngularVelocity();
		float directionX = (float) (currentCheckpoint.getX() - myCurrX);
		float directionY = (float) (currentCheckpoint.getY() - myCurrY);
		float orientation2CP = (float) Math.atan2(directionY, directionX);
		float angleBetweenOrientations = orientation2CP - myCurrOrientation;
		float distance2CP = (float) Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2));
		float currVelocity = (float) Math.sqrt(Math.pow(info.getVelocity().x, 2) + Math.pow(info.getVelocity().y, 2));

		float angleValue = 18;
		float lengthMultiplier = 40;
		float steering = 0;

		if (obstacles.length > 2) {
			Point2D.Double forward = new Point2D.Double(
					((float) (info.getX() + Math.cos(info.getOrientation()) * lengthMultiplier)),
					(float) (info.getY() + Math.sin(info.getOrientation()) * lengthMultiplier));
			Point2D.Double left = new Point2D.Double(
					(float) (info.getX() + Math.cos(info.getOrientation() - angleValue) * (lengthMultiplier - 5)),
					(float) (info.getY() + Math.sin(info.getOrientation() - angleValue) * (lengthMultiplier - 5)));
			Point2D.Double right = new Point2D.Double(
					((float) (info.getX() + Math.cos(info.getOrientation() + angleValue) * (lengthMultiplier - 5))),
					(float) (info.getY() + Math.sin(info.getOrientation() + angleValue) * (lengthMultiplier - 5)));

			for (int i = 2; i < obstacles.length; i++) {
				if (obstacles[i].contains(right) || obstacles[i].contains(right) && obstacles[i].contains(forward))
					return new DriverAction(0.1f, 0.4f);
				if (obstacles[i].contains(left) || obstacles[i].contains(left) && obstacles[i].contains(forward))
					return new DriverAction(0.1f, -0.4f);
			}
		}

		if (angleBetweenOrientations > Math.PI)
			angleBetweenOrientations -= 2 * Math.PI;
		if (angleBetweenOrientations < -Math.PI)
			angleBetweenOrientations += 2 * Math.PI;

		float tolerance = 0.005f;
		float wishTime = 1.1f;
		float wishAngularVelocity = 0;
		float throttle = maxVelocity;
		double abbremswinkel = Math.PI / 3.5;

		if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= abbremswinkel) {
			throttle = 1f;
			wishAngularVelocity = (float) (angleBetweenOrientations * maxAngularVelocity / abbremswinkel);
		}

		else if (Math.abs(angleBetweenOrientations) > abbremswinkel) {
			throttle = 1;
			wishAngularVelocity = Math.signum(angleBetweenOrientations) * maxAngularVelocity;
		}

		steering = (wishAngularVelocity - currAngularVelocity) / wishTime;

		if (distance2CP < 10) {
			throttle = (currVelocity < 0.5 * maxVelocity) ? 0.5f : 0;
			steering = 1;
		} else if (distance2CP < 40) {
			throttle = (currVelocity < 0.7 * maxVelocity) ? 0.3f : 0;
		} else if (distance2CP < 100) {
			throttle = distance2CP * maxVelocity / 0.4f;
		} else {
			throttle = (float) ((maxVelocity - currVelocity) / wishTime);
		}
		return new DriverAction(throttle, steering);
	}

	private void getObstacleLines() {

		for (Polygon obstacle : obstacles) {
			for (int pos = 0; pos < obstacle.xpoints.length; pos++) {

				int xpoint = obstacle.xpoints[pos];
				int nextXpoint = obstacle.xpoints[(pos + 1) % obstacle.npoints];
				int nextNextXpoint = obstacle.xpoints[(pos + 2) % obstacle.npoints];

				int ypoint = obstacle.ypoints[pos];
				int nextYpoint = obstacle.ypoints[(pos + 1) % obstacle.npoints];
				int nextNextYpoint = obstacle.ypoints[(pos + 2) % obstacle.npoints];

				obstacleLines.add(new Line2D.Double(new Point2D.Double(xpoint, ypoint),
						new Point2D.Double(nextXpoint, nextYpoint)));
				if (isLeftTurn(xpoint, ypoint, nextXpoint, nextYpoint, nextNextXpoint, nextNextYpoint)) {
					Point2D movedPoint = movePoint(xpoint, ypoint, nextXpoint, nextYpoint, nextNextXpoint,
							nextNextYpoint);
					points.add(movedPoint);
				}
			}
			obstacleLines.add(new Line2D.Double(
					new Point2D.Double(obstacle.xpoints[obstacle.npoints - 1], obstacle.ypoints[obstacle.npoints - 1]),
					new Point2D.Double(obstacle.xpoints[0], obstacle.ypoints[0])));
		}
	}

	private Point2D movePoint(int x0, int y0, int x1, int y1, int x2, int y2) {
		Point2D point = new Point2D.Double(x1, y1);

		Vector2f vec1 = new Vector2f(x1 - x0, y1 - y0);
		vec1.normalise();
		Vector2f vec2 = new Vector2f(x1 - x2, y1 - y2);
		vec2.normalise();
		Vector2f vec3 = new Vector2f();
		Vector2f.add(vec1, vec2, vec3);
		vec3.scale(0.5f);
		vec3.normalise();
		vec3.scale(10);
		Point2D newPoint = new Point2D.Double(point.getX() + vec3.x, point.getY() + vec3.y);

		return newPoint;
	}

	private boolean isLeftTurn(int x0, int y0, int x1, int y1, int x2, int y2) {
		int value = 0;
		value = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
		if (value > 0) {
			return true;
		} else {
			return false;
		}
	}

	public void createGraph() {

		for (int l = 0; l < points.size() - 1; l++) {
			edges.add(new ArrayList<Edge>());
			for (int m = 0; m < points.size(); m++) {
				Line2D.Double currLine = new Line2D.Double(points.get(l), points.get(m));

				boolean intersects = false;
				for (Line2D line : obstacleLines) {
					intersects = currLine.intersectsLine(line);
					if (intersects)
						break;
				}

				if (!intersects) {
					Point2D p1 = points.get(l);
					Point2D p2 = points.get(m);
					Vector2f cost = new Vector2f((float) (p2.getX() - p1.getX()), (float) ((p2.getY() - p1.getY())));
					edges.get(l).add(new Edge(p2, cost.length()));
				}
			}
		}
		addPos(info.getX(), info.getY()); // add my very fisrt starting position
		addPos(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY()); // add
																						// first
																						// checkpoint
																						// to
																						// reach
	}

	private void addPos(double x, double y) {

		edges.add(new ArrayList<Edge>());
		Point2D startPos = new Point2D.Double(x, y);
		for (int i = 0; i < points.size(); i++) {

			for (Line2D line : obstacleLines) {
				Line2D currEdge = new Line2D.Double(startPos, points.get(i));
				if (!currEdge.intersectsLine(line)) {
					Vector2f cost = new Vector2f((float) (points.get(i).getX() - startPos.getX()),
							(float) (points.get(i).getY() - startPos.getY()));
					edges.get(edges.size() - 1).add(new Edge(points.get(i), cost.length()));
				}
			}
		}
	}

	// Q ← { Startknoten: 0 }
	// • Solange Knoten erreichbar [Q nicht leer]
	//  v ← Entferne Knoten mit kleinsten Kosten k+h(v)
	// aus Q
	//  Für alle von v ausgehenden Kanten (v, n)
	// • Falls n nicht fertig [in Liste F]
	//  Falls n bisher nicht erreichbar [nicht in Q]
	// • n ist erreichbar mit Kosten ∞ [n zu Q hinzufügen]
	//  Falls k + Kantengewicht(v,n) < Kosten von n in Q?
	// • Kosten von n in Q ← k + Kantengewicht(v,n)
	// • Setze v als Vorgänger von n
	//  v ist fertig [v zu F hinzufügen]

	private Vertex astar(int start, int destination) {
		PriorityQueue<Vertex> q = new PriorityQueue<>();
		HashSet<Vertex> f = new HashSet<Vertex>();
		Vertex finalVertex = null;

		q.add(new Vertex(start, 0, start));
		while (!q.isEmpty()) {
			Vertex v = q.poll();
			for (Edge edg : edges.get(v.vertex)) {
				boolean inF = false;
				for (Vertex such : f) {
					if (such.vertex == getTargetInt(edg.getTarget())) {
						inF = true;
						break;
					}
				}
				if (!inF) {
					Vertex nInQ = null;
					for (Vertex such : q) {
						if (such.vertex == getTargetInt(edg.getTarget())) {
							nInQ = such;
							break;
						}
					}
					if (nInQ == null) {
						nInQ = new Vertex(getTargetInt(edg.getTarget()), Float.POSITIVE_INFINITY, v.vertex);
						q.add(nInQ);
					}
					if (v.cost + edg.getCost() < nInQ.cost) {
						nInQ.cost = v.cost + edg.getCost();
						nInQ.preVertex = v.vertex;
					}
				}

			}
			if (v.vertex == destination)
				finalVertex = v;
			f.add(v);
		}
		return finalVertex;
	}

	public int getTargetInt(Point2D point) {
		double pointX = point.getX();
		double pointY =  point.getY();
		int position = 0;
		for (int i = 0; i < points.size(); i++) {
			double compareX = points.get(i).getX();
			double compareY = points.get(i).getY();
			if (pointX == compareX && pointY == compareY) {
				position = i;
				System.out.println("(edgetarget) " + pointX + " " + pointY + " is in pointsList " + position + " ("
						+ points.get(position).getX() + " " + points.get(position).getY() + ")");
				break;
			}
		}
		return position;
	}

	@Override
	public void doDebugStuff() {
		createGraph();
		drawPoints();
		drawOrientationLines();
		drawGraph();
	}

	private void drawPoints() {
		// points with with small distance to obstacle
		for (int i = 0; i < points.size(); i++) {
			glColor3f(0, 1, 0); // green
			glBegin(GL_POINTS);
			glPointSize(44f);
			glVertex2d(points.get(i).getX(), points.get(i).getY());
			glEnd();
		}
	}

	private void drawOrientationLines() {
		float testValue = 18;
		float lengthMultiplier = 40;

		glBegin(GL_LINES);
		// orientation to next CP (black)
		glVertex2f(info.getX(), info.getY());
		glVertex2f((float) info.getCurrentCheckpoint().getX(), (float) info.getCurrentCheckpoint().getY());
		// vector for current orientation (blue)
		glColor3f(0, 0, 1);
		glVertex2f(info.getX(), info.getY());
		glVertex2f((float) (info.getX() + Math.cos(info.getOrientation()) * lengthMultiplier),
				(float) (info.getY() + Math.sin(info.getOrientation()) * lengthMultiplier));
		// left and right view vectors (red)
		glColor3f(1, 0, 0);
		glVertex2f(info.getX(), info.getY());
		glVertex2f((float) (info.getX() + Math.cos(info.getOrientation() + testValue) * lengthMultiplier),
				(float) (info.getY() + Math.sin(info.getOrientation() + testValue) * lengthMultiplier));
		glColor3f(1, 0, 0);
		glVertex2f(info.getX(), info.getY());
		glVertex2f((float) (info.getX() + Math.cos(info.getOrientation() - testValue) * lengthMultiplier),
				(float) (info.getY() + Math.sin(info.getOrientation() - testValue) * lengthMultiplier));
		glEnd();
	}

	private void drawGraph() {

		// for (int m = 0; m < verticesAdjList.size(); m++) {
		// float p1x = (float) vertices.get(m).getPosition().getX();
		// float p1y = (float) vertices.get(m).getPosition().getY();
		// for (int n = 0; n < verticesAdjList.get(m).size(); n++) {
		// float p2x = (float)
		// verticesAdjList.get(m).get(n).getPosition().getX();
		// float p2y = (float)
		// verticesAdjList.get(m).get(n).getPosition().getY();
		// glBegin(GL_LINES);
		// glColor3f(0, 0, 0);
		// glVertex2f(p1x, p1y);
		// glVertex2f(p2x, p2y);
		// glEnd();
		// }
		// }
	}
}