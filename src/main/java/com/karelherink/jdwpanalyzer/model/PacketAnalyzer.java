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

import com.karelherink.jdwpanalyzer.entity.MethodType;
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.constants.*;

/**
 * Created on December 6, 2002, 1:56 PM
 *
 * @author Martin Ryzl, Karel Herink
 */
public class PacketAnalyzer {

	protected static int fieldIDSize = 8;
	protected static int methodIDSize = 8;
	protected static int objectIDSize = 8;
	protected static int referenceTypeIDSize = 8;
	protected static int frameIDSize = 8;
	protected static int locationSize = 1 + referenceTypeIDSize + methodIDSize + 8;

	protected synchronized static void setIDSizes(int[] sizes) {
		fieldIDSize = sizes[0];
		methodIDSize = sizes[1];
		objectIDSize = sizes[2];
		referenceTypeIDSize = sizes[3];
		frameIDSize = sizes[4];
		locationSize = 1 + referenceTypeIDSize + methodIDSize + 8;
	}

	protected static long getVal(Packet packet, int index, int n) {
		int len = 0;
		int last = index + n;
		for (int i = index; i < last; i++) {
			len <<= 8;
			len += packet.getData()[i] & 0xff;
		}
		return len;
	}

	public static class Value {
		private int offset;
		private Object value;
		public Value(int offset, Object value) {
			this.offset = offset;
			this.value = value;
		}
		public int getOffset() {
			return this.offset;
		}
		public Object getValue() {
			return this.value;
		}
		
		public String toString() {
			return this.value.toString();
		}
	}
	protected static Value getValue(Packet packet, int index) {
		switch(packet.getData()[index++]) {
			case Tag.ARRAY:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
			case Tag.BYTE:
				return new Value(1 + 1, new Byte((byte)getVal(packet, index, 1)));
			case Tag.CHAR:
				return new Value(2 + 1, new Character((char)getVal(packet, index, 2)));
			case Tag.OBJECT:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
			case Tag.FLOAT: 
				//TODO : http://stevehollasch.com/cgindex/coding/ieeefloat.html
				return new Value(4 + 1, "TODO: FLOAT");
			case Tag.DOUBLE:
				//TODO : http://stevehollasch.com/cgindex/coding/ieeefloat.html
				return new Value(8 + 1, "TODO: DOUBLE");
			case Tag.INT:
				return new Value(4 + 1, new Long(getVal(packet, index, 4)));
			case Tag.LONG:
				return new Value(8 + 1, new Long(getVal(packet, index, 8)));
			case Tag.SHORT:
				return new Value(2 + 1, new Short((short)getVal(packet, index, 2)));
			case Tag.VOID:
				return new Value(0 + 1, "void/null");
			case Tag.BOOLEAN:
				return new Value(1 + 1, new Boolean(getVal(packet, index, 1) == 0 ? false : true));
			case Tag.STRING:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
			case Tag.THREAD:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
			case Tag.THREAD_GROUP:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
			case Tag.CLASS_LOADER:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
			case Tag.CLASS_OBJECT:
				return new Value(objectIDSize + 1, new Long(getVal(packet, index, objectIDSize)));
            default:
                return new Value(0 + 1, "ERR - UNKNOWN");
		}
	}
	
	protected static Value getUntaggedValue(Packet packet, int index, int tag) {
		switch(tag) {
			case Tag.ARRAY:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
			case Tag.BYTE:
				return new Value(1, new Byte((byte)getVal(packet, index, 1)));
			case Tag.CHAR:
				return new Value(2, new Character((char)getVal(packet, index, 2)));
			case Tag.OBJECT:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
			case Tag.FLOAT: 
				//TODO : http://stevehollasch.com/cgindex/coding/ieeefloat.html
				return new Value(4, "TODO: FLOAT");
			case Tag.DOUBLE:
				//TODO : http://stevehollasch.com/cgindex/coding/ieeefloat.html
				return new Value(8, "TODO: DOUBLE");
			case Tag.INT:
				return new Value(4, new Long(getVal(packet, index, 4)));
			case Tag.LONG:
				return new Value(8, new Long(getVal(packet, index, 8)));
			case Tag.SHORT:
				return new Value(2, new Short((short)getVal(packet, index, 2)));
			case Tag.VOID:
				return new Value(0, "void/null");
			case Tag.BOOLEAN:
				return new Value(1, new Boolean(getVal(packet, index, 1) == 0 ? false : true));
			case Tag.STRING:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
			case Tag.THREAD:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
			case Tag.THREAD_GROUP:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
			case Tag.CLASS_LOADER:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
			case Tag.CLASS_OBJECT:
				return new Value(objectIDSize, new Long(getVal(packet, index, objectIDSize)));
            default:
                return new Value(0, "ERR - UNKNOWN");
		}
	}
	
