/*
 * Created on Nov 13, 2005
 */
package edu.umd.cs.hcil.socialaction.action.linlog;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.data.Node;

/**
 * Basic data type for the small world graph visualization. Useful for
 * visualizing dendrograms.
 * 
 * Modified from SmallWorld
 * (http://www.cs.ubc.ca/~sfingram/cs533C/small_world.html)
 * (ca.ubc.cs.smallworld.types.Cluster) to work with Prefuse Beta
 * 
 * @author Stephen Frowe Ingram
 * @author Adam Perer
 */
public interface Cluster extends Node {

	public Point2D getCenter();

	public void setCenter(Point2D center);

	public void setBounds(Rectangle2D bounds);

	public float getRadius();

	public float getDistance();

	public Iterator getChildren();

	public int getChildrenCount();

	public int getHeight();

	public Cluster getParent();

	public void setParent(Cluster parent);

	public Rectangle2D getBounds();

	public boolean hasChildren();

	public boolean isRoot();

	public void setRoot(boolean root);
}
