package edu.umd.cs.hcil.socialaction.control;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.ui.panels.GraphReadabilityPanel;

public class DragReadabilityControl extends ControlAdapter {

	private SocialAction m_app;

	private GraphReadabilityPanel rp;

	HashSet<VisualItem> matches = new HashSet<VisualItem>();

	public DragReadabilityControl(SocialAction m_app) {
		super();
		this.m_app = m_app;
	}

	public void itemDragged(VisualItem item, java.awt.event.MouseEvent e) {
		super.itemDragged(item, e);
		if (!(item instanceof TableNodeItem))
			return;
		TableNodeItem n = (TableNodeItem) item;

		SocialActionNeighborControl neighborControl = m_app.getNeighborControl();
		neighborControl.setNeighborHighlight(n, false);

		int total = 0;
		boolean printTotals = false;
		boolean printDetails = false;
		if (printTotals || printDetails)
			System.out.println();

		rp = m_app.getReadabilityPanel();
		rp.setupTable();// XXX better spot

		HashSet<VisualItem> newMatches = new HashSet<VisualItem>();

		int ei = rp.getEdgeIntersections(n, newMatches, printTotals, printDetails);
		rp.setIntMeasure(n, GraphReadabilityPanel.EDGE_CROSSING, ei);
		total += ei;

		int it = rp.getTriggeredTunnels(n, newMatches, printTotals, printDetails);
		rp.setIntMeasure(n, GraphReadabilityPanel.TRIGGERED_EDGE_TUNNELS, it);
		total += it;

		int dt = rp.getLocalTunnels(n, newMatches, printTotals, printDetails);
		rp.setIntMeasure(n, GraphReadabilityPanel.LOCAL_EDGE_TUNNELS, dt);
		total += dt;

		int no = rp.getNodeOverplot(n, newMatches, printTotals, printDetails);
		rp.setIntMeasure(n, GraphReadabilityPanel.NODE_OCCLUSION, no);
		total += no;

		rp.globalMeasures[0] = (rp.getAllNodeOverplots(new HashSet<VisualItem>(), printTotals, printDetails));
		rp.globalMeasures[1] = (rp.getAllLocalTunnels(new HashSet<VisualItem>(), printTotals, printDetails));
		rp.globalMeasures[2] = (rp.getAllEdgeIntersections(new HashSet<VisualItem>(), printTotals, printDetails));
		if (printTotals)
			System.out.println("Sum: " + total);

		/*
		 * HashSet<VisualItem> union = new HashSet<VisualItem>(matches); union.addAll(newMatches);
		 * 
		 * HashSet<VisualItem> intersection = new HashSet<VisualItem>(matches); intersection.retainAll(newMatches);
		 */

		HashSet<VisualItem> difference = new HashSet<VisualItem>(matches);
		difference.removeAll(newMatches);

		for (VisualItem vi : difference) {
			vi.setHighlighted(false);
		}
		for (VisualItem vi : newMatches) {
			vi.setHighlighted(true);
		}
		matches = newMatches;

		m_app.setReadabilityPanel(item);

	}

	public void itemReleased(VisualItem item, java.awt.event.MouseEvent e) {
		super.itemReleased(item, e);
		if (!(item instanceof TableNodeItem))
			return;
		TableNodeItem n = (TableNodeItem) item;

		// if(n == current){//We don't care. If it's another, we want to disable anyway
		for (VisualItem vi : matches) {
			vi.setHighlighted(false);
		}
		matches = new HashSet<VisualItem>();
		// }
		SocialActionNeighborControl neighborControl = m_app.getNeighborControl();
		neighborControl.setNeighborHighlight(n, true);
	}

	// XXX below here tagged for removal after Prefuse code problems explored
	/** Indicates no intersection between shapes */
	public static final int NO_INTERSECTION = 0;
	/** Indicates intersection between shapes */
	public static final int COINCIDENT = 1;
	/** Indicates intersection between shapes */
	public static final int INTERSECT = 1;
	/** Indicates two lines are parallel */
	public static final int PARALLEL = 2;

	/**
	 * Compute the intersection of two line segments.
	 * 
	 * @param a1x
	 *            the x-coordinate of the first endpoint of the first line
	 * @param a1y
	 *            the y-coordinate of the first endpoint of the first line
	 * @param a2x
	 *            the x-coordinate of the second endpoint of the first line
	 * @param a2y
	 *            the y-coordinate of the second endpoint of the first line
	 * @param b1x
	 *            the x-coordinate of the first endpoint of the second line
	 * @param b1y
	 *            the y-coordinate of the first endpoint of the second line
	 * @param b2x
	 *            the x-coordinate of the second endpoint of the second line
	 * @param b2y
	 *            the y-coordinate of the second endpoint of the second line
	 * @param intersect
	 *            a Point in which to store the intersection point
	 * @return the intersection code. One of {@link #NO_INTERSECTION}, {@link #COINCIDENT}, or {@link #PARALLEL}.
	 */
	public static int intersectLineLine(double a1x, double a1y, double a2x, double a2y, double b1x, double b1y,
			double b2x, double b2y, Point2D intersect) {

		double ua_t = (b2x - b1x) * (a1y - b1y) - (b2y - b1y) * (a1x - b1x);
		double ub_t = (a2x - a1x) * (a1y - b1y) - (a2y - a1y) * (a1x - b1x);
		double u_b = (b2y - b1y) * (a2x - a1x) - (b2x - b1x) * (a2y - a1y);

		if (u_b != 0) {
			double ua = ua_t / u_b;
			double ub = ub_t / u_b;

			if (0 <= ua && ua <= 1 && 0 <= ub && ub <= 1) {
				intersect.setLocation(a1x + ua * (a2x - a1x), a1y + ua * (a2y - a1y));
				return INTERSECT;
			} else {
				return NO_INTERSECTION;
			}
		} else {
			return (ua_t == 0 && ub_t == 0 ? COINCIDENT : PARALLEL);
		}
	}

	public static int intersectLineRectangle(Point2D a1, Point2D a2, Rectangle2D r, Point2D[] pts) {
		double a1x = a1.getX(), a1y = a1.getY();
		double a2x = a2.getX(), a2y = a2.getY();
		double mxx = r.getMaxX(), mxy = r.getMaxY();
		double mnx = r.getMinX(), mny = r.getMinY();

		if (pts[0] == null)
			pts[0] = new Point2D.Double();
		if (pts[1] == null)
			pts[1] = new Point2D.Double();

		int i = 0;
		if (intersectLineLine(mnx, mny, mxx, mny, a1x, a1y, a2x, a2y, pts[i]) > 0)
			i++;
		if (intersectLineLine(mxx, mny, mxx, mxy, a1x, a1y, a2x, a2y, pts[i]) > 0)
			i++;
		if (i == 2)
			return i;
		if (intersectLineLine(mxx, mxy, mnx, mxy, a1x, a1y, a2x, a2y, pts[i]) > 0)
			i++;
		if (i == 2)
			return i;
		if (intersectLineLine(mnx, mxy, mnx, mny, a1x, a1y, a2x, a2y, pts[i]) > 0)
			i++;
		return i;
	}
}
