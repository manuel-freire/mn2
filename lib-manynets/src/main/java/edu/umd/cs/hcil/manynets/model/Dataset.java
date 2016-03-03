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

import edu.umd.cs.hcil.manynets.model.DistributionFactory.LookupValueMapper;
import edu.umd.cs.hcil.manynets.model.Schema.Entity;
import edu.umd.cs.hcil.manynets.model.Schema.Relationship;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.TableFile;
import edu.umd.cs.hcil.socialaction.io.HCILFormatTableReader;
import edu.umd.cs.hcil.manynets.hist.DefaultHistogramModel;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.io.AbstractTableReader;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.TableReader;
import prefuse.util.collections.IntIterator;

/**
 * A configuration file for a ManyNets dataset. Example:

<?xml version="1.0" encoding="UTF-8"?>
<config name="MovieLens and FilmTrust" date="" author="manuel.freire@gmail.com">
  <description>
    MovieLens and FilmTrust datasets.
  </description>
  <schema>
    <entity id="films" idcol="ID" labelcol="TITLE"/>
    <entity id="users" idcol="ID" labelcol="ID"/>
    <relationship id="user-film" idcol="ID"
       sourceid="users" sourcecol="ID1"
       targetid="films" targetcol="ID1"
       directed="true"
    />
  </schema>
  <instance name="MovieLens" format="tsv">
    <table type="entity" ref="films" file="ml-movies.tsv"/>
    <table type="entity" ref="users" file="ml-users.tsv"/>
    <table type="relationship" ref="user-film" file="ml-um-movies.tsv"/>
  </instance>
  <instance name="FilmTrust" format="tsv">
    <table type="entity" ref="films" file="ft-movies.tsv"/>
    <table type="entity" ref="users" file="ft-users.tsv"/>
    <table type="relationship" ref="user-film" file="ft-pm-edges.tsv"/>
  </instance>
</config>

 * @author Manuel Freire
 */
public class Dataset {

    private String name;
    private String date;
    private String author;
    private String description;
    
    private Schema schema;
    private ArrayList<SchemaInstance> instances;

    // list of postprocessors, typically used to parse date columns
    static PostProcessor pps[] = new PostProcessor[] {
        new DefaultDateFixer(),
        new Vast08DateFixer(),
        new MovieLensDateFixer1(),
        new MovieLensDateFixer2(),
        new DistributionFixer()
    };

    /**
     * Find names of all valid instances in a given dataset; useful to select
     * names of instances to load
     */
    public static ArrayList<String> readInstanceNames(File f) throws IOException {
        FileInputStream fis = null;
        ArrayList<String> names = new ArrayList<String>();
        try {
            SAXBuilder builder = new SAXBuilder();
            fis = new FileInputStream(f);
            Document doc = builder.build(fis);
            Element root = doc.getRootElement();
            for (Element c : (List<Element>)root.getChildren("instance")) {
                names.add(c.getAttributeValue("name"));
            }
        } catch (Exception e) {
            throw new IOException("Error loading dataset from '"
                    + f.getAbsolutePath() + "' xml save file", e);
        } finally {
            try { if (fis != null) fis.close(); } catch (Exception e2) {}
        }
        return names;
    }

    /**
     * Loads a dataset description (and the tables themselves) from an xml file
     * @param f
     * @throws IOException
     */
    public void load(File f, DatasetListener listener) throws IOException {
        load(f, null, listener);
    }

    /**
     * Loads a dataset description (and the tables themselves) from an xml file
     * @param f
     * @param instanceNames names of instances to load; if null, load all.
     * @param listener to notify of progress during load (called eg. once per table)
     * @throws IOException
     */
    public void load(File f, Set<String> instanceNames, DatasetListener listener) throws IOException {
        
        FileInputStream fis = null;

        try {
            SAXBuilder builder = new SAXBuilder();
            fis = new FileInputStream(f);
            Document doc = builder.build(fis);
            Element root = doc.getRootElement();
            
            load(root, f.getParentFile(), instanceNames, listener);

        } catch (Exception e) {
            throw new IOException("Error loading dataset from '"
                    + f.getAbsolutePath() + "' xml save file", e);
        } finally {
            try { if (fis != null) fis.close(); } catch (Exception e2) {}
        }
    }

