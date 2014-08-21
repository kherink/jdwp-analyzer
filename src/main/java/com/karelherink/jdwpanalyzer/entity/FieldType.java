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

import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * @author karel herink
 */
public class FieldType extends Type {
	private FieldType(Long typeId) {
		super(typeId.longValue());
	}

    private static Map fieldTypes = Collections.synchronizedMap(new HashMap());
    
	public static FieldType getType(long referenceTypeID, long fieldId) {
		return getType(new Long(referenceTypeID), new Long(fieldId));
	}	
    public static FieldType getType(Long referenceTypeID, Long fieldId) {
    	Long combinedId = new Long(referenceTypeID.longValue() + fieldId.longValue());
    	FieldType fieldType = (FieldType) fieldTypes.get(combinedId);
    	if (fieldType == null) {
    		fieldType = new FieldType(combinedId);
    		fieldType.referenceTypeID = referenceTypeID;
    		fieldType.fieldId = fieldId;
    		fieldTypes.put(combinedId, fieldType);
    	}
    	return fieldType;
    }
    
    
    private Long referenceTypeID;
    private Long fieldId;
    private String name;
    private String signature;
	private String signatureWithGeneric;
    private Integer modBits;
    private PacketAnalyzer.Value value;
    
	public Component getTypeDetailedView() {
		Vector colData;
		Vector rowData = new Vector();
		Vector colNames = new Vector();

		colNames.add("Description");
		colNames.add("Value");

		colData = new Vector();
		colData.add("UniqueID[" + this.referenceTypeID + "+" + this.fieldId + "]:");
		colData.add(new Long(super.getTypeId()));
		rowData.add(colData);

		colData = new Vector();
		colData.add("FieldID:");
		colData.add(this.fieldId);
		rowData.add(colData);

		colData = new Vector();
		colData.add("RefTypeID:");
		colData.add(this.referenceTypeID);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Name:");
		colData.add(this.name);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Signature:");
		colData.add(this.signature);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("SignatureWithGeneric:");
		colData.add(this.signatureWithGeneric);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("ModBits:");
		colData.add(this.modBits);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("Value:");
		colData.add(this.value);
		rowData.add(colData);
		
		return new JTable(rowData, colNames);
	}
    
	public Integer getModBits() {
		return this.modBits;
	}
	public void setModBits(Integer modBits) {
		this.modBits = modBits;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSignature() {
		return this.signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}    
	public String getSignatureWithGeneric() {
		return this.signatureWithGeneric;
	}
	public void setSignatureWithGeneric(String signatureWithGeneric) {
		this.signatureWithGeneric = signatureWithGeneric;
	}
	public PacketAnalyzer.Value getValue() {
		return this.value;
	}
	public void setValue(PacketAnalyzer.Value value) {
		this.value = value;
	}

}
