package edu.umd.cs.hcil.socialaction.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import prefuse.Constants;
import prefuse.data.Edge;
import prefuse.data.tuple.TupleSet;
import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class SelectedEdgeRenderer extends EdgeRenderer {

	SocialAction m_app;

	int[] grayScaleMap;

	private boolean m_renderArrows = false;
	private boolean m_renderConnectorMarker = false;
	private Ellipse2D m_connectorMarkerShape = new Ellipse2D.Double();

	public SelectedEdgeRenderer(SocialAction app) {
		m_app = app;

		grayScaleMap = ColorLib.getGrayscalePalette(125);
		setArrowRendered(m_renderArrows);
		setConnectorMarkerRendered(m_renderConnectorMarker);

		updateConnectorMarker(8, 8);

	}

	protected AffineTransform m_connectorMarkerTrans = new AffineTransform();
	protected Shape m_curConnectorMarker;
	protected Shape m_curConnectorMarkerSource;

	protected void updateConnectorMarker(int w, int h) {
		m_connectorMarkerShape.setFrame(-w / 2, -w / 2, w, w);
	}

	public boolean isArrowRendered() {
		return m_renderArrows;
	}

	public void setArrowRendered(boolean value) {
		m_renderArrows = value;
		if (value == true)
			m_edgeArrow = Constants.EDGE_ARROW_FORWARD;
		else
			m_edgeArrow = Constants.EDGE_ARROW_NONE;

	}

	public boolean isConnectorMarkerRendered() {
		return m_renderConnectorMarker;
	}

	public void setConnectorMarkerRendered(boolean value) {
		m_renderConnectorMarker = value;

	}

	/**
	 * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
	 */
	protected Shape getRawShape(VisualItem item) {
		EdgeItem edge = (EdgeItem) item;

		VisualItem item1 = edge.getSourceItem();
		VisualItem item2 = edge.getTargetItem();

		int type = m_edgeType;

		getAlignedPoint(m_tmpPoints[0], item1.getBounds(), m_xAlign1, m_yAlign1);
		getAlignedPoint(m_tmpPoints[1], item2.getBounds(), m_xAlign2, m_yAlign2);
		m_curWidth = (float) (m_width * getLineWidth(item));

		// create the arrow head, if needed
		EdgeItem e = (EdgeItem) item;
		if (e.isDirected() && m_edgeArrow != Constants.EDGE_ARROW_NONE) {
			// get starting and ending edge endpoints
			boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
			Point2D start = null, end = null;
			start = m_tmpPoints[forward ? 0 : 1];
			end = m_tmpPoints[forward ? 1 : 0];

			// compute the intersection with the target bounding box
			VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
			int i = GraphicsLib.intersectLineRectangle(start, end, dest.getBounds(), m_isctPoints);
			if (i > 0)
				end = m_isctPoints[0];

			// create the arrow head shape
			AffineTransform at = getArrowTrans(start, end, m_curWidth);
			m_curArrow = at.createTransformedShape(m_arrowHead);

			// update the endpoints for the edge shape
			// need to bias this by arrow head size
			Point2D lineEnd = m_tmpPoints[forward ? 1 : 0];
			lineEnd.setLocation(0, -m_arrowHeight);
			at.transform(lineEnd, lineEnd);
		} else {
			m_curArrow = null;
		}

		// create the connector marker, if needed

		if (m_renderConnectorMarker) {
			// get starting and ending edge endpoints
			Point2D start = null, end = null;
			start = m_tmpPoints[0];
			end = m_tmpPoints[1];

			// compute the intersection with the target bounding box
			VisualItem dest = e.getTargetItem();
			int i = GraphicsLib.intersectLineRectangle(start, end, dest.getBounds(), m_isctPoints);
			if (i > 0)
				end = m_isctPoints[0];

			// create the arrow head shape
			AffineTransform at = getArrowTrans(start, end, m_curWidth);
			m_curConnectorMarker = at.createTransformedShape(m_connectorMarkerShape);

			start = m_tmpPoints[1];
			end = m_tmpPoints[0];

			// compute the intersection with the target bounding box
			dest = e.getSourceItem();
			i = GraphicsLib.intersectLineRectangle(start, end, dest.getBounds(), m_isctPoints);
			if (i > 0)
				end = m_isctPoints[0];

			// create the arrow head shape
			at = getArrowTrans(start, end, m_curWidth);
			m_curConnectorMarkerSource = at.createTransformedShape(m_connectorMarkerShape);

			// update the endpoints for the edge shape
			// need to bias this by arrow head size
			// Point2D lineEnd = m_tmpPoints[1];
			// lineEnd.setLocation(0, -m_arrowHeight);
			// at.transform(lineEnd, lineEnd);
		} else {
			m_curConnectorMarker = null;
			m_curConnectorMarkerSource = null;
		}

		// create the edge shape
		Shape shape = null;
		double n1x = m_tmpPoints[0].getX();
		double n1y = m_tmpPoints[0].getY();
		double n2x = m_tmpPoints[1].getX();
		double n2y = m_tmpPoints[1].getY();
		switch (type) {
		case Constants.EDGE_TYPE_LINE:
			m_line.setLine(n1x, n1y, n2x, n2y);
			shape = m_line;
			break;
		case Constants.EDGE_TYPE_CURVE:
			getCurveControlPoints(edge, m_ctrlPoints, n1x, n1y, n2x, n2y);
			m_cubic.setCurve(n1x, n1y, m_ctrlPoints[0].getX(), m_ctrlPoints[0].getY(), m_ctrlPoints[1].getX(),
					m_ctrlPoints[1].getY(), n2x, n2y);
			shape = m_cubic;
			break;
		default:
			throw new IllegalStateException("Unknown edge type");
		}

		// return the edge shape
		return shape;
	}

	public void render(Graphics2D g, VisualItem item) {

		TupleSet sliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_EDGE_SLIDER_GROUP);
		// TupleSet nodeSliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_NODE_SLIDER_GROUP);

		// System.out.println(sliderSet.getTupleCount());

		// System.out.println(item.ge);

		boolean as = sliderSet.containsTuple(item.getSourceTuple()); // item.isInGroup(SocialAction.RANGE_SLIDER_GROUP);

		if (as) {

			Edge e = (Edge) item.getSourceTuple();
			// // System.out.println(e.getTargetNode() + " " + nodeSliderSet.getTupleCount());
			//
			// boolean source = nodeSliderSet.containsTuple(e.getSourceNode());
			// boolean target = nodeSliderSet.containsTuple(e.getTargetNode());

			// if (source && target) {
			if (as) {

				if (false) {
					if (e.canGetInt("weight")) {
						int tmp = e.getInt("weight");

						int logtmp = (int) Math.log(tmp);

						if (tmp > 280)
							tmp = 400;
						tmp -= 190;
						if (tmp < 0)
							tmp = 0;

						Color color = ColorLib.getColor(0, 0, 0, tmp);

						item.setStrokeColor(ColorLib.color(color));
						// item.setStrokeColor(grayScaleMap[tmp]);
					}
				} 
				if (!m_renderArrows) {
					m_curArrow = null;
				}
				// render the edge line
				super.render(g, item);

				// render the connector marker, if appropriate
				if (m_curConnectorMarker != null) {
					g.setPaint(ColorLib.getColor(item.getFillColor()));
					g.fill(m_curConnectorMarker);
					g.fill(m_curConnectorMarkerSource);
				}

				// if (m_renderArrows) {
				// // render the edge arrow head, if appropriate
				// if (m_curArrow != null) {
				// g.setPaint(ColorLib.getColor(item.getFillColor()));
				// g.fill(m_curArrow);
				// }
				// }

			}
		}
	}

}
