package s0553863;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.geom.Area;
import java.awt.geom.Line2D;

//import org.lwjgl.util.vector.Vector2f;
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

	public LosersInc(Info info) {
		super(info);
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

	private void debugInfo(float throttle, float steering, float angleBetweenOrientations, float wishAngularVelocity,
			float distance2CP, float currVelocity) {
		// System.out.println("Throttle: " + throttle + " Steering: " + steering
		// + " Angle between Orientations: "
		// + angleBetweenOrientations + " wishAngularVelocity: " +
		// wishAngularVelocity);
		System.out.println("Distance to CP: " + distance2CP + " current Velocity: " + currVelocity);
	}

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
	}

	public void drawObstacleGraph() {
		// output obstacle coords
		if (!obstacleOutput) {
			for (int i = 0; i < obstacles.length; i++) {
				System.out.println("\nObstacle " + i);
				Polygon obstacle = obstacles[i];
				for (int n = 0; n < obstacle.xpoints.length; n++) {
					System.out.print(obstacle.xpoints[n] + " ");

					System.out.print(obstacle.ypoints[n] + "\n");
				}
			}
			obstacleOutput = true;
		}

		// draw all edge connections
		for (int i = 0; i < obstacles.length - 1; i++) {
			Polygon obstacle = obstacles[i];

			for (int k = 0; k < obstacles.length; k++) {
				Polygon otherObstacle = obstacles[k];

				for (int l = 0; l < obstacle.xpoints.length; l++) {
					for (int m = 0; m < otherObstacle.xpoints.length; m++) {
						Line2D.Double line1 = new Line2D.Double(
								new Point2D.Double(obstacle.xpoints[l], obstacle.ypoints[l]),
								new Point2D.Double(otherObstacle.xpoints[m], otherObstacle.ypoints[m]));

						boolean intersects = false;
						for (Line2D line2 : obstacleLines) {
							intersects = line1.intersectsLine(line2);;
						}
						if (!intersects) {
							System.out.println("Draw: " + obstacle.xpoints[l] + " " + obstacle.ypoints[l] + " to " + otherObstacle.xpoints[m] + " " +otherObstacle.ypoints[m]);
							glBegin(GL_LINES);
							glColor3f(0, 0, 0);
							glVertex2f(obstacle.xpoints[l], obstacle.ypoints[l]);
							glVertex2f(otherObstacle.xpoints[m], otherObstacle.ypoints[m]);
							glEnd();
						}
						else
							System.out.println("Don't Draw: " + obstacle.xpoints[l] + " " + obstacle.ypoints[l] + " to " + otherObstacle.xpoints[m] + " " + otherObstacle.ypoints[m]);
					}
				}
			}
		}
	}

	private void getObstacleLines() {

		obstacleLines = new ArrayList<Line2D>();
		for (Polygon obstacle : obstacles) {
			for (int x = 0; x < obstacle.xpoints.length - 1; x++) {
				int xpoint = obstacle.xpoints[x];
				int nextXpoint = obstacle.xpoints[x + 1];

				for (int y = 0; y < obstacle.ypoints.length; y++) {
					int ypoint = obstacle.ypoints[y];
					int nextYpoint = obstacle.ypoints[y];

					if (x < obstacle.xpoints.length - 1 || y < obstacle.ypoints.length - 1)
						obstacleLines.add(new Line2D.Double(new Point2D.Double(xpoint, ypoint),
								new Point2D.Double(nextXpoint, nextYpoint)));
					else
						obstacleLines.add(new Line2D.Double(new Point2D.Double(xpoint, ypoint),
								new Point2D.Double(obstacle.xpoints[0], obstacle.ypoints[0])));
				}
			}
		}
	}
}