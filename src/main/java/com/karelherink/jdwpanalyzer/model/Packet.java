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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created on December 5, 2002, 11:53 AM
 *
 * @author Martin Ryzl, Karel Herink
 */
public class Packet {
    
    private static long packetCount = 0;
    
    private long sequentialPacketNumber;
    
    protected byte[] header;
    protected byte[] data;
    
    /** Creates a new instance of PacketParser */
    protected Packet(byte[] header, byte[] data, boolean incrementPacketCount) {
        this.header = header;
        this.data = data;
        if (incrementPacketCount)
        	this.sequentialPacketNumber = packetCount++;
    }
    
    public static Packet readPacket(DataInputStream is, boolean incrementPacketCount) throws IOException {
        Packet packet;
        byte header[] = new byte[11];
        is.readFully(header);
        
        byte[] data = new byte[getDataLenght(header)];
        is.readFully(data);

        if (Packet.isResponse(header)) {
            packet = new Response(header, data, incrementPacketCount);
        }
        else {
            packet = new Request(header, data, incrementPacketCount);
        }
        
        return packet;
    }
    
    public static void writePacket(Packet p, DataOutputStream os) throws IOException {
        os.write(p.header);
        os.write(p.data);
    }
    
    public int getDataLen() {
        return getLength() - 11;
    }
    
    public boolean isResponse() {
        return (getFlag() & 0x80) > 0;
    }
    
    public int getFlag() {
        return getUnsignedByte(header[8]);
    }
    
    public int getId() {
        return getVal(4, 4);
    }
    
    public int getLength() {
        return getVal(0, 4);
    }
    
    int getVal(int index, int n) {
        int len = 0;
        int last = index + n;
        for(int i = index; i < last; i++) {
            len <<= 8;
            len += getUnsignedByte(header[i]);
        }
        return len;
    }
    
    int getDataVal(int index, int n) {
        int len = 0;
        int last = index + n;
        for(int i = index; i < last; i++) {
            len <<= 8;
            len += data[i] & 0xff;
        }
        return len;
    }
    
    protected int getUnsignedByte(byte b) {
        return b & 0xff;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public long getSequentialPacketNumber() {
        return this.sequentialPacketNumber;
    }
    
    //static helpers
    private static boolean isResponse(byte[] header) {
        return ((((header[8]) & 0xff) & 0x80) > 0);
    }
    private static int getDataLenght(byte[] header) {
        return getVal(header, 0, 4) - 11;
    }
    private static int getVal(byte[] header, int index, int n) {
        int len = 0;
        int last = index + n;
        for(int i = index; i < last; i++) {
            len <<= 8;
            len += header[i] & 0xff;
        }
        return len;
    }
    
    public String toLongString() {
        return "Packet #" + this.sequentialPacketNumber;
    }
}
