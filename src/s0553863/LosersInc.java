package s0553863;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
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

	public LosersInc(Info info) {
		super(info);
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
		float tolerance = 0.007f;
		float distance2CP = (float) Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2));
		float currVelocity = (float) Math.sqrt(Math.pow(info.getVelocity().x, 2) + Math.pow(info.getVelocity().y, 2));

		float angleValue = 18;
		float lengthMultiplier = 33;
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
		float wishTime = 1.5f;
		float wishAngularVelocity = 0;
		float throttle = maxVelocity;

		// Winkel zw. Orientierungen < Abbremswinkel
		// Wunschdrehgeschw. = (Zielorient. – Startorient.)∙ max.
		// Drehgeschwindigkeit / Abbremswinkel
		if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= Math.PI / 4) {
			throttle = 1f;
			wishAngularVelocity = (angleBetweenOrientations * maxAngularVelocity / 0.4f);
		}

		// Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else if (Math.abs(angleBetweenOrientations) > Math.PI / 4) {
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

		float testValue = 18;
		float lengthMultiplier = 33;

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

}