	protected static String getStr(Packet packet, int index, int n) {
		StringBuffer sb = new StringBuffer(n + 1);
		for (int i = index; i < index + n; i++) {
			sb.append((char) packet.getData()[i]);
		}
		return sb.toString();
	}

	protected static Value[] getArrayRegion(Packet packet, int index) {
		int tag = packet.getData()[index++];
		int numValues = (int) getVal(packet, index, 4);
		index += 4;
		Value[] values = new Value[numValues];
		for (int i = 0; i < numValues; i++) {
			if (Tag.isPrimitive(tag)) {
				Value value = getUntaggedValue(packet, index, tag);
				values[i] = value;
				index += value.offset;
			}
			else {
				Value value = getValue(packet, index);
				values[i] = value;
				index += value.offset;
			}
		}
		return values;
	}
	
	private static final int ACC_PUBLIC =		0x0001;
	private static final int ACC_PRIVATE =		0x0002;
	private static final int ACC_PROTECTED =	0x0004;
	private static final int ACC_STATIC =		0x0008;
	private static final int ACC_FINAL =		0x0010;
	private static final int ACC_VOLATILE =		0x0040;
	private static final int ACC_TRANSIENT =	0x0080;
	protected static String getModBitsAsStr(int modBits) {
		String description = "";
		if ((modBits & ACC_PUBLIC) == ACC_PUBLIC)
			description += "PUBLIC ";
		if ((modBits & ACC_PRIVATE) == ACC_PRIVATE)
			description += "PRIVATE ";
		if ((modBits & ACC_PROTECTED) == ACC_PROTECTED)
			description += "PROTECTED ";
		if ((modBits & ACC_STATIC) == ACC_STATIC)
			description += "STATIC ";
		if ((modBits & ACC_FINAL) == ACC_FINAL)
			description += "FINAL ";
		if ((modBits & ACC_VOLATILE) == ACC_VOLATILE)
			description += "VOLATILE ";
		if ((modBits & ACC_TRANSIENT) == ACC_TRANSIENT)
			description += "TRANSIENT ";
		return description.trim();
	}
	
	protected static String getInvokeOptionsAsStr(int invokeOptions) {
		String description = "";
		if ((invokeOptions & InvokeOptions.INVOKE_SINGLE_THREADED) == InvokeOptions.INVOKE_SINGLE_THREADED)
			description += "SINGLE_THREADED ";
		if ((invokeOptions & InvokeOptions.INVOKE_NONVIRTUAL) == InvokeOptions.INVOKE_NONVIRTUAL)
			description += "NONVIRTUAL ";
		return description.trim();
	}
	
	protected static boolean getBoolean(Packet packet, int index) {
		int val = packet.getData()[index++];
		return val == 0 ? false : true;
	}	

