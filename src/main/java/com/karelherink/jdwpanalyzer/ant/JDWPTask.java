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

package com.karelherink.jdwpanalyzer.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import com.karelherink.jdwpanalyzer.model.JDWPProxy;

import java.io.IOException;
import java.net.Socket;

/**
 * @author karel herink
 */
public class JDWPTask extends Task {

    private static final int UNSET = -1;
    
    private int inPort = UNSET;
    private String outAddress;
    private int outPort = UNSET; /* GMC - superseded by 'outAddress', but here for backwards compatibility */
    private int requestDelay = 0;
    private int responseDelay = 0;
    
    private String logDir;
    private String inPortProperty;
    
    public int getInPort() {
        return this.inPort;
    }
    
    public void setInPort(int inport) {
        this.inPort = inport;
    }
    
    public int getOutPort() {
        return this.outPort;
    }
    
    public void setOutPort(int outport) {
        this.outPort = outport;
    }
    
    public String getOutAddress() {
        return this.outAddress;
    }

    public void setOutAddress(String outaddress) {
        this.outAddress = outaddress;
    }

    public int getRequestDelay() {
        return this.requestDelay;
    }

    public void setRequestDelay(int requestdelay) {
        this.requestDelay = requestdelay;
    }

    public int getResponseDelay() {
        return this.responseDelay;
    }

    public void setResponseDelay(int responsedelay) {
        this.responseDelay = responsedelay;
    }

	public String getInPortProperty() {
		return this.inPortProperty;
	}

	public void setInPortProperty(String inPortProperty) {
		this.inPortProperty = inPortProperty;
	}
    
	public String getLogDir() {
		return this.logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

   public void execute() throws BuildException {
	   
	   /* if both means of specifying a remote VM address are left unspecified ... error */
        if ( (this.outAddress == null) && (outPort == UNSET) ) {
        	throw new BuildException("Set outgoing port property 'outport' to the debug port number of the listening VM");
        } else {
        	if (outAddress == null) outAddress = String.valueOf(outPort);
        }
        
        if (this.inPort == UNSET) {
        	try {
        		this.inPort = this.determineFreePort();
			} catch (IOException e) {
				throw new BuildException(e);
			}
        }
        
        if (inPortProperty != null) {
        	this.getProject().setProperty(inPortProperty, Integer.toString(inPort));
        }
        
        try {
            new JDWPProxy(this.inPort, this.outAddress, this.requestDelay, this.responseDelay, this.logDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
    
    /**
     * Finds a free port to be used for listening for debugger connection.
     * @return free port number
     * @throws IOException
     */
    private int determineFreePort() throws IOException {
    	Socket sock = new Socket();
    	sock.bind(null);
    	int port = sock.getLocalPort();
    	sock.close();
    	return port;
    }


}
