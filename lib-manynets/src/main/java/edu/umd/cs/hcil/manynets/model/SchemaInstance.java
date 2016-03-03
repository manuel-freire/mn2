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

import edu.umd.cs.hcil.manynets.model.Schema.Entity;
import edu.umd.cs.hcil.manynets.model.Schema.Relationship;
import edu.umd.cs.hcil.manynets.model.TableWrapper.Level;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.ImageIcon;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 * An instance of a Schema, with all its tables. Can be used as a lightweight
 * database (in the sense of being VERY feature-light). Contains references to the
 * actual files (if any)
 *
 * @author Manuel Freire
 */
public class SchemaInstance {

    // the name of this instance
    private String name;
    // the schema being used for this instance
    private Schema schema;
    // tables of entities; their names correspond to schema entity ids
    private ArrayList<EntityTableWrapper> entities = new ArrayList<EntityTableWrapper>();
    // tables of relationships; their names correspond to schema rel. ids
    private ArrayList<RelTableWrapper> relationships = new ArrayList<RelTableWrapper>();
    // correspondence between entities and schema objects (entities or relationships)
    private HashMap<String, TableWrapper> mapping = new HashMap<String, TableWrapper>();
    // paths to the files; if 'null', the file has not been written
    private HashMap<String, File> files = new HashMap<String, File>();

    public static class TableFile {
        private Table t;
        private File f;
        private File iconFile;
        public TableFile(Table t, File f, File iconFile) {
            this.t = t; this.f = f; this.iconFile = iconFile;
        }
        public Table getTable() { return t; }
        public File getFile() { return f; }
        public File getIconFile() { return iconFile; }
    }

    /**
     * Builds a SchemaInstance from a schema and all the required tables
     * @param schema
     * @param tables, the tableFiles
     * for that table.
     */
    public SchemaInstance(String name, Schema schema, HashMap<String, TableFile> tables) {
        this.name = name;
        this.schema = schema;

        for (Entity e : schema.getEntities().values()) {
            TableFile tf = tables.get(e.getId());
            if (tf != null) {
                EntityTableWrapper w = new EntityTableWrapper(this, e, tf.getTable(), true);
                if (tf.getIconFile() != null) {
                    try {
                        w.setIcon(new ImageIcon(tf.getIconFile().toURI().toURL()));
                    } catch (MalformedURLException ex) {
                        System.err.println("failed icon load " + tf.getIconFile());
                    }
                }
                entities.add(w);
                mapping.put(e.getId(), w);
                files.put(e.getId(), tables.get(e.getId()).getFile());
            }
        }
        for (Relationship r : schema.getRelationships().values()) {
            Table t = tables.get(r.getId()).getTable();
            RelTableWrapper w = new RelTableWrapper(this, r, t, true);
            TableFile tf = tables.get(r.getId());
            if (tf.getIconFile() != null) {
                try {
                    w.setIcon(new ImageIcon(tf.getIconFile().toURI().toURL()));
                } catch (MalformedURLException ex) {
                    System.err.println("failed icon load " + tf.getIconFile());
                }
            }
            relationships.add(w);
            mapping.put(r.getId(), w);
            files.put(r.getId(), tables.get(r.getId()).getFile());
        }

        // second pass, to fill in any empty tables
        for (Entity e : schema.getEntities().values()) {
            TableFile tf = tables.get(e.getId());
            if (tf == null) {
                HashSet ids = new HashSet();
                Table nt = new Table();
                nt.addColumn(e.getIdField(), Integer.TYPE);
                if ( ! e.getIdField().equals(e.getLabelField())) {
                    nt.addColumn(e.getLabelField(), String.class);
                }
                // collect all referenced IDs, and add them to the table
                for (Relationship r : e.getIncoming()) {
                    Iterator tt = mapping.get(r.getId()).getTable().tuples();
                    while (tt.hasNext()) {
                        Tuple tuple = (Tuple)tt.next();
                        int id = tuple.getInt(r.getTargetField());
                        if ( ! ids.contains(id)) {
                            int row = nt.addRow();
                            nt.set(row, e.getIdField(), id);
                            // may be the same field as above, but it will not hurt
                            try {
                                nt.set(row, e.getLabelField(), id);
                            } catch (Exception e2) {
                                throw new IllegalArgumentException(
                                "error setting " + e.getLabelField() + " on table " +
                                        r.getId() + " to " + id, e2);
                            }
                            ids.add(id);
                        }
                    }
                }
                for (Relationship r : e.getOutgoing()) {
                    Iterator tt = mapping.get(r.getId()).getTable().tuples();
                    while (tt.hasNext()) {
                        Tuple tuple = (Tuple)tt.next();
                        int id = tuple.getInt(r.getSourceField());
                        if ( ! ids.contains(id)) {
                            int row = nt.addRow();
                            nt.set(row, e.getIdField(), id);
                            // may be the same field as above, but it will not hurt
                            nt.set(row, e.getLabelField(), id);
                            ids.add(id);
                        }
                    }
                }
                EntityTableWrapper w = new EntityTableWrapper(this, e, nt, true);
                entities.add(w);
                mapping.put(e.getId(), w);
            }
        }
        crossReference();
    }

