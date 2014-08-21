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

import com.karelherink.jdwpanalyzer.model.constants.ClassStatus;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * @author karel herink
 */
public class ReferenceType extends Type {

	private static Map referenceTypes = Collections.synchronizedMap(new HashMap());

	public static ReferenceType getType(long typeId) {
		return getType(new Long(typeId));
	}	
	public static ReferenceType getType(Long typeId) {
		ReferenceType refType = (ReferenceType) referenceTypes.get(typeId);
		if (refType == null) {
			refType = new ReferenceType(typeId);
			referenceTypes.put(typeId, refType);
		}
		return refType;
	}

	private Byte typeTag;
	private String signature;
	private Long classLoaderId;
	private Integer modifiers;
	private Long[] fieldIds;
	private Long[] methodIds;
	// private Hashtable fieldValues;/*fieldId,PacketAnalyzer.Value[]*/
	private String sourceFile;
	private Long[] nestedTypeIds;
	private Integer status;
	private Long[] interfaceTypeIds;
	private Long classObjectId;
	private String sourceDebugExtension;
	private String signatureWithGeneric;
	private Long[] fieldsWithGenericFieldIds;
	private Long[] methodsWithGenericMethodIds;
	private byte[] classBytes;
	
	//TODO - see about making subclasses for Class, Array & Interface
	//CLASS specific
	private Long superclass;

