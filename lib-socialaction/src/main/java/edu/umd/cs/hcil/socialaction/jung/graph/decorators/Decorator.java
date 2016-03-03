/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.umd.cs.hcil.socialaction.jung.graph.decorators;

import prefuse.data.Tuple;

/**
 * Abstract decorator for cases where attributes are to be stored along with the graph/edge/vertex which they describe
 * 
 * Modified from JUNG v1.7.4 (edu.uci.ics.jung.graph.decorators.Decorator) to work with Prefuse.
 * 
 * @author Scott White
 * @author Adam Perer
 */
public abstract class Decorator {
	private String mKey;

	/**
	 * Constructs and initializes the decorator
	 * 
	 * @param key
	 * @param action
	 */
	protected Decorator(String key) {
		mKey = key;

	}

	/**
	 * Retrieves the user datum key that this decorator uses when setting new values
	 */
	public String getKey() {
		return mKey;
	}

	/**
	 * Removes the values from the user data container
	 * 
	 * @param udc
	 *            the vertex/edge/graph being whose value is being removed
	 */
	public void removeValue(Tuple udc) {
		udc.set(mKey, null);
		// udc.removeUserDatum(mKey);
	}
}
