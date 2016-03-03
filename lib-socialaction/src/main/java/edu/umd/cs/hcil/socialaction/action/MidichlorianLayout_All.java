package edu.umd.cs.hcil.socialaction.action;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Influenced by VizsterLayout by Jeff Heer
 * 
 * From J. Heer and D. Boyd "Vizster: Visualizing online social networks": <blockquote>"To optimize the layout, we
 * parameterize the tension of the individual spring-edges by node connectivity. Nodes with lower connectivity are given
 * higher tension, causing singly-connected nodes to remain close to expanded nodes and connected communities to assume
 * higher 'orbits.' This additionally causes nodes with lower connectivity to more closely �tag-along� with their
 * friends, reinforcing their limited connections. We currently assign spring tension proportionally to the inverse
 * logarithm of the degree of the edge�s minimally connected node. As discussed later, we also change spring tensions to
 * improve the layout of inferred community groupings." </blockquote> <blockquote>"To further improve the layout,
 * extra-community edges are given weaker spring forces, promoting the spatial separation of inferred communities."
 * </blockquote>
 * 
 * @author Jeff Heer
 * @author Adam Perer
 * @author Cody Dunne
 */
public class MidichlorianLayout_All extends ForceDirectedLayout {

	// private float normal = 2E-5f;
	// private float slack1 = 2E-6f;
	// private float slack2 = 2E-7f;

	// private float NO_TENSION = 0f;
	// private float SINGLETON = 8E-5f;
	// private float ORBIT = 1E-5f;

	/** The spring coefficient for intra-community springs */
	private float EXTRACOMM;

	// private float EXTRACOMM = 1E-6f;
	// private float NORMAL = 2E-5f;

	private SocialAction m_app;

	private boolean m_layoutFilteredEdges = true;

	private boolean m_layoutFilteredNodes = true;

	public MidichlorianLayout_All(SocialAction app, String g, boolean enforceBounds) {
		super(g, enforceBounds);
		m_app = app;

		// Get the default intra-community spring coefficient
		EXTRACOMM = SocialAction.LAYOUT_VALUES_COMMUNITY[1];
		// initialize the force simulator
		// ForceSimulator fsim = new ForceSimulator();
		// fsim.addForce(new NBodyForce(-2.3f, -1f, 0.9f));
		// fsim.addForce(new SpringForce(2E-5f, 150f));
		// fsim.addForce(new DragForce(-0.005f));
		// setForceSimulator(fsim);

		// this.setMaxTimeStep(25L);
	} //

	public boolean getLayoutFilteredEdges() {
		return m_layoutFilteredEdges;
	}

	public boolean getLayoutFilteredNodes() {
		return m_layoutFilteredNodes;
	}

	public void setLayoutFilteredNodes(boolean layout) {
		m_layoutFilteredNodes = layout;
	}

	public void setLayoutFilteredEdges(boolean layout) {
		m_layoutFilteredEdges = layout;
	}

	protected float getSpringLength(EdgeItem e) {
		NodeItem n1 = (NodeItem) e.getSourceItem();
		NodeItem n2 = (NodeItem) e.getTargetItem();
		int minE = Math.min(n1.getDegree(), n2.getDegree());
		double doi = Math.max(n1.getDOI(), n2.getDOI());
		float len = lengthFunc(minE);
		return (minE == 1 ? 50.f : (doi == 0 ? 200.f : len));
	}

	/**
	 * Used by getSpringLength
	 * 
	 * @param numE
	 *            the minimum degree of either node
	 * @return the default length of the spring
	 */
	protected float lengthFunc(int numE) {
		numE = (numE > 10 ? 10 : numE - 1);
		return 50.f + (((float) numE) / 100.f) * 150.f;
	}

