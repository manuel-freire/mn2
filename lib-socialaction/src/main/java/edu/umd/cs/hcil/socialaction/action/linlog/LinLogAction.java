/*
 * Created on Dec 3, 2005
 */
package edu.umd.cs.hcil.socialaction.action.linlog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ca.ubc.cs.smallworld.action.LinLogLayout;
import ca.ubc.cs.smallworld.action.MinimizerBarnesHut;
import ca.ubc.cs.smallworld.types.ProgressUpdate;

import prefuse.action.Action;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import edu.umd.cs.hcil.socialaction.SocialAction;

/**
 * Basically a prefuse wrapper of Andreas Noack's linlog layout code.
 *
 * Modified from SmallWorld
 * (http://www.cs.ubc.ca/~sfingram/cs533C/small_world.html)
 * (ca.ubc.cs.smallworld.action.LinLogAction) to work with Prefuse Beta
 * 
 * @author Stephen Frowe Ingram
 * @author Adam Perer
 */
public class LinLogAction extends Action {

	protected boolean m_dirty = true;
	protected ProgressUpdate m_progress = null;
	SocialAction m_app;

	public LinLogAction(SocialAction app, ProgressUpdate progress) {
		m_progress = progress;
		m_app = app;
	}

	public LinLogAction(SocialAction app) {
		this(app, null);
	}

	/**
	 * Only set this to true if the structure of the graph has somehow changed
	 * 
	 * @param dirty
	 */
	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public boolean getDirty() {
		return m_dirty;
	}

	public void doLayout() {

		if (m_dirty) {

			m_dirty = false;

			if (m_app.m_graph != null) {

				// convert the prefuse graph to a map of maps for linlog

				Graph graph = m_app.m_graph;

				Map<String, Map<String, Float>> temp_graph = new HashMap<String, Map<String, Float>>();

				Iterator edge_iter = graph.edges();
				while (edge_iter.hasNext()) {
					Edge edge = (Edge) edge_iter.next();
					String source = edge.getSourceNode().get(m_app.getKeyField()).toString();
					String target = edge.getTargetNode().get(m_app.getKeyField()).toString();
					if (temp_graph.get(source) == null)
						temp_graph.put(source, new HashMap<String, Float>());
					temp_graph.get(source).put(target, 1.0f);
				}

				// make it symmetric (it likely probably already is)
				temp_graph = LinLogLayout.makeSymmetricGraph(temp_graph);

				Map<String, Integer> nodeToId = LinLogLayout.makeIds(temp_graph);
				float[][] positions = LinLogLayout.makeInitialPositions(temp_graph);

				// see class MinimizerBarnesHut for a description of the parameters

				MinimizerBarnesHut minimizer = new MinimizerBarnesHut(LinLogLayout
						.makeAttrIndexes(temp_graph, nodeToId), LinLogLayout.makeAttrWeights(temp_graph, nodeToId),
						LinLogLayout.makeRepuWeights(temp_graph, nodeToId), 1.0f, 0.0f, 0.01f, positions, m_progress);

				// run the minimizer for at least 100 iterations

				minimizer.minimizeEnergy(100); // TODO make this a variable parameter
				// minimizer.minimizeEnergy(1); // TODO make this a variable parameter
				Map<String, float[]> nodeToPosition = LinLogLayout.convertPositions(positions, nodeToId);

				// update the graph and fit the nodes to the display

				Iterator node_iter = graph.nodes();
				while (node_iter.hasNext()) {
					Tuple cluster = (Tuple) node_iter.next();
					float[] pos = nodeToPosition.get(cluster.get(m_app.getKeyField()).toString());
					// pos[0] *= 30;
					// pos[1] *= 30;
					m_app.getVisualization().getVisualItem(SocialAction.graph, cluster).setX(pos[0]);
					m_app.getVisualization().getVisualItem(SocialAction.graph, cluster).setY(pos[1]);
					m_app.getVisualization().getVisualItem(SocialAction.graph, cluster).setBounds(pos[0] - 10.0f,
							pos[1] - 10.0f, 2 * 10.0f, 2 * 10.0f);
					//                    
					System.out.println(m_app.getVisualization().getVisualItem(SocialAction.graph, cluster) + " "
							+ pos[0] + ", " + pos[1]);

					// ?cluster.setCenter( new Point2D.Double( pos[0], pos[1] ) );
					// cluster.setBounds( new Rectangle2D.Double( pos[0]-cluster.getRadius(),
					// pos[1]-cluster.getRadius(), 2*cluster.getRadius(), 2*cluster.getRadius() ) );
				}

			}

		}

	}

	@Override
	public void run(double frac) {

		System.out.println("LINLOG2!");
		doLayout();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// if( args.length > 0 ) {
		//         
		// BasicGraphReader bgr = new BasicGraphReader( );
		// try {
		// Graph graph = bgr.loadGraph( args[0] );
		//
		//                
		// // LinLogAction lla = new LinLogAction( );
		// //
		// // System.out.print("Testing Layout...");
		// // long begin_time = System.currentTimeMillis();
		// // // test the linlog layout
		// // lla.doLayout( registry );
		// // System.out.println("done in " + ((System.currentTimeMillis()-begin_time)) + "seconds" );
		//                
		// // Iterator iter = registry.getGraph().getNodes();
		// // while( iter.hasNext() ) {
		// // Cluster cluster = (Cluster) iter.next();
		// // System.out.println("Cluster " + cluster.getAttribute("id") + " at "
		// // + cluster.getCenter() );
		// // }
		// //
		// } catch ( FileNotFoundException e ) {
		// e.printStackTrace();
		// } catch ( IOException e ) {
		// e.printStackTrace();
		// }
		//                        
		// }

	}

}
