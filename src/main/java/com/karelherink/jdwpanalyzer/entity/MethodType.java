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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author karel herink
 */
public class MethodType extends Type {
	
	private MethodType(Long typeId) {
		super(typeId.longValue());
	}

    private static Map methodTypes = Collections.synchronizedMap(new HashMap());
    
    /**
     * The unique typeId MUST be the in the form: referenceTypeId + methodId
     * because methods are unique only per type.
     *  
     * @param typeId
     * @return
     */
	public static MethodType getType(long referenceTypeID, long methodId) {
		return getType(new Long(referenceTypeID), new Long(methodId));
	}	
    public static MethodType getType(Long referenceTypeID, Long methodId) {
    	Long combinedId = new Long(referenceTypeID.longValue() + methodId.longValue());
    	MethodType methodType = (MethodType) methodTypes.get(combinedId);
    	if (methodType == null) {
    		methodType = new MethodType(combinedId);
    		methodType.referenceTypeID = referenceTypeID;
    		methodType.methodId = methodId;
    		methodTypes.put(combinedId, methodType);
    	}
    	return methodType;
    }
    
    private Long referenceTypeID;
    private Long methodId;
    private String name;
    private String signature;
	private String signatureWithGeneric;
    private Integer modBits;
    private Long startCodeIndex;
    private Long endCodeIndex;
    private Integer numLines;
    private LineMapping[] lineMapping;
    private Integer argCount;
    private Variable[] variables;
    private byte[] byteCode;
    private Boolean isObsolete;
    
	public Component getTypeDetailedView() {
		Vector colData;
		Vector rowData = new Vector();
		Vector colNames = new Vector();

		colNames.add("Description");
		colNames.add("Value");

		colData = new Vector();
		colData.add("UniqueID[" + this.referenceTypeID + "+" + this.methodId + "]:");
		colData.add(new Long(super.getTypeId()));
		rowData.add(colData);

		colData = new Vector();
		colData.add("MethodID:");
		colData.add(this.methodId);
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
		colData.add("StartCodeIndex:");
		colData.add(this.startCodeIndex);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("EndCodeIndex:");
		colData.add(this.endCodeIndex);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("NumLines:");
		colData.add(this.numLines);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("LineMappings:");
		colData.add(this.lineMapping == null ? null : String.valueOf(this.lineMapping.length));
		if (this.lineMapping != null && this.lineMapping.length != 0) {
			for (int i = 0; i < lineMapping.length; i++) {
				colData = new Vector();
				colData.add("LineMapping:");
				colData.add(this.lineMapping[i]);
				rowData.add(colData);
			}
		}
		
		colData = new Vector();
		colData.add("ArgCount:");
		colData.add(this.argCount);
		rowData.add(colData);
		
		//private Variable[] variables;
		colData = new Vector();
		colData.add("Variables:");
		colData.add(this.variables == null ? null : String.valueOf(this.variables.length));
		if (this.variables != null && this.variables.length != 0) {
			for (int i = 0; i < variables.length; i++) {
				colData = new Vector();
				colData.add("Variable:");
				colData.add(this.variables[i]);
				rowData.add(colData);
			}
		}
				
		colData = new Vector();
		colData.add("ByteCode:");
		colData.add(this.byteCode == null ? null : this.byteCode.length + " Bytes");
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("IsObsolete:");
		colData.add(this.isObsolete);
		rowData.add(colData);
		
		return new JTable(rowData, colNames);
	}
    
    public static class LineMapping {
    	private Long lineCodeIndex;
    	private Integer lineNumber;
    	public LineMapping(Long lineCodeIndex, Integer lineNumber) {
    		this.lineCodeIndex = lineCodeIndex;
    		this.lineNumber = lineNumber;
    	}
		public String toString() {
			return "lineCodeIndex: " + this.lineCodeIndex + ", lineNumber: " + this.lineNumber;
		}
		public Long getLineCodeIndex() {
			return this.lineCodeIndex;
		}
		public Integer getLineNumber() {
			return this.lineNumber;
		}
    }
    
    public static class Variable {
    	private Long codeIndex;
    	private String name;
    	private String signature;
    	private String genericSignature;
    	private Integer lenght;
    	private Integer frameIndex;
		public Variable(Long codeIndex, String name, String signature, String genericSignature, Integer lenght, Integer frameIndex) {
			this.codeIndex = codeIndex;
			this.name = name;
			this.signature = signature;
			this.genericSignature = genericSignature;
			this.lenght = lenght;
			this.frameIndex = frameIndex;
		}
		public String toString() {
			return "name:" + name + ",signature:" + signature 
				+ ",genSignature:" + genericSignature + ",codeIndex:" + codeIndex + 
				",length:" + lenght + ",frameIndex:" + frameIndex;
		}		
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
	public Long getEndCodeIndex() {
		return this.endCodeIndex;
	}
	public void setEndCodeIndex(Long endCodeIndex) {
		this.endCodeIndex = endCodeIndex;
	}
	public LineMapping[] getLineMapping() {
		return this.lineMapping;
	}
	public void setLineMapping(LineMapping[] lineMapping) {
		this.lineMapping = lineMapping;
	}
	public Integer getNumLines() {
		return this.numLines;
	}
	public void setNumLines(Integer numLines) {
		this.numLines = numLines;
	}
	public Long getStartCodeIndex() {
		return this.startCodeIndex;
	}
	public void setStartCodeIndex(Long startCodeIndex) {
		this.startCodeIndex = startCodeIndex;
	}
	public Integer getArgCount() {
		return this.argCount;
	}
	public void setArgCount(Integer argCount) {
		this.argCount = argCount;
	}
	public Variable[] getVariables() {
		return this.variables;
	}
	public void setVariables(Variable[] variables) {
		this.variables = variables;
	}
	public byte[] getByteCode() {
		return this.byteCode;
	}
	public void setByteCode(byte[] byteCode) {
		this.byteCode = byteCode;
	}
	public Boolean getIsObsolete() {
		return this.isObsolete;
	}
	public void setIsObsolete(Boolean isObsolete) {
		this.isObsolete = isObsolete;
	}
}
