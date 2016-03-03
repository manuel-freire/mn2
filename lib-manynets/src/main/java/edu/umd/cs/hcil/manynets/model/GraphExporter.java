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

package edu.umd.cs.hcil.manynets.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.CSVTableWriter;

/**
 *
 * @author Manuel Freire
 */
public class GraphExporter {

    public enum Format {
        CSV(new ECSV()),
        GEXF(new EGExf()),
        Guess(new EGuess()),
        Pajek(new EPajek());

        private EFormat ef;
        Format(EFormat ef) {
            this.ef = ef;
        }
        public EFormat getFormat() {
            return ef;
        }
    }

    public static abstract class EFormat {

        private String error;
        protected File currentFile;
        protected int currentNet;
        protected ArrayList<FileOutputStream> open
                = new ArrayList<FileOutputStream>();

        public boolean export(ArrayList<PGraph> pgs, File outputDir) {
            try {
                currentNet = 0;
                currentFile = null;
                internalExport(pgs, outputDir);
                System.err.println("Exported " + pgs.size() + " graphs OK");
                return true;
            } catch (Exception e) {
                error = "While writing to " + currentFile.getAbsolutePath() 
                        + " during processing of net " 
                        + currentNet + " of " +  pgs.size()
                        + ": " + e.getMessage();
                System.err.println("error writing to file " + error);
                e.printStackTrace();
                return false;
            } finally {
                for (FileOutputStream fos : open) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {}
                }
            }
        }

        public String getError() {
            return error;
        }

