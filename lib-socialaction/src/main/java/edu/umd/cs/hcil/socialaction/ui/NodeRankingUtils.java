package edu.umd.cs.hcil.socialaction.ui;

import prefuse.data.Graph;
import prefuse.data.Table;

public class NodeRankingUtils {

	public static Table addBetweennessColumn(Table t, Graph g) {

		t.addColumn("Betweenness", double.class);

		return t;

	}
	// static methods, e.g. Add Betweenness Column, etc.
	// NodeRankingUtils.addBetweennessColumn(Table t)
}
