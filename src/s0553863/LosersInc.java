package s0553863;

import java.awt.Point;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class LosersInc extends AI {

	public LosersInc(Info info) {
		super(info);
	}

	@Override
	public String getName() {
		return "Numero Uno";
	}

	@Override
	public DriverAction update(boolean arg0) {

		float maxAngularVelocity = info.getMaxAngularVelocity(); // Value = 1.5f
																	// - make
																	// field ?
		float maxAngularAcceleration = info.getMaxAngularAcceleration();
		float maxAcceleration = info.getMaxAcceleration();
		float maxVelocity = info.getMaxVelocity();

		float myCurrX = info.getX();
		float myCurrY = info.getY();
		float myCurrOrientation = info.getOrientation();

		Point currentCheckpoint = info.getCurrentCheckpoint();

		float directionX = (float) (currentCheckpoint.getX() - myCurrX);
		float directionY = (float) (currentCheckpoint.getY() - myCurrY);
		float orientation2CP = (float) Math.atan2(directionY, directionX);
		float angleBetweenOrientations = orientation2CP - myCurrOrientation;
		// System.out.println("Angle between orientations: " +
		// angleBetweenOrientations);

		float smoothTurnSpeed;
		float throttle = 1;
		float steering = 0;
		
		// Winkel zwischen Orientierungen < Toleranz
		//  Bereits angekommen – Fertig!
		if (Math.abs(angleBetweenOrientations) < 0.01 || Math.abs(angleBetweenOrientations) > 6.2f) {
			throttle = 1;
			steering = 0;
		}
		// • Winkel zw. Orientierungen < Abbremswinkel
		//  Wunschdrehgeschw. = (Zielorient. – Startorient.)
		// ∙ max. Drehgeschwindigkeit / Abbremswinkel
		else if (angleBetweenOrientations <= 0.4f) {
			smoothTurnSpeed = ((orientation2CP - myCurrOrientation) * maxAngularVelocity / angleBetweenOrientations);
			throttle = 0.1f;
			steering = smoothTurnSpeed;
		} else if (angleBetweenOrientations >= 5.8f) {
			smoothTurnSpeed = ((orientation2CP - myCurrOrientation) * maxAngularVelocity / angleBetweenOrientations);
			throttle = 0.1f;
			steering = smoothTurnSpeed;
		}
		//  Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else {
			if (angleBetweenOrientations > 0.4f && angleBetweenOrientations < Math.PI) {
				throttle = 1;
				steering = maxAngularVelocity;
			}
			if (angleBetweenOrientations >= Math.PI && angleBetweenOrientations < 5.8f) {
				throttle = 1;
				steering = -1f * maxAngularVelocity;
			}
		}
		
		System.out.println("Throttle: " + throttle + " Steering: " + steering + " Angle between Orientations: " + angleBetweenOrientations);
		return new DriverAction(throttle, steering);
	}

	// • Beschleunigung = (Wunschdrehgeschw.
	// – aktuelle Drehgeschwindigkeit) / Wunschzeit

	// Face
	//  Zu einem Zielpunkt orientieren
	//  Richtung = (Ziel – Start)
	//  atan2(Richtung.y, Richtung.x)

	// • Look where you are going
	//  In Bewegungsrichtung orientieren
	//  atan2(Geschwindigkeit.y, Geschwindigkeit.x)

}
