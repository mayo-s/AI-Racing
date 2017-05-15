package s0553863;

import java.awt.Point;
import java.awt.Polygon;
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
		float tolerance = 0.005f;
		float distance2CP = (float) Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2));
		float currVelocity = (float) Math.sqrt(Math.pow(info.getVelocity().x, 2) + Math.pow(info.getVelocity().y, 2));

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
		if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= 0.4f) {
			throttle = 1f;
			wishAngularVelocity = (angleBetweenOrientations * maxAngularVelocity / 3f);
		}

		// Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else if (Math.abs(angleBetweenOrientations) > 0.4) {
			throttle = 1;
			wishAngularVelocity = Math.signum(angleBetweenOrientations) * maxAngularVelocity;
		}

		// DrehBeschleunigung = (Wunschdrehgeschw. – aktuelle
		// Drehgeschwindigkeit) / Wunschzeit
		float steering = (wishAngularVelocity - currAngularVelocity) / wishTime;

		// Abstand(Start, Ziel) < Zielradius
		// Bereits angekommen – Fertig!
		if (distance2CP < 10) {
			throttle = 0;
			steering = 1;
		} else if (distance2CP < 40 && currVelocity > 10) {
			throttle = 0;
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

	private void avoidObstacle(float currX, float currY) {

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
		
		glBegin(GL_LINES);
			glVertex2f(info.getX(), info.getY());
			glVertex2f((float) info.getCurrentCheckpoint().getX(), (float) info.getCurrentCheckpoint().getY());
		glEnd();
		
	}
	

}
