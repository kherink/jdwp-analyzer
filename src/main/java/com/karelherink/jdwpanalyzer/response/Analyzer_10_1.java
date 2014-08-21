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

import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.model.*;

/**
 * @author karel herink
 */
public class Analyzer_10_1 extends PacketAnalyzer {

    public Analyzer_10_1() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long objTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ObjectType objectType = ObjectType.getType(objTypeId);
		
		Node node = this.getPacketInfo(packet);
		String value = (String) node.getValue().getRealValue();
		objectType.setStringValue(value);
	}    
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		
        int len = (int) getVal(packet, index, 4);
        index += 4;
		String value = getStr(packet, index, len);
        index += len;
		Node root = new Node (new Node.Descriptor("StringValue:"), new Node.Value(value));
		
		return root;
    }

}
