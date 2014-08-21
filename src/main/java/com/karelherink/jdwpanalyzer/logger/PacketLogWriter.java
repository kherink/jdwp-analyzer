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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;


/**
 * @author karel herink
 */
public class PacketLogWriter extends Thread {

	static final String LOG_SEQ_FILE = "seq.log";
	
	private LinkedList packetLogRequestQueue;
	private DataOutputStream dosSeq;
	
	public PacketLogWriter(String logDir) throws IOException {
		this.createFiles(logDir);
		this.packetLogRequestQueue = new LinkedList();
	}

	private void createFiles(String logDirPath) throws IOException {
		File logDir = new File(logDirPath);
		if (!logDir.exists() && !logDir.mkdirs())
			throw new IllegalArgumentException("Couldn't create directory: " + logDirPath);
		
		File seqLogFile = new File(logDir + File.separator + LOG_SEQ_FILE);
		seqLogFile.delete();
		seqLogFile.createNewFile();
		this.dosSeq = new DataOutputStream((new FileOutputStream(seqLogFile)));
	}
	
	public void requestPacketLog(Packet packet) {
		synchronized (this.packetLogRequestQueue) {
			this.packetLogRequestQueue.addFirst(packet);
			//System.out.println("packet log queue added: " + packet.toLongString());
			this.packetLogRequestQueue.notifyAll();
		}
	}
	
	public void run() {
		while (!interrupted()) {
			synchronized (this.packetLogRequestQueue) {
				while(this.packetLogRequestQueue.size() == 0) {
					try {
						this.packetLogRequestQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						this.interrupt();
						break;
					}
				}
				while (this.packetLogRequestQueue.size() > 0) {
					Packet p = (Packet) this.packetLogRequestQueue.removeLast();
					try {
						Packet.writePacket(p, dosSeq);
					} catch (IOException ioe) {
						ioe.printStackTrace();
						interrupt();
						break;
					}
				}
			}
		}
		System.out.println("Interrupted - shutting down logger.");
		try {
			this.dosSeq.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
