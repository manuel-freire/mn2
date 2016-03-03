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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * An ArchiveFormat encapsulates a data compression algorithm and/or a file
 * archival format. Data compression means that data that is fed into the
 * ArchiveFormat is reduced in size; file archival means that the format
 * understands how to arhive and expand a series of files into/from a single
 * file.
 *
 * Zip is both; Gzip is only a data compression algorithm; Tar is only an archiver.
 * Not all archive formats are required to implement all operations.
 *
 * @author mfreire
 */
public interface ArchiveFormat {
    
    /**
     * Returns the pattern that describes the extensions that this archiver
     * can process
     */
    public String getArchiveExtensions();
    
    /**
     * List the contents of the archive
     */
    public ArrayList<String>list(File source) throws IOException;
    
    /**
     * Expand the archive into its component files
     */
    public void expand(File source, File destDir) throws IOException;
    
    /**
     * Expand the archive into its component files
     */
    public boolean extractOne(File source, String path, File dest) throws IOException;
    
    /**
     * Return the *size* (in bytes) of compressing the input stream with this algorithm
     * This allows the archiver to be used in compression-distance calculations, without the need
     * to create intermediate files
     */
    public int compressedSize(InputStream is) throws IOException;
    
    /**
     * Create an archive from the given sources; files in the archive are
     * relative to baseDir.
     */
    public void create(ArrayList<File> sources, File destFile, File baseDir) throws IOException;    
}
