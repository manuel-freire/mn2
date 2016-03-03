package edu.umd.cs.hcil.socialaction.analysis.community;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;

import prefuse.action.layout.Layout;
import prefuse.data.Node;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Based on Jeff Heer's Vizter (vizster.action.CommunityLayout)
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class CommunityLayout extends Layout {

	// buffer for computing convex hulls
	private double[] m_pts;
	private AggregateTable m_at;

	public CommunityLayout(String communityKey, AggregateTable at) {
		super(communityKey);

		m_at = at;
	} //

	/**
	 * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
	 */
	public void run(double frac) {
		// FocusManager fman = registry.getFocusManager();

		// AggregateTable aggr = (AggregateTable)m_vis.getFocusGroup(m_group);
		//        
		// if (aggr!=null)
		// {
		// System.out.println("yo!");
		// }

		SubgraphSet comm = (SubgraphSet) m_vis.getFocusGroup(SocialAction.community);

		// SubgraphSet comm = (SubgraphSet)fman.getFocusSet(m_key);

		// do we have any communities to process?
		int num = comm.getCommunityCount();
		if (num == 0)
			return;

		System.out.println(comm.getCommunityCount());

		// update buffers
		int maxsz = 0;
		for (int i = 0; i < num; i++)
			maxsz = Math.max(maxsz, 4 * 2 * comm.getCommunityMembers(i).size());
		if (m_pts == null || maxsz > m_pts.length) {
			m_pts = new double[maxsz];
		}

		int growth = 5;

		for (int i = 0; i < num; i++) {

			AggregateItem aitem = null;

			int idx = 0;
			Set<Node> set = comm.getCommunityMembers(i);
			Node node = null;
			Iterator<Node> iter = set.iterator();

			while (iter.hasNext()) {
				node = (Node) iter.next();
				VisualItem item = m_vis.getVisualItem(SocialAction.graph, node);
				if (item != null) {
					addPoint(m_pts, idx, item, growth);
					idx += 2 * 4;
				}

				// aitem.addItem(item);
			}

			// assumes each node will only have one aggregate item
			Iterator it = m_at.getAggregates(node);
			if (it.hasNext()) {
				aitem = (AggregateItem) it.next();
			}

			// aitem _vis.getVisualItem(SocialAction.community, node);

			// if (aggr.getAggregates(node) != null)//.hasNext())
			// aitem = (AggregateItem) aggr.getAggregates(node).next();
			// else
			// continue;
			//            

			// aitem = m_vis.ge.getAggregateItem(node);

			// if no community members are visible, do nothing
			if (idx == 0)
				continue;

			// compute convex hull
			double[] nhull = GraphicsLib.convexHull(m_pts, idx);

			// prepare viz attribute array
			float[] fhull = (float[]) aitem.get(VisualItem.POLYGON);
			if (fhull == null || fhull.length < nhull.length)
				fhull = new float[nhull.length];
			else if (fhull.length > nhull.length)
				fhull[nhull.length] = Float.NaN;

			// copy hull values
			for (int j = 0; j < nhull.length; j++)
				fhull[j] = (float) nhull[j];

			aitem.set(VisualItem.POLYGON, fhull);
			aitem.setValidated(false);
			// aitem.setVizAttribute("polygon", fhull);
		}

		System.out.println("rows " + m_at.getRowCount());
	}

	protected void addPoint(double[] pts, int idx, VisualItem item, int growth) {
		Rectangle2D b = item.getBounds();
		double minX = (b.getMinX()) - growth, minY = (b.getMinY()) - growth;
		double maxX = (b.getMaxX()) + growth, maxY = (b.getMaxY()) + growth;
		pts[idx] = minX;
		pts[idx + 1] = minY;
		pts[idx + 2] = minX;
		pts[idx + 3] = maxY;
		pts[idx + 4] = maxX;
		pts[idx + 5] = minY;
		pts[idx + 6] = maxX;
		pts[idx + 7] = maxY;
	} //

} // end of class CommunityLayout
