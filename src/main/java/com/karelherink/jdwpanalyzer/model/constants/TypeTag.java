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
public class TypeTag {

    public static final byte CLASS = 1; //ReferenceType is a class.
    public static final byte INTERFACE = 2; //ReferenceType is an interface.
    public static final byte ARRAY = 3; //ReferenceType is an array.
    
    public static String asString(byte typeTagCode) {
        switch (typeTagCode) {
            case CLASS:
                return "CLASS";
            case INTERFACE:
                return "INTERFACE";
            case ARRAY:
                return "ARRAY";

            default:
                return "UNKNOWN";
        }
    }
}
