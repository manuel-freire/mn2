package edu.umd.cs.hcil.socialaction.analysis.community;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;

import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.data.Node;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Based on Jeff Heer's Vizter (vizster.action.CommunityFilter)
 * 
 * @version 1.0
 * @author Adam Perer
 */
public class CommunityFilter extends Layout {

	// private ArrayList m_aggs = new ArrayList(50);
	Visualization m_vis;
	// AggregateTable m_at;
	SocialAction m_app;

	public CommunityFilter(SocialAction app, Visualization vis, String communityKey, AggregateTable at) {
		super(communityKey);
		m_vis = vis;
		m_app = app;
		// m_at = at;
	} //

	public void run(double frac) {
		// AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
		// do we have any to process?
		// int num = aggr.getTupleCount();
		// if ( num == 0 ) return;

		SubgraphSet comm = (SubgraphSet) m_vis.getFocusGroup(m_group);

		AggregateTable m_at = (AggregateTable) m_vis.getGroup(SocialAction.communityAggr);

		int num = comm.getCommunityCount();
		m_at.clear();

		// filter the aggregates to use
		for (int i = 0; i < num; i++) {
			Set<Node> set = comm.getCommunityMembers(i);

			AggregateItem aitem = (AggregateItem) m_at.addItem();
			aitem.setInt(SocialAction.AGGREGATE_ID_COLUMN_NAME, i);
			// aitem.setString(SocialAction.LABEL_COLUMN_NAME, m_app.aggregateLabels[i]);
			aitem.setString(SocialAction.LABEL_COLUMN_NAME, new Integer(i).toString());

			Iterator<Node> it = set.iterator();
			while (it.hasNext()) {

				Node node = (Node) it.next();
				aitem.addItem((VisualItem) node);
			}
			// m_aggs.add(getAggregate(registry, set));
		}

		// garbage collect the aggregates
		// super.run(registry, frac);

		// fill out the aggregate mappings
		// for ( int i=0; i<num; i++ ) {
		// Set set = comm.getCommunityMembers(i);
		// AggregateItem aitem = (AggregateItem)m_aggs.get(i);
		//            
		// Iterator iter = set.iterator();
		// while ( iter.hasNext() ) {
		// Node node = (Node)iter.next();
		// registry.addMapping(node,aitem);
		// }
		// }

		// clear the temporary list
		// m_aggs.clear();
	} //

	private int m_margin = 5; // convex hull pixel margin
	private double[] m_pts; // buffer for computing convex hulls

	private static void addPoint(double[] pts, int idx, VisualItem item, int growth) {
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
	}

	public void aggregateLayout() {

		AggregateTable aggr = (AggregateTable) m_vis.getGroup(m_group);
		// do we have any to process?
		int num = aggr.getTupleCount();
		if (num == 0)
			return;

		// update buffers
		int maxsz = 0;
		for (Iterator aggrs = aggr.tuples(); aggrs.hasNext();)
			maxsz = Math.max(maxsz, 4 * 2 * ((AggregateItem) aggrs.next()).getAggregateSize());
		if (m_pts == null || maxsz > m_pts.length) {
			m_pts = new double[maxsz];
		}

		// compute and assign convex hull for each aggregate
		Iterator aggrs = m_vis.visibleItems(m_group);
		while (aggrs.hasNext()) {
			AggregateItem aitem = (AggregateItem) aggrs.next();

			int idx = 0;
			if (aitem.getAggregateSize() == 0)
				continue;
			VisualItem item = null;
			Iterator iter = aitem.items();
			while (iter.hasNext()) {
				item = (VisualItem) iter.next();
				if (item.isVisible()) {
					addPoint(m_pts, idx, item, m_margin);
					idx += 2 * 4;
				}
			}
			// if no aggregates are visible, do nothing
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
			aitem.setValidated(false); // force invalidation
		}
	}

	// protected TableAggregateItem getAggregate(Set set) {
	//    	
	//        
	// AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
	// // do we have any to process?
	// // int num = aggr.getTupleCount();
	// // if ( num == 0 ) return;
	//        
	// AggregateItem aitem = null;
	// boolean highlight = true;
	//        
	// Node n = null;
	// Iterator iter = set.iterator();
	// while ( iter.hasNext() ) {
	// n = (Node)iter.next();
	//            
	// aitem = registry.getAggregateItem(n);
	// if ( aitem != null && aitem.getDirty() > 0 ) {
	// highlight = aitem.isHighlighted();
	// break;
	// } else {
	// aitem = null;
	// }
	// }
	// if ( aitem == null ) {
	// aitem = registry.getAggregateItem(n, true);
	// }
	//    	
	// registry.removeMappings(aitem);
	//    	
	// float[] poly = (float[])aitem.getVizAttribute("polygon");
	// Paint color = aitem.getColor();
	// Paint fill = aitem.getFillColor();
	//    	
	// aitem.init(registry, ItemRegistry.DEFAULT_AGGR_CLASS, n);
	// aitem.setVizAttribute("polygon", poly);
	// aitem.setColor(color);
	// aitem.setFillColor(fill);
	// aitem.setHighlighted(highlight);
	// aitem.setLocation(0,0);
	// aitem.setAggregateSize(set.size());
	// aitem.setInteractive(false);
	// aitem.setVisible(true);
	// return aitem;
	// } //

} // end of class CommunityFilter
