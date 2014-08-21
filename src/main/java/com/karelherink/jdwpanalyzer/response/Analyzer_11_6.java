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
public class Analyzer_11_6 extends PacketAnalyzer {

    public Analyzer_11_6() {
    }
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		
        int numFrames = (int) getVal(packet, index, 4);
        index += 4;
		Node root = new Node (new Node.Descriptor("NumFrames:"), new Node.Value(new Integer(numFrames)));
		
		for(int i = 0; i < numFrames; i++) {
			
			long frameId = getVal(packet, index, frameIDSize);
			index += frameIDSize;
			Node frameIdInfo = new Node (new Node.Descriptor("FrameID:"), new Node.Value(new Long(frameId)));
			root.addChild(frameIdInfo);
			Location location = getLocation(packet, index);
            index += locationSize;
            Node locationInfo = new Node (new Node.Descriptor("Location:"), new Node.Value(location));
            root.addChild(locationInfo);
		}
		return root;
    }
}
