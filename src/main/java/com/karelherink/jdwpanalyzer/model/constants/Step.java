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

package com.karelherink.jdwpanalyzer.model.constants;

/**
 * @author karel herink
 */
public class Step {

    public static final byte DEPTH_INTO = 0; //Step into any method calls that occur before the end of the step.  
    public static final byte DEPTH_OVER = 1;  //Step over any method calls that occur before the end of the step.  
    public static final byte DEPTH_OUT = 2; //Step out of the current method.  
    
    public static final byte SIZE_MIN = 0; //Step by the minimum possible amount (often a bytecode instruction).  
    public static final byte SIZE_LINE = 1; //Step to the next source line.  

    public static String depthAsString(int depthCode) {
    	switch (depthCode) {
			case DEPTH_INTO:
				return "DEPTH_INTO";
			case DEPTH_OVER:
				return "DEPTH_OVER";
			case DEPTH_OUT:
				return "DEPTH_OUT";
			default:
				return "UNKNOWN";
		}
    }

    public static String sizeAsString(int sizeCode) {
    	switch (sizeCode) {
			case SIZE_MIN:
				return "SIZE_MIN";
			case SIZE_LINE:
				return "SIZE_LINE";
			default:
				return "UNKNOWN";
		}
    }

}
