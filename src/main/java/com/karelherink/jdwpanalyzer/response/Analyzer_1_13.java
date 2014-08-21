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
public class Analyzer_1_13 extends PacketAnalyzer {

    public Analyzer_1_13() {
    }
    
	public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
		
        int len = (int) getVal(packet, index, 4);
        index += 4;
		String basedir = getStr(packet, index, len);
        index += len;
		Node basedirInfo = new Node (new Node.Descriptor("BaseDir:"), new Node.Value(basedir));
        root.addChild(basedirInfo);

        int numClassPaths = (int) getVal(packet, index, 4);
        index += 4;
		Node classPathsInfo = new Node (new Node.Descriptor("NumClassPaths:"), new Node.Value(new Integer(numClassPaths)));
		
		for(int i = 0; i < numClassPaths; i++) {
	        len = (int) getVal(packet, index, 4);
	        index += 4;
			String classPath = getStr(packet, index, len);
	        index += len;
			Node classPathInfo = new Node (new Node.Descriptor("ClassPath:"), new Node.Value(classPath));
			classPathsInfo.addChild(classPathInfo);
		}
		root.addChild(classPathsInfo);
		
        int numBootClassPaths = (int) getVal(packet, index, 4);
        index += 4;
		Node bootClassPathsInfo = new Node (new Node.Descriptor("NumBootClassPaths:"), new Node.Value(new Integer(numBootClassPaths)));
		
		for(int i = 0; i < numBootClassPaths; i++) {
	        len = (int) getVal(packet, index, 4);
	        index += 4;
			String bootClassPath = getStr(packet, index, len);
	        index += len;
			Node bootClassPathInfo = new Node (new Node.Descriptor("BootClassPath:"), new Node.Value(bootClassPath));
			classPathsInfo.addChild(bootClassPathInfo);
		}
		root.addChild(bootClassPathsInfo);

		return root;
    }
	
}