	public static String getStringTranslation(int cmdSet, int cmd) {
		String translation = "";
		switch (cmdSet) {
			case CommandSet.VirtualMachine:
				switch (cmd) {
					case Command.VirtualMachine_Version:
						translation += "VirtualMachine_Version";
						break;
					case Command.VirtualMachine_ClassesBySignature:
						translation += "VirtualMachine_ClassesBySignature";
						break;
					case Command.VirtualMachine_AllClasses:
						translation += "VirtualMachine_AllClasses";
						break;
					case Command.VirtualMachine_AllThreads:
						translation += "VirtualMachine_AllThreads";
						break;
					case Command.VirtualMachine_TopLevelThreadGroups:
						translation += "VirtualMachine_TopLevelThreadGroups";
						break;
					case Command.VirtualMachine_Dispose:
						translation += "VirtualMachine_Dispose";
						break;
					case Command.VirtualMachine_IDSizes:
						translation += "VirtualMachine_IDSizes";
						break;
					case Command.VirtualMachine_Suspend:
						translation += "VirtualMachine_Suspend";
						break;
					case Command.VirtualMachine_Resume:
						translation += "VirtualMachine_Resume";
						break;
					case Command.VirtualMachine_Exit:
						translation += "VirtualMachine_Exit";
						break;
					case Command.VirtualMachine_CreateString:
						translation += "VirtualMachine_CreateString";
						break;
					case Command.VirtualMachine_Capabilities:
						translation += "VirtualMachine_Capabilities";
						break;
					case Command.VirtualMachine_ClassPaths:
						translation += "VirtualMachine_ClassPaths";
						break;
					case Command.VirtualMachine_DisposeObjects:
						translation += "VirtualMachine_DisposeObjects";
						break;
					case Command.VirtualMachine_HoldEvents:
						translation += "VirtualMachine_HoldEvents";
						break;
					case Command.VirtualMachine_ReleaseEvents:
						translation += "VirtualMachine_ReleaseEvents";
						break;
					case Command.VirtualMachine_CapabilitiesNew:
						translation += "VirtualMachine_CapabilitiesNew";
						break;
					case Command.VirtualMachine_RedefineClasses:
						translation += "VirtualMachine_RedefineClasses";
						break;
					case Command.VirtualMachine_SetDefaultStratum:
						translation += "VirtualMachine_SetDefaultStratum";
						break;
					case Command.VirtualMachine_AllClassesWithGeneric:
						translation += "VirtualMachine_AllClassesWithGeneric";
						break;
					
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
			case CommandSet.ReferenceType:
				switch (cmd) {
					case Command.ReferenceType_Signature:
						translation += "ReferenceType_Signature";
						break;
					case Command.ReferenceType_ClassLoader:
						translation += "ReferenceType_ClassLoader";
						break;
					case Command.ReferenceType_Modifiers:
						translation += "ReferenceType_Modifiers";
						break;
					case Command.ReferenceType_Fields:
						translation += "ReferenceType_Fields";
						break;
					case Command.ReferenceType_Methods:
						translation += "ReferenceType_Methods";
						break;
					case Command.ReferenceType_GetValues:
						translation += "ReferenceType_GetValues";
						break;
					case Command.ReferenceType_SourceFile:
						translation += "ReferenceType_SourceFile";
						break;
					case Command.ReferenceType_NestedTypes:
						translation += "ReferenceType_NestedTypes";
						break;
					case Command.ReferenceType_Status:
						translation += "ReferenceType_Status";
						break;
					case Command.ReferenceType_Interfaces:
						translation += "ReferenceType_Interfaces";
						break;
					case Command.ReferenceType_ClassObject:
						translation += "ReferenceType_ClassObject";
						break;
					case Command.ReferenceType_SourceDebugExtension:
						translation += "ReferenceType_SourceDebugExtension";
						break;
					case Command.ReferenceType_SignatureWithGeneric:
						translation += "ReferenceType_SignatureWithGeneric";
						break;
					case Command.ReferenceType_FieldsWithGeneric:
						translation += "ReferenceType_FieldsWithGeneric";
						break;
					case Command.ReferenceType_MethodsWithGeneric:
						translation += "ReferenceType_MethodsWithGeneric";
						break;

					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.ClassType:
				switch (cmd) {
					case Command.ClassType_Superclass:
						translation += "ClassType_Superclass";
						break;
					case Command.ClassType_SetValues:
						translation += "ClassType_SetValues";
						break;
					case Command.ClassType_InvokeMethod:
						translation += "ClassType_InvokeMethod";
						break;
					case Command.ClassType_NewInstance:
						translation += "ClassType_NewInstance";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.ArrayType:
				switch (cmd) {
					case Command.ArrayType_NewInstance:
						translation += "ArrayType_NewInstance";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.InterfaceType:
				switch (cmd) {
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.Method:
				switch (cmd) {
					case Command.Method_LineTable:
						translation += "Method_LineTable";
						break;
					case Command.Method_VariableTable:
						translation += "Method_VariableTable";
						break;
					case Command.Method_Bytecodes:
						translation += "Method_Bytecodes";
						break;
					case Command.Method_IsObsolete:
						translation += "Method_IsObsolete";
						break;
					case Command.Method_VariableTableWithGeneric:
						translation += "Method_VariableTableWithGeneric";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.Field:
				switch (cmd) {
					default:
						break;
				}

				break;
			case CommandSet.ObjectReference:
				switch (cmd) {
					case Command.ObjectReference_ReferenceType:
						translation += "ObjectReference_ReferenceType";
						break;
					case Command.ObjectReference_GetValues:
						translation += "ObjectReference_GetValues";
						break;
					case Command.ObjectReference_SetValues:
						translation += "ObjectReference_SetValues";
						break;
					case Command.ObjectReference_MonitorInfo:
						translation += "ObjectReference_MonitorInfo";
						break;
					case Command.ObjectReference_InvokeMethod:
						translation += "ObjectReference_InvokeMethod";
						break;
					case Command.ObjectReference_DisableCollection:
						translation += "ObjectReference_DisableCollection";
						break;
					case Command.ObjectReference_EnableCollection:
						translation += "ObjectReference_EnableCollection";
						break;
					case Command.ObjectReference_IsCollected:
						translation += "ObjectReference_IsCollected";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.StringReference:
				switch (cmd) {
					case Command.StringReference_Value:
						translation += "StringReference_Value";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}

				break;
			case CommandSet.ThreadReference:
				switch (cmd) {
					case Command.ThreadReference_Name:
						translation += "ThreadReference_Name";
						break;
					case Command.ThreadReference_Suspend:
						translation += "ThreadReference_Suspend";
						break;
					case Command.ThreadReference_Resume:
						translation += "ThreadReference_Resume";
						break;
					case Command.ThreadReference_Status:
						translation += "ThreadReference_Status";
						break;
					case Command.ThreadReference_ThreadGroup:
						translation += "ThreadReference_ThreadGroup";
						break;
					case Command.ThreadReference_Frames:
						translation += "ThreadReference_Frames";
						break;
					case Command.ThreadReference_FrameCount:
						translation += "ThreadReference_FrameCount";
						break;
					case Command.ThreadReference_OwnedMonitors:
						translation += "ThreadReference_OwnedMonitors";
						break;
					case Command.ThreadReference_CurrentContendedMonitor:
						translation += "ThreadReference_CurrentContendedMonitor";
						break;
					case Command.ThreadReference_Stop:
						translation += "ThreadReference_Stop";
						break;
					case Command.ThreadReference_Interrupt:
						translation += "ThreadReference_Interrupt";
						break;
					case Command.ThreadReference_SuspendCount:
						translation += "ThreadReference_SuspendCount";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
				
			case CommandSet.ThreadGroupReference:
				switch (cmd) {
					case Command.ThreadGroupReference_Name:
						translation += "ThreadGroupReference_Name";
						break;
					case Command.ThreadGroupReference_Parent:
						translation += "ThreadGroupReference_Parent";
						break;
					case Command.ThreadGroupReference_Children:
						translation += "ThreadGroupReference_Children";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
				
			case CommandSet.ArrayReference:
				switch (cmd) {
					case Command.ArrayReference_Length:
						translation += "ArrayReference_Length";
						break;
					case Command.ArrayReference_GetValues:
						translation += "ArrayReference_GetValues";
						break;
					case Command.ArrayReference_SetValues:
						translation += "ArrayReference_SetValues";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
				
			case CommandSet.ClassLoaderReference:
				switch (cmd) {
					case Command.ClassLoaderReference_VisibleClasses:
						translation += "ClassLoaderReference_VisibleClasses";
						break;
					default:
						break;
				}
				break;
				
			case CommandSet.EventRequest:
				switch (cmd) {
					case Command.EventRequest_Set:
						translation += "EventRequest_Set";
						break;
					case Command.EventRequest_Clear:
						translation += "EventRequest_Clear";
						break;
					case Command.EventRequest_ClearAllBreakpoints:
						translation += "EventRequest_ClearAllBreakpoints";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
				
			case CommandSet.StackFrame:
				switch (cmd) {
					case Command.StackFrame_GetValues:
						translation += "StackFrame_GetValues";
						break;
					case Command.StackFrame_SetValues:
						translation += "StackFrame_SetValues";
						break;
					case Command.StackFrame_ThisObject:
						translation += "StackFrame_ThisObject";
						break;
					case Command.StackFrame_PopFrames:
						translation += "StackFrame_PopFrames";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
				
			case CommandSet.ClassObjectReference:
				switch (cmd) {
					case Command.ClassObjectReference_ReflectedType:
						translation += "ClassObjectReference_ReflectedType";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;
				
			case CommandSet.Event:
				switch (cmd) {
					case Command.Event_Composite:
						translation += "Event_Composite";
						break;
					default:
						translation += "ERROR: in command set " + cmdSet + " there is no command " + cmd;
						break;
				}
				break;

			default:
				translation += "ERROR: there is no command set " + cmdSet;
				break;
		}
		return translation;
	}

	public static Location getLocation(Packet packet, int index) {
		byte typeTag = packet.getData()[index++];
		long classID = getVal(packet, index, referenceTypeIDSize);
		index += referenceTypeIDSize;
		long methodID = getVal(packet, index, methodIDSize);
		index += methodIDSize;
		long execIndex = getVal(packet, index, 8);
		index += 8;
		return new Location(typeTag, classID, methodID, execIndex);
	}

	public static class Location {
		private byte typeTag;
		private long classID;
		private long methodID;
		private long execIndex;
		
		private String decription = "Location:";
		
		public Location(byte typeTag, long classID, long methodID, long execIndex) {
			this.typeTag = typeTag;
			this.classID = classID;
			this.methodID = methodID;
			this.execIndex = execIndex;
		}
		public String toString() {
			return TypeTag.asString(typeTag) + " - [classID]:" + classID + ", [methodID]:" + methodID + ", [index]:" + execIndex;
		}
		public Node toNode() {
			Node root = new Node(new Node.Descriptor(decription), new Node.Value(this.toString()));
			
			ReferenceType refType = ReferenceType.getType(this.classID);
			Node typeInfo = new Node(new Node.Descriptor("RefTypeID:", refType), new Node.Value(new Long(this.classID)));
			root.addChild(typeInfo);
			
			MethodType methType = MethodType.getType(this.classID, this.methodID);
			Node methInfo = new Node(new Node.Descriptor("MethodID:", methType), new Node.Value(new Long(this.methodID)));
			root.addChild(methInfo);
			
			return root;
		}
		public long getClassID() {
			return this.classID;
		}
		public void setClassID(long classID) {
			this.classID = classID;
		}
		public long getExecIndex() {
			return this.execIndex;
		}
		public void setExecIndex(long execIndex) {
			this.execIndex = execIndex;
		}
		public long getMethodID() {
			return this.methodID;
		}
		public void setMethodID(long methodID) {
			this.methodID = methodID;
		}
		public int getTypeTag() {
			return this.typeTag;
		}
		public void setTypeTag(byte typeTag) {
			this.typeTag = typeTag;
		}
		public String getDecription() {
			return this.decription;
		}
		public void setDecription(String decription) {
			this.decription = decription;
		}
	}
	
	public static String taggedObjectIDToString(Packet packet, int index) {
		int tag = packet.getData()[index++] & 0xff;
		long objectID = getVal(packet, index, objectIDSize);
		
		return Tag.asString(tag) + "-" + objectID;
	}

	public void updateInternalDataModel(Packet packet) {
	}
	
	public Node getPacketInfo(Packet packet) {
		return new Node(new Node.Descriptor("NONE"), new Node.Value("NONE"));
	}
	
}
