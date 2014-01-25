package st.tori.cnc.stencil.gerber.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import st.tori.cnc.stencil.canvas.Drawable;
import st.tori.cnc.stencil.canvas.PositionXYInterface;
import st.tori.cnc.stencil.canvas.SimpleXY;
import st.tori.cnc.stencil.canvas.applet.DimensionController;
import st.tori.cnc.stencil.gerber.exception.IllegalReflectionException;
import st.tori.cnc.stencil.gerber.exception.NoLastStatementExistsException;
import st.tori.cnc.stencil.gerber.statement.GStatement;
import st.tori.cnc.stencil.gerber.statement.GStatement02;
import st.tori.cnc.stencil.gerber.statement.GStatement03;
import st.tori.cnc.stencil.gerber.statement.GStatement36;
import st.tori.cnc.stencil.gerber.statement.GStatement37;
import st.tori.cnc.stencil.gerber.statement.GStatement74;
import st.tori.cnc.stencil.gerber.statement.GStatement75;
import st.tori.cnc.stencil.gerber.statement.MStatement02;
import st.tori.cnc.stencil.gerber.statement.StatementInterface;
import st.tori.cnc.stencil.util.NumberUtil;


public class Gerber extends ArrayList<StatementInterface> implements Drawable {

	private static final String RET = "\n";

	private boolean initialized = false;
	private boolean finalized = false;
	
	public final void initialize() {
		initialized = true;
	}
	public final void finalize() {
		if(!initialized)return;
		add(new MStatement02());
		finalized = true;
	}
		
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		Iterator<StatementInterface> ite = super.iterator();
		StatementInterface lastStatement = null;
		GStatement lastGStatement = null;
		PositionXYInterface lastPosition = null;
		while(ite.hasNext()) {
			StatementInterface statement = ite.next();
			if(lastStatement!=null)
				buf.append(RET);
			buf.append(statement.getSimpleName());
			if(statement instanceof PositionXYInterface) {
				PositionXYInterface position = (PositionXYInterface)statement;
				if(lastPosition==null||lastPosition.getX()!=position.getX()||lastPosition.getY()!=position.getY()) {
					buf.append("X").append(NumberUtil.toGCodeValue(position.getX()));
					buf.append("Y").append(NumberUtil.toGCodeValue(position.getY()));
				}
				lastPosition = position;
			}
			lastStatement = statement;
		}
		buf.append(RET);
		//Remove ret+ret which inserts around first Z-XY positioning
		return buf.toString().replaceAll(RET+RET+"?", RET);
	}
	
	public enum INTERPOLATION_MODE {
		UNDEF,
		LINER,
		CLOCKWISE_CIRCULAR,
		COUNTERCLOCKWISE_CIRCULAR,
	}
	public enum REGION_MODE {
		UNDEF,
		ON,
		OFF,
	}
	public enum QUADRANT_MODE {
		UNDEF,
		SINGLE,
		MULTI,
	}
	
	private INTERPOLATION_MODE interpolation = INTERPOLATION_MODE.UNDEF;
	private REGION_MODE region = REGION_MODE.UNDEF;
	private QUADRANT_MODE quadrant = QUADRANT_MODE.UNDEF;
	
	private GStatement lastStatement = null;
	private PositionXYInterface lastPosition = null;
	
	public GStatement getLastStatement(){	return lastStatement;	}
	public PositionXYInterface getLastPosition(){	return lastPosition;	}
	
	public GStatement cloneLastStatement() throws NoLastStatementExistsException, IllegalReflectionException {
		if(lastStatement==null)
			throw new NoLastStatementExistsException();
		try {
			return (GStatement)(lastStatement.getClass().getConstructor(getClass()).newInstance(this));
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalReflectionException(e);
		}
	}
	
	@Override
	public final boolean add(StatementInterface statement) {
		if(statement instanceof GStatement02) {
			interpolation = INTERPOLATION_MODE.CLOCKWISE_CIRCULAR;
		}else if(statement instanceof GStatement03) {
			interpolation = INTERPOLATION_MODE.COUNTERCLOCKWISE_CIRCULAR;
		}else if(statement instanceof GStatement36) {
			region = REGION_MODE.ON;
		}else if(statement instanceof GStatement37) {
			region = REGION_MODE.OFF;
		}else if(statement instanceof GStatement74) {
			quadrant = QUADRANT_MODE.SINGLE;
		}else if(statement instanceof GStatement75) {
			quadrant = QUADRANT_MODE.MULTI;
		}
		if(statement instanceof GStatement)
			lastStatement = (GStatement)statement;
		if(statement instanceof PositionXYInterface)
			lastPosition = (PositionXYInterface)statement;
		return super.add(statement);
	}
	
	private List<Drawable> drawables = new ArrayList<Drawable>();
	@Override
	public void draw(DimensionController dc) {
		Iterator<Drawable> ite = drawables.iterator();
		while(ite.hasNext())
			ite.next().draw(dc);
	}
	
	@Override
	public PositionXYInterface[] getXYMinMax() {
		if(drawables.size()<=0)return null;
		PositionXYInterface minX = null;
		PositionXYInterface minY = null;
		PositionXYInterface maxX = null;
		PositionXYInterface maxY = null;
		for(int i=0;i<drawables.size();i++) {
			PositionXYInterface[] xyMinMax = drawables.get(i).getXYMinMax();
			if(xyMinMax==null)continue;
			if(minX==null||xyMinMax[0].getX()<minX.getX())minX = xyMinMax[0];
			if(minY==null||xyMinMax[0].getY()<minY.getY())minY = xyMinMax[0];
			if(maxX==null||xyMinMax[1].getX()>maxX.getX())maxX = xyMinMax[1];
			if(maxY==null||xyMinMax[1].getY()>maxY.getY())maxY = xyMinMax[1];
		}
		if(minX==null||minY==null||maxX==null||maxY==null)return null;
		return new PositionXYInterface[]{
			new SimpleXY(minX.getX(), minY.getY()),
			new SimpleXY(maxX.getX(), maxY.getY()),
		};
	}
	
	public final boolean add(Drawable drawable) {
		return drawables.add(drawable);
	}

}