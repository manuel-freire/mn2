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

import edu.umd.cs.hcil.manynets.model.SchemaInstance.EntityTableWrapper;
import edu.umd.cs.hcil.manynets.model.SchemaInstance.RelTableWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.jdom.Element;
import prefuse.data.Graph;
import prefuse.data.Table;

/**
 * A schema. Defines relationships between a set of entities and relationships,
 * which can then be instanced in particular tables (used as part of pgraphs)
 * Can be loaded and saved to-from an XML element (used as part of datasets).
 * Allows simple queries.
 * @author Manuel Freire
 */
public class Schema {

    private HashMap<String, Entity> entities =
            new HashMap<String, Entity>();
    private HashMap<String, Relationship> relationships =
            new HashMap<String, Relationship>();

    private static Schema defaultSchemaDirected;
    private static Schema defaultSchemaUndirected;

    public static Schema getDefaultSchema(boolean directed) {
        Schema s = null;
        if (directed) {
            if (defaultSchemaDirected == null) {
                s = defaultSchemaDirected = new Schema();
                s.addEntity("nodes", "ID", "ID");
                s.addRelationship("edges", "ID", "nodes", "ID1", "nodes", "ID2", true);
            } else {
                s = defaultSchemaDirected;
            }
        } else {
            if (defaultSchemaUndirected == null) {
                s = defaultSchemaUndirected = new Schema();
                s.addEntity("nodes", "ID", "ID");
                s.addRelationship("edges", "ID", "nodes", "ID1", "nodes", "ID2", false);
            } else {
                s = defaultSchemaUndirected;
            }
        }
        return s;
    }

    public Entity getEnt(String id) {
        return entities.get(id);
    }

    public Relationship getRel(String id) {
        return relationships.get(id);
    }

    public Graph toBimodalGraph(PGraph pg) {
        Table nodes = new Table();
        nodes.addColumn(PGraph.idField, Integer.TYPE);
        nodes.addColumn("Entity", Entity.class);
        nodes.addColumn("Label", String.class);
        nodes.addColumn("Type", String.class);
        Table edges = new Table();
        edges.addColumn(PGraph.sourceField, Integer.TYPE);
        edges.addColumn(PGraph.targetField, Integer.TYPE);
        edges.addColumn("Relationship", Relationship.class);
        edges.addColumn("Label", String.class);
        TreeMap<String, Integer> ei = new TreeMap<String, Integer>();

        System.err.println(pg.getNodeTables().size());
        for (Entity e : getEntities().values()) {
            boolean found = false;
            for (EntityTableWrapper etw : pg.getNodeTables()) {
                if (etw.getEntity().equals(e)) {                   
                    found = true;
                    break;
                }
            }
            if ( ! found) {
                continue;
            }

            ei.put(e.id, ei.size());
            int i = nodes.addRow();
            nodes.set(i, 0, ei.get(e.id));
            nodes.set(i, 1, e);
            nodes.set(i, 2, "Entity Table\n" + e.getId());
            nodes.set(i, 3, "Entity");
        }
        int bogusEdgeIds = nodes.getRowCount();
        for (Relationship r : getRelationships().values()) {
            boolean found = false;
            for (RelTableWrapper rtw : pg.getEdgeTables()) {
                if (rtw.getRelationship().equals(r)) found = true;
            }
            if ( ! found) {
                continue;
            }

            int i = edges.addRow();
            edges.set(i, 0, ei.get(r.source.id));
            edges.set(i, 1, bogusEdgeIds);
            edges.set(i, 2, r);
            edges.set(i, 3, r.getId());
            i = nodes.addRow();
            nodes.set(i, 0, bogusEdgeIds);
            nodes.set(i, 2, "Rel. Table\n" + r.getId());
            nodes.set(i, 3, "Relationship");                        
            i = edges.addRow();
            edges.set(i, 0, bogusEdgeIds);
            edges.set(i, 1, ei.get(r.target.id));
            edges.set(i, 2, r);
            edges.set(i, 3, r.getId());
            bogusEdgeIds ++;
        }
        
        return new Graph(nodes, edges, true, PGraph.idField,
                PGraph.sourceField, PGraph.targetField);
    }