	protected float jonoCoeffFunc(EdgeItem e) {
		// String type = e.getAttribute( );
		// if ( type.equals("R") ) {
		// return 1E-4f;
		// } else if ( type.equals("PC") ) {
		// return 5E-5f;
		// } else if ( type.equals("SF") ) {
		// return 1E-6f;
		// }
		return 5E-6f;
	}

	protected float getSpringCoefficient(EdgeItem e) {
		NodeItem n1 = (NodeItem) e.getSourceItem();
		NodeItem n2 = (NodeItem) e.getTargetItem();

		if (!m_layoutFilteredNodes) {
			TupleSet nodeSliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_NODE_SLIDER_GROUP);

			System.err.println("node set\t: " + nodeSliderSet.getTupleCount());

			boolean as1 = nodeSliderSet.containsTuple(n1.getSourceTuple());
			boolean as2 = nodeSliderSet.containsTuple(n2.getSourceTuple());
			if (!as1 || !as2)
				return 0;
		}

		if (!m_layoutFilteredEdges) {
			TupleSet sliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_EDGE_SLIDER_GROUP);

			// System.err.println("edge set\t: " + sliderSet.getTupleCount());

			boolean as = sliderSet.containsTuple(e.getSourceTuple()); // item.isInGroup(SocialAction.RANGE_SLIDER_GROUP);
			if (!as) {
				return 0;
			}
		}

		int ec1 = n1.getDegree();
		int ec2 = n2.getDegree();
		// int maxE = Math.max(ec1,ec2);
		int minE = Math.min(ec1, ec2);

		Boolean b = e.getBoolean("extraCommunity");// false;//(Boolean)e.getVizAttribute("extraCommunity");
		boolean v = (b == null ? false : b.booleanValue());

		if (v) {
			if (m_app.getSubgraphCount() == 0)
				v = false;
		}
		float alpha = calcAlpha(minE);
		return alpha * (v ? EXTRACOMM : -1.f);

		// return -1.f;
	}

	//    
	//    
	// protected float getSpringCoefficientOld(EdgeItem e) {
	//        
	//
	//        
	// NodeItem n1 = (NodeItem)e.getSourceItem();
	// NodeItem n2 = (NodeItem)e.getTargetItem();
	// int ec1 = n1.getDegree();
	// int ec2 = n2.getDegree();
	// // int maxE = Math.max(ec1,ec2);
	// int minE = Math.min(ec1,ec2);
	//        
	// // get DOI values, this should be set by the FisheyeGraphFilter
	// // use them to determine if nodes are expanded foci
	// double doi1 = n1.getDOI();
	// double doi2 = n2.getDOI();
	// double doi = Math.max(n1.getDOI(), n2.getDOI());
	//        
	// if ( doi1 == 0 && doi2 == 0 ) {
	// // no tension at all, two fixed nodes
	// return NO_TENSION;
	// } else if ( minE == 1 && doi == 0 ) {
	// // singleton node, use strong tension
	// return SINGLETON;
	// } else if ( doi == 0 ) {
	// // loosen tension based on edge count of non-focus
	// int ec = (doi1 == 0 ? ec2 : ec1);
	// float alpha = calcAlpha(ec);
	// return alpha*ORBIT;
	// } else {
	// // two non-focus nodes, weight by the lesser edge count
	// // use a lesser baseline tension for extra-community edges
	//            
	// Boolean b = e.getBoolean("extraCommunity");// false;//(Boolean)e.getVizAttribute("extraCommunity");
	// boolean v = (b == null ? false : b.booleanValue() );
	//         
	// float alpha = calcAlpha(minE);
	// return alpha*(v?EXTRACOMM:NORMAL);
	// }
	// } //
	//    
	protected float getMassValue(NodeItem n) {
		return (float) n.getSize();
	}

	private float calcAlpha(int ec) {
		float ainv = (float) Math.max(1, 0.5 * Math.log(ec));
		return 1.0f / ainv;
	}

	public void setEXTRACOMM(float EXTRACOMM) {
		this.EXTRACOMM = EXTRACOMM;
	}
}
