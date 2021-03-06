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
import com.karelherink.jdwpanalyzer.model.constants.SuspendStatus;
import com.karelherink.jdwpanalyzer.model.constants.ThreadStatus;

import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_11_4 extends PacketAnalyzer {

    public Analyzer_11_4() {
    }

	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long objTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ObjectType objectType = ObjectType.getType(objTypeId);
		
		List nodes = this.getPacketInfo(packet).getChildren();
		Integer threadStatus = (Integer) ((Node) nodes.get(0)).getValue().getRealValue();
		objectType.setThreadStatus(threadStatus);
		Integer suspendStatus = (Integer) ((Node) nodes.get(0)).getValue().getRealValue();
		objectType.setSuspendStatus(suspendStatus);
	}
    
    public Node getPacketInfo(Packet packet) {
		Node root = new Node (null, null);
		int index = 0;
		
        int threadStatus = (int) getVal(packet, index, 4);
        index += 4;
		Node threadStatusInfo = new Node (new Node.Descriptor("ThreadStatus:"), new Node.Value(new Integer(threadStatus), ThreadStatus.asString(threadStatus)));
		root.addChild(threadStatusInfo);
		
        int suspendStatus = (int) getVal(packet, index, 4);
        index += 4;
		Node suspendStatusInfo = new Node (new Node.Descriptor("SuspendStatus:"), new Node.Value(new Integer(suspendStatus), SuspendStatus.asString(suspendStatus)));
		root.addChild(suspendStatusInfo);
		
		return root;
    }

}
