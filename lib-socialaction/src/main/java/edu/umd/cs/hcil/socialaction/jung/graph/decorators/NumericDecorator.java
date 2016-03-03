/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.graph.decorators;

import prefuse.data.Tuple;

/**
 * Decorator for any value type that extends the java.lang.Number class
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.graph.decorators.NumericDecorator) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public class NumericDecorator extends Decorator {

	/**
	 * Constructs and initializes the decorator
	 * 
	 * @param key
	 * @param copyAction
	 */
	public NumericDecorator(String key) {
		super(key);
	}

	/**
	 * Retrieves the decorated value for the given graph/vertex/edge as an integer
	 * 
	 * @param udc
	 *            the graph/vertex/edge
	 * @return the integer value
	 */
	public int intValue(Tuple udc) {
		return udc.getInt(getKey());
	}

	/**
	 * Returns the decorated value as Object
	 * 
	 * @param udc
	 *            the graph/vertex/edge
	 * @return the value
	 */
	public Number getValue(Tuple udc) {
		return (Number) udc.get(getKey());
	}

	/**
	 * Sets the value for a given graph/vertex/edge
	 * 
	 * @param value
	 *            the value to be stored
	 * @param udc
	 *            the graph/vertex/edge being decorated
	 */
	public void setValue(double value, Tuple udc) {
		udc.set(getKey(), value);

	}
}
