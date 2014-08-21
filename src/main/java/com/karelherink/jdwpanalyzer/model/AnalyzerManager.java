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

import java.util.HashMap;

/**
 * @author  karel herink
 */
public class AnalyzerManager {
    
    /* <Key, PacketAnalyzer> */
    private static HashMap requestAnalyzers = new HashMap(40);    
    /* <Key, PacketAnalyzer> */
    private static HashMap responseAnalyzers = new HashMap(40);
    
    
    public static synchronized PacketAnalyzer createPacketAnalyzer(Packet packet) {
        String analyzerClassName = null;
        PacketAnalyzer packetAnalyzer = null;
        
        if (packet instanceof Request) {
            Request request = (Request) packet;
            int cmdSet = request.getCmdSet();
            int cmd = request.getCmd();
            Key key = new Key(cmdSet, cmd);
            if ((packetAnalyzer = (PacketAnalyzer) requestAnalyzers.get(key)) != null)
                return packetAnalyzer;
            analyzerClassName = "com.karelherink.jdwpanalyzer.request.Analyzer_" + cmdSet + "_" + cmd;
            packetAnalyzer = loadInstance(analyzerClassName);
            requestAnalyzers.put(key, packetAnalyzer);
            return packetAnalyzer;
        }
        Response response = (Response) packet;
        Request request = Request.getRequest(response.getId());
        int cmdSet = request.getCmdSet();
        int cmd = request.getCmd();
        Key key = new Key(cmdSet, cmd);
        if ((packetAnalyzer = (PacketAnalyzer) responseAnalyzers.get(key)) != null)
            return packetAnalyzer;
        analyzerClassName = "com.karelherink.jdwpanalyzer.response.Analyzer_" + cmdSet + "_" + cmd;
        packetAnalyzer = loadInstance(analyzerClassName);
        responseAnalyzers.put(key, packetAnalyzer);
        return packetAnalyzer;
    }
    
    //TODO : fix the way exceptions and handled - this is ridiculous :)
    private static PacketAnalyzer loadInstance(String analyzerClassName) {
        PacketAnalyzer packetAnalyzer = null;
        try {
        	//System.out.println("--- Looking for : " + analyzerClassName);
            packetAnalyzer = (PacketAnalyzer) Class.forName(analyzerClassName).newInstance();
            //System.out.println("--- Found : " + analyzerClassName);
        } catch (InstantiationException e) {
            e.printStackTrace();
            //System.out.println("--- Creating the default PacketAnalyzer (InstantiationException) ---");
            packetAnalyzer = new PacketAnalyzer();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            //System.out.println("--- Creating the default PacketAnalyzer (IllegalAccessException) ---");
            packetAnalyzer = new PacketAnalyzer();
        } catch (ClassNotFoundException e) {
            //System.out.println("--- PacketAnalyzer subclass " + analyzerClassName + " not found ---");
            //System.out.println("--- Creating the default PacketAnalyzer ---");
            packetAnalyzer = new PacketAnalyzer();
        }
        return packetAnalyzer;
    }
}
