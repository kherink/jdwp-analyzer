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

package com.karelherink.jdwpanalyzer.entity;

import javax.swing.*;
import java.awt.*;

/**
 * @author karel herink
 */
public class Type {

	private long typeId;
	
	Type(long typeId) {
		this.typeId = typeId;
	}
	
	public long getTypeId() {
		return this.typeId;
	}
		
	public boolean equals(Object obj) {
		if (obj instanceof Type) {
			return ((Type) obj).typeId == this.typeId;
		}
		return false;
	}

	public int hashCode() {
		return (int)(this.typeId ^ (this.typeId >>> 32));
	}

	public String toString() {
		return "TypeID: " + this.typeId;
	}
	
	public Component getTypeDetailedView() {
		JTextField textField = new JTextField("No detail available.");
		textField.setEditable(false);
		return textField;
	}

}
