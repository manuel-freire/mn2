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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * A very simple XML-based configuration system.
 *
 * @author Manuel Freire
 */
public class Config {

    private File file;
    private Element root;
    private Document document;

    public Config(String xmlFileName) {
        file = new File(xmlFileName);
        reload();
    }

    public Element reload() {
        try {
            if ( ! file.exists() || ! file.canRead()) {
                System.err.println("Did not find config file, generated new one");
                root = new Element("Config");
                document = new Document(root);
                save();
            }
            System.err.println("Loading config file from " + file.getAbsolutePath());
            SAXBuilder builder = new SAXBuilder();
            document = builder.build(file);
            root = document.getRootElement();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading from " + file.getAbsolutePath()
                    + ": " + e);
        }
        return null;
    }

    public File getFile(String elementName) {
        if (root.getChild(elementName) != null) {
            File f = new File(root.getChildText(elementName));
            if (f.exists()) {
                return f;
            }
        }
        return new File(".");
    }

    public void setFile(String elementName, File f) {
        if (f.getParentFile().equals(new File("."))) {
            f = new File(f.getName());
        }
        if (root.getChild(elementName) == null) {
            root.addContent(new Element(elementName));
        }
        root.getChild(elementName).setText(f.getPath());
        save();
    }

    public String getString(String elementName) {
        if (root.getChild(elementName) != null) {
            return root.getChildText(elementName);
        }
        return null;
    }

    public void setString(String s, String elementName) {
        if (root.getChild(elementName) == null) {
            root.addContent(new Element(elementName));
        }
        root.getChild(elementName).setText(s);
        save();
    }

    /**
     * Save the config to the predefined file
     */
    public void save() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
        root.setAttribute("timestamp", sdf.format(new Date()));

        // TODO: save other attributes & elements

        FileOutputStream fos = null;
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            fos = new FileOutputStream(file);
            outputter.output(document, fos);
        } catch (Exception e) {
            System.err.println("Error saving config to " + file.getAbsolutePath()
                    + ": " + e);
            e.printStackTrace();
        } finally {
            // ignore closing exceptions
            try { if (fos != null) fos.close(); } catch (Exception e2) {}
        }
    }

    public static File[] splitFile(File f) {
        ArrayList<File> al = new ArrayList<File>();
        for (File p = f; p != null; p = p.getParentFile()) {
            al.add(p);
        }
        Collections.reverse(al);
        return al.toArray(new File[al.size()]);
    }

    public static void main(String args[]) {
        File f = new File("filmtrust").getAbsoluteFile();
        try {
            File[] base = splitFile(new File(".").getCanonicalFile());
            File[] path = splitFile(f.getCanonicalFile());
            System.err.println(path + "\n" + base);
            int ok=0;
            for (ok=0; base[ok].equals(path[ok]); ok++) { /**/ }
            // same ancestors: can substitute for '.'

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
