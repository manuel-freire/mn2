/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.importance;

import prefuse.data.Node;

/**
 * A data container for a node ranking.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.NodeRanking) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class NodeRanking extends Ranking {

	/**
	 * Allows the values to be set on construction.
	 * 
	 * @param originalPos
	 *            The original (0-indexed) position of the instance being ranked
	 * @param rankScore
	 *            The actual rank score (normally between 0 and 1)
	 * @param vertex
	 *            The vertex being ranked
	 */
	public NodeRanking(int originalPos, double rankScore, Node vertex) {
		super(originalPos, rankScore);
		this.vertex = vertex;
	}

	/**
	 * The vertex being ranked
	 */
	public Node vertex;
}