    public Graph toBimodalGraph() {
        Table nodes = new Table();
        nodes.addColumn(PGraph.idField, Integer.TYPE);
        nodes.addColumn("Entity", Entity.class);
        nodes.addColumn("Label", String.class);
        nodes.addColumn("Type", String.class);
        Table edges = new Table();
        edges.addColumn(PGraph.sourceField, Integer.TYPE);
        edges.addColumn(PGraph.targetField, Integer.TYPE);
        edges.addColumn("Relationship", Relationship.class);
        edges.addColumn("Label", String.class);
        TreeMap<String, Integer> ei = new TreeMap<String, Integer>();

        for (Entity e : getEntities().values()) {
            ei.put(e.id, ei.size());
            int i = nodes.addRow();
            nodes.set(i, 0, ei.get(e.id));
            nodes.set(i, 1, e);
            nodes.set(i, 2, "Entity Table\n" + e.getId());
            nodes.set(i, 3, "Entity");
        }
        int bogusEdgeIds = nodes.getRowCount();
        for (Relationship r : getRelationships().values()) {
            int i = edges.addRow();
            edges.set(i, 0, ei.get(r.source.id));
            edges.set(i, 1, bogusEdgeIds);
            edges.set(i, 2, r);
            edges.set(i, 3, r.getId());
            i = nodes.addRow();
            nodes.set(i, 0, bogusEdgeIds);
            nodes.set(i, 2, "Rel. Table\n" + r.getId());
            nodes.set(i, 3, "Relationship");
            i = edges.addRow();
            edges.set(i, 0, bogusEdgeIds);
            edges.set(i, 1, ei.get(r.target.id));
            edges.set(i, 2, r);
            edges.set(i, 3, r.getId());
            bogusEdgeIds ++;
        }

        return new Graph(nodes, edges, true, PGraph.idField,
                PGraph.sourceField, PGraph.targetField);
    }

    public Graph toGraph() {
        Table nodes = new Table();
        nodes.addColumn(PGraph.idField, Integer.TYPE);
        nodes.addColumn("Entity", Entity.class);
        nodes.addColumn("Label", String.class);
        Table edges = new Table();
        edges.addColumn(PGraph.sourceField, Integer.TYPE);
        edges.addColumn(PGraph.targetField, Integer.TYPE);
        edges.addColumn("Relationship", Relationship.class);
        edges.addColumn("Label", String.class);
        TreeMap<String, Integer> ei = new TreeMap<String, Integer>();

        for (Entity e : getEntities().values()) {
            ei.put(e.id, ei.size());
            int i = nodes.addRow();
            nodes.set(i, 0, ei.get(e.id));
            nodes.set(i, 1, e);
            nodes.set(i, 2, e.getId());
        }
        for (Relationship r : getRelationships().values()) {
            int i = edges.addRow();
            edges.set(i, 0, ei.get(r.source.id));
            edges.set(i, 1, ei.get(r.target.id));
            edges.set(i, 2, r);
            edges.set(i, 3, r.getId());
        }

        return new Graph(nodes, edges, true, PGraph.idField,
                PGraph.sourceField, PGraph.targetField);
    }


    public Graph toGraph(PGraph pg) {

        Table nodes = new Table();
        nodes.addColumn(PGraph.idField, Integer.TYPE);
        nodes.addColumn("Entity", Entity.class);
        nodes.addColumn("Label", String.class);
        Table edges = new Table();
        edges.addColumn(PGraph.sourceField, Integer.TYPE);
        edges.addColumn(PGraph.targetField, Integer.TYPE);
        edges.addColumn("Relationship", Relationship.class);
        edges.addColumn("Label", String.class);
        TreeMap<String, Integer> ei = new TreeMap<String, Integer>();

        for (Entity e : getEntities().values()) {
            boolean found = false;
            for (EntityTableWrapper etw : pg.getNodeTables()) {
                if (etw.getEntity().equals(e)) {
                    found = true;
                    break;
                }
            }
            if ( ! found) {
                continue;
            }
            
            ei.put(e.id, ei.size());
            int i = nodes.addRow();
            nodes.set(i, 0, ei.get(e.id));
            nodes.set(i, 1, e);
            nodes.set(i, 2, e.getId());
        }
        for (Relationship r : getRelationships().values()) {
            boolean found = false;
            for (RelTableWrapper rtw : pg.getEdgeTables()) {
                if (rtw.getRelationship().equals(r)) found = true;
            }
            if ( ! found) {
                continue;
            }

            int i = edges.addRow();
            edges.set(i, 0, ei.get(r.source.id));
            edges.set(i, 1, ei.get(r.target.id));
            edges.set(i, 2, r);
            edges.set(i, 3, r.getId());
        }

        return new Graph(nodes, edges, true, PGraph.idField,
                PGraph.sourceField, PGraph.targetField);
    }

