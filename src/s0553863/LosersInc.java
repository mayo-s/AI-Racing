package s0553863;

import java.awt.Point;

import org.lwjgl.util.vector.Vector2f;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class LosersInc extends AI {

	float maxVelocity = info.getMaxVelocity();
	float maxAngularVelocity = info.getMaxAngularVelocity();
	float maxAcceleration = info.getMaxAcceleration();
	float maxAngularAcceleration = info.getMaxAngularAcceleration();

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
//		Vector2f currVelocity = info.getVelocity();

		float directionX = (float) (currentCheckpoint.getX() - myCurrX);
		float directionY = (float) (currentCheckpoint.getY() - myCurrY);
		float orientation2CP = (float) Math.atan2(directionY, directionX);
		float angleBetweenOrientations = Math.abs(orientation2CP - myCurrOrientation);
		float smoothTurnSpeed;
		// Winkel zwischen Orientierungen < Toleranz
		//  Bereits angekommen – Fertig!
		float throttle = 1;
		float steering = 0;

		// • Winkel zw. Orientierungen < Abbremswinkel
		//  Wunschdrehgeschw. = (Zielorient. – Startorient.)∙ max. Drehgeschwindigkeit / Abbremswinkel
		if (angleBetweenOrientations >= 0.1f && angleBetweenOrientations <= 0.4f) {
			smoothTurnSpeed = ((orientation2CP - myCurrOrientation) * maxAngularVelocity / angleBetweenOrientations);
			
			if(currAngularVelocity < 0.5f) throttle = 0.1f;
			if(currAngularVelocity > 0.7f) throttle = -0.5f;
			steering = -1 * smoothTurnSpeed;
		}
		else if (angleBetweenOrientations >= 5.8f && angleBetweenOrientations <= 6.2f) {
			smoothTurnSpeed = ((orientation2CP - myCurrOrientation) * maxAngularVelocity / angleBetweenOrientations);

			if(currAngularVelocity < 0.5f) throttle = 0.1f;
			if(currAngularVelocity > 0.7f) throttle = -0.5f;
			steering = smoothTurnSpeed;
		}
		//  Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else if(angleBetweenOrientations > 0.4 && angleBetweenOrientations <= Math.PI){
//			throttle = (maxAngularVelocity - currAngularVelocity)/10;
			throttle = 1;
			steering = -1 * maxAngularVelocity;	
		}
		else if(angleBetweenOrientations > Math.PI && angleBetweenOrientations < 5.8f){
			throttle = 1;
			steering = maxAngularVelocity;
		}

		debugInfo(throttle, steering, angleBetweenOrientations);
		
		return new DriverAction(throttle, steering);
	}

	
	
	private void debugInfo(float throttle, float steering, float angleBetweenOrientations) {
		System.out.println("Throttle: " + throttle + " Steering: " + steering + " Angle between Orientations: "
				+ angleBetweenOrientations);
		
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
