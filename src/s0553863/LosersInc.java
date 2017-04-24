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
		
		float myCurrX = info.getX();
		float myCurrY = info.getY();
		float myCurrOrientation = info.getOrientation();
		
		Point currentCheckpoint = info.getCurrentCheckpoint();
		
		float directionX = (float) (currentCheckpoint.getX() - myCurrX);
		float directionY = (float) (currentCheckpoint.getY() - myCurrY);
		float orientation2CP = (float) Math.atan2(directionY, directionX);
		float newOrientation = Math.abs(orientation2CP - myCurrOrientation);
		System.out.println("new orientation: " + newOrientation);
		
		float smoothTurnSpeed = 0.1f;
		if(newOrientation < 0.01){
			return new DriverAction(1, 0);
		}
		else if(newOrientation < 0.4){
			smoothTurnSpeed = ((orientation2CP - myCurrOrientation) * 0.3f / newOrientation);
			return new DriverAction(1, 0.1f);
		}	
		else
			return new DriverAction(1, 0.2f);
	}

}
