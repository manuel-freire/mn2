package edu.umd.cs.hcil.socialaction.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import edu.umd.cs.hcil.socialaction.SocialAction;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.EdgeRanking;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.NodeRanking;
import edu.umd.cs.hcil.socialaction.jung.algorithms.importance.Ranking;
import edu.umd.cs.hcil.socialaction.render.SocialColorMap;
import edu.umd.cs.hcil.socialaction.ui.tables.GraphTableModel;

// something is broken with current SwingLabs release of JXTable. Try to find a newer release that works.
// resorting to JTable now...
/**
 * TableSorterDemo is like TableDemo, except that it inserts a custom model -- a sorter -- between the table and its
 * data model. It also has column tool tips.
 */
public class RankTable extends JPanel {

    /** Never change */
    private static final long serialVersionUID = 2168477192286541073L;
    public static final String LABEL = "jung.io.PajekNetReader.LABEL";
    public static final int LABEL_COLUMN = 1;
    public static final String LABEL_COLUMN_LABEL = "Label";
    public static final int RANK_COLUMN = 0;
    public static final String RANK_COLUMN_LABEL = "Rank";
    public static final int PARTITION_COLUMN = 3;
    public static final String PARTITION_COLUMN_LABEL = "Type";
    public static final int ENTITY_COLUMN = 2;
    public static final String ENTITY_COLUMN_LABEL = "Entity";
    public static final String[] COLUMN_NAMES = {RANK_COLUMN_LABEL, LABEL_COLUMN_LABEL/* , PARTITION_COLUMN_LABEL */,
        ENTITY_COLUMN_LABEL};
    private GraphTableModel tableModel;
    private JXTable table;
    private boolean allow_nodes;
    // public final PrefuseConverter.JungGraph graph;
    private SocialAction m_app = null;
    private ListSelectionModel rowSM;
    private ColorRenderer colorRenderer;
    private ListSelectionListener listener;

    public RankTable(SocialAction app, boolean allow_nodes, boolean allow_edges) {

        super(new GridLayout(1, 0));

        this.m_app = app;
        this.allow_nodes = allow_nodes;

        tableModel = new GraphTableModel(COLUMN_NAMES);

        table = new JXTable(tableModel); // NEW

        // table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        table.getColumnExt(ENTITY_COLUMN_LABEL).setVisible(false);

        // table.getColumn(table.getColumnName(ENTITY_COLUMN)).setWidth(1);

        // Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to this panel.
        add(scrollPane);

        // add listener to table that updates paint after each click.
        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                table.repaint();
                table.revalidate();
            }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        colorRenderer = new ColorRenderer(m_app);
        // table.setDefaultRenderer(NodeRanking.class, colorRenderer );
        // table.setDefaultRenderer(EdgeRanking.class, colorRenderer );
        System.out.println("didn't set renderer for node and edge rankings");

        table.setDefaultRenderer(NodeRanking.class, colorRenderer);
        table.setDefaultRenderer(EdgeRanking.class, colorRenderer);

        rowSM = table.getSelectionModel();

