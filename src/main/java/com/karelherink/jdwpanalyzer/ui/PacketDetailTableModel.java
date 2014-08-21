/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.karelherink.jdwpanalyzer.ui;

import com.karelherink.jdwpanalyzer.model.Node;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;


/**
 * @author karel herink
 */
public class PacketDetailTableModel implements TableModel {
	
	private ArrayList tableModelListeners;
	
	private ArrayList descriptions;
	private ArrayList values;
	
	private static final int NUM_COLS = 2;
	
	public PacketDetailTableModel(Node node) {
		this.tableModelListeners = new ArrayList();
		this.initFlatNodeStructure(node);
	}
	
	private void initFlatNodeStructure(Node node) {
		this.descriptions = new ArrayList();
		this.values = new ArrayList();
		this.addToStructure(node);
		this.updateStructureRecursive(node.getChildren());
	}
	
	private void updateStructureRecursive(ArrayList children) {
		if (children == null)
			return;
		
		for (int i = 0; i < children.size(); i++) {
			Node child = (Node) children.get(i);
			this.addToStructure(child);
			this.updateStructureRecursive(child.getChildren());
		}
	}
	
	private void addToStructure(Node node) {
		if (node.getDescriptor() == null && node.getValue() == null)
			return;
		this.descriptions.add(node.getDescriptor());
		this.values.add(node.getValue());
	}
	
	public int getColumnCount() {
		return NUM_COLS;
	}

	public int getRowCount() {
		return this.descriptions.size();
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0)
			return Node.Descriptor.class;
		return Object.class;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex>= this.getRowCount() || columnIndex < 0 || columnIndex >= this.getColumnCount())
			return null;
		
		if (columnIndex == 0)
			return this.descriptions.get(rowIndex);
		
		Node.Value value = (Node.Value) this.values.get(rowIndex);
		return value;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		//Not implemented
	}

	public String getColumnName(int columnIndex) {
		return columnIndex == 0 ? "Description" : "Value";
	}

	public void addTableModelListener(TableModelListener l) {
		this.tableModelListeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		this.tableModelListeners.remove(l);
	}

}
