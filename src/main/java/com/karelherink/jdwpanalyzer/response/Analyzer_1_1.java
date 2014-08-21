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
public class Analyzer_1_1 extends PacketAnalyzer {

	public Analyzer_1_1() {
	}

	public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
		
		int len = (int) getVal(packet, index, 4);
		index += 4;
		String desc = getStr(packet, index, len);
		index += len;
		Node descInfo = new Node(new Node.Descriptor("Description:"), new Node.Value(desc));
		root.addChild(descInfo);

		int major = (int) getVal(packet, index, 4);
		index += 4;
		Node majorInfo = new Node(new Node.Descriptor("JDWP Major:"), new Node.Value(new Integer(major)));
		root.addChild(majorInfo);

		int minor = (int) getVal(packet, index, 4);
		index += 4;
		Node minorInfo = new Node(new Node.Descriptor("JDWP Minor:"), new Node.Value(new Integer(minor)));
		root.addChild(minorInfo);

		len = (int) getVal(packet, index, 4);
		index += 4;
		String version = getStr(packet, index, len);
		index += len;
		Node versionInfo = new Node(new Node.Descriptor("VM Version:"), new Node.Value(version));
		root.addChild(versionInfo);

		len = (int) getVal(packet, index, 4);
		index += 4;
		String name = getStr(packet, index, len);
		index += len;
		Node nameInfo = new Node(new Node.Descriptor("VM Name:"), new Node.Value(name));
		root.addChild(nameInfo);
		
		
		return root;
	}

}
