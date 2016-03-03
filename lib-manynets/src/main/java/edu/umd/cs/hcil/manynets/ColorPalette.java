/*
 *  This file is part of ManyNets.
 *
 *  ManyNets is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  ManyNets is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with ManyNets.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  ManyNets was created at the Human Computer Interaction Lab, 
 *  University of Maryland at College Park. See the README file for details
 */

package edu.umd.cs.hcil.manynets;

import java.awt.Color;
import java.util.TreeMap;
import prefuse.util.ColorLib;

/**
 * Color schemes from http://colorbrewer2.org, using hex values.
 * Note - this would have been somewhat cleaner using Prefuse's ColorLib directly
 *
 * @author awalin sopan
 */
public class ColorPalette {

    private Color[] colors;

    private static TreeMap<String, ColorPalette> palettes
            = new TreeMap<String, ColorPalette>();
    
    private static void initPalettes() {
        // color scheme: 10 categorical colors
        palettes.put("categorical", new ColorPalette( new Color[]{
                new Color(0xA6CEE3),new Color(0xB2DF8A),new Color(0x1F78B4),
                new Color(0x33A02C),new Color(0xFB9A99),new Color(0xE31A1C),
                new Color(0xFDBF6F),new Color(0xCAB2D6),new Color(0xFF7F00),
                new Color(0x6A3D9A)}));

        // color scheme: 5 colors, 2 hues, Purple and Blue
        palettes.put("all", new ColorPalette(new Color[] {
            new Color(0xF1EEF6), new Color(0xBDC9E1),
            new Color(0x74A9CF), new Color(0x2B8CBE) ,
            new Color(0x045A8D) }));

        // single hue, Red, 3 colors
        palettes.put("red", new ColorPalette(new Color[] {
            new Color(0xFB6A4A), new Color(0xDE2D26), new Color(0xA50F15)}));

        // single hue, Blue, 3 colors
        palettes.put("blue", new ColorPalette(new Color[] {
            new Color(0x6BAED6), new Color(0x3182BD), new Color(0x08519C)}));

        // single hue, Green, 4 colors
        palettes.put("green", new ColorPalette(new Color[] {
            new Color(0xB2E2E2), new Color(0x66C2A4), new Color(0x2CA25F),
            new Color(0x006D2C)}));

        // yellow-to-green ColorBrewer2 sequential
        palettes.put("yellow-green", new ColorPalette(
                new ColorPalette(new Color(0xffffcc), new Color(0xc2e699), 84),
                new ColorPalette(new Color(0xc2e699), new Color(0x78c679), 84),
                new ColorPalette(new Color(0x78c679), new Color(0x238443), 84)));


        // orange-red ColorBrewer2 sequential
        palettes.put("orange-red", new ColorPalette(
                new ColorPalette(new Color(0xfef0d9), new Color(0xfdcc8a), 84),
                new ColorPalette(new Color(0xfdcc8a), new Color(0xfc8d59), 84),
                new ColorPalette(new Color(0xfc8d59), new Color(0xd7301f), 84)));

        // white-red (not colorbrewer-derived)
        palettes.put("white-red",
                new ColorPalette(Color.WHITE, new Color(0xA50F15), 256));

        // white-blue (not colorbrewer-derived)
        palettes.put("white-blue", new ColorPalette(
                new ColorPalette(new Color(0xffffff), new Color(0x6BAED6), 84),
                new ColorPalette(new Color(0x6BAED6), new Color(0x3182BD), 84),
                new ColorPalette(new Color(0x3182BD), new Color(0x08519C), 84)));

        // white-blue (not colorbrewer-derived)
        palettes.put("white-red2", new ColorPalette(
                new ColorPalette(new Color(0xffffff), new Color(0xFB6A4A), 80),
                new ColorPalette(new Color(0xFB6A4A), new Color(0xDE2D26), 60),
                new ColorPalette(new Color(0xDE2D26), new Color(0xA50F15), 40)));

        // white-blue (not colorbrewer-derived)
        palettes.put("white-blue2", new ColorPalette(
                new ColorPalette(new Color(0xffffff), new Color(0x6BAED6), 80),
                new ColorPalette(new Color(0x6BAED6), new Color(0x3182BD), 60),
                new ColorPalette(new Color(0x3182BD), new Color(0x08519C), 40)));

        // black-green (not colorbrewer-derived)
        palettes.put("black-green",
                new ColorPalette(Color.BLACK, Color.GREEN, 256));

        // red-black-green scheme (not colorbrewer-derived)
        palettes.put("red-black-green", new ColorPalette(
                new ColorPalette(Color.RED, Color.BLACK, 128),
                new ColorPalette(Color.BLACK, Color.GREEN, 128)));

        // red-black-green scheme (not colorbrewer-derived)
        palettes.put("blue-white-orange", new ColorPalette(
                new ColorPalette(Color.BLUE, Color.WHITE, 128),
                new ColorPalette(Color.WHITE, Color.ORANGE, 128)));
    }

    private ColorPalette(Color[] colors) {
        this.colors = colors;
    }

    /**
     * Interpolated palette from first to second color
     */
    private ColorPalette(Color first, Color second, int steps) {
        int[] intColors = ColorLib.getInterpolatedPalette(
                steps, ColorLib.color(first), ColorLib.color(second));
        this.colors = new Color[steps];
        for (int i=0; i<steps; i++) {
            colors[i] = ColorLib.getColor(intColors[i]);
        }
    }

    /**
     * Multiple palettes one after another; supresses duplicated colors
     * @param subpalettes
     */
    private ColorPalette(ColorPalette ... subpalettes) {
        int total=0;
        int dupes=0;
        Color lastColor = null;
        for (ColorPalette cp : subpalettes) {
            total += cp.colors.length;
            if (lastColor != null && lastColor.equals(cp.colors[0])) {
                dupes ++;
            }
            lastColor = cp.colors[cp.colors.length - 1];
        }
        this.colors = new Color[total-dupes];
        int pos = 0;
        lastColor = null;
        for (ColorPalette cp : subpalettes) {
            if (lastColor != null && lastColor.equals(cp.colors[0])) {
                dupes --;
                pos --;
            }
            for (int i=0; i<cp.colors.length; i++) {
                colors[pos++] = cp.colors[i];
            }
            lastColor = cp.colors[cp.colors.length - 1];
        }
        assert(dupes == 0);
    }

    public static ColorPalette getPalette(String name) {
        if (palettes.isEmpty()) {
            initPalettes();
        }
        return palettes.get(name);
    }

    public Color mapCategorical(int n) {
       return colors[n % colors.length];
    }

    public Color map(float value) {
       float x = Math.min(value * colors.length, colors.length - 1);
       return colors[Math.round(x)];
    }

    public Color map(float max, float min, float value) {
       float x = (value - min) * colors.length /( max- min);
       // avoid value == max leading to off-by-one values
       x = Math.min(x, colors.length - 1);
       return colors[Math.round(x)];
    }
}
