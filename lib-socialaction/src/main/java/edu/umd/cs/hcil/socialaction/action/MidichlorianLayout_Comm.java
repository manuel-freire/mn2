package edu.umd.cs.hcil.socialaction.action;

import java.util.Iterator;
import java.util.Set;

import prefuse.Visualization;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.analysis.community.SubgraphSet;

/**
 * Influenced by VizsterLayout by Jeff Heer
 * 
 * From J. Heer and D. Boyd "Vizster: Visualizing online social networks": <blockquote>"To optimize the layout, we
 * parameterize the tension of the individual spring-edges by node connectivity. Nodes with lower connectivity are given
 * higher tension, causing singly-connected nodes to remain close to expanded nodes and connected communities to assume
 * higher 'orbits.' This additionally causes nodes with lower connectivity to more closely 'tag-along' with their
 * friends, reinforcing their limited connections. We currently assign spring tension proportionally to the inverse
 * logarithm of the degree of the edge's minimally connected node. As discussed later, we also change spring tensions to
 * improve the layout of inferred community groupings." </blockquote> <blockquote>"To further improve the layout,
 * extra-community edges are given weaker spring forces, promoting the spatial separation of inferred communities."
 * </blockquote>
 * 
 * @author Jeff Heer
 * @author Adam Perer
 * @author Cody Dunne
 */
public class MidichlorianLayout_Comm extends ForceDirectedLayout {

	// private float normal = 2E-5f;
	// private float slack1 = 2E-6f;
	// private float slack2 = 2E-7f;
	// private float NO_TENSION = 0f;
	// private float SINGLETON = 8E-5f;
	// private float ORBIT = 1E-5f;

	/** The spring coefficient for intra-community springs */
	private float EXTRACOMM;

	private SocialAction m_app;

	private boolean m_layoutFilteredEdges = true;

	private boolean m_layoutFilteredNodes = true;

	private boolean considerNeighbors = true;

	public MidichlorianLayout_Comm(SocialAction app, String g, boolean enforceBounds, boolean considerNeighbors) {
		super(g, enforceBounds);
		m_app = app;

		this.considerNeighbors = considerNeighbors;

		// Get the default intra-community spring coefficient
		EXTRACOMM = SocialAction.LAYOUT_VALUES_COMMUNITY[1];
		// initialize the force simulator
		// ForceSimulator fsim = new ForceSimulator();
		// fsim.addForce(new NBodyForce(-2.3f, -1f, 0.9f));
		// fsim.addForce(new SpringForce(2E-5f, 150f));
		// fsim.addForce(new DragForce(-0.005f));
		// setForceSimulator(fsim);
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

	/**
	 * Overrides Prefuse's getSpringLength
	 * 
	 */
	protected float getSpringLength(EdgeItem e) {

		NodeItem n1 = (NodeItem) e.getSourceItem();
		NodeItem n2 = (NodeItem) e.getTargetItem();
		// TODO: are the node in the same community?
		// if yes - increase the spring force
		// if not - descrease the spring force
		boolean inSameCommunity = areInSameCommunity(n1, n2);
		float communityDelta;

		String colorField = SocialAction.getInstance().getNodeJoinAttribute();

		/*
		 * if (colorField != null ){ if (n1.get(colorField).equals(n2.get(colorField))) communityDelta = 0.2f; else
		 * communityDelta = 2.0f; } else
		 */
		if (inSameCommunity || (colorField != null && n1.get(colorField).equals(n2.get(colorField))))
			communityDelta = 0.2f;
		else
			communityDelta = 2.0f;

		int minDegree = Math.min(n1.getDegree(), n2.getDegree());
		double degreeOfInterest = Math.max(n1.getDOI(), n2.getDOI());
		float len = lengthFunc(minDegree);

		if (minDegree == 1)
			return 50.0f * communityDelta;
		else if (degreeOfInterest == 0)
			return 200.0f * communityDelta;
		else
			return len * communityDelta;
	}

	protected float lengthFunc(int minDegree) {
		if (minDegree > 10)
			minDegree = 10;
		else
			minDegree--;

		return 50.f + ((float) minDegree) * 1.5f;
	} //

	protected float getSpringCoefficient(EdgeItem e) {
		NodeItem n1 = e.getSourceItem();
		NodeItem n2 = e.getTargetItem();

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

		// check whether the edge is between commnuities or within one community
		Boolean b = e.getBoolean("extraCommunity");
		boolean visualize;
		if (b == null)
			visualize = false;
		else
			visualize = b.booleanValue();

		if (m_app.getSubgraphCount() == 0)
			visualize = false;

		float alpha = calculateAlpha(n1, n2);

		// System.out.println("alpha is " + alpha);
		// unless the edge is outside the community in question, set spring coefficient to -1
		return alpha * (visualize ? EXTRACOMM : -1.f);
		// return (float)Math.random();
		// return -1.f;
	}

	/**
	 * Calculate a coefficient for the spring based on the min node degree of the vertices incident on the edge
	 */

	private float calculateAlpha(NodeItem n1, NodeItem n2) {
		String colorField = SocialAction.getInstance().getNodeJoinAttribute();
		// joinByAttribute = SocialAction.getInstance().mc

		float alpha = 0.0f;

		int nodeDegree1 = n1.getDegree();
		int nodeDegree2 = n2.getDegree();
		int minDegree = Math.min(nodeDegree1, nodeDegree2);

		// if in the different communities
		SubgraphSet subgraphs = (SubgraphSet) m_app.m_vis.getFocusGroup(SocialAction.community);
		Set<Node> neighbors2 = n2.getNeighborSet();

		// Object o = n1.get(colorField);
		// Object o2 = n2.get(colorField);

		/*
		 * if (colorField != null) { if (n1.get(colorField).equals(n2.get(colorField))) { alpha = 0.01f; } else { alpha
		 * = 100.0f; } } else // join by communities
		 */if (subgraphs.areInSameCommunity(n1, n2)
				|| (colorField != null && n1.get(colorField).equals(n2.get(colorField)))) {
			if (considerNeighbors && neighbors2.contains(n1)) {
				// check how many neighbours of these two nodes are connected
				// n1.neighbors() returns copied nodes; contains() won't return true
				Iterator<Node> iterator = n1.getNeighborSet().iterator();

				int commonNeighborsCnt = 2;
				Node node;
				while (iterator.hasNext()) {
					node = iterator.next();
					if (neighbors2.contains(node))
						commonNeighborsCnt++;
				}

				alpha = (float) 0.01f / commonNeighborsCnt;
			} else
				alpha = (float) 0.01f;
		} else { // pull apart
			alpha = (float) Math.max(1, 0.5 * Math.log(minDegree));
		}
		return 1.0f / alpha;
	}

	private boolean areInSameCommunity(NodeItem n1, NodeItem n2) {
		SocialAction sa = SocialAction.getInstance();
		Visualization visualization = sa.m_vis;
		SubgraphSet subgraphs = (SubgraphSet) visualization.getFocusGroup(SocialAction.community);

		return subgraphs.areInSameCommunity(n1, n2);
	}

	public void setEXTRACOMM(float EXTRACOMM) {
		this.EXTRACOMM = EXTRACOMM;
	}
}
