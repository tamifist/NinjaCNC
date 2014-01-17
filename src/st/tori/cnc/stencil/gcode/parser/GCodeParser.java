package st.tori.cnc.stencil.gcode.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import st.tori.cnc.stencil.gcode.action.ActionFactory;
import st.tori.cnc.stencil.gcode.action.ActionInterface;
import st.tori.cnc.stencil.gcode.action.GAction;
import st.tori.cnc.stencil.gcode.action.GCode;
import st.tori.cnc.stencil.gcode.action.PositionInterface;
import st.tori.cnc.stencil.gcode.exception.InvalidIndexException;
import st.tori.cnc.stencil.gcode.exception.PositionNotSupportedException;
import st.tori.cnc.stencil.gcode.exception.SpeedNotSupportedException;
import st.tori.cnc.stencil.gcode.exception.UnsupportedPrefixException;

public class GCodeParser {

	private static final Pattern PATTERN = Pattern.compile("([A-Z])([\\.\\-0-9]+)");

	public GCode parse(File file) throws UnsupportedPrefixException, InvalidIndexException, PositionNotSupportedException, SpeedNotSupportedException {
		GCode gCode = new GCode();
		PositionInterface lastPosition = null;
		SpeedInterface lastSpeed = null;
		GAction action = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int lineCount = 0;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				line = line.trim();
				if(line.startsWith("("))continue;
				Matcher matcher;
				matcher = PATTERN.matcher(line);
				while(matcher.find()) {
					String prefix = matcher.group(1);
					String valStr = matcher.group(2);
					double val = Double.valueOf(valStr);
					if(valStr.indexOf(".")<0&&!"G".equals(prefix)&&!"M".equals(prefix))val /= 1000.0;
					if("G".equals(prefix)) {
						if(action!=null){
							gCode.add(action);
						}
						action = ActionFactory.createGAction((int)val,lastAction,lastSpeed);
					}else if("M".equals(prefix)) {
						gCode.add(ActionFactory.createMAction((int)val));
					}else if("X".equals(prefix)) {
						if(action==null)action = ActionFactory.createGAction(-1,lastAction,lastSpeed);
						if(action instanceof PositionInterface)
							((PositionInterface)action).setX(val);
						else
							throw new PositionNotSupportedException(line);
					}else if("Y".equals(prefix)) {
						if(action==null)action = ActionFactory.createGAction(-1,lastAction,lastSpeed);
						if(action instanceof PositionInterface)
							((PositionInterface)action).setY(val);
						else
							throw new PositionNotSupportedException(line);
					}else if("Z".equals(prefix)) {
						if(action==null)action = ActionFactory.createGAction(-1,lastAction,lastSpeed);
						if(action instanceof PositionInterface)
							((PositionInterface)action).setZ(val);
						else
							throw new PositionNotSupportedException(line);
					}else if("F".equals(prefix)) {
						if(action==null)action = ActionFactory.createGAction(-1,lastAction,lastSpeed);
						lastSpeed = val;
						if(action instanceof SpeedInterface)
							((SpeedInterface)action).setF(val);
						else
							throw new SpeedNotSupportedException(line);
					}else{
						throw new UnsupportedPrefixException(prefix);
					}
				}
				if(action!=null) {
					gCode.add(action);
					lastAction = action;
				}
				action = null;
				lineCount++;
			}
			br.close();
			System.out.println("There are "+lineCount+" lines,"+gCode.size()+" actions");
			Iterator<ActionInterface> ite = gCode.iterator();
			while(ite.hasNext())
				System.out.print("["+ite.next().getClass().getSimpleName()+"]");
			System.out.println();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		return gCode;
	}

}
