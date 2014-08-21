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

package com.karelherink.jdwpanalyzer.model;

import com.karelherink.jdwpanalyzer.entity.Type;

import java.util.ArrayList;

/**
 * @author  karel herink
 */
public class Node {
	
	private Node parent;
	private Descriptor descriptor;
	private Value value;
	private ArrayList children;
	
	public Node(Descriptor descriptor, Value value) {
		this.descriptor = descriptor;
		this.value = value;
		this.children = new ArrayList();
	}
	
	public void addChild(Node node) {
		this.children.add(node);
		if (node.parent != null) {
			node.parent.children.remove(node);
		}
		node.parent = this;
	}
	
	public Value getValue() {
		return this.value;
	}
	
	public Descriptor getDescriptor() {
		return this.descriptor;
	}
	
	public ArrayList getChildren() {
		return this.children;
	}

	//XXX untested
	public int getChildCount() {
		return this.countKidsRecursive(children);
	}
	
	private int countKidsRecursive(ArrayList children) {
		if (children == null)
			return 0;
		
		int count = 0;
		for (int i = 0; i < children.size(); i++) {
			Node child = (Node) children.get(i);
			count += countKidsRecursive(child.children);
		}
		return count;
	}
	
	public static class Descriptor {
		
		private String label = "UNKNOWN";
		private String description = "";
		private Type associatedType = null;
		
		public Descriptor(String label) {
			this.label = label;
		}
		public Descriptor(String label, Type associatedType) {
			this.label = label;
			this.associatedType = associatedType;
		}
		public String toString() {
			return this.label;
		}
		public Type getAssociatedType() {
			return this.associatedType;
		}
		public String getLabel() {
			return this.label;
		}
		public String getDescription() {
			return this.description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	public static class Value {
		private Object realValue;
		private String printableValue;

		public Value(Object realValue) {
			this(realValue, null);
		}
		
		public Value(Object realValue, String printableValue) {
			this.realValue = realValue;
			this.printableValue = printableValue;
		}

		public String toString() {
			String str = "";
			if (this.printableValue != null)
				str += this.printableValue;
			if (this.realValue != null) {
				if(this.printableValue != null) {
					str += " [" + this.realValue + "]";
				}
				else {
					str += this.realValue;
				}
			}
			return str;
		}
		public String getPrintableValue() {
			return this.printableValue;
		}
		public void setPrintableValue(String printableValue) {
			this.printableValue = printableValue;
		}
		public Object getRealValue() {
			return this.realValue;
		}
		public void setRealValue(Object realValue) {
			this.realValue = realValue;
		}
	}
	
}
