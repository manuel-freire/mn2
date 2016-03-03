/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.algorithms.importance;

import prefuse.data.Edge;

/**
 * A data container for an edge ranking which stores:
 * <ul>
 * <li>the rank score</li>
 * <li>the original position of the edge before the ranking were generated</li>
 * <li>a reference to the edge itself</li>
 * </ul>
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.algorithms.importance.EdgeRanking) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class EdgeRanking extends Ranking {
	/**
	 * The edge that was ranked.
	 */
	public Edge edge;

	/**
	 * Simple constructor that allows all data elements to be initialized on construction.
	 * 
	 * @param originalPos
	 *            the original position of the edge before the ranking were generated
	 * @param rankScore
	 *            the rank score of this edge
	 * @param edge
	 *            a reference to the edge that was ranked
	 */
	public EdgeRanking(int originalPos, double rankScore, Edge edge) {
		super(originalPos, rankScore);
		this.edge = edge;
	}
}
