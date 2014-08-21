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
package com.karelherink.jdwpanalyzer.response;

import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

/**
 * @author karel herink
 */
public class Analyzer_1_17 extends PacketAnalyzer {

    public Analyzer_1_17() {
    }
    
	public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(new Node.Descriptor("VM Abilities New:"), null);
		
		boolean watchFieldModification = getBoolean(packet, index++);
		Node watchFieldModificationInfo = new Node(new Node.Descriptor("WatchFieldModification:"), new Node.Value(new Boolean(watchFieldModification)));
		root.addChild(watchFieldModificationInfo);
		
		boolean watchFieldAccess = getBoolean(packet, index++);
		Node watchFieldAccessInfo = new Node(new Node.Descriptor("WatchFieldAccess:"), new Node.Value(new Boolean(watchFieldAccess)));
		root.addChild(watchFieldAccessInfo);
		
		boolean getBytecodes = getBoolean(packet, index++);
		Node getBytecodesInfo = new Node(new Node.Descriptor("GetBytecodes:"), new Node.Value(new Boolean(getBytecodes)));
		root.addChild(getBytecodesInfo);
		
		boolean getSyntheticAttribute = getBoolean(packet, index++);
		Node getSyntheticAttributeInfo = new Node(new Node.Descriptor("GetSyntheticAttribute:"), new Node.Value(new Boolean(getSyntheticAttribute)));
		root.addChild(getSyntheticAttributeInfo);
		
		boolean getOwnedMonitor = getBoolean(packet, index++);
		Node getOwnedMonitorInfo = new Node(new Node.Descriptor("GetOwnedMonitor:"), new Node.Value(new Boolean(getOwnedMonitor)));
		root.addChild(getOwnedMonitorInfo);
		
		boolean getCurrentContendedMonitor = getBoolean(packet, index++);
		Node getCurrentContendedMonitorInfo = new Node(new Node.Descriptor("GetCurrentContendedMonitor:"), new Node.Value(new Boolean(getCurrentContendedMonitor)));
		root.addChild(getCurrentContendedMonitorInfo);
		
		boolean getMonitor = getBoolean(packet, index++);
		Node getMonitorInfo = new Node(new Node.Descriptor("GetMonitor:"), new Node.Value(new Boolean(getMonitor)));
		root.addChild(getMonitorInfo);
		
		boolean RedefineClasses = getBoolean(packet, index++);
		Node RedefineClassesInfo = new Node(new Node.Descriptor("RedefineClasses:"), new Node.Value(new Boolean(RedefineClasses)));
		root.addChild(RedefineClassesInfo);

		boolean AddMethod = getBoolean(packet, index++);
		Node AddMethodInfo = new Node(new Node.Descriptor("AddMethod:"), new Node.Value(new Boolean(AddMethod)));
		root.addChild(AddMethodInfo);

		boolean UnrestrictedlyRedefineClasses = getBoolean(packet, index++);
		Node UnrestrictedlyRedefineClassesInfo = new Node(new Node.Descriptor("UnrestrictedlyRedefineClasses:"), new Node.Value(new Boolean(UnrestrictedlyRedefineClasses)));
		root.addChild(UnrestrictedlyRedefineClassesInfo);

		boolean PopFrames = getBoolean(packet, index++);
		Node PopFramesInfo = new Node(new Node.Descriptor("PopFrames:"), new Node.Value(new Boolean(PopFrames)));
		root.addChild(PopFramesInfo);

		boolean UseInstanceFilters = getBoolean(packet, index++);
		Node UseInstanceFiltersInfo = new Node(new Node.Descriptor("UseInstanceFilters:"), new Node.Value(new Boolean(UseInstanceFilters)));
		root.addChild(UseInstanceFiltersInfo);

		boolean GetSourceDebugExtension = getBoolean(packet, index++);
		Node GetSourceDebugExtensionInfo = new Node(new Node.Descriptor("GetSourceDebugExtension:"), new Node.Value(new Boolean(GetSourceDebugExtension)));
		root.addChild(GetSourceDebugExtensionInfo);

		boolean RequestVMDeathEvent = getBoolean(packet, index++);
		Node RequestVMDeathEventInfo = new Node(new Node.Descriptor("RequestVMDeathEvent:"), new Node.Value(new Boolean(RequestVMDeathEvent)));
		root.addChild(RequestVMDeathEventInfo);

		boolean SetDefaultStratum = getBoolean(packet, index++);
		Node SetDefaultStratumInfo = new Node(new Node.Descriptor("SetDefaultStratum:"), new Node.Value(new Boolean(SetDefaultStratum)));
		root.addChild(SetDefaultStratumInfo);

		return root;
	}
	
}
