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

package com.karelherink.jdwpanalyzer.model;

import com.karelherink.jdwpanalyzer.logger.PacketLogReader;
import com.karelherink.jdwpanalyzer.logger.PacketLogWriter;
import com.karelherink.jdwpanalyzer.ui.View;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


/**
 *
 * @author  karel herink
 */
public class JDWPProxy {
	
	private PacketLogWriter packetLogWriter;
    
    /* References to Request objects in the same seqence they were received */
    static List sequentialRequests = Collections.synchronizedList(new ArrayList());
    
    private View view;

	private Socket socketVM;
	private Socket socketDebugger;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NumberFormatException, IOException, UnknownHostException {
        if (args.length != 4 && args.length != 5 && args.length != 1) {
            System.err.println("\nUsage: java -jar JDWPAnalyzer.jar (<inPort> <outAddress> <reqDelay> <respDelay> [<log_dir>]) | <log_dir>\n\n" +
            		"inPort     = port to which debugger will connect\n" +
            		"outAddress = address as 'host:port' on which the remote VM is accepting debugger connections.\n" +
            		"             localhost addresses can just specify a port - no host required\n" +
            		"reqDelay   = num MS to wait before passing each request to VM\n" +
            		"respDelay  = num MS to wait before passing each response to debugger\n" +
            		"log_dir    = log directory path for writing or reading\n");
            System.exit(1);
        }
        if (args.length >= 4) {
        	int inPort = Integer.parseInt(args[0]);
        	String outAddress = args[1];
        	int reqDelay = Integer.parseInt(args[2]);
        	int respDelay = Integer.parseInt(args[3]);
        	if (args.length == 5) {
        		String logFile = args[4];
        		new JDWPProxy(inPort, outAddress, reqDelay, respDelay, logFile);
        	}
        	else
        		new JDWPProxy(inPort, outAddress, reqDelay, respDelay, null);
        }
        else {
        	new JDWPProxy(args[0]);
        }
        
    }
    
    public JDWPProxy(int inPort, String outAddress, int reqDelay, int respDelay, String logDir) throws IOException {
    	this.activeMode(inPort, outAddress, reqDelay, respDelay, logDir);
    }
    
    public JDWPProxy(String logDir) throws IOException {
    	this.passiveMode(logDir);
    }
    
    /**
     * In passive mode only use 1 thread for reading packets from files. Since logged packets
     * are already in sequence (sorted out by PacketLogreader) reading them with 2 threads
     * only messes things up, ie. packets are out of sequence unless some more syncing
     * is put in place.
     * 
     * @param logDir
     * @throws IOException
     */
    private void passiveMode(String logDir) throws IOException {
    	PipedInputStream pin = new PipedInputStream();
    	PipedOutputStream pout = new PipedOutputStream();
    	pin.connect(pout);
    	DataInputStream din = new DataInputStream(pin);
    	DataOutputStream dos = new DataOutputStream(pout);
    	
		T t = new T(din, null, 0);
		t.setName("Sequential reader");
        System.out.println("Starting replay..");
        
        view = new View(this, JDWPProxy.sequentialRequests);

        t.setDaemon(true);
        t.start();
    	
    	PacketLogReader packetLogReader = new PacketLogReader(logDir, dos);
    	packetLogReader.setDaemon(true);
    	packetLogReader.start();
    }
    
    private void activeMode(int inPort, String outAddress, int reqDelay, int respDelay, String logDir)  throws IOException {
    	if (logDir != null)
    		this.initLogWriter(logDir);
    	
        ServerSocket serverSock = new ServerSocket(inPort);
        System.out.println("JDWPProxy: waiting for connection on port " + inPort + "..");
        socketDebugger = serverSock.accept();
        
        /* if the address contains a ':', the address has a host - otherwise it is considered to be just a port */
		if (outAddress.indexOf(':') == -1 ) {
			/* no address specified, just use the port with the local address */
			System.out.println("JDWPProxy: Connection received, connecting to VM on port " + outAddress + "..");
	        socketVM = new Socket(InetAddress.getLocalHost(), Integer.valueOf(outAddress));
		} else {
			/* host is given - assume format of "host:port" */
			System.out.println("JDWPProxy: Connection received, connecting to VM at address " + outAddress + "..");
			StringTokenizer st = new StringTokenizer(outAddress, ":");
			String host = st.nextToken();
			int port = Integer.valueOf(st.nextToken());
	        socketVM = new Socket(host, port);
		}
        
		T dbToVm = new T(new DataInputStream(socketDebugger.getInputStream()), new DataOutputStream(socketVM.getOutputStream()), reqDelay);
		T vmToDb = new T(new DataInputStream(socketVM.getInputStream()), new DataOutputStream(socketDebugger.getOutputStream()), respDelay);
		dbToVm.setName("=> DEBUGGER TO VM");
        vmToDb.setName("<= VM TO DEBUGGER");
        
        System.out.println("JDWPProxy: Connected Starting communication..");
        
        view = new View(this, JDWPProxy.sequentialRequests);
        
        dbToVm.shakeHands();
        vmToDb.shakeHands();
        dbToVm.setDaemon(true);
        vmToDb.setDaemon(true);
        dbToVm.start();
        vmToDb.start();
    }
    
    private void initLogWriter(String logDir) throws IOException {
    	this.packetLogWriter = new PacketLogWriter(logDir);
    	this.packetLogWriter.setDaemon(true);
    	this.packetLogWriter.setPriority(Thread.MIN_PRIORITY);
    	this.packetLogWriter.start();
    }
    
    public void shutDown() {
    	try {
    		System.out.println("Shut down.");
    		if (this.socketDebugger != null)
    			this.socketDebugger.close();
    		if (this.socketVM != null)
    			this.socketVM.close();
			if (this.packetLogWriter != null)
				this.packetLogWriter.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }
    
    private class T extends Thread {
        private DataInputStream dis;
        private DataOutputStream dos;
        private int delay;
        
        public T(DataInputStream dis, DataOutputStream dos, int delay) {
        	this.dis = dis;
        	this.dos = dos;
            this.delay = delay;
        }
        
        private void shakeHands() throws IOException {
            byte[] handShake = new byte[14];
            dis.readFully(handShake);
            for(int i = 0; i < handShake.length; i++) {
                System.out.print((char) handShake[i]);
            }
            System.out.println();
            dos.write(handShake);
        }
        
        public void run() {
            try {
                //main processing loop
                while (true) {
                    Packet packet = Packet.readPacket(dis, true);
                    
                    if (JDWPProxy.this.packetLogWriter != null)
                    	JDWPProxy.this.packetLogWriter.requestPacketLog(packet);
                    
                    PacketAnalyzer packetAnalyzer = AnalyzerManager.createPacketAnalyzer(packet);
                    
                    if (packet instanceof Request) {
                        packetAnalyzer.updateInternalDataModel(packet);
                        JDWPProxy.sequentialRequests.add(packet);
                    }
                	if (packet instanceof Response) {
            			Response resp = (Response) packet;
            			if (!resp.isError())
                            packetAnalyzer.updateInternalDataModel(packet);
            		}
                    JDWPProxy.this.view.updateView(packet);
                    if (dos != null)
                    	Packet.writePacket(packet, dos);
                    this.doDelay(delay);
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (dis != null) dis.close();
                } catch (IOException e2) {}
                try {
                    if (dos != null) dos.close();
                } catch (IOException e2) {}
            }
        }
        
        private void doDelay(int delay) {
        	try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
    
}
