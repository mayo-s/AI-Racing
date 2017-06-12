package s0553863;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.geom.Area;
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
	private boolean obstacleOutput = false;
	private ArrayList<Line2D> obstacleLines;
	private ArrayList<Point2D> points;

	public LosersInc(Info info) {
		super(info);
		obstacleLines = new ArrayList<Line2D>();
		points = new ArrayList<Point2D>();
		getObstacleLines();
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

		// Winkel zwischen Orientierungen < Toleranz
		// Bereits angekommen – Fertig!
		float tolerance = 0.005f;
		float wishTime = 1.1f;
		float wishAngularVelocity = 0;
		float throttle = maxVelocity;
		double abbremswinkel = Math.PI / 3.5;

		// Winkel zw. Orientierungen < Abbremswinkel
		// Wunschdrehgeschw. = (Zielorient. – Startorient.)∙ max.
		// Drehgeschwindigkeit / Abbremswinkel
		if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= abbremswinkel) {
			throttle = 1f;
			wishAngularVelocity = (float) (angleBetweenOrientations * maxAngularVelocity / abbremswinkel);
		}

		// Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else if (Math.abs(angleBetweenOrientations) > abbremswinkel) {
			throttle = 1;
			wishAngularVelocity = Math.signum(angleBetweenOrientations) * maxAngularVelocity;
		}

		// DrehBeschleunigung = (Wunschdrehgeschw. – aktuelle
		// Drehgeschwindigkeit) / Wunschzeit
		steering = (wishAngularVelocity - currAngularVelocity) / wishTime;

		// Abstand(Start, Ziel) < Zielradius
		// Bereits angekommen – Fertig!
		if (distance2CP < 10) {
			throttle = (currVelocity < 0.5 * maxVelocity) ? 0.5f : 0;
			steering = 1;
		} else if (distance2CP < 40) {
			throttle = (currVelocity < 0.7 * maxVelocity) ? 0.3f : 0;
		}
		// Abstand(Start, Ziel) < Abbremsradius
		// Wunschgeschwindigkeit = (Ziel – Start) * maximale Geschwindigkeit /
		// Abbremsradius
		else if (distance2CP < 100) {
			throttle = distance2CP * maxVelocity / 0.4f;
		}
		// Sonst: Wunschgeschwindigkeit = max. Geschw.
		// Beschleunigung = (Wunschgeschwindigkeit – aktuelle Geschwindigkeit) /
		// Wunschzeit
		else {
			throttle = (float) ((maxVelocity - currVelocity) / wishTime);
		}

		// debugInfo(throttle, steering, angleBetweenOrientations,
		// wishAngularVelocity, distance2CP, currVelocity);

		return new DriverAction(throttle, steering);

	}

	// private void debugInfo(float throttle, float steering, float
	// angleBetweenOrientations, float wishAngularVelocity,
	// float distance2CP, float currVelocity) {
	// // System.out.println("Throttle: " + throttle + " Steering: " + steering
	// // + " Angle between Orientations: "
	// // + angleBetweenOrientations + " wishAngularVelocity: " +
	// // wishAngularVelocity);
	// System.out.println("Distance to CP: " + distance2CP + " current Velocity:
	// " + currVelocity);
	// }

	@Override
	public void doDebugStuff() {

		drawObstacleGraph();

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

		for (int i = 0; i < points.size(); i++) {
			glBegin(GL_POINTS);
			glPointSize(10f);
			glVertex2d(points.get(i).getX(), points.get(i).getY());
			glEnd();
		}
	}

	private Point2D movePoint(int x0, int y0, int x1, int y1, int x2, int y2) {
		Point2D point = new Point2D.Double(x1, y1);

		// Vector2f newVec = new Vector2f(x2 - x0, y2 - y0);
		// Vector2f normalVec = new Vector2f(-(newVec.getY()), newVec.getX());
		// normalVec.normalise();
		// normalVec.scale(50);
		// Point2D newPoint = new Point2D.Double(point.getX() +
		// normalVec.getX(), point.getY() + normalVec.getY());

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

	public void drawObstacleGraph() {

		for (int l = 0; l < points.size() - 1; l++) {
			for (int m = l; m < points.size(); m++) {
				Line2D.Double currLine = new Line2D.Double(points.get(l), points.get(m));

				boolean intersects = false;
				for (Line2D line : obstacleLines) {
					intersects = currLine.intersectsLine(line);
					if (intersects)
						break;
				}

				if (!intersects) {
					glBegin(GL_LINES);
					glColor3f(0, 0, 0);
					glVertex2d(points.get(l).getX(), points.get(l).getY());
					glVertex2d(points.get(m).getX(), points.get(m).getY());
					glEnd();
				}
				// else {
				// System.out.println("Don't Draw: " +
				// obstacle.xpoints[l] + " " + obstacle.ypoints[l] + " to "
				// + otherObstacle.xpoints[m] + " " +
				// otherObstacle.ypoints[m]);
				// glBegin(GL_LINES);
				// glColor3f(1, 0, 0);
				// glVertex2f(obstacle.xpoints[l], obstacle.ypoints[l]);
				// glVertex2f(otherObstacle.xpoints[m],
				// otherObstacle.ypoints[m]);
				// glEnd();
				// }
			}
		}
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
					points.add(movePoint(xpoint, ypoint, nextXpoint, nextYpoint, nextNextXpoint, nextNextYpoint));
				}

			}
			obstacleLines.add(new Line2D.Double(
					new Point2D.Double(obstacle.xpoints[obstacle.npoints - 1], obstacle.ypoints[obstacle.npoints - 1]),
					new Point2D.Double(obstacle.xpoints[0], obstacle.ypoints[0])));
		}

	}
}