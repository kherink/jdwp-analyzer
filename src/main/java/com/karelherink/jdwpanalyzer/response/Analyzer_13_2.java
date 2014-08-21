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
public class Analyzer_13_2 extends PacketAnalyzer {

    public Analyzer_13_2() {
    }
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
		
		Value[] values = getArrayRegion(packet, index);
		for (int i = 0; i < values.length; i++) {
			Node valueInfo = new Node (new Node.Descriptor("Value:"), new Node.Value(values[i].getValue()));
			root.addChild(valueInfo);
		}
		
		return root;
    }

}