    public void crossReference() {
        for (RelTableWrapper w : relationships) {
            Relationship rel = w.relationship;
            w.source = (EntityTableWrapper)mapping.get(rel.getSource().getId());
            w.getStat(w.getTable().getColumnNumber(w.getSourceField()))
                    .setIcon(w.source.getIcon());
            w.target = (EntityTableWrapper)mapping.get(rel.getTarget().getId());
            w.getStat(w.getTable().getColumnNumber(w.getTargetField()))
                    .setIcon(w.target.getIcon());
        }
    }
    
    public HashMap<String, File> getFiles() {
        return files;
    }

    public HashMap<String, TableWrapper> getMapping() {
        return mapping;
    }

    public String getName() {
        return name;
    }

    public Schema getSchema() {
        return schema;
    }

    public ArrayList<EntityTableWrapper> getEntities() {
        return entities;
    }

    public ArrayList<RelTableWrapper> getRelationships() {
        return relationships;
    }

    /**
     * Describes a node table.
     */
    public static class EntityTableWrapper extends TableWrapper {
        private Schema.Entity entity;
        private SchemaInstance si;

        /**
         * copy from parent, should be subclasses for type-safety
         */
        @Override
        public EntityTableWrapper copy() {
            return new EntityTableWrapper(si, this);
        }

        public EntityTableWrapper(SchemaInstance si, EntityTableWrapper parent) {
            super(parent);
            this.si = si;
            this.entity = parent.entity;
        }
        public EntityTableWrapper(SchemaInstance si, Schema.Entity entity, Table t, boolean detached) {
            super(entity.getId(), t, detached, entity.getIdField(), Level.Entity);
            this.si = si;
            this.entity = entity;
        }
        public Schema.Entity getEntity() {
            return entity;
        }
        public String getLabelField() {
            return entity.getLabelField();
        }   
        @Override
        public Ref createRef(int id) {
            return new Ref<EntityTableWrapper>(null, this, id);
        }
        public SchemaInstance getSchemaInstance() {
            return si;
        }
    }

    /**
     * Describes an edge table. Source and target are defined only after the
     * referring SchemaInstance has been "crossReferenced".
     */
    public static class RelTableWrapper extends TableWrapper {
        private Schema.Relationship relationship;
        private EntityTableWrapper source;
        private EntityTableWrapper target;
        private SchemaInstance si;

        public RelTableWrapper(SchemaInstance si, RelTableWrapper parent) {
            super(parent);
            this.si = si;
            this.relationship = parent.relationship;
            this.source = parent.source;
            this.target = parent.source;
        }

        /**
         * copy from parent, should be subclasses for type-safety
         */
        @Override
        public RelTableWrapper copy() {
            return new RelTableWrapper(si, this);
        }


        public RelTableWrapper(SchemaInstance si, Schema.Relationship relationship,
                Table t, boolean detached) {
            super(relationship.getId(), t,
                    detached, relationship.getIdField(), Level.Relationship);
            this.si = si;
            this.relationship = relationship;
        }

        @Override
        public String getColumnName(int columnIndex) {
            String name = super.getColumnName(columnIndex);
            if (columnIndex == getTable().getColumnNumber(getSourceField())) {
                name += " (@" + source.getName() + ")";
            } else if (columnIndex == getTable().getColumnNumber(getTargetField())) {
                name += " (@" + target.getName() + ")";
            }
            return name;
        }

        @Override
        public Ref createRef(int id) {
            return new Ref<RelTableWrapper>(null, this, id);
        }

        public Schema.Relationship getRelationship() {
            return relationship;
        }

        public String getSourceField() {
            return relationship.getSourceField();
        }
        public String getTargetField() {
            return relationship.getTargetField();
        }
        public EntityTableWrapper getSourceTW() {
            return source;
        }
        public EntityTableWrapper getTargetTW() {
            return target;
        }
        public boolean isDirected() {
            return relationship.isDirected();
        }
        public SchemaInstance getSchemaInstance() {
            return si;
        }
    }
}
