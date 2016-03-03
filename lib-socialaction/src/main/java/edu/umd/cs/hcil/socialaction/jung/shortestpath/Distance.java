/*
 * Created on Apr 2, 2004
 * 
 * Copyright (c) 2004, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.shortestpath;

import java.util.Map;

import prefuse.data.Node;

/**
 * An interface for classes which calculate the distance between one vertex and another.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.shortestpath.Distance) to work with Prefuse.
 * 
 * @author Joshua O'Madadhain
 * @author Adam Perer
 */
public interface Distance {
	/**
	 * Returns the distance from the <code>source</code> vertex to the <code>target</code> vertex. If
	 * <code>target</code> is not reachable from <code>source</code>, returns null.
	 */
	public abstract Number getDistance(Node source, Node target);

	/**
	 * <p>
	 * Returns a <code>Map</code> which maps each vertex in the graph (including the <code>source</code> vertex) to its
	 * distance (represented as a Number) from <code>source</code>. If any vertex is not reachable from
	 * <code>source</code>, no distance is stored for that vertex.
	 */
	public abstract Map<Node, Double> getDistanceMap(Node source);
}
