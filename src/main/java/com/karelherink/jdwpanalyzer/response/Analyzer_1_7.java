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
public class Analyzer_1_7 extends PacketAnalyzer {

    public Analyzer_1_7() {
    }
    
    public void updateInternalDataModel(Packet packet) {
        int[] sizes = this.getSizes(packet);
        PacketAnalyzer.setIDSizes(sizes);
    }
    
	public Node getPacketInfo(Packet packet) {
		Node root = new Node(null, null);
		
		int fieldSize = (int) getVal(packet, 0, 4);
		Node fieldSizeInfo = new Node (new Node.Descriptor("Field:"), new Node.Value(new Integer(fieldSize)));
		root.addChild(fieldSizeInfo);

		int methSize = (int) getVal(packet, 4, 4);
		Node methSizeInfo = new Node (new Node.Descriptor("Method:"), new Node.Value(new Integer(methSize)));
		root.addChild(methSizeInfo);

		int objSize = (int) getVal(packet, 8, 4);
		Node objSizeInfo = new Node (new Node.Descriptor("Object:"), new Node.Value(new Integer(objSize)));
		root.addChild(objSizeInfo);

		int refSize = (int) getVal(packet, 12, 4);
		Node refSizeInfo = new Node (new Node.Descriptor("RefType:"), new Node.Value(new Integer(refSize)));
		root.addChild(refSizeInfo);

		int frameSize = (int) getVal(packet, 16, 4);
		Node frameSizeInfo = new Node (new Node.Descriptor("Frame:"), new Node.Value(new Integer(frameSize)));
		root.addChild(frameSizeInfo);

		return root;
	}
		
	private synchronized int[] getSizes(Packet packet) {
        int[] sizes = new int[] {
        		(int) getVal(packet, 0, 4),
        		(int) getVal(packet, 4, 4),
        		(int) getVal(packet, 8, 4),
        		(int) getVal(packet, 12, 4),
        		(int) getVal(packet, 16, 4),
            };
        return sizes;
    }
    
}
