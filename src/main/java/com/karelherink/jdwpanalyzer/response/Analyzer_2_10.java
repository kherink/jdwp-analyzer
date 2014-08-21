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

import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.*;
import com.karelherink.jdwpanalyzer.model.constants.TypeTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_2_10 extends PacketAnalyzer {

    public Analyzer_2_10() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long referenceTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ReferenceType referenceType = ReferenceType.getType(referenceTypeId);
		
		List interfacesRefTypeIds = new ArrayList();
		List interfacesInfo = this.getPacketInfo(packet).getChildren();
		
		for (int i = 0; i < interfacesInfo.size(); i++) {
			Node singleInterfaceInfo = (Node) interfacesInfo.get(i);
			Long interfaceRefTypeId = (Long) singleInterfaceInfo.getValue().getRealValue();
			interfacesRefTypeIds.add(interfaceRefTypeId);
			
			ReferenceType interfaceRefType = ReferenceType.getType(interfaceRefTypeId);
			interfaceRefType.setTypeTag(new Byte(TypeTag.INTERFACE));
		}
		referenceType.setInterfaceTypeIds((Long[]) interfacesRefTypeIds.toArray(new Long[interfacesRefTypeIds.size()]));
	}

    public Node getPacketInfo(Packet packet) {
		int index = 0;
				
		int numClasses = (int) getVal(packet, index, 4);
		index += 4;
		Node interfacesInfo = new Node (new Node.Descriptor("NumInterfaces:"), new Node.Value(new Integer(numClasses)));
		
		for (int i = 0; i < numClasses; i++) {
			long interfaceRefTypeId = getVal(packet, index, referenceTypeIDSize);
			index += referenceTypeIDSize;

			ReferenceType referenceType = ReferenceType.getType(interfaceRefTypeId);
			Node singleInterfaceInfo = new Node(new Node.Descriptor("RefTypeID:", referenceType), new Node.Value(new Long(interfaceRefTypeId)));
			interfacesInfo.addChild(singleInterfaceInfo);
		}
		return interfacesInfo;
	}

}
