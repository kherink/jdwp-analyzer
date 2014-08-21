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
public class ObjectType extends Type {

	private ObjectType(Long typeId) {
		super(typeId.longValue());
	}

    private static Map objectTypes = Collections.synchronizedMap(new HashMap());
    
	public static ObjectType getType(long typeId) {
		return getType(new Long(typeId));
	}	
    public static ObjectType getType(Long typeId) {
    	if (typeId.longValue() == -1)
    		return null;
    	ObjectType objectType = (ObjectType) objectTypes.get(typeId);
    	if (objectType == null) {
    		objectType = new ObjectType(typeId);
    		objectTypes.put(typeId, objectType);
    	}
    	return objectType;
    }

    private Long referenceTypeId;
    private Long ownerThreadId;
    private Integer monitorEntryCount;
    private Long[] waitingThreadIds;
    private Boolean isCollected;
    private Boolean disposeRequested;
    
    //String specific:
    private String stringValue;
    
    //Thread specific:
    private String threadName;
    private Integer suspendCount;
    private Integer threadStatus;
    private Integer suspendStatus;
    private Long threadGroupId;
    
    //ThreadGroup specific:
    private String threadGroupName;
    private Long parentGroupId;
    private Long[] childThreadIds;
    private Long[] childThreadGroupIds;
    
    //Array specific
    private Integer length;
    
