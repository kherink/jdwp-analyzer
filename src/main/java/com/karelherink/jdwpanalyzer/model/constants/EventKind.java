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
public class EventKind {

    public static final byte VM_DISCONNECTED = 100; //Never sent by across JDWP  
    public static final byte VM_START = 90;
    public static final byte THREAD_DEATH = 7;
    public static final byte SINGLE_STEP = 1;
    public static final byte BREAKPOINT = 2;
    public static final byte FRAME_POP = 3; //not used in JDWP
    public static final byte EXCEPTION = 4;
    public static final byte USER_DEFINED = 5; //not used in JDWP
    public static final byte THREAD_START = 6;
    public static final byte THREAD_END = THREAD_DEATH;
    public static final byte CLASS_PREPARE = 8;
    public static final byte CLASS_UNLOAD = 9;
    public static final byte CLASS_LOAD = 10; //not used in JDWP
    public static final byte FIELD_ACCESS = 20;
    public static final byte FIELD_MODIFICATION = 21;
    public static final byte EXCEPTION_CATCH = 30; //not used in JDWP
    public static final byte METHOD_ENTRY = 40;
    public static final byte METHOD_EXIT = 41;
    public static final byte VM_INIT = VM_START;
    public static final byte VM_DEATH = 99;
    
    public static String asString(byte eventKindCode) {
        switch (eventKindCode) {
            case VM_DISCONNECTED:
                return "VM_DISCONNECTED";
            case VM_START:
                return "VM_START";
            case THREAD_DEATH:
                return "THREAD_DEATH";
            case SINGLE_STEP:
                return "SINGLE_STEP";
            case BREAKPOINT:
                return "BREAKPOINT";
            case FRAME_POP:
                return "FRAME_POP";
            case EXCEPTION:
                return "EXCEPTION";
            case USER_DEFINED:
                return "USER_DEFINED";
            case THREAD_START:
                return "THREAD_START";
            case CLASS_PREPARE:
                return "CLASS_PREPARE";
            case CLASS_UNLOAD:
                return "CLASS_UNLOAD";
            case CLASS_LOAD:
                return "CLASS_LOAD";
            case FIELD_ACCESS:
                return "FIELD_ACCESS";
            case FIELD_MODIFICATION:
                return "FIELD_MODIFICATION";
            case EXCEPTION_CATCH:
                return "EXCEPTION_CATCH";
            case METHOD_ENTRY:
                return "METHOD_ENTRY";
            case METHOD_EXIT:
                return "METHOD_EXIT";
            case VM_DEATH:
                return "VM_DEATH";

            default:
                return "UNKNOWN";
        }
    }

    
}