    public void addEntity(String id, String idField, String labelField) {
        Entity e = new Entity();
        e.schema = this;
        e.id = id;
        e.idField = idField;
        e.labelField = labelField;
        entities.put(id, e);
    }

    public void addRelationship(String id, String idField,
            String sourceId, String sourceCol,
            String targetId, String targetCol, boolean directed) {

        if ( ! entities.containsKey(sourceId)) {
            throw new IllegalArgumentException(
                    "Entity id " + sourceId + " not found");
        }
        if ( ! entities.containsKey(targetId)) {
            throw new IllegalArgumentException(
                    "Entity id " + targetId + " not found");
        }

        Relationship r = new Relationship();
        r.schema = this;
        r.id = id;
        r.idField = idField;
        r.source = entities.get(sourceId);
        r.target = entities.get(targetId);
        r.sourceField = sourceCol;
        r.targetField = targetCol;
        r.directed = directed;

        if (relationships.put(id, r) == null) {
            r.source.outgoing.add(r);
            r.target.incoming.add(r);
        }
    }

    public HashMap<String, Entity> getEntities() {
        return entities;
    }

    public HashMap<String, Relationship> getRelationships() {
        return relationships;
    }

    public void load(Element e) {
        entities.clear();
        relationships.clear();
        for (Element c : (List<Element>)e.getChildren("entity")) {
            addEntity(c.getAttributeValue("id"),
                c.getAttributeValue("idcol"),
                c.getAttributeValue("labelcol"));
        }
        for (Element c : (List<Element>)e.getChildren("relationship")) {
            addRelationship(c.getAttributeValue("id"),
                c.getAttributeValue("idcol"),
                c.getAttributeValue("sourceid"),
                c.getAttributeValue("sourcecol"),
                c.getAttributeValue("targetid"),
                c.getAttributeValue("targetcol"),
                "true".equals(c.getAttributeValue("directed").toLowerCase()));
        }
    }
    
    public void save(Element e) {
        for (Entity c : entities.values()) {
            Element n = new Element("entity");
            n.setAttribute("id", c.id);
            n.setAttribute("idcol", c.idField);
            n.setAttribute("labelcol", c.labelField);
            e.addContent(n);
        }
        for (Relationship c : relationships.values()) {
            Element n = new Element("entity");
            n.setAttribute("id", c.id);
            n.setAttribute("idcol", c.idField);
            n.setAttribute("sourceid", c.source.id);
            n.setAttribute("sourcecol", c.sourceField);
            n.setAttribute("targetid", c.target.id);
            n.setAttribute("targetcol", c.targetField);
            n.setAttribute("directed", c.directed ? "true" : "false");
            e.addContent(n);
        }
    }

    public static class Entity {
        private Schema schema;
        private String id;
        private String idField;
        private String labelField;
        private ArrayList<Relationship> outgoing
                = new ArrayList<Relationship>();
        private ArrayList<Relationship> incoming
                = new ArrayList<Relationship>();

        public Schema getSchema() {
            return schema;
        }

        public String getId() {
            return id;
        }

        public String getIdField() {
            return idField;
        }

        public ArrayList<Relationship> getIncoming() {
            return incoming;
        }

        public String getLabelField() {
            return labelField;
        }

        public ArrayList<Relationship> getOutgoing() {
            return outgoing;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entity other = (Entity) obj;
            if (this.schema != other.schema && (this.schema == null || !this.schema.equals(other.schema))) {
                return false;
            }
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.schema != null ? this.schema.hashCode() : 0);
            hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }


    }
    public static class Relationship {
        private Schema schema;
        private String id;
        private String idField;
        private Entity source;
        private Entity target;
        private String sourceField;
        private String targetField;
        private boolean directed;

        public Schema getSchema() {
            return schema;
        }

        public boolean isDirected() {
            return directed;
        }

        public String getId() {
            return id;
        }

        public String getIdField() {
            return idField;
        }

        public Entity getSource() {
            return source;
        }

        public String getSourceField() {
            return sourceField;
        }

        public Entity getTarget() {
            return target;
        }

        public String getTargetField() {
            return targetField;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Relationship other = (Relationship) obj;
            if (this.schema != other.schema && (this.schema == null || !this.schema.equals(other.schema))) {
                return false;
            }
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.schema != null ? this.schema.hashCode() : 0);
            hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }
    }
}