	public Component getTypeDetailedView() {
		Vector colData;
		Vector rowData = new Vector();
		Vector colNames = new Vector();

		colNames.add("Description");
		colNames.add("Value");

		colData = new Vector();
		colData.add("RefTypeId:");
		colData.add(new Long(super.getTypeId()));
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
		colData.add("SourceFile:");
		colData.add(this.sourceFile);
		rowData.add(colData);

		colData = new Vector();
		colData.add("ClassLoaderId:");
		colData.add(this.classLoaderId);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Modifiers:");
		colData.add(this.modifiers);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Num FieldIds:");
		colData.add(this.fieldIds == null ? null : String.valueOf(this.fieldIds.length));
		rowData.add(colData);
		if (this.fieldIds != null && this.fieldIds.length != 0) {
			for (int i = 0; i < fieldIds.length; i++) {
				colData = new Vector();
				colData.add("FieldId:");
				colData.add(this.fieldIds[i]);
				rowData.add(colData);

				// TODO : this should be in the FieldType.java
				// colData = new Vector();
				// colData.add("FieldValue:");
				// PacketAnalyzer.Value fieldValue = (PacketAnalyzer.Value) this.fieldValues.get(new Long(fieldIds[i]));
				// colData.add(/*fieldValue == null ? Constants.UNKNOWN :*/ fieldValue);
				// rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("Num MethodIds:");
		colData.add(this.methodIds == null ? null : String.valueOf(this.methodIds.length));
		rowData.add(colData);
		if (this.methodIds != null && this.methodIds.length != 0) {
			for (int i = 0; i < methodIds.length; i++) {
				colData = new Vector();
				colData.add("MethodId:");
				colData.add(this.methodIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("Num NestedTypeIds:");
		colData.add(this.nestedTypeIds == null ? null : String.valueOf(this.nestedTypeIds.length));
		rowData.add(colData);
		if (this.nestedTypeIds != null && this.nestedTypeIds.length != 0) {
			for (int i = 0; i < nestedTypeIds.length; i++) {
				colData = new Vector();
				colData.add("NestedTypeId:");
				colData.add(this.nestedTypeIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("Status:");
		colData.add(this.status == null ? null : ClassStatus.asString(this.status.intValue()));
		rowData.add(colData);

		colData = new Vector();
		colData.add("Num InterfaceTypeIds:");
		colData.add(this.interfaceTypeIds == null ? null : String.valueOf(this.interfaceTypeIds.length));
		rowData.add(colData);
		if (this.interfaceTypeIds != null && this.interfaceTypeIds.length != 0) {
			for (int i = 0; i < interfaceTypeIds.length; i++) {
				colData = new Vector();
				colData.add("InterfaceTypeId:");
				colData.add(this.interfaceTypeIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("ClassObjectId:");
		colData.add(this.classObjectId);
		rowData.add(colData);

		colData = new Vector();
		colData.add("SourceDebugExtension:");
		colData.add(this.sourceDebugExtension);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Num FieldWithGenericIds:");
		colData.add(this.fieldsWithGenericFieldIds == null ? null : String.valueOf(this.fieldsWithGenericFieldIds.length));
		if (this.fieldsWithGenericFieldIds != null && this.fieldsWithGenericFieldIds.length != 0) {
			for (int i = 0; i < fieldsWithGenericFieldIds.length; i++) {
				colData = new Vector();
				colData.add("FieldWithGenericId:");
				colData.add(this.fieldsWithGenericFieldIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("Num MethodWithGenericIds:");
		colData.add(this.methodsWithGenericMethodIds == null ? null : String.valueOf(this.methodsWithGenericMethodIds.length));
		if (this.methodsWithGenericMethodIds != null && this.methodsWithGenericMethodIds.length != 0) {
			for (int i = 0; i < methodsWithGenericMethodIds.length; i++) {
				colData = new Vector();
				colData.add("MethodWithGenericId:");
				colData.add(this.methodsWithGenericMethodIds[i]);
				rowData.add(colData);
			}
		}
		return new JTable(rowData, colNames);
	}

	private ReferenceType(Long typeId) {
		super(typeId.longValue());
	}

	public String toString() {
		return "ReferenceType";
	}

	public Long getClassLoaderId() {
		return this.classLoaderId;
	}

	public void setClassLoaderId(Long classLoaderId) {
		this.classLoaderId = classLoaderId;
	}

	public Long getClassObjectId() {
		return this.classObjectId;
	}

	public void setClassObjectId(Long classObjectId) {
		this.classObjectId = classObjectId;
	}

	public Long[] getFieldIds() {
		return this.fieldIds;
	}

	public void setFieldIds(Long[] fieldIds) {
		this.fieldIds = fieldIds;
	}

	public Long[] getFieldsWithGenericFieldIds() {
		return this.fieldsWithGenericFieldIds;
	}

	public void setFieldsWithGenericFieldIds(Long[] fieldsWithGenericFieldIds) {
		this.fieldsWithGenericFieldIds = fieldsWithGenericFieldIds;
	}

	public Long[] getInterfaceTypeIds() {
		return this.interfaceTypeIds;
	}

	public void setInterfaceTypeIds(Long[] interfaceTypeIds) {
		this.interfaceTypeIds = interfaceTypeIds;
	}

	public Long[] getMethodIds() {
		return this.methodIds;
	}

	public void setMethodIds(Long[] methodIds) {
		this.methodIds = methodIds;
	}

	public Long[] getMethodsWithGenericMethodIds() {
		return this.methodsWithGenericMethodIds;
	}

	public void setMethodsWithGenericMethodIds(Long[] methodsWithGenericMethodIds) {
		this.methodsWithGenericMethodIds = methodsWithGenericMethodIds;
	}

	public Integer getModifiers() {
		return this.modifiers;
	}

	public void setModifiers(Integer modifiers) {
		this.modifiers = modifiers;
	}

	public Long[] getNestedTypeIds() {
		return this.nestedTypeIds;
	}

	public void setNestedTypeIds(Long[] nestedTypeIds) {
		this.nestedTypeIds = nestedTypeIds;
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

	public String getSourceDebugExtension() {
		return this.sourceDebugExtension;
	}

	public void setSourceDebugExtension(String sourceDebugExtension) {
		this.sourceDebugExtension = sourceDebugExtension;
	}

	public String getSourceFile() {
		return this.sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Byte getTypeTag() {
		return this.typeTag;
	}

	public void setTypeTag(Byte typeTag) {
		this.typeTag = typeTag;
	}

	public Long getSuperclass() {
		return this.superclass;
	}
	
	public void setSuperclass(Long superclass) {
		this.superclass = superclass;
	}
	public byte[] getClassBytes() {
		return this.classBytes;
	}
	public void setClassBytes(byte[] classBytes) {
		this.classBytes = classBytes;
	}
}
