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

import java.util.Hashtable;

/**
 * @author karel herink
 */
public class Request extends Packet {
    
    /* <ID, Packet> */
    private static Hashtable requests = new Hashtable();        
    private static int requestCount = 0;
    
    private int sequentialRequestNumber = 0;
    
    public Request(byte[] header, byte[] data, boolean incrementPacketCount) {
        super(header, data, incrementPacketCount);
        if (incrementPacketCount)
        	this.sequentialRequestNumber = requestCount++;
        Request.requests.put(new Integer(getId()), this);
    }

    public int getCmdSet() {
        return getUnsignedByte(header[9]);
    }
    
    public int getCmd() {
        return getUnsignedByte(header[10]);
    }
    public String toString() {
        return "[" + getSequentialRequestNumber() + "]  " + PacketAnalyzer.getStringTranslation(getCmdSet(), getCmd());
    }
    
    public String toLongString() {
        return ">> JDWP REQ [id = " + getId() + ", " + PacketAnalyzer.getStringTranslation(getCmdSet(), getCmd()) + ", packet # " + this.getSequentialPacketNumber() +" , req # " + this.getSequentialRequestNumber() + ", flag = " + getFlag() +  ", cmdSet = " + getCmdSet() + ", cmd = " + getCmd() + ", data = " + getDataLen() + "]";
    }

    public static Request getRequest(int id) {
        return (Request) Request.requests.get(new Integer(id));
    }
    
    public int getSequentialRequestNumber() {
        return this.sequentialRequestNumber;
    }
}