        protected abstract void internalExport(
                ArrayList<PGraph> pgs, File outputDir) throws Exception;
    }

    private static class EGExf extends EFormat {

        private static String header =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<gexf xmlns=\"http://www.gexf.net/1.1draft\" " +
                "xmlns:viz=\"http://www.gexf.net/1.1draft/viz\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.gexf.net/1.1draft " +
                    "http://gexf.net/1.1draft.xsd\" version=\"1.1\">\n";
        private static String metaTemplate =
            "<meta lastmodifieddate=\"__date__\">\n" +   // YYYY-MM-DD
            "   <creator>ManyNets2</creator>\n" +
            "   <description>__name__</description>\n" + // name of net
            "</meta>\n" +
            "<graph defaultedgetype=\"__edge_type__\">"; // directed or undirected
        private static String footer =
            "</graph>\n" +
            "</gexf>\n";
//    <graph defaultedgetype="directed">
//        <attributes class="node">
//            <attribute id="0" title="url" type="string"/>
//            <attribute id="1" title="indegree" type="float"/>
//            <attribute id="2" title="frog" type="boolean">
//                <default>true</default>
//            </attribute>
//        </attributes>
//        <nodes>
//            <node id="a" label="glossy">
//                <viz:color r="239" g="173" b="66"/>
//                <viz:position x="15.783598" y="40.109245" z="0.0"/>
//                <viz:size value="2.0375757"/>
//                <viz:shape value="disc"/>
//                <attvalues>
//                    <attvalue for="0" value="http://gephi.org"/>
//                    <attvalue for="1" value="1"/>
//                </attvalues>
//            </node>
//        </nodes>
//        <edges>
//            <edge id="0" source="0" target="1"/>
//            <edge id="1" source="0" target="2"/>

        @Override
        protected void internalExport(ArrayList<PGraph> pgs, File outputDir)
                throws Exception {
            for (PGraph pg : pgs) {
                currentNet ++;
                currentFile = new File(outputDir, pg.getName() + ".gexf");
                FileOutputStream fos = new FileOutputStream(currentFile);
                open.add(fos);
                exportGexf(pg, new PrintWriter(fos));
            }
        }
        
        private static String escape(String s) {
            return s.replaceAll("&", "&amp;")
                    .replaceAll("\"", "&quot;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("'", "&apos;");
        }

        /**
         * Outputs the graph in 'gexf' format
         * @throws Exception
         */
        private static void exportGexf(PGraph pg, PrintWriter p) throws Exception {
            Graph g = pg.getGraph();
            int nk = g.getNodeTable().getColumnNumber(g.getNodeKeyField());
            int lk = g.getNodeTable().getColumnNumber(PGraph.labelField);
            int ek = g.getEdgeTable().getColumnNumber(PGraph.idField);
            int sk = g.getEdgeTable().getColumnNumber(g.getEdgeSourceField());
            int tk = g.getEdgeTable().getColumnNumber(g.getEdgeTargetField());

            TreeMap<String, Integer> am = new TreeMap<String, Integer>();

            p.println(header);
            p.println(metaTemplate
                    .replace("__date__", (new SimpleDateFormat("yyyy-MM-dd"))
                        .format(new Date()))
                    .replace("__name__", pg.getName())
                    .replace("__edge_type__", "undirected"));
            populateAttributes(am, "node", g.getNodeTable(), p, nk, lk);
            populateAttributes(am, "edge", g.getEdgeTable(), p, ek, sk, tk);
            p.println("\t<nodes>");
            for (Iterator i = g.nodes(); i.hasNext(); /**/) {
                Node n = (Node) i.next();
                p.println("\t\t<node id=\"" + n.getInt(nk) + "\"" +
                        " label=\"" + escape(n.getString(lk)) + "\">");
                populateFields(am, "node", n, p, nk, lk);
                p.println("\t\t</node>");
            }
            p.println("\t</nodes>");

            p.println("\t<edges>");
            for (Iterator i = g.edges(); i.hasNext(); /**/) {
                Edge e = (Edge) i.next();
                p.println("\t\t<edge id=\"" + e.getInt(ek) + "\"" +
                        " source=\"" + e.getInt(sk) + "\"" +
                        " target=\"" + e.getInt(tk) + "\">");
                populateFields(am, "edge", e, p, ek, sk, tk);
                p.println("\t\t</edge>");
        }
            p.println("\t</edges>");
            p.println(footer);
            p.flush();
        }

        private static void populateAttributes(TreeMap<String, Integer> am, 
                String name, Table t, PrintWriter p, int... avoid) {

            HashSet<String> prev = new HashSet<String>();
            p.println("\t<attributes class=\"" + name + "\">");

            outer: for (int i = 0; i < t.getColumnCount(); i++) {
                for (int a : avoid) if (a == i) continue outer;

                // generate attribute name, avoiding dupes
                String n = t.getColumnName(i).toLowerCase().replaceAll("[^a-z: ]+", "_");
                if (!prev.add(n)) {
                    n += "" + i;
                    prev.add(n);
                }
                // generate type
                Class c = t.getColumnType(i);
                String type;
                if (c.equals(Integer.TYPE)) {
                    type = "integer";
                } else if (c.equals(Double.TYPE) || c.equals(Float.TYPE)) {
                    type = "double";
                } else {
                    type = "string";
                }
                // register for later use
                int id = am.size();
                am.put(name + i, id);
//                System.err.println("added " + name+i + " -> " + id + " mapping");

                p.println("\t\t<attribute id=\"" + id + "\"" +
                        " title=\"" + escape(n) + "\" type=\"" + type + "\"/>");
            }

            p.println("\t</attributes>");
        }

        private static void populateFields(TreeMap<String, Integer> am, String prefix,
                Tuple t, PrintWriter p, int... avoid) {
            
            p.println("\t\t\t<attvalues>");
            outer: for (int i = 0; i < t.getColumnCount(); i++) {
                for (int a : avoid) if (a == i) continue outer;
                Object o = t.get(i);
                if (o == null) continue;
//                System.err.println("looking for " + prefix + i);
                int id = am.get(prefix + i);
                p.println("\t\t\t\t<attvalue for=\"" + id + "\" value=\"" + 
                        escape("" + o) + "\"/>");
            }
            p.println("\t\t\t</attvalues>");
        }
    }

    private static class ECSV extends EFormat {

        @Override
        protected void internalExport(ArrayList<PGraph> pgs, File outputDir) 
                throws Exception {
            CSVTableWriter writer = new CSVTableWriter();
            for (PGraph pg : pgs) {
                currentNet ++;
                currentFile = new File(outputDir, pg.getName() + "-nodes.csv");
                FileOutputStream nos = new FileOutputStream(currentFile);
                open.add(nos);
                writer.writeTable(pg.getNodeTable().getTable(), nos);
                currentFile = new File(outputDir, pg.getName() + "-edges.csv");
                FileOutputStream eos = new FileOutputStream(currentFile);
                open.add(eos);
                writer.writeTable(pg.getEdgeTable().getTable(), eos);
            }
        }
    }

    private static class EGuess extends EFormat {

        @Override
        protected void internalExport(ArrayList<PGraph> pgs, File outputDir)
                throws Exception {
            for (PGraph pg : pgs) {
                currentNet ++;
                currentFile = new File(outputDir, pg.getName() + ".gdf");
                FileOutputStream fos = new FileOutputStream(currentFile);
                open.add(fos);
                exportGdf(pg.getGraph(), new PrintWriter(fos));
            }
        }

        /**
         * Outputs the graph in 'guess' format
         * http://guess.wikispot.org/The_GUESS_.gdf_format
         * @param g
         * @param p
         * @throws Exception
         */
        private static void exportGdf(Graph g, PrintWriter p) throws Exception {
            StringBuilder sb = new StringBuilder();
            int nk = g.getNodeTable().getColumnNumber(g.getNodeKeyField());
            int sk = g.getEdgeTable().getColumnNumber(g.getEdgeSourceField());
            int tk = g.getEdgeTable().getColumnNumber(g.getEdgeTargetField());

            populateGdfHeaders(g.getNodeTable(), sb, nk, -1);
            p.println("nodedef> name" + sb.toString());
            for (Iterator i = g.nodes(); i.hasNext(); /**/) {
                Node n = (Node) i.next();
                sb.setLength(0);
                populateGdfFields(n, sb, nk, -1);
                p.println("v" + n.getInt(nk) + sb);
            }
            sb.setLength(0);
            populateGdfHeaders(g.getEdgeTable(), sb, sk, tk);
            p.println("edgedef> node1,node2" + sb.toString());
            for (Iterator i = g.edges(); i.hasNext(); /**/) {
                Edge e = (Edge) i.next();
                sb.setLength(0);
                populateGdfFields(e, sb, sk, tk);
                p.println("v" + e.getInt(sk) + ","
                        + "v" + e.getInt(tk) + sb);
            }
            p.flush();
        }

        private static void populateGdfHeaders(Table t, StringBuilder sb, int avoid1, int avoid2) {
            HashSet<String> prev = new HashSet<String>();
            for (int i = 0; i < t.getColumnCount(); i++) {
                if (i == avoid1 || i == avoid2) {
                    continue;
                }
                String s = t.getColumnName(i).toLowerCase().replaceAll("[^a-z]+", "_");
                // avoid several columns with the same name, confuses Guess
                if (!prev.add(s)) {
                    s += "" + i;
                    prev.add(s);
                }
                sb.append("," + s);
                Class c = t.getColumnType(i);
                if (c.equals(Integer.TYPE)) {
                    sb.append(" INT default 0");
                } else if (c.equals(Double.TYPE)) {
                    sb.append(" FLOAT default 0");
                } else {
                    sb.append(" VARCHAR(128)");
                }
            }
        }

        private static void populateGdfFields(Tuple t, StringBuilder sb, int avoid1, int avoid2) {
            for (int i = 0; i < t.getColumnCount(); i++) {
                if (i == avoid1 || i == avoid2) {
                    continue;
                }
                Object o = t.get(i);
                sb.append("," + ((o == null) ? ""
                        : ((o instanceof String) ? "\""
                        + o.toString().replaceAll("'", "`")
                        + "\"" : o)));
            }
        }
    }

    private static class EPajek extends EFormat {

        @Override
        protected void internalExport(ArrayList<PGraph> pgs, File outputDir)
                throws Exception {
            for (PGraph pg : pgs) {
                currentNet ++;
                currentFile = new File(outputDir, pg.getName() + ".net");
                FileOutputStream fos = new FileOutputStream(currentFile);
                open.add(fos);
                exportPajek(pg.getGraph(), new PrintWriter(fos));
            }
        }

        /**
         * Outputs the graph in 'pajek' format
         * http://vw.indiana.edu/tutorials/pajek/
         * @param g
         * @param p
         * @throws Exception
         */
        private static void exportPajek(Graph g, PrintWriter p) throws Exception {
            int nk = g.getNodeTable().getColumnNumber(g.getNodeKeyField());
            int ck = g.getNodeTable().getColumnNumber("Source");
            int sk = g.getEdgeTable().getColumnNumber(g.getEdgeSourceField());
            int tk = g.getEdgeTable().getColumnNumber(g.getEdgeTargetField());

            // color by source type, if sources available
            String[] colors = new String[] {
                "Red","Blue","Green","Cyan","Magenta","Gray","Brown"
            };
            int currentColor = 0;

            TreeMap<Integer,Integer> m = new TreeMap<Integer,Integer>();
            TreeMap<String,String> cm = new TreeMap<String,String>();
            for (Iterator i = g.nodes(); i.hasNext(); /**/) {
                Node n = (Node) i.next();
                m.put(n.getInt(nk), m.size() + 1);
                if (tk != -1 && ! cm.containsKey(n.getString(ck))) {
                    cm.put(n.getString(ck), colors[currentColor++]);
                }
            }

            p.println("*Vertices " + g.getNodeCount());
            for (Iterator i = g.nodes(); i.hasNext(); /**/) {
                Node n = (Node) i.next();
                p.println("" + m.get(n.getInt(nk))
                        + " \"" + n.getString("Label") + "\""
                        + (ck != -1 ? " ic " + cm.get(n.getString(ck)) : ""));
            }

            p.println("*Edges");
            for (Iterator i = g.edges(); i.hasNext(); /**/) {
                Edge e = (Edge) i.next();
                p.println("" + m.get(e.getInt(sk)) + " "
                        + "" + m.get(e.getInt(tk)));
            }
            p.flush();
        }
    }
}
