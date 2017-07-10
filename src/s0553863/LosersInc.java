package s0553863;

import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector2f;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class LosersInc extends lenz.htw.ai4g.ai.AI {

	Polygon[] obstacles;
	Polygon[] slowZones;
	private ArrayList<Line2D> linesObstacles;
	private ArrayList<Line2D> linesSlowZones;
	private ArrayList<Line2D> linesToDraw;
	private ArrayList<Line2D> linesNotToDraw;
	public ArrayList<Point2D> pointsObstacle;
	public ArrayList<Point2D> pointsSlowZones;
	private ArrayList<ArrayList<Edge>> edges;
	ArrayList<Point2D> open;
	ArrayList<Point2D> closed;
	ArrayList<Point2D> path;
	Point2D prevCheckpoint;
	Point2D finalCheckpoint;
	int lastCPx;
	int lastCPy;

	public LosersInc(Info info) {
		super(info);

		obstacles = info.getTrack().getObstacles();
		slowZones = info.getTrack().getSlowZones();
		linesObstacles = new ArrayList<Line2D>();
		linesSlowZones = new ArrayList<Line2D>();
		pointsObstacle = new ArrayList<Point2D>();
		pointsSlowZones = new ArrayList<Point2D>();
		linesToDraw = new ArrayList<Line2D>();
		linesNotToDraw = new ArrayList<Line2D>();
		edges = new ArrayList<ArrayList<Edge>>();
		prevCheckpoint = info.getCurrentCheckpoint();
		finalCheckpoint = info.getCurrentCheckpoint();
		path = new ArrayList<Point2D>();
		lastCPx = info.getCurrentCheckpoint().x;
		lastCPy = info.getCurrentCheckpoint().y;

		addPoints();
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
	public DriverAction update(boolean carReseted) {
		if (carReseted) {
			nextCheckpoint();
			System.out.println("new checkpoint");
		}

		if (info.getCurrentCheckpoint().x != lastCPx || info.getCurrentCheckpoint().y != lastCPy) {
			nextCheckpoint();
			lastCPx = info.getCurrentCheckpoint().x;
			lastCPy = info.getCurrentCheckpoint().y;
			System.out.println("CP changed");
		}

		float myCurrentX = info.getX();
		float myCurrentY = info.getY();
		Point2D myPos = new Point2D.Double(myCurrentX, myCurrentY);
		Point2D currentCheckpoint = new Point2D.Double();
		if (!path.isEmpty()) {
			currentCheckpoint = path.get(0);
		}
		// float speed = (float) Math.sqrt(Math.pow(info.getVelocity().x, 2) +
		// Math.pow(info.getVelocity().y, 2));
		float myOrientation = info.getOrientation();
		float orientationX = (float) currentCheckpoint.getX() - myCurrentX;
		float orientationY = (float) currentCheckpoint.getY() - myCurrentY;
		float desOrientation = (float) Math.atan2((double) orientationY, (double) orientationX);
		float angleBetweenOrientations = desOrientation - myOrientation;
		float maxAngularVelocity = info.getMaxAngularVelocity();

		float currAngularVelocity = info.getAngularVelocity();
		float maxVelocity = info.getMaxVelocity();
		float tolerance = 0.007f;
		float distance2CP = (float) Math.sqrt(Math.pow(orientationX, 2) + Math.pow(orientationY, 2));
		float currVelocity = (float) Math.sqrt(Math.pow(info.getVelocity().x, 2) + Math.pow(info.getVelocity().y, 2));

		if (getVectorLength(myPos, currentCheckpoint) <= 20 && currentCheckpoint != finalCheckpoint) {
			System.out.println("removing");
			path.remove(0);
		}

		if (angleBetweenOrientations > Math.PI)
			angleBetweenOrientations -= 2 * Math.PI;
		if (angleBetweenOrientations < -Math.PI)
			angleBetweenOrientations += 2 * Math.PI;

		float lengthMultiplier = 40;
		float angleValue = 18;

		Point2D.Double forward = new Point2D.Double(
				((float) (info.getX() + Math.cos(info.getOrientation()) * lengthMultiplier)),
				(float) (info.getY() + Math.sin(info.getOrientation()) * lengthMultiplier));
		Point2D.Double left = new Point2D.Double(
				(float) (info.getX() + Math.cos(info.getOrientation() - angleValue) * (lengthMultiplier - 7)),
				(float) (info.getY() + Math.sin(info.getOrientation() - angleValue) * (lengthMultiplier - 7)));
		Point2D.Double right = new Point2D.Double(
				((float) (info.getX() + Math.cos(info.getOrientation() + angleValue) * (lengthMultiplier - 7))),
				(float) (info.getY() + Math.sin(info.getOrientation() + angleValue) * (lengthMultiplier - 7)));
		for (int i = 0; i < obstacles.length; i++) {
			if (obstacles[i].contains(right) || obstacles[2].contains(right) && obstacles[2].contains(forward))
				return new DriverAction(0.1f, 0.4f);
			if (obstacles[i].contains(left) || obstacles[2].contains(left) && obstacles[2].contains(forward))
				return new DriverAction(0.1f, -0.4f);
		}

		float wishTime = 1.5f;
		float wishAngularVelocity = 0;
		float throttle = maxVelocity;
		float steering = 0;

		if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= Math.PI / 4) {
			throttle = 1f;
			wishAngularVelocity = (angleBetweenOrientations * maxAngularVelocity / 0.4f);
		}

		else if (Math.abs(angleBetweenOrientations) > Math.PI / 4) {
			throttle = 1;
			wishAngularVelocity = Math.signum(angleBetweenOrientations) * maxAngularVelocity;
		}
		steering = (wishAngularVelocity - currAngularVelocity) / wishTime;

		if (distance2CP < 10) {
			throttle = (currVelocity < 0.5 * maxVelocity) ? 0.5f : 0;
			steering = 1;
		} else if (distance2CP < 40) {
			throttle = (currVelocity < 0.7 * maxVelocity) ? 0.3f : 0;
		}

		else if (distance2CP < 100) {
			throttle = distance2CP * maxVelocity / 0.4f;
		} else {
			throttle = (float) ((maxVelocity - currVelocity) / wishTime);
		}

		return new DriverAction(throttle, steering);
	}

	private void addPoints() {

		polygonPoints(obstacles, true);
		polygonPoints(slowZones, false);

		pointsObstacle.add(new Point2D.Double(info.getX(), info.getY()));
		pointsObstacle.add(info.getCurrentCheckpoint());

		this.linesObstacles = addLines(obstacles);
		this.linesSlowZones = addLines(slowZones);

		saveLinesToDraw();
	}

	private void polygonPoints(Polygon[] polygons, boolean move) {
		for (int i = 0; i < polygons.length; i++) {
			int pointCountInObstacle = polygons[i].npoints;
			for (int j = 0; j < pointCountInObstacle; j++) {
				if (isLeftTurn(polygons[i].xpoints[j], polygons[i].ypoints[j],
						polygons[i].xpoints[(j + 1) % pointCountInObstacle],
						polygons[i].ypoints[(j + 1) % pointCountInObstacle],
						polygons[i].xpoints[(j + 2) % pointCountInObstacle],
						polygons[i].ypoints[(j + 2) % pointCountInObstacle])) {
					if (move) {
						pointsObstacle.add(movePoint(polygons[i].xpoints[j], polygons[i].ypoints[j],
								polygons[i].xpoints[(j + 1) % pointCountInObstacle],
								polygons[i].ypoints[(j + 1) % pointCountInObstacle],
								polygons[i].xpoints[(j + 2) % pointCountInObstacle],
								polygons[i].ypoints[(j + 2) % pointCountInObstacle]));
					} else {
						pointsSlowZones.add(new Point2D.Double(polygons[i].xpoints[j], polygons[i].ypoints[j]));
					}
				}
			}
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
		// vec3.scale(0.5f);
		vec3.normalise();
		vec3.scale(25);
		Point2D newPoint = new Point2D.Double(point.getX() + vec3.x, point.getY() + vec3.y);

		return newPoint;
	}

	private ArrayList<Line2D> addLines(Polygon[] polygons) {
		ArrayList<Line2D> lines = new ArrayList<Line2D>();

		for (int i = 0; i < polygons.length; i++) {
			int pointCountInPolygon = polygons[i].npoints;
			for (int j = 0; j < pointCountInPolygon; j++) {

				lines.add(new Line2D.Double(polygons[i].xpoints[j], polygons[i].ypoints[j],
						polygons[i].xpoints[(j + 1) % pointCountInPolygon],
						polygons[i].ypoints[(j + 1) % pointCountInPolygon]));
			}
		}
		return lines;
	}

	private void saveLinesToDraw() {
		for (int l = 0; l < pointsObstacle.size(); l++) {
			edges.add(new ArrayList<Edge>());
			for (int m = 0; m < pointsObstacle.size(); m++) {
				Line2D.Double currLine = new Line2D.Double(pointsObstacle.get(l), pointsObstacle.get(m));
				boolean intersects = false;
				for (Line2D line : linesObstacles) {
					if (currLine.intersectsLine(line)) {
						linesNotToDraw.add(currLine);
						intersects = true;
						break;
					}
				}
				for (Line2D line : linesSlowZones) {
					if (currLine.intersectsLine(line)) {
						linesNotToDraw.add(currLine);
						intersects = true;
						break;
					}
				}				
				
				if (intersects == false) {
					if (!linesToDraw.contains(currLine)) {
						linesToDraw.add(currLine);
						Vector2f cost = new Vector2f(
								(float) (pointsObstacle.get(m).getX() - pointsObstacle.get(l).getX()),
								(float) (pointsObstacle.get(m).getY() - pointsObstacle.get(l).getY()));
						edges.get(l).add(new Edge(cost.length(), pointsObstacle.get(m)));
					}
				}
			}
		}
		findPath((edges.size() - 1), (edges.size() - 2));
	}
	
//	private void saveLinesToDraw() {
//		for (int l = 0; l < points.size(); l++) {
//			edges.add(new ArrayList<Edge>());
//			for (int m = 0; m < points.size(); m++) {
//				Line2D.Double currLine = new Line2D.Double(points.get(l), points.get(m));
//				boolean intersects = false;
//				for (Line2D line : lines) {
//					if (currLine.intersectsLine(line)) {
//						linesNotToDraw.add(currLine);
//						intersects = true;
//						break;
//					}
//				}
//				if (intersects == false) {
//					if (!linesToDraw.contains(currLine)) {
//						linesToDraw.add(currLine);
//						boolean intersects2 = false;
//						for (int x = 0; x < slowLines.size(); x++) {
//							if (currLine.intersectsLine(slowLines.get(x))) {
//								Vector2f cost = new Vector2f((float) (points.get(m).getX() - points.get(l).getX()),
//										(float) (points.get(m).getY() - points.get(l).getY()));
//								edges.get(l).add(new Edge(cost.length() * 9, points.get(m)));
//								intersects2 = true;
//								break;
//							}
//						}
//
//						if (intersects2 == false) {
//							boolean intersects3 = false;
//							for (int y = 0; y < fastLines.size(); y++) {
//								if (currLine.intersectsLine(fastLines.get(y))) {
//									Vector2f cost = new Vector2f((float) (points.get(m).getX() - points.get(l).getX()),
//											(float) (points.get(m).getY() - points.get(l).getY()));
//									edges.get(l).add(new Edge(cost.length() * 0.8f, points.get(m)));
//									intersects3 = true;
//									break;
//								}
//							}
//							if (intersects3 == false) {
//								Vector2f cost = new Vector2f((float) (points.get(m).getX() - points.get(l).getX()),
//										(float) (points.get(m).getY() - points.get(l).getY()));
//								edges.get(l).add(new Edge(cost.length(), points.get(m)));
//							}
//
//						}
//					}
//
//				}
//			}
//
//		}
//		x((edges.size() - 1), (edges.size() - 2));
//	}

	private float getVectorLength(Point2D p1, Point2D p2) {
		float length = 0;
		Vector2f vector = new Vector2f((float) p2.getX() - (float) (p1.getX()), (float) p2.getY() - (float) p1.getY());
		length = vector.length();
		return length;
	}

	private Vertex findPath(int start, int destination) {
		// open
		PriorityQueue<Vertex> q = new PriorityQueue<>();
		// closed
		Set<Vertex> f = new HashSet<>();
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
			if (v.vertex == destination) {
				finalVertex = v;
				break;
			}
			f.add(v);
		}
		preVertex(finalVertex, q, f);
		return finalVertex;
	}

	// find previous Vertex in open or closed
	private void preVertex(Vertex v, PriorityQueue<Vertex> q, Set<Vertex> f) {
		while (v.vertex != v.preVertex) {
			path.add(pointsObstacle.get(v.preVertex));
			boolean found = false;
			for (Vertex ding : q) {
				if (v.preVertex == ding.vertex) {
					v.vertex = ding.vertex;
					v.preVertex = ding.preVertex;
					found = true;
					break;
				}
			}
			if (!found) {
				for (Vertex ding : f) {
					if (v.preVertex == ding.vertex) {
						v.vertex = ding.vertex;
						v.preVertex = ding.preVertex;
						break;
					}
				}
			}
		}
		for (int i = 0; i < path.size(); i++) {
			if (!path.contains(pointsObstacle.get(pointsObstacle.size() - 1))) {
				path.add(pointsObstacle.get(pointsObstacle.size() - 1));
				break;
			}
		}
	}

	private void nextCheckpoint() {

		obstacles = info.getTrack().getObstacles();
		linesObstacles = new ArrayList<Line2D>();
		pointsObstacle = new ArrayList<Point2D>();
		linesToDraw = new ArrayList<Line2D>();
		linesNotToDraw = new ArrayList<Line2D>();
		edges = new ArrayList<ArrayList<Edge>>();
		prevCheckpoint = info.getCurrentCheckpoint();
		finalCheckpoint = info.getCurrentCheckpoint();
		this.path = new ArrayList<Point2D>();

		addPoints();
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

	private int getTargetInt(Point2D point) {
		double pointX = point.getX();
		double pointY = point.getY();
		int position = 0;
		for (int i = 0; i < pointsObstacle.size(); i++) {
			double compareX = pointsObstacle.get(i).getX();
			double compareY = pointsObstacle.get(i).getY();
			if (pointX == compareX && pointY == compareY) {
				position = i;
				break;
			}
		}
		return position;
	}

	private void getSlowZones() {

	}

	@Override
	public void doDebugStuff() {
		drawPoints();
		drawOrientationLines();
		drawGraph();
	}

	private void drawPoints() {
		// obstacle points with lil distance
		for (int i = 0; i < pointsObstacle.size(); i++) {
			glColor3f(0, 1, 0); // green
			glBegin(GL_POINTS);
			glPointSize(44f);
			glVertex2d(pointsObstacle.get(i).getX(), pointsObstacle.get(i).getY());
			glEnd();
		}
		// slow zone edge points
		for (int i = 0; i < pointsSlowZones.size(); i++) {
			glColor3f(0.5f, 0, 0.5f); // purple
			glBegin(GL_POINTS);
			glPointSize(44f);
			glVertex2d(pointsSlowZones.get(i).getX(), pointsSlowZones.get(i).getY());
			glEnd();
		}
	}

	private void drawOrientationLines() {
		float testValue = 18;
		float lengthMultiplier = 40;

		glBegin(GL_LINES);
		// orientation to next CP (green)
		glColor3f(0, 1, 0);
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

		glBegin(GL_LINES);
		glColor3f(0, 0, 0);
		for (Line2D line : linesToDraw) {
			glVertex2d(line.getX1(), line.getY1());
			glVertex2d(line.getX2(), line.getY2());
		}
		glEnd();
	}
}
