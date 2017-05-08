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
		float tolerance = 0.02f;
//		Vector2f currVelocity = info.getVelocity();

		float directionX = (float) (currentCheckpoint.getX() - myCurrX);
		float directionY = (float) (currentCheckpoint.getY() - myCurrY);
		float orientation2CP = (float) Math.atan2(directionY, directionX);
		float angleBetweenOrientations = orientation2CP - myCurrOrientation;
		
		if(angleBetweenOrientations > Math.PI) angleBetweenOrientations -= 2*Math.PI;
		if(angleBetweenOrientations < -Math.PI) angleBetweenOrientations += 2*Math.PI;
		
		// Winkel zwischen Orientierungen < Toleranz
		//  Bereits angekommen – Fertig!
		float wishTurnSpeed = 0;
		float throttle = 1f;
		float steering = 0;

		// • Winkel zw. Orientierungen < Abbremswinkel
		//  Wunschdrehgeschw. = (Zielorient. – Startorient.)∙ max. Drehgeschwindigkeit / Abbremswinkel
		if (Math.abs(angleBetweenOrientations) >= tolerance && Math.abs(angleBetweenOrientations) <= 0.4f) {
			wishTurnSpeed = (angleBetweenOrientations * maxAngularVelocity / 0.4f);
			
			
		}

		//  Sonst: Wunschdrehgeschw. = max. Drehgeschw.
		else if(Math.abs(angleBetweenOrientations) > 0.4){
//			throttle = (maxAngularVelocity - currAngularVelocity)/10;
			throttle = 1;
			wishTurnSpeed = maxAngularVelocity;	
		}

		// • DrehBeschleunigung = (Wunschdrehgeschw.
		// – aktuelle Drehgeschwindigkeit) / Wunschzeit
		
		steering = (wishTurnSpeed - currAngularVelocity) / 3;
		
		debugInfo(throttle, steering, angleBetweenOrientations, wishTurnSpeed);
		
		return new DriverAction(throttle, steering);
	}

	
	
	private void debugInfo(float throttle, float steering, float angleBetweenOrientations, float smooothTurnSpeed) {
		System.out.println("Throttle: " + throttle + " Steering: " + steering + " Angle between Orientations: "
				+ angleBetweenOrientations + " smoothTurnSpeed: " + smooothTurnSpeed);
		
		
	}


	// Face
	//  Zu einem Zielpunkt orientieren
	//  Richtung = (Ziel – Start)
	//  atan2(Richtung.y, Richtung.x)

	// • Look where you are going
	//  In Bewegungsrichtung orientieren
	//  atan2(Geschwindigkeit.y, Geschwindigkeit.x)

}
