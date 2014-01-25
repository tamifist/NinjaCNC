package st.tori.cnc.stencil.test;

import java.applet.Applet;
import java.awt.Graphics;
import java.io.File;

import st.tori.cnc.stencil.canvas.applet.DimensionController;
import st.tori.cnc.stencil.gcode.drill.Drill;
import st.tori.cnc.stencil.gcode.exception.GCodeException;
import st.tori.cnc.stencil.gcode.parser.GCode;
import st.tori.cnc.stencil.gcode.parser.GCodeParser;

public class TestApplet extends Applet {

	public void paint(Graphics g) {
		try {
			GCodeParser gcParser = new GCodeParser();
			GCode code = gcParser.parse(Drill.ORIMIN_VC, new File("../gerber/Levistone_tcream.ncd"));
			DimensionController dc = new DimensionController(true,this,g,code.getXYMinMax());
			code.draw(dc);
		} catch (GCodeException e) {
			e.printStackTrace();
		}
	}

}