package edu.umd.cs.hcil.socialaction.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import prefuse.render.PolygonRenderer;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;

public class AggregateRenderer extends PolygonRenderer {

	public AggregateRenderer(int polyType) {
		super(polyType);
	}

	protected void drawShape(Graphics2D g, VisualItem item, Shape shape) {

		g.fill(shape);
		g.draw(shape);

		Rectangle2D r = shape.getBounds2D();
		Font m_font = new Font(SocialAction.DEFAULT_FONTNAME, Font.PLAIN, 100);
		double size = r.getHeight() - (r.getHeight() * 0.2);// item.getSize();

		if (size != 1)
			m_font = FontLib.getFont(m_font.getName(), m_font.getStyle(), size);

		g.setFont(m_font);
		FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
		double y = r.getY() + (r.getHeight() - fm.getHeight()) / 2 + fm.getAscent();

		// This code controls the text displayed inside the community bubbles
		String st = item.getString(SocialAction.LABEL_COLUMN_NAME);
		if (st == null)
			st = "";

		// int m_horizBorder = 3;
		double x = r.getMinX() + (r.getWidth() - fm.charsWidth(st.toCharArray(), 0, st.length())) / 2;
		// r.getMinX() + size*m_horizBorder;

		GraphicsLib.paint(g, item, shape, getStroke(item), getRenderType(item));
		// The color of the community bubble text
		g.setPaint(new Color(255, 255, 255, 80));
		g.drawString(st, (float) x, (float) y);

	}

}
