package s0553863;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class LosersInc extends AI {
	
	public LosersInc(Info info) {
		super(info);
	}

	@Override
	public String getName() {
		return "Icke";
	}

	@Override
	public DriverAction update(boolean arg0) {
		return new DriverAction(1, 2);
	}

}
