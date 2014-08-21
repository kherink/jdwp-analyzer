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
public class Error {

	public static final short INVALID_TAG = 500; //object type id or class tag
	public static final short ALREADY_INVOKING = 502; //previous invoke not complete
	public static final short INVALID_INDEX = 503;
	public static final short INVALID_LENGTH = 504;
	public static final short INVALID_STRING = 506;
	public static final short INVALID_CLASS_LOADER = 507;
	public static final short INVALID_ARRAY = 508;
	public static final short TRANSPORT_LOAD = 509;
	public static final short TRANSPORT_INIT = 510;
	public static final short NATIVE_METHOD = 511;
	public static final short INVALID_COUNT = 512;
	public static final short NONE = 0;
	public static final short INVALID_THREAD = 10;
	public static final short INVALID_THREAD_GROUP = 11;
	public static final short INVALID_PRIORITY = 12;
	public static final short THREAD_NOT_SUSPENDED = 13;
	public static final short THREAD_SUSPENDED = 14;
	public static final short INVALID_OBJECT = 20;
	public static final short INVALID_CLASS = 21;
	public static final short CLASS_NOT_PREPARED = 22;
	public static final short INVALID_METHODID = 23;
	public static final short INVALID_LOCATION = 24;
	public static final short INVALID_FIELDID = 25;
	public static final short INVALID_FRAMEID = 30;
	public static final short NO_MORE_FRAMES = 31;
	public static final short OPAQUE_FRAME = 32;
	public static final short NOT_CURRENT_FRAME = 33;
	public static final short TYPE_MISMATCH = 34;
	public static final short INVALID_SLOT = 35;
	public static final short DUPLICATE = 40;
	public static final short NOT_FOUND = 41;
	public static final short INVALID_MONITOR = 50;
	public static final short NOT_MONITOR_OWNER = 51;
	public static final short INTERRUPT = 52;
	public static final short INVALID_CLASS_FORMAT = 60;
	public static final short CIRCULAR_CLASS_DEFINITION = 61;
	public static final short FAILS_VERIFICATION = 62;
	public static final short ADD_METHOD_NOT_IMPLEMENTED = 63;
	public static final short SCHEMA_CHANGE_NOT_IMPLEMENTED = 64;
	public static final short INVALID_TYPESTATE = 65;
	public static final short NOT_IMPLEMENTED = 99;
	public static final short NULL_POINTER = 100;
	public static final short ABSENT_INFORMATION = 101;
	public static final short INVALID_EVENT_TYPE = 102;
	public static final short ILLEGAL_ARGUMENT = 103;
	public static final short OUT_OF_MEMORY = 110;
	public static final short ACCESS_DENIED = 111;
	public static final short VM_DEAD = 112;
	public static final short INTERNAL = 113;
	public static final short UNATTACHED_THREAD = 115;

	public static String asString(int errCode) {
		return errCode == 0 ? "OK" : "ERR : " + getStringTranslation(errCode);
	}

	private static String getStringTranslation(int errCode) {
		switch (errCode) {
			case INVALID_TAG:
				return "INVALID_TAG";
			case ALREADY_INVOKING:
				return "ALREADY_INVOKING";
			case INVALID_INDEX:
				return "INVALID_INDEX";
			case INVALID_LENGTH:
				return "INVALID_LENGTH";
			case INVALID_STRING:
				return "INVALID_STRING";
			case INVALID_CLASS_LOADER:
				return "INVALID_CLASS_LOADER";
			case INVALID_ARRAY:
				return "INVALID_ARRAY";
			case TRANSPORT_LOAD:
				return "TRANSPORT_LOAD";
			case TRANSPORT_INIT:
				return "TRANSPORT_INIT";
			case NATIVE_METHOD:
				return "NATIVE_METHOD";
			case INVALID_COUNT:
				return "INVALID_COUNT";
			case NONE:
				return "NONE";
			case INVALID_THREAD:
				return "INVALID_THREAD";
			case INVALID_THREAD_GROUP:
				return "INVALID_THREAD_GROUP";
			case INVALID_PRIORITY:
				return "INVALID_PRIORITY";
			case THREAD_NOT_SUSPENDED:
				return "THREAD_NOT_SUSPENDED";
			case THREAD_SUSPENDED:
				return "THREAD_SUSPENDED";
			case INVALID_OBJECT:
				return "INVALID_OBJECT";
			case INVALID_CLASS:
				return "INVALID_CLASS";
			case CLASS_NOT_PREPARED:
				return "CLASS_NOT_PREPARED";
			case INVALID_METHODID:
				return "INVALID_METHODID";
			case INVALID_LOCATION:
				return "INVALID_LOCATION";
			case INVALID_FIELDID:
				return "INVALID_FIELDID";
			case INVALID_FRAMEID:
				return "INVALID_FRAMEID";
			case NO_MORE_FRAMES:
				return "NO_MORE_FRAMES";
			case OPAQUE_FRAME:
				return "OPAQUE_FRAME";
			case NOT_CURRENT_FRAME:
				return "NOT_CURRENT_FRAME";
			case TYPE_MISMATCH:
				return "TYPE_MISMATCH";
			case INVALID_SLOT:
				return "INVALID_SLOT";
			case DUPLICATE:
				return "DUPLICATE";
			case NOT_FOUND:
				return "NOT_FOUND";
			case INVALID_MONITOR:
				return "INVALID_MONITOR";
			case NOT_MONITOR_OWNER:
				return "NOT_MONITOR_OWNER";
			case INTERRUPT:
				return "INTERRUPT";
			case INVALID_CLASS_FORMAT:
				return "INVALID_CLASS_FORMAT";
			case CIRCULAR_CLASS_DEFINITION:
				return "CIRCULAR_CLASS_DEFINITION";
			case FAILS_VERIFICATION:
				return "FAILS_VERIFICATION";
			case ADD_METHOD_NOT_IMPLEMENTED:
				return "ADD_METHOD_NOT_IMPLEMENTED";
			case SCHEMA_CHANGE_NOT_IMPLEMENTED:
				return "SCHEMA_CHANGE_NOT_IMPLEMENTED";
			case INVALID_TYPESTATE:
				return "INVALID_TYPESTATE";
			case NOT_IMPLEMENTED:
				return "NOT_IMPLEMENTED";
			case NULL_POINTER:
				return "NULL_POINTER";
			case ABSENT_INFORMATION:
				return "ABSENT_INFORMATION";
			case INVALID_EVENT_TYPE:
				return "INVALID_EVENT_TYPE";
			case ILLEGAL_ARGUMENT:
				return "ILLEGAL_ARGUMENT";
			case OUT_OF_MEMORY:
				return "OUT_OF_MEMORY";
			case ACCESS_DENIED:
				return "ACCESS_DENIED";
			case VM_DEAD:
				return "VM_DEAD";
			case INTERNAL:
				return "INTERNAL";
			case UNATTACHED_THREAD:
				return "UNATTACHED_THREAD";
			default:
				return "UNKNOWN";
		}
	}

}