        listener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {

                // Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    // no rows are selected
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();

                    table.getColumnExt(ENTITY_COLUMN_LABEL).setVisible(true);
                    Object selectedObject = table.getValueAt(selectedRow, ENTITY_COLUMN);
                    table.getColumnExt(ENTITY_COLUMN_LABEL).setVisible(false);

                    if (selectedObject instanceof Node) {

                        Node value = (Node) selectedObject;
                        updatePickedState(value);

                    } else if (selectedObject instanceof EdgeRanking) {

                        EdgeRanking value = (EdgeRanking) selectedObject;
                        updatePickedState(value.edge);

                    } else {
                        System.out.println(selectedObject);
                    }

                    table.repaint();
                    table.revalidate();

                }
            }
        };

        rowSM.addListSelectionListener(listener);

    }

    public void setRankType(String rankType) {
    }
    VisualItem oldPickedItem = null;

    public void updatePickedState(Object value) {

        if (value instanceof Node) {

            Node item = (Node) value;

            VisualItem graphItem = m_app.getVisualization().getVisualItem(SocialAction.nodes, item);

            TupleSet focusGroup = m_app.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
            focusGroup.setTuple(graphItem);

            // if (graphItem != null) {
            // graphItem.setHighlighted(true);
            //
            // if (oldPickedItem != null)
            // oldPickedItem.setHighlighted(false);
            //
            // oldPickedItem = graphItem;
            //
            // }
        }
    }

    public void selectCell(Node vertex) {

        if (vertex == null) {
            table.clearSelection();
        }

        table.getColumnExt(ENTITY_COLUMN_LABEL).setVisible(true);

        for (int i = 0; i < table.getRowCount(); i++) {

            Object currentValue = table.getValueAt(i, ENTITY_COLUMN);

            if (currentValue instanceof Node) {
                Node value = (Node) currentValue;
                if (value != null) {
                    if (value.equals(vertex)) {
                        if (i != table.getSelectedRow()) {
                            // don't let the table's ActionListener get fired this one time.
                            rowSM.removeListSelectionListener(listener);
                            table.changeSelection(i, LABEL_COLUMN, false, false);
                            rowSM.addListSelectionListener(listener);
                            break;
                        }
                    }
                }
            }

        }
        table.getColumnExt(ENTITY_COLUMN_LABEL).setVisible(false);
        // table.getColumnExt(PARTITION_COLUMN_LABEL).setVisible(false);

    }

    public void setContent(List<Ranking> list, double min, double max, Object alphaSliderMin, Object alphaSliderMax) {
        if (alphaSliderMin instanceof Double) {
            setContent(list, min, max, ((Double) alphaSliderMin).doubleValue(), ((Double) alphaSliderMax).doubleValue());
        } else if (alphaSliderMin instanceof Integer) {
            setContent(list, min, max, ((Integer) alphaSliderMin).doubleValue(), ((Integer) alphaSliderMax).doubleValue());
        } else {
            System.err.println("Possible casting problem in RankTable.");
        }
    }

    @SuppressWarnings("unchecked")
    public void setContent(List<Ranking> list, double min, double max, double alphaSliderMin, double alphaSliderMax) {

        // VisualGraph vg = (VisualGraph) m_app.getVisualization().getVisualGroup(SocialAction.graph);
        //
        // JPrefuseTable.showTableWindow(vg.getNodeTable());

        boolean isNodeRanking = true;

        Iterator<Ranking> i = list.iterator();
        int size = 0;
        while (i.hasNext()) {

            Ranking value = i.next();
            if (value instanceof EdgeRanking) {
                isNodeRanking = false;
            }

            if ((value.rankScore <= alphaSliderMax) && (value.rankScore >= alphaSliderMin)) {
                size++;
            }
        }

        tableModel.data = new Object[size][6];

        if (isNodeRanking) {
            m_app.clearNodeRangeSliderSet();
        } else {
            m_app.clearEdgeRangeSliderSet();
        }

        // Remove list selection listener temporarily while we update
        // Had unnecessary calls with TupleSetListener for Visualization.FOCUS_ITEMS group in iOpener workbench
        rowSM = table.getSelectionModel();
        rowSM.removeListSelectionListener(listener);

        i = list.iterator();
        int cnt = 0;
        while (i.hasNext()) {
            Object value = i.next();
            Ranking ranking = (Ranking) value;
            if (ranking.rankScore > alphaSliderMax) {
                continue;
            }
            if (ranking.rankScore < alphaSliderMin) {
                continue;
            }

            if (value instanceof NodeRanking && allow_nodes) {
                NodeRanking nodeRanking = (NodeRanking) value;

                Node vertex = nodeRanking.vertex;
                m_app.addToNodeRangeSliderSet(vertex);

                if (vertex.canSetString("rankValue")) {
                    vertex.set("rankValue", String.valueOf(nodeRanking.rankScore));
                }

                tableModel.setValueAt(nodeRanking.vertex.get(m_app.getLabelField()), cnt, LABEL_COLUMN);
                tableModel.setValueAt(nodeRanking.vertex, cnt, ENTITY_COLUMN);
                tableModel.setValueAt(nodeRanking, cnt, RANK_COLUMN);
                tableModel.setValueAt("Country", cnt, PARTITION_COLUMN);

                cnt++;

            } else if (value instanceof EdgeRanking) {
                EdgeRanking edgeRanking = (EdgeRanking) value;

                Edge edge = edgeRanking.edge;
                m_app.addToEdgeRangeSliderSet(edge);

                String edgeLabel = "None";

                try {
                    edgeLabel = edgeRanking.edge.getSourceNode().get(m_app.getLabelField()) + "->"
                            + edgeRanking.edge.getTargetNode().get(m_app.getLabelField());
                } catch (Exception e) {
                    System.err.println("Error labeling edge " + edge.getRow() + " from " +
                            edgeRanking.edge.getSourceNode().getRow() + " to " +
                            edgeRanking.edge.getTargetNode().getRow());
                    e.printStackTrace();
                }
                tableModel.setValueAt(edgeLabel, cnt, LABEL_COLUMN);
                tableModel.setValueAt(edgeRanking.edge, cnt, ENTITY_COLUMN);
                tableModel.setValueAt(edgeRanking, cnt, RANK_COLUMN);
                tableModel.setValueAt("Edge", cnt, PARTITION_COLUMN);
                cnt++;
            }
        }

        // Add back list selection listener after updating
        // Had unnecessary calls with TupleSetListener for Visualization.FOCUS_ITEMS group in iOpener workbench
        rowSM.addListSelectionListener(listener);

        if (isNodeRanking) {

            m_app.clearEdgeRangeSliderSet();

            Iterator<Edge> it = m_app.getGraph().edges();
            TupleSet nodeSliderSet = m_app.getVisualization().getFocusGroup(SocialAction.RANGE_NODE_SLIDER_GROUP);

            while (it.hasNext()) {
                Edge e = it.next();
                // System.out.println(e.getTargetNode() + " " + nodeSliderSet.getTupleCount());

                boolean source = nodeSliderSet.containsTuple(e.getSourceNode());
                boolean target = nodeSliderSet.containsTuple(e.getTargetNode());

                if (source && target) {
                    m_app.addToEdgeRangeSliderSet(e);
                }

            }
        }
        // tableModel.fireTableDataChanged();
        // revalidate();
    }

    public class ColorRenderer extends JLabel implements TableCellRenderer {

        /** Never change */
        private static final long serialVersionUID = 4360028998580865653L;
        SocialColorMap colorMap = null;
        DecimalFormat df = new DecimalFormat();
        SocialAction m_app;

        public ColorRenderer(SocialAction app) {
            setOpaque(true); // MUST do this for background to show up.
            setForeground(Color.WHITE);

            m_app = app;

            df.setMaximumFractionDigits(5);
            df.setMinimumFractionDigits(5);
        }

        public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (object instanceof NodeRanking) {
                NodeRanking ranking = (NodeRanking) object;

                // set colors
                Node node = ranking.vertex;
                VisualItem vi = m_app.getVisualization().getVisualItem(SocialAction.graph, node);

                int color = vi.getFillColor(); // vi.getInt("_fillColor");
                int backgroundInt = (color);
                Color backgroundColor = ColorLib.getColor(backgroundInt);
                setBackground(backgroundColor);

                // set text
                df.setDecimalSeparatorAlwaysShown(true);
                setText(df.format(ranking.rankScore));
                setFont(table.getFont());

                // set highlighting box (if selected)
                if (isSelected) {
                    setBorder(BorderFactory.createLineBorder(table.getGridColor()));
                } else {
                    setBorder(null);
                }
            } else if (object instanceof EdgeRanking) {
                EdgeRanking ranking = (EdgeRanking) object;

                // set colors
                Edge edge = ranking.edge;
                VisualItem vi = m_app.getVisualization().getVisualItem(SocialAction.graph, edge);

                int color = vi.getStrokeColor(); // vi.getInt("_fillColor");
                int backgroundInt = (color);
                Color backgroundColor = ColorLib.getColor(backgroundInt);
                setBackground(backgroundColor);

                // set text
                df.setDecimalSeparatorAlwaysShown(true);
                setText(df.format(ranking.rankScore));
                setFont(table.getFont());

                // set highlighting box (if selected)
                if (isSelected) {
                    setBorder(BorderFactory.createLineBorder(table.getGridColor()));
                } else {
                    setBorder(null);
                }
            }

            return this;
        }

        // All these overriden as suggested by DefaultTableCellRenderer
        public void invalidate() {
        }

        public void validate() {
        }

        public void revalidate() {
        }

        public void repaint(long tm, int x, int y, int width, int height) {
        }

        public void repaint(Rectangle r) {
        }

        public void repaint() {
        }

        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            // Strings get interned...
            if (propertyName == "text"
                    || propertyName == "labelFor"
                    || propertyName == "displayedMnemonic"
                    || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }
}
