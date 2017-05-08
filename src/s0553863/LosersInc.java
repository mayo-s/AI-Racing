package s0553863;

import java.awt.Point;
import java.awt.Polygon;
//import org.lwjgl.util.vector.Vector2f;
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
		float tolerance = 0.01f;
		float distance2CP = (float) Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2));
		float wishAngularVelocity = setWishAngularVelocity(angleBetweenOrientations, tolerance);

		if (angleBetweenOrientations > Math.PI)
			angleBetweenOrientations -= 2 * Math.PI;
		if (angleBetweenOrientations < -Math.PI)
			angleBetweenOrientations += 2 * Math.PI;

		float throttle = setThrottle(angleBetweenOrientations, tolerance, distance2CP);
		float steering = setSteering(wishAngularVelocity, currAngularVelocity);
		// debugInfo(throttle, steering, angleBetweenOrientations,
		// wishAngularVelocity, distance2CP, currVelocity);

		return new DriverAction(throttle, steering);
	}

	private float setThrottle(float angleBetweenOrientations, float tolerance, float distance2CP) {
		// Winkel zwischen Orientierungen < Toleranz
		// Bereits angekommen – Fertig!
		float throttle = maxVelocity;

		// Winkel zw. Orientierungen < Abbremswinkel
		// Wunschdrehgeschw. = (Zielorient. – Startorient.)∙ max.Drehgeschwindigkeit / Abbremswinkel

		if(distance2CP < 100) throttle = approachCP(distance2CP);
		else{
			if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= 0.4f) {
				throttle = 1f;
			}
			
			// Sonst: Wunschdrehgeschw. = max. Drehgeschw.
			else if (Math.abs(angleBetweenOrientations) > 0.4) {
				throttle = maxAngularVelocity;
			}
			
		}
		
		return throttle;
	}

	private float approachCP(float distance2CP) {
		float throttle = maxVelocity;
		float currVelocity = (float) Math.sqrt(Math.pow(info.getVelocity().x, 2) + Math.pow(info.getVelocity().y, 2));
		// Abstand(Start, Ziel) < Zielradius
		// Bereits angekommen – Fertig!
		if (distance2CP < 40 && currVelocity > 10) {
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
			throttle = (float) ((28 - currVelocity) / 2.5);
		}

		return throttle;
	}

	private float setWishAngularVelocity(float ABO, float tolerance) {
		float wishAngularVelocity = 0;
		if (Math.abs(ABO) >= tolerance && Math.abs(ABO) <= 0.4f) {
			wishAngularVelocity = (ABO * maxAngularVelocity / 0.4f);
		}

		// Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else if (Math.abs(ABO) > 0.4) {
			wishAngularVelocity = Math.signum(ABO) * maxAngularVelocity;
		}
		return wishAngularVelocity;
	}

	private float setSteering(float wishAngularVelocity, float currAngularVelocity) {
		// DrehBeschleunigung = (Wunschdrehgeschw. – aktuelle
		// Drehgeschwindigkeit) / Wunschzeit
		float steering = (wishAngularVelocity - currAngularVelocity) / 2.5f;
		return steering;
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

}
