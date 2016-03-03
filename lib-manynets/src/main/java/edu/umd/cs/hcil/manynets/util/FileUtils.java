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

package edu.umd.cs.hcil.manynets.util;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Miscelaneous utilities 
 *
 * @author mfreire
 */
public class FileUtils {
    
    public static String DEFAULT_FILE_ENCODING = "UTF-8";    
    
    /**
     * Copies a file from source to destination. Uses then 'nio' package
     * for great justice (well, actually only compactness and speed).
     */
    public static void copy(File source, File destination) throws IOException {
        FileInputStream in = new FileInputStream(source);
        FileOutputStream out= new FileOutputStream(destination);
        byte[] buffer = new byte[1024*16];
        int len;
        try {
            while ((len = in.read(buffer)) != -1) 
                out.write(buffer, 0, len);
        }
        finally {
            in.close();
            out.close();
        }        
    }    

    /**
     * Deletes a file or directory recursively. Returns 'true' if ok, or
     * 'false' on error.
     */
    public static boolean delete(File file) {
        // Recursive call
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i=0; i < files.length; i++) {
                if (! delete(files[i])) return false;
            }
        }
        // File deletion
        if (! file.delete()) {
            System.err.println("Imposible to delete file "+file.getAbsolutePath());
            return false;
        }
        return true;
    }     
    
    /**
     * Clears a directory of all its files. Will fail miserably
     * if any of them are actually directories
     */
    public static void clearFiles(File dir) throws IOException {
        File[] files = dir.listFiles();        
        for (int i=0; i<files.length; i++) files[i].delete();
    }
    
    /**
     * Counts size of a file or directory recursively. Returns total number
     * of bytes in files
     */
    public static int getDiskUsage(File file) {
        long usage = 0;
        
        // Recursive call
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i=0; i < files.length; i++) {
                usage += getDiskUsage(files[i]);
            }
        }
        // Local size
        usage += file.length();
        
        return (int)usage;
    }     

    /**
     * User 'parse', then call getText()
     * from http://stackoverflow.com/questions/240546/removing-html-from-a-java-string
     */
    public static class HtmlToText extends HTMLEditorKit.ParserCallback {
         StringBuilder s;

         public void parse(Reader in) throws IOException {
           s = new StringBuilder();
           ParserDelegator delegator = new ParserDelegator();
           // the third parameter is TRUE to ignore charset directive
           delegator.parse(in, this, Boolean.TRUE);
         }

         @Override
         public void handleText(char[] text, int pos) {
           s.append(text);
         }

         public String getText() {
           return s.toString();
         }
    }

    // small static list of accepted formats
    private static ArchiveFormat[] formats = new ArchiveFormat[] { 
                new ZipFormat()
    };
    
    /**
     * Returns 'true' if the given file can be uncompressed (may fail, because
     * it is based on matching the extension against a known list...)
     */    
    public static boolean canUncompress(File f) {
        return f.exists() && f.isFile() && getArchiverFor(f.getName()) != null;            
    }
    
    /**
     * Returns the ArchiveFormat to use for a given extension
     */
    public static ArchiveFormat getArchiverFor(String name) {
        if (name != null && name.lastIndexOf('.') != -1) {
            name = name.toLowerCase();
            name = name.substring(name.lastIndexOf('.'), name.length());
            for (ArchiveFormat af : formats) {
                if (name.matches(af.getArchiveExtensions())) return af;
            }
        }
        return null;
    }
       
    /**
     * Canonicalizes a path, transforming windows '\' to unix '/', and 
     * stripping off any './' or '../' occurrences, and trimming 
     * start and end whitespace
     */
    public static String toCanonicalPath(String name) {
        name = name.replaceAll("\\\\", "/").trim();
        name = name.replaceAll("(\\.)+/", "");
        return name;
    }
    
    /**
     * Lists all files in a given location (recursive)
     */
    public static ArrayList<File> listFiles(File dir) {
        ArrayList<File> al = new ArrayList<File>();
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                al.addAll(listFiles(f));
            }
            else {
                al.add(f);
            }
        }
        return al;
    }

    /**
     * Reads an http URL to a string, specifying an encoding
     */
    public static String readURLToString(String url, String encoding) throws IOException {
        char buffer[] = new char[1024];
        StringBuilder sb = new StringBuilder();
        InputStream is = (new URL(url)).openStream();
        BufferedReader br = new BufferedReader(
              new InputStreamReader(is, encoding));
        while (br.read(buffer, 0, buffer.length) > 0) {     
            sb.append(buffer);
        }
        br.close();
        return sb.toString();
    }

    /**
     * Reads a file to a string, specifying an encoding
     */
    public static String readFileToString(File f, String encoding) throws IOException {
        char buffer[] = new char[(int)(f.length() * 1.5)];
        FileInputStream fis = new FileInputStream(f);
        BufferedReader br = new BufferedReader(
              new InputStreamReader(fis, encoding));
        br.read(buffer, 0, (int)f.length());        
        br.close();
        return new String(buffer, 0, (int)f.length());
    }  

    /**
     * Reads a file to a string, automagically choosing the correct encoding
     * (internally, it will be converted to UTF-8, which is what Java uses)
     *
     * ONLY USE WHEN ENCODING IS UNKNOWN
     */
    public static String readFileToString(File f) throws IOException {
        String s1 = readFileToString(f, "UTF-8");
        String s2 = readFileToString(f, "ISO-8859-1");
        int badChars1 = 0;
        int badChars2 = 0;
        for (int i=0; i<s1.length(); i++) {
            if (s1.charAt(i) == '?') badChars1 ++;                
            if (s2.charAt(i) == '?') badChars2 ++;
        }
        return (badChars1 < badChars2) ? s1 : s2;
    }      
    
    /**
     * Writes a string to a file
     */
    public static void writeStringToFile(File f, String s) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        BufferedWriter bw = new BufferedWriter(
            new OutputStreamWriter(fos, DEFAULT_FILE_ENCODING));
        bw.write(s);
        bw.close();
    }
    
    /**
     * Converts a full path into a relative one
     */
    public static String relativePath(String fullPath) {
        int pathPos = 0;
        pathPos = Math.max(pathPos, fullPath.lastIndexOf('=')+1);  // a form
        pathPos = Math.max(pathPos, fullPath.lastIndexOf('/')+1);  // *nix
        pathPos = Math.max(pathPos, fullPath.lastIndexOf('\\')+1); // windows
        return fullPath.substring(pathPos);
    }        
    
    /**
     * Create a file from a 'relative' file URL 
     */
    public static File getFileFromUrl(String url, File base) {
        
        // check to see if absolute (no '.' as first char) - and ignore base if so
        try {
            if ( ! url.startsWith("file://.")) {
                java.net.URI fileURI = new java.net.URI(url);
                return new File(fileURI);
            }
        }
        catch (java.net.URISyntaxException urise) {
            throw new IllegalArgumentException("Bad URL, cannot process "+url);
        }
        
        // return a file using the base
        return new File(base, url.substring("file://.".length()));
    }
        
    /**
     * Ask the user to provide a file or directory
     */
    public static File[] chooseFile(Component p, File defaultDir, String message,
            boolean toOpen, int fileType, boolean multiple) {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle(message);
        if (defaultDir != null) {
            jfc.setCurrentDirectory(defaultDir);
        }
        jfc.setFileSelectionMode(fileType);
        jfc.setMultiSelectionEnabled(multiple);
        File[] fs = null;
        while (fs == null) {
            int rc = (toOpen ? jfc.showOpenDialog(p) : jfc.showSaveDialog(p));
            if (rc == JFileChooser.CANCEL_OPTION) {
                fs = null;
                break;
            }
            fs = multiple ? 
                jfc.getSelectedFiles() :
                new File[] { jfc.getSelectedFile() };
            if (fs == null ||  ( ! fs[0].exists() && toOpen ) ||
                    (fileType == JFileChooser.FILES_ONLY && fs[0].isDirectory())) {
                JOptionPane.showMessageDialog(null, 
                        "Error: not a valid file or directory",
                        "Error", JOptionPane.ERROR_MESSAGE);
                fs = null;
                continue;
            }            
        }        
        return fs;
    }

    /**
     * Asks the user to provide a regex for file-loading purposes
     */
    public static String getFileRegexp(File d) {
        StringBuilder sb = new StringBuilder();
        for (File f : d.listFiles()) {
            if (f.isFile()) {
                sb.append("<li>" + f.getName() + "</li>");
            }
        }
        return JOptionPane.showInputDialog(null, "<html>" +
                "Files in selected folder: <br><ul>" + sb.toString() +
                "</ul><br>Choose regexp (or leave blank for none):</html>");
    }
    
    /**
     * Check the magic in a file - or in a buffer from that file
     */
    public static boolean startMatches(File f, int[] magic) throws IOException {
        FileInputStream in = null;
        try {
            in =  new FileInputStream(f);
            return startMatches(in, magic);
        }
        finally {        
            try { if (in != null) in.close(); } catch(Exception e) {};
        }
    }    
    
    public static boolean startMatches(InputStream is, int[] magic) throws IOException {
        for (int i=0; i<magic.length; i++) {
            int r = is.read();
            if (r == -1 || r != magic[i]) return false;                
        }
        return true;
    }
    
    /**
     * Utility method to calculate compressed sizes. Many compression algorithms define 
     * "output streams" which make this very easy to implement. The output stream should
     * wrap the "bos".
     */
    static int compressedSize(InputStream is, OutputStream os, ByteArrayOutputStream bos) 
        throws IOException {
        
        // dump file into output, counting the number of total output bytes
        int n;
        byte [] bytes = new byte [1024];
        while ((n = is.read(bytes)) > -1) {
            os.write(bytes, 0, n);
        }
        is.close();         
        os.close();
        return bos.size();    
    }    
}
