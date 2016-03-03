package edu.umd.cs.hcil.socialaction.action;

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * An Action that can be parameterized to process a particular group of items.
 * 
 * @author Adam Perer
 */
public class RelationshipConnectorAction extends Action {

	Graph m_graph;
	String m_relationship;
	SocialAction m_app;

	/**
	 * Create a new GroupAction that processes the specified group.
	 * 
	 * @param vis
	 *            the {@link prefuse.Visualization} to process
	 * @param group
	 *            the name of the group to process
	 */

	// WE NEED TO FIX THIS TO MAKE USE OF THE KEY...
	public RelationshipConnectorAction(SocialAction app, Visualization vis, Graph graph, String relationshipField) {
		super(vis);

		m_graph = graph;
		m_relationship = relationshipField;
		this.m_app = app;

	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {

		Iterator iEdges = m_graph.edges();
		while (iEdges.hasNext()) {
			Edge e = (Edge) iEdges.next();
			m_graph.removeEdge(e);
		}

		// add edges

		if (m_graph.getNodeTable().getColumnType(m_relationship) == int.class) {

			Iterator iIt = m_graph.nodes();
			while (iIt.hasNext()) {

				Node i = (Node) iIt.next();
				Integer iValue = (Integer) (i.get(m_relationship));
				Iterator jIt = m_graph.nodes();
				while (jIt.hasNext()) {
					Node j = (Node) jIt.next();
					Integer jValue = (Integer) j.get(m_relationship);
					if (iValue.intValue() == jValue.intValue()) {
						m_graph.addEdge(i, j);
					}

				}

			}
		} else if (m_graph.getNodeTable().getColumnType(m_relationship) == int[].class) {

			Iterator iIt = m_graph.nodes();
			while (iIt.hasNext()) {

				Node i = (Node) iIt.next();
				int[] iArray = (int[]) (i.get(m_relationship));

				for (int cnt = 0; cnt < iArray.length; cnt++) {

					Node j = m_graph.getNode(iArray[cnt] - 1);
					m_graph.addEdge(i, j);
				}

				/*
				 * Iterator jIt = m_graph.nodes(); while (jIt.hasNext()) { Node j = (Node) jIt.next(); Integer jValue =
				 * (Integer) j.get(selectedRelationshipField); if (iValue.intValue() == jValue.intValue()) {
				 * m_graph.addEdge(i, j); }
				 * 
				 * }
				 */

			}

		}

	}

} // end of class GroupAction
