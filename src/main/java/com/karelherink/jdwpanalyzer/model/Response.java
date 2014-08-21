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

import com.karelherink.jdwpanalyzer.model.constants.Error;

import java.util.Hashtable;

/**
 * @author karel herink
 */
public class Response extends Packet {

    /* <ID, Packet> */
    private static Hashtable responses = new Hashtable();  
    private static int responseCount = 0;
    
    private int sequentialResponseNumber = 0;

    public Response(byte[] header, byte[] data, boolean incrementPacketCount) {
        super(header, data, incrementPacketCount);
        if (incrementPacketCount)
        	this.sequentialResponseNumber = responseCount++;
        Response.responses.put(new Integer(getId()), this);
    }
    
    public boolean isError() {
    	return (getErrCode() != 0);
    }
    
    public int getErrCode() {
        return getVal(9, 2);
    }
    
    public String toString() {
        return Error.asString(this.getErrCode()) + "  [" + getSequentialResponseNumber() + "]";
    }

    public String toLongString() {
        return "<< JDWP RSP [id = " + getId() + ", packet # " + this.getSequentialPacketNumber() +" , resp # " + this.getSequentialResponseNumber() + ", flag = " + getFlag() +  ", err = " + getErrCode() + ", data = " + getDataLen() + "]";
    }
    
    public static Response getResponse(int id) {
        return (Response) Response.responses.get(new Integer(id));
    }

    public int getSequentialResponseNumber() {
        return this.sequentialResponseNumber;
    }
}