    /**
     * Saves the contents of this dataset to an XML file
     * @param f the file
     * @throws java.io.IOException
     */
    public void save(File f, String name, String author, String description,
            DatasetListener listener) throws IOException {

        Element root = new Element("configuration");

        // save the schema; the files are assumed to be locally present
        save(root, name, author, description);

        FileOutputStream fos = null;
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            fos = new FileOutputStream(f);
            outputter.output(new Document(root), fos);
        }
        finally {
            // ignore closing exceptions
            try { if (fos != null) fos.close(); } catch (Exception e2) {}
        }
    }

    /**
     * Listener to implement to listen to load progress
     */
    public interface DatasetListener {
        /** Called when schema read */
        public void notifySchemaOk();
        /** Called when instance-count known */
        public void notifyInstanceCount(int n);
        /** Called when size of instance known */
        public void notifyInstanceStart(String in, int nt);
        /** Called when instance table has been read OK */
        public void notifyInstanceProgress(String in, String en, int ni);
    }

    /**
     * A chatty progress listener
     */
    public static class StderrDatasetListener implements DatasetListener {
        public void notifySchemaOk() {
            System.err.println("Schema read ok");
        }

        public void notifyInstanceCount(int n) {
            System.err.println("Instances = " + n);
        }

        public void notifyInstanceStart(String in, int nt) {
            System.err.println("Instance " + in + " has " + nt + " tables");
        }

        public void notifyInstanceProgress(String in, String en, int ni) {
            System.err.println("Instance " + in + " table " + en + " (" + ni + ")");
        }
    }

    /**
     * Load everything from the specified XML doc
     * @param e
     */
    public void load(Element e, File baseDir, Set<String> instanceNames,
            DatasetListener listener) throws IOException {
        name = e.getAttributeValue("name");
        date = e.getAttributeValue("date");
        author = e.getAttributeValue("author");
        description = e.getChildText("description");

        schema = new Schema();
        schema.load(e.getChild("schema"));
        if (listener != null) {
            listener.notifySchemaOk();
            listener.notifyInstanceCount(e.getChildren("instance").size());
        }

        instances = new ArrayList<SchemaInstance>();
        for (Element c : (List<Element>)e.getChildren("instance")) {
            String iName = c.getAttributeValue("name");
            if (instanceNames != null && ! instanceNames.contains(iName)) {
                continue;
            }

            if (listener != null) {
                listener.notifyInstanceStart(iName, 
                        c.getChildren("table").size());
            }
            int ni = 0;
            HashMap<String, TableFile> tfs = new HashMap<String, TableFile>();
            for (Element tc : (List<Element>)c.getChildren("table")) {
                String ref = tc.getAttributeValue("ref");
                String type = tc.getAttributeValue("type");
                File f = new File(baseDir, tc.getAttributeValue("file"));
                System.err.println("... about to read " + f.getAbsolutePath());
                File iconFile = new File(baseDir, ref + ".png");
                iconFile = (iconFile.isFile() ? iconFile : null);
                TableFile tf = null;
                Table loaded = "entity".equals(type.toLowerCase()) ?
                        loadEntityTable(f, schema.getEnt(ref)) :
                        loadRelationshipTable(f, schema.getRel(ref));
                tf = new TableFile(loaded, f, iconFile);
                tfs.put(ref, tf);
                if (listener != null) {
                    listener.notifyInstanceProgress(iName, ref, ++ni);
                }
            }
            instances.add(new SchemaInstance(iName, schema, tfs));
        }
    }


    private static void appendCell(StringBuilder sb, String s, int targetWidth) {
        if (s.length() > targetWidth) {
            sb.append(s.substring(0, targetWidth));
        } else {
            int padding = targetWidth - s.length();
            for (int i=0; i<padding; i++) sb.append(' ');
            sb.append(s);
        }
    }

    public static String dumpTable(Table t, int nRows) {
        StringBuilder sb = new StringBuilder();
        int cellWidth = 16;
        for (int i=0; i<t.getColumnCount(); i++) {
            appendCell(sb, "'" + t.getColumnName(i) + "'", cellWidth);
            if (i+1==t.getColumnCount()) sb.append("\n");
        }
        for (int i=0; i<t.getColumnCount(); i++) {
            appendCell(sb, t.getColumnType(i).getSimpleName(), cellWidth);
            if (i+1==t.getColumnCount()) sb.append("\n");
        }
        IntIterator ii = t.rows();
        for (int j=0; j<nRows && j<t.getRowCount(); j++) {
            int r = ii.nextInt();
            for (int i=0; i<t.getColumnCount(); i++) {
                appendCell(sb, ""+t.get(r, i), cellWidth);
                if (i+1==t.getColumnCount()) sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static Table loadEntityTable(File f, Entity e) throws IOException {
        Table t = loadTable(f);
        if ( ! t.canGetInt(e.getIdField())) {
            dumpTable(t, 5);
            throw new IllegalArgumentException("table for entity '"
                    + e.getId() + "' has bad ID col type:\n" + dumpTable(t, 5));
        }
        if (t.getColumnNumber(e.getLabelField()) < 0) {
            throw new IllegalArgumentException("table for entity '"
                    + e.getId() + "' has bad label col:\n" + dumpTable(t, 5));
        }
        return t;
    }

    public static Table loadRelationshipTable(File f, Relationship r) throws IOException {
        Table t = loadTable(f);
        if ( ! t.canGetInt(r.getSourceField())) {
            throw new IllegalArgumentException("table for rel. '"
                    + r.getId() + "' has bad source col: '"
                    +  r.getSourceField() + "' (" + t.getColumnNumber(r.getSourceField()) + ") "
                    +  "\n" + dumpTable(t, 5));
        }
        if ( ! t.canGetInt(r.getTargetField())) {
            throw new IllegalArgumentException("table for rel. '"
                    + r.getId() + "' has bad target col: '"
                    +  r.getTargetField() + "' (" + t.getColumnNumber(r.getTargetField()) + ") "
                    + "\n" + dumpTable(t, 5));
        }
        return t;
    }

    /**
     * Internal class used for postprocessing data columns
     */
    private interface PostProcessor {
        public boolean canProcess(Class cc, String colName);
        public Object process(Column c, int i) throws Exception;
        public Class getType();
        public String getColName(String colName);
    }

    private static class DefaultDateFixer implements PostProcessor {
        public boolean canProcess(Class cc, String colName) {
            return cc.equals(String.class) && colName.contains("_DATE");
        }
        // eg.: "9/24/2009 0:00"
        private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        public Object process(Column c, int i) throws Exception {
            return sdf.parse(c.getString(i));
        }
        public Class getType() { return Date.class; }
        public String getColName(String colName) { return colName; }
    }

    private static class Vast08DateFixer implements PostProcessor {
        public boolean canProcess(Class cc, String colName) {
            return cc.isArray() && colName.equals("Timestamp");
        }
        private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm");
        private static DecimalFormat df = new DecimalFormat("0000");
        public Object process(Column c, int i) throws Exception {
            int[] d = (int[])c.get(i);
            return sdf.parse(d[0] + " " + df.format(d[1]));
        }
        public Class getType() { return Date.class; }
        public String getColName(String colName) { return colName; }
    }

    private static class MovieLensDateFixer1 implements PostProcessor {
        public boolean canProcess(Class cc, String colName) {
            return cc.equals(Integer.TYPE) && colName.equals("TIMESTAMP");
        }
        public Object process(Column c, int i) throws Exception {
            return new Date(1000 * (long)c.getInt(i));
        }
        public Class getType() { return Date.class; }
        public String getColName(String colName) { return colName; }
    }

    private static class MovieLensDateFixer2 implements PostProcessor {
        public boolean canProcess(Class cc, String colName) {
            return cc.equals(String.class) && ( colName.equals("RELEASE") );
        }
        private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        public Object process(Column c, int i) throws Exception {
            return sdf.parse(c.getString(i));
        }
        public Class getType() { return Date.class; }
        public String getColName(String colName) { return colName; }
    }

    private static class DistributionFixer implements PostProcessor {
        private String colName;
        public boolean canProcess(Class cc, String colName) {
            this.colName = colName;
//            System.err.println("colname= " +colName + " ;");
            if ((cc.equals(int[].class) || cc.equals(float[].class))
                    && colName.matches(".*[(].*[|].*[)]")) {
//                System.err.println(" -matches- ");
                return true;
            }
            return false;
        }
        public String getColName(String colName) { 
            return colName.replaceAll("[(].*", "").trim();
        }
        public Object process(Column c, int i) throws Exception {
            DefaultHistogramModel dhm = new DefaultHistogramModel();
            String cs[]=null;
            // Age (13-17|18-25|26-35|36-50|51-65|65-100)
            String col = colName;
            if (col.matches(".*[(].*[|].*[)]")) {
//                System.err.println("matches");
                col = col.substring(col.indexOf('(') + 1);
                col = col.replace(")", "");
//                System.err.println(" sub= " + col);
                cs = col.split("\\|");
//                for (int j = 0; j < cs.length; j++) {
//                    System.err.println(cs[j]);
//                }
            }

            if (c.getColumnType().equals(int[].class)) {
                int[] v = (int[])c.get(i);
                for (int j=0; j<cs.length; j++) {
    //                System.err.print(j + " add " + v[j]);
                    dhm.addValues(j, null, v[j]);
    //                System.err.println(" as value");
                }
            } else if (c.getColumnType().equals(float[].class)) {
                float[] v = (float[])c.get(i);
                for (int j=0; j<cs.length; j++) {
    //                System.err.print(j + " add " + v[j]);
                    dhm.addValues(j, null, Math.max(1, (int)(v[j]*1000)));
    //                System.err.println(" as value");
                }
            } else {
                throw new IllegalArgumentException("cannot parse " + c.getColumnType());
            }

            LookupValueMapper vm = new LookupValueMapper(cs, String.class);
            dhm.setValueMapper(vm);
            return dhm;
        }
        public Class getType() { return Distribution.class; }
    }
    /**
     * This will be called once for each table loaded. It can be used for
     * dataset-specific hackery, eg, converting datatypes.
     * @param t
     * @throws Exception
     */
    public static void postProcessTable(Table t, String tableName) throws Exception {

        for (int i=0; i<t.getColumnCount(); i++) {
            Column c = t.getColumn(i);
            String colName = t.getColumnName(i);
            for (PostProcessor pp : pps) {       
//                System.err.println("Evaluating " + pp.getClass().getSimpleName() + " on col " + colName);
                if (pp.canProcess(c.getColumnType(), colName)) {
                    System.err.println("Triggering postprocessor on col " + colName);
                    Object[] o = new Object[c.getRowCount()];
                    boolean firstError = true;
                    for (int j=0; j<c.getRowCount(); j++) {
                        try {
                            o[j] = pp.process(c, j);
                        } catch (Exception e) {
                            System.err.println("Error postprocessing col '"
                                + colName + " as "
                                + c.get(j) + "' as "
                                + pp.getType().getSimpleName() + ", in data row "
                                + i + " from " + tableName);
                            if (firstError) {
                                e.printStackTrace();
                                System.err.println("Final diagnosis is " + e);
                                firstError = false;
                            }
                        }
                    }
                    t.removeColumn(c);
                    String newName = pp.getColName(colName);
                    t.addColumn(newName, pp.getType());
                    Column nc = t.getColumn(newName);
                    for (int j=0; j<nc.getRowCount(); j++) {
                        if (o[j] != null) nc.set(o[j], j);
                    }
                    // max 1 postprocessor per column; remove&add may
                    i--;
                    break;
                }
            }            
        }
    }        
   
    public static Table loadTable(File f) throws IOException {
        AbstractTableReader reader = null;
        try {
            reader = f.getName().matches(".*\\.csv") ?
                new CSVTableReader() : 
                new HCILFormatTableReader();
            Table t = reader.readTable(f);
            postProcessTable(t, f.getName());
            return t;
        } catch (Exception e) {
            throw new IOException("Loading table from file " + f.getName(), e);
        } finally {
            // dang and blast it, no way to close Prefuse's open descriptors here...
        }
    }

    /**
     * Save everything to the specified XML doc
     * @param e
     */
    public void save(Element e, String name, String author, String description) {

        Element c = new Element("description");
        c.addContent(description);
        e.addContent(c);

        c = new Element("schema");
        schema.save(c);
        e.addContent(c);

        for (SchemaInstance si : instances) {
            c = new Element("instance");
            e.addContent(c);
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<SchemaInstance> getInstances() {
        return instances;
    }

    public Schema getSchema() {
        return schema;
    }
}
