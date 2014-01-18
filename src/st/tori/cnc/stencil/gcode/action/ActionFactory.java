package st.tori.cnc.stencil.gcode.action;

import st.tori.cnc.stencil.gcode.exception.IllegalReflectionException;
import st.tori.cnc.stencil.gcode.exception.InvalidIndexException;
import st.tori.cnc.stencil.gcode.exception.NoLastActionExistsException;

public class ActionFactory {

	public static GAction createGAction(int gIndex, GCode gCode) throws InvalidIndexException, NoLastActionExistsException, IllegalReflectionException {
		GAction action;
		if(gIndex<0) {
			action = gCode.cloneLastAction();
			if(action instanceof GAction54to59)
				((GAction54to59)action).setGIndex(gCode.getLastAction().getGIndex());
		}else if(gIndex==0)
			action = new GAction00(gCode);
		else if(gIndex==1)
			action = new GAction01(gCode);
		else if(gIndex==21)
			action = new GAction21(gCode);
		else if(gIndex>=54&&gIndex<=59) {
			action = new GAction54to59(gCode);
			((GAction54to59)action).setGIndex(gIndex);
		}else if(gIndex==61)
			action = new GAction61(gCode);
		else if(gIndex==90)
			action = new GAction90(gCode);
		else
			throw new InvalidIndexException("G",gIndex);
		return action;
	}
	
	public static MAction createMAction(int mIndex) throws InvalidIndexException {
		if(mIndex==3)return new MAction03();
		if(mIndex==30)return new MAction30();
		throw new InvalidIndexException("M",mIndex);
	}

}
