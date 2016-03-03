package edu.umd.cs.hcil.socialaction.render;

import java.awt.Color;
import java.awt.Paint;

import prefuse.util.ColorLib;
import prefuse.util.ColorMap;

public class SocialColorMap extends ColorMap {

	public SocialColorMap(int[] map, double min, double max) {
		super(map, min, max);
	} //

	public static int[] getInterpolatedMap(int size, Color c1, Color c2, Color c3) {
		// Paint[] cm = new Paint[size];
		int[] cmInt = new int[size];

		int halfsize = size / 2;

		for (int i = 0; i < halfsize; i++) {
			float f = ((float) i) / (halfsize - 1);
			Color color = getIntermediateColor(c1, c2, f);
			cmInt[i] = ColorLib.color(color);
		}
		for (int i = halfsize; i < size; i++) {
			float f = ((float) i) / (size - 1);
			Color color = getIntermediateColor(c2, c3, f);
			cmInt[i] = ColorLib.color(color);
		}

		return cmInt;
	}

	public static Color getIntermediateColor(Color c1, Color c2, double frac) {
		return ColorLib.getColor((int) Math.round(frac * c2.getRed() + (1 - frac) * c1.getRed()), (int) Math.round(frac
				* c2.getGreen() + (1 - frac) * c1.getGreen()), (int) Math.round(frac * c2.getBlue() + (1 - frac)
				* c1.getBlue()), (int) Math.round(frac * c2.getAlpha() + (1 - frac) * c1.getAlpha()));
	}

	/**
	 * Returns a color map array of default size that ranges from black to white through shades of gray.
	 * 
	 * @return the color map array
	 */
	public static final int DEFAULT_MAP_SIZE = 64;

	public static Paint[] getReverseGrayscaleMap() {
		return getReverseGrayscaleMap(DEFAULT_MAP_SIZE);
	} //

	/**
	 * /** Returns a color map array of specified size that ranges from black to white through shades of gray.
	 * 
	 * @param size
	 *            the size of the color map array
	 * @return the color map array
	 */
	public static Paint[] getReverseGrayscaleMap(int size) {
		Paint[] cm = new Paint[size];
		for (int i = 0; i < size; i++) {
			float g = ((float) i) / (size - 1);
			cm[size - 1 - i] = ColorLib.getColor(g, g, g, 1.f);
		}
		return cm;
	} //

	public static Paint[] getReverseAlphaMap(int size) {
		Paint[] cm = new Paint[size];
		for (int i = 0; i < size; i++) {
			// float g = ((float) i) / (size - 1);
			cm[size - 1 - i] = ColorLib.getColor(0.f, 0.f, 0.f, 1.0f);

			// System.out.println((size-1-i) + " alpha: " + g );
		}

		cm[0] = ColorLib.getColor(0.f, 0.f, 0.f, 1.0f);
		cm[1] = ColorLib.getColor(0.f, 0.f, 0.f, 1.0f);
		cm[2] = ColorLib.getColor(0.f, 0.f, 0.f, 0.5f);
		cm[3] = ColorLib.getColor(0.f, 0.f, 0.f, 0.25f);
		cm[4] = ColorLib.getColor(0.f, 0.f, 0.f, 0f);

		// cm[i] = ColorLib.getColor(0.f,0.f,0.f,0.075f);

		return cm;
	} //

}
