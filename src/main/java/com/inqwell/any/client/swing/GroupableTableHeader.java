/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.swing;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


/**
 * This is the object which manages the header of the JTable and
 * also provides functionality for groupable headers.
 */
public class GroupableTableHeader extends JTableHeader {

    /**
     * Identifies the UI class which draws the header.
     */
    private static final String uiClassID = "GroupableTableHeaderUI";

    /**
     * Constructs a GroupableTableHeader which is initialized with cm as the
     * column model. If cm is null this method will initialize the table header
     * with a default TableColumnModel.
     * @param model the column model for the table
     */
    public GroupableTableHeader(GroupableTableColumnModel model) {
        super(model);
        setUI(new GroupableTableHeaderUI());
        setReorderingAllowed(false);
    }


    /**
     * Sets the margins correctly for all groups within
     * the header.
     */
    public void setColumnMargin() {
        int columnMargin = getColumnModel().getColumnMargin();
        Iterator iter = ((GroupableTableColumnModel)columnModel).columnGroupIterator();
        while (iter.hasNext()) {
            ColumnGroup cGroup = (ColumnGroup)iter.next();
            cGroup.setColumnMargin(columnMargin);
        }
    }

}

