/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* Java Version 1.4.2_03-b02
 *
 * |-------------------------------------------------------------------|
 * |        |             Name              |         Language         |
 * |        |-------------------------------|--------------------------|
 * |  SNo.  |       |        Second         |        |      Others     |
 * |        | First |-----------------------| Native |-----------------|
 * |        |       |   1   |    2   |      |   2    |   3    |        |
 * |-------------------------------------------------------------------|
 * |        |       |       |        |      |        |        |        |
 *
 */

package com.inqwell.any.client.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

// Steve Webb 16/09/04 swebb99_uk@hotmail.com

public class GroupableColumnExample extends JFrame {

    GroupableColumnExample() {
        super( "Multi-Width Header Example" );

        DefaultTableModel dm = new DefaultTableModel();
        dm.setDataVector(new Object[][]{
            {"119","Finbar", "John","Saunders","ja","ko","zh"},
            {"911","Roger", "Peter","Melly","en","fr","pt"}},
            new Object[]{"SNo.","First", "1","2","Native","2","3"});

            // Setup table
            JTable table = new JTable( /*dm, new GroupableTableColumnModel()*/);
            table.setColumnModel(new GroupableTableColumnModel());
            table.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel)table.getColumnModel()));
            table.setModel(dm);

            // Setup Column Groups
            GroupableTableColumnModel cm = (GroupableTableColumnModel)table.getColumnModel();
            ColumnGroup g_name = new ColumnGroup( "Name");
            g_name.add(cm.getColumn(1));
            ColumnGroup g_token = new ColumnGroup(new GroupableTableCellRenderer(), "Second");
            g_name.add(g_token);
            g_token.add(cm.getColumn(2));
            g_token.add(cm.getColumn(3));
            ColumnGroup g_lang = new ColumnGroup("Language");
            g_lang.add(cm.getColumn(4));
            ColumnGroup g_other = new ColumnGroup("Others");
            g_other.add(cm.getColumn(5));
            cm.getColumn(6).setHeaderRenderer(new GroupableTableCellRenderer());
            g_other.add(cm.getColumn(6));
            g_lang.add(g_other);
            GroupableTableHeader header = (GroupableTableHeader)table.getTableHeader();
            cm.addColumnGroup(g_name);
            cm.addColumnGroup(g_lang);

            // Finish off gui
            JScrollPane scroll = new JScrollPane( table );
            getContentPane().add( scroll );
            setSize( 800, 200 );
    }

    public static void main(String[] args) {
        GroupableColumnExample frame = new GroupableColumnExample();
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                System.exit(0);
            }
        });
        frame.setVisible(true);
    }
}

/**
 * Demo renderer just to prove they can be used.
 */
class GroupableTableCellRenderer extends DefaultTableCellRenderer {
    /**
     *
     * @param table
     * @param value
     * @param selected
     * @param focused
     * @param row
     * @param column
     * @return
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
    boolean selected, boolean focused, int row, int column) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            setForeground(Color.WHITE);
            setBackground(Color.RED);
        }
        setHorizontalAlignment(SwingConstants.CENTER);
        setText(value != null ? value.toString() : " ");
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return this;
    }
}

