/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.graph.decorators;

import prefuse.data.Edge;

/**
 * A generalized interface for setting and getting <code>Number</code>s of <code>ArchetypeEdge</code>s. Using this
 * interface allows algorithms to work without having to know how edges store this data.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.graph.decorators.NumberEdgeValue) to work with Prefuse.
 * 
 * @author Joshua O'Madadhain
 * @author Adam Perer
 */
public interface NumberEdgeValue {
	/**
	 * @param e
	 *            the edge to examine
	 * @return the Number associated with this edge
	 */
	public Number getNumber(Edge e);

	/**
	 * 
	 * @param e
	 *            the edge whose value we're setting
	 * @param n
	 *            the Number to which we're setting the edge
	 */
	public void setNumber(Edge e, Number n);
}
