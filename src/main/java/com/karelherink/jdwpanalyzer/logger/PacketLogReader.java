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

package com.karelherink.jdwpanalyzer.logger;

import com.karelherink.jdwpanalyzer.model.Packet;

import java.io.*;

/**
 * @author karel herink
 */
public class PacketLogReader extends Thread {
	
	private DataInputStream disSeq;
	private DataOutputStream dos;
	
	public PacketLogReader(String logDirPath, DataOutputStream dos) throws IOException {
		this.dos = dos;
		File logDir = new File(logDirPath);
		if (!logDir.exists())
			throw new IllegalArgumentException("Log directory does NOT exist: " + logDirPath);
		
		File seqLogFile = new File(logDir + File.separator + PacketLogWriter.LOG_SEQ_FILE);
		
		this.disSeq = new DataInputStream(new BufferedInputStream(new FileInputStream(seqLogFile)));
	}

	public void run() {
		while (!interrupted()) {
			try {
				Packet p = Packet.readPacket(disSeq, false);
				Packet.writePacket(p, dos);
			} catch (IOException e) {
				e.printStackTrace();
				this.interrupt();
			}
		}
		System.out.println("Reader interrupted!!");
	}
	
}
