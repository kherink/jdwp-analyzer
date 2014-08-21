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
public class Tag {

	public static final byte ARRAY = 91; // '[' - an array object (objectID size).
	public static final byte BYTE = 66; // 'B' - a byte value (1 byte).
	public static final byte CHAR = 67; // 'C' - a character value (2 bytes).
	public static final byte OBJECT = 76; // 'L' - an object (objectID size).
	public static final byte FLOAT = 70; // 'F' - a float value (4 bytes).
	public static final byte DOUBLE = 68; // 'D' - a double value (8 bytes).
	public static final byte INT = 73; // 'I' - an int value (4 bytes).
	public static final byte LONG = 74; // 'J' - a long value (8 bytes).
	public static final byte SHORT = 83; // 'S' - a short value (2 bytes).
	public static final byte VOID = 86; // 'V' - a void value (no bytes).
	public static final byte BOOLEAN = 90; // 'Z' - a boolean value (1 byte).
	public static final byte STRING = 115; // 's' - a String object (objectID size).
	public static final byte THREAD = 116; // 't' - a Thread object (objectID size).
	public static final byte THREAD_GROUP = 103; // 'g' - a ThreadGroup object (objectID size).
	public static final byte CLASS_LOADER = 108; // 'l' - a ClassLoader object (objectID size).
	public static final byte CLASS_OBJECT = 99; // 'c' - a class object object (objectID size).

	public static String asString(int tagCode) {
		switch (tagCode) {
			case ARRAY:
				return "ARRAY";
			case BYTE:
				return "BYTE";
			case CHAR:
				return "CHAR";
			case OBJECT:
				return "OBJECT";
			case FLOAT:
				return "FLOAT";
			case DOUBLE:
				return "DOUBLE";
			case INT:
				return "INT";
			case LONG:
				return "LONG";
			case SHORT:
				return "SHORT";
			case VOID:
				return "VOID";
			case BOOLEAN:
				return "BOOLEAN";
			case STRING:
				return "STRING";
			case THREAD:
				return "THREAD";
			case THREAD_GROUP:
				return "THREAD_GROUP";
			case CLASS_LOADER:
				return "CLASS_LOADER";
			case CLASS_OBJECT:
				return "CLASS_OBJECT";

            default:
                return "UNKNOWN";
		}
	}
	
	public static boolean isPrimitive(int tagCode) {
		switch (tagCode) {
			case BYTE:
			case CHAR:
			case FLOAT:
			case DOUBLE:
			case INT:
			case LONG:
			case SHORT:
			case VOID:
			case BOOLEAN:
				return true;
			default:
				return false;
		}
	}

}
