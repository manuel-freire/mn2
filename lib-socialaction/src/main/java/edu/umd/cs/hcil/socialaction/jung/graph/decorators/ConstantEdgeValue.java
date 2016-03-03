/*
 * Copyright (c) 2004, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.graph.decorators;

import prefuse.data.Edge;

/**
 * Returns a constructor-specified constant value for each edge.
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.graph.decorators.ConstantEdgeValue) to work with Prefuse.
 * 
 * @author Joshua O'Madadhain
 * @author Adam Perer
 */
public class ConstantEdgeValue implements NumberEdgeValue {
	protected Number value;

	public ConstantEdgeValue(double value) {
		this.value = new Double(value);
	}

	public ConstantEdgeValue(Number value) {
		this.value = value;
	}

	/**
	 * @see edu.uci.ics.jung.graph.decorators.NumberEdgeValue#getNumber(edu.uci.ics.jung.graph.ArchetypeEdge)
	 */
	public Number getNumber(Edge arg0) {
		return value;
	}

	/**
	 * @see edu.uci.ics.jung.graph.decorators.NumberEdgeValue#setNumber(edu.uci.ics.jung.graph.ArchetypeEdge,
	 *      java.lang.Number)
	 */
	public void setNumber(Edge arg0, Number arg1) {
		throw new UnsupportedOperationException();
	}

}
