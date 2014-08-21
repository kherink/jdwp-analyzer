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
public class ThreadStatus {

    public static final byte ZOMBIE = 0;
    public static final byte RUNNING = 1;
    public static final byte SLEEPING = 2;
    public static final byte MONITOR = 3;
    public static final byte WAIT = 4;
  
	public static String asString(int status) {
		switch (status) {
		case ZOMBIE:
			return "ZOMBIE";
		case RUNNING:
			return "RUNNING";
		case SLEEPING:
			return "SLEEPING";
		case MONITOR:
			return "MONITOR";
		case WAIT:
			return "WAIT";
			
		default:
			return "UNKNOWN";
		}
	}


}
