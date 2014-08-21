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
public class ClassStatus {
    
    public static final byte VERIFIED = 1;
    public static final byte PREPARED = 2;
    public static final byte INITIALIZED = 4;
    public static final byte ERROR = 8;

    public static String asString(int statusCode) {
        switch (statusCode) {
            case VERIFIED:
                return "VERIFIED";
            case PREPARED:
                return "PREPARED";
            case INITIALIZED:
                return "INITIALIZED";
            case ERROR:
                return "ERROR";

            default:
                return "UNKNOWN";
        }
    }

}