	public Component getTypeDetailedView() {
		Vector colData;
		Vector rowData = new Vector();
		Vector colNames = new Vector();

		colNames.add("Description");
		colNames.add("Value");
		
		colData = new Vector();
		colData.add("ObjTypeId:");
		colData.add(new Long(super.getTypeId()));
		rowData.add(colData);

		colData = new Vector();
		colData.add("RefTypeId:");
		colData.add(this.referenceTypeId);
		rowData.add(colData);

		colData = new Vector();
		colData.add("OwnerThreadId:");
		colData.add(this.ownerThreadId);
		rowData.add(colData);

		colData = new Vector();
		colData.add("MonitorEntryCount:");
		colData.add(this.monitorEntryCount);
		rowData.add(colData);

		//private Long[] waitingThreadIds;
		colData = new Vector();
		colData.add("Num WaitingThreadIDs:");
		colData.add(this.waitingThreadIds == null ? null : String.valueOf(this.waitingThreadIds.length));
		rowData.add(colData);
		if (this.waitingThreadIds != null && this.waitingThreadIds.length != 0) {
			for (int i = 0; i < waitingThreadIds.length; i++) {
				colData = new Vector();
				colData.add("ThreadId:");
				colData.add(this.waitingThreadIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("IsCollected:");
		colData.add(this.isCollected);
		rowData.add(colData);

		colData = new Vector();
		colData.add("DisposeRequested:");
		colData.add(this.disposeRequested);
		rowData.add(colData);

		colData = new Vector();
		colData.add("String Specific:");
		colData.add("");
		rowData.add(colData);

		colData = new Vector();
		colData.add("StringValue:");
		colData.add(this.stringValue);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Thread specific:");
		colData.add("");
		rowData.add(colData);

		colData = new Vector();
		colData.add("ThreadName:");
		colData.add(this.threadName);
		rowData.add(colData);

		colData = new Vector();
		colData.add("SuspendCount:");
		colData.add(this.suspendCount);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("ThreadStatus:");
		colData.add(this.threadStatus);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("SuspendStatus:");
		colData.add(this.suspendStatus);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("ThreadGroupId:");
		colData.add(this.threadGroupId);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("ThreadGroup specific:");
		colData.add("");
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("ThreadGroupName:");
		colData.add(this.threadGroupName);
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("ParentGroupId:");
		colData.add(this.parentGroupId);
		rowData.add(colData);

		colData = new Vector();
		colData.add("Num ChildThreadIds:");
		colData.add(this.childThreadIds == null ? null : String.valueOf(this.childThreadIds.length));
		rowData.add(colData);
		if (this.childThreadIds != null && this.childThreadIds.length != 0) {
			for (int i = 0; i < childThreadIds.length; i++) {
				colData = new Vector();
				colData.add("ChildThreadId:");
				colData.add(this.childThreadIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("Num ChildThreadGroupIds:");
		colData.add(this.childThreadGroupIds == null ? null : String.valueOf(this.childThreadGroupIds.length));
		rowData.add(colData);
		if (this.childThreadGroupIds != null && this.childThreadGroupIds.length != 0) {
			for (int i = 0; i < childThreadIds.length; i++) {
				colData = new Vector();
				colData.add("ChildThreadGroupId:");
				colData.add(this.childThreadGroupIds[i]);
				rowData.add(colData);
			}
		}

		colData = new Vector();
		colData.add("Array specific:");
		colData.add("");
		rowData.add(colData);
		
		colData = new Vector();
		colData.add("Length:");
		colData.add(this.length);
		rowData.add(colData);
		
		return new JTable(rowData, colNames);

	}
    
    
    
    
	public Integer getSuspendStatus() {
		return this.suspendStatus;
	}
	public void setSuspendStatus(Integer suspendStatus) {
		this.suspendStatus = suspendStatus;
	}
	public Integer getThreadStatus() {
		return this.threadStatus;
	}
	public void setThreadStatus(Integer threadStatus) {
		this.threadStatus = threadStatus;
	}
	public Long getReferenceTypeId() {
		return this.referenceTypeId;
	}
	public void setReferenceTypeId(Long referenceTypeId) {
		this.referenceTypeId = referenceTypeId;
	}
	public Integer getMonitorEntryCount() {
		return this.monitorEntryCount;
	}
	public void setMonitorEntryCount(Integer monitorEntryCount) {
		this.monitorEntryCount = monitorEntryCount;
	}
	public Long getOwnerThreadId() {
		return this.ownerThreadId;
	}
	public void setOwnerThreadId(Long ownerThreadId) {
		this.ownerThreadId = ownerThreadId;
	}
	public Long[] getWaitingThreadIds() {
		return this.waitingThreadIds;
	}
	public void setWaitingThreadIds(Long[] waitingThreadIds) {
		this.waitingThreadIds = waitingThreadIds;
	}
	public Boolean getIsCollected() {
		return this.isCollected;
	}
	public void setIsCollected(Boolean isCollected) {
		this.isCollected = isCollected;
	}
	public String getStringValue() {
		return this.stringValue;
	}
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	public String getThreadName() {
		return this.threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	public Integer getSuspendCount() {
		return this.suspendCount;
	}
	public void setSuspendCount(Integer suspendCount) {
		this.suspendCount = suspendCount;
	}
	public Long getThreadGroupId() {
		return this.threadGroupId;
	}
	public void setThreadGroupId(Long threadGroupId) {
		this.threadGroupId = threadGroupId;
	}
	public Long[] getChildThreadGroupIds() {
		return this.childThreadGroupIds;
	}
	public void setChildThreadGroupIds(Long[] childThreadGroupIds) {
		this.childThreadGroupIds = childThreadGroupIds;
	}
	public Long[] getChildThreadIds() {
		return this.childThreadIds;
	}
	public void setChildThreadIds(Long[] childThreadIds) {
		this.childThreadIds = childThreadIds;
	}
	public Long getParentGroupId() {
		return this.parentGroupId;
	}
	public void setParentGroupId(Long parentGroupId) {
		this.parentGroupId = parentGroupId;
	}
	public String getThreadGroupName() {
		return this.threadGroupName;
	}
	public void setThreadGroupName(String threadGroupName) {
		this.threadGroupName = threadGroupName;
	}
	public Boolean getDisposeRequested() {
		return this.disposeRequested;
	}
	public void setDisposeRequested(Boolean disposeRequested) {
		this.disposeRequested = disposeRequested;
	}
}
