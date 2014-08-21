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
package com.karelherink.jdwpanalyzer.request;

import com.karelherink.jdwpanalyzer.entity.FieldType;
import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;
import com.karelherink.jdwpanalyzer.model.constants.ClassStatus;
import com.karelherink.jdwpanalyzer.model.constants.EventKind;
import com.karelherink.jdwpanalyzer.model.constants.SuspendPolicy;
import com.karelherink.jdwpanalyzer.model.constants.TypeTag;

/**
 * @author martin ryzl, karel herink
 */
public class Analyzer_64_100 extends PacketAnalyzer {

	//public static final int[] WTK = new int[] {8, 4, 4, 4, 4};

	/** Creates a new instance of CDAnalyzer */
	public Analyzer_64_100() {
	}

    public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
		
		byte suspend = packet.getData()[index++];
		Node suspendInfo = new Node(new Node.Descriptor("SuspendPolicy:"), new Node.Value(new Byte(suspend), SuspendPolicy.asString(suspend)));
		root.addChild(suspendInfo);
		
		int numEvents = (int) getVal(packet, index, 4);
		index += 4;	
		Node eventsInfo = new Node(new Node.Descriptor("NumEvents:"), new Node.Value(new Integer(numEvents)));
		root.addChild(eventsInfo);
		
		for (int i = 0; i < numEvents; i++) {
			byte eventKind = packet.getData()[index++];
			
			Node eventInfo = new Node(new Node.Descriptor("EventKind:"), new Node.Value(new Byte(eventKind), EventKind.asString(eventKind)));
			eventsInfo.addChild(eventInfo);
			
			int requestId = (int) getVal(packet, index, 4);
			index += 4;
			
			switch (eventKind) {
				case EventKind.VM_START:
//					fallthrough
				case EventKind.THREAD_DEATH:
//					fallthrough
				case EventKind.THREAD_START:
					long threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					ObjectType thread = ObjectType.getType(threadId);
					Node threadInfo = new Node(new Node.Descriptor("ThreadID:", thread), new Node.Value(new Long(threadId)));
					eventInfo.addChild(threadInfo);
					break;
					
				case EventKind.SINGLE_STEP:
//					fallthrough
				case EventKind.METHOD_ENTRY:
//					fallthrough
				case EventKind.METHOD_EXIT:
//					fallthrough
				case EventKind.BREAKPOINT:
					threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					thread = ObjectType.getType(threadId);
					threadInfo = new Node(new Node.Descriptor("ThreadID:", thread), new Node.Value(new Long(threadId)));
					eventInfo.addChild(threadInfo);
					
					Location location = getLocation(packet, index);
					index += locationSize;
					Node locationInfo = location.toNode();
					eventInfo.addChild(locationInfo);
					break;

				case EventKind.EXCEPTION:
					threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					thread = ObjectType.getType(threadId);
					threadInfo = new Node(new Node.Descriptor("ThreadID:", thread), new Node.Value(new Long(threadId)));
					eventInfo.addChild(threadInfo);
					
					Location locationThrown = getLocation(packet, index);
					locationThrown.setDecription("LocationThrown:");
					index += locationSize;
					Node locationThrownInfo = locationThrown.toNode();
					eventInfo.addChild(locationThrownInfo);
					
					Value value = getValue(packet, index);
					index += value.getOffset();
					Long objectId = (Long) value.getValue();
					ObjectType objectType = ObjectType.getType(objectId);
					Node singleValueInfo = new Node(new Node.Descriptor("ExceptionObjectID:", objectType), new Node.Value(value));
					eventInfo.addChild(singleValueInfo);
					
					Location locationCaught = getLocation(packet, index);
					locationCaught.setDecription("LocationCaught");
					index += locationSize;
					Node locationCaughtInfo = locationCaught.toNode();
					eventInfo.addChild(locationCaughtInfo);
					break;

				case EventKind.CLASS_PREPARE:
					threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					thread = ObjectType.getType(threadId);
					threadInfo = new Node(new Node.Descriptor("LoaderThreadID:", thread), new Node.Value(new Long(threadId)));
					eventInfo.addChild(threadInfo);

					byte typeTag = packet.getData()[index++];
					Node typeTagInfo = new Node(new Node.Descriptor("TypeTag:"), new Node.Value(new Byte(typeTag), TypeTag.asString(typeTag)));
					eventInfo.addChild(typeTagInfo);
					
					long refId = getVal(packet, index, referenceTypeIDSize);
					index += referenceTypeIDSize;
					ReferenceType refType = ReferenceType.getType(refId);
					Node refIdInfo = new Node(new Node.Descriptor("ReferenceID:", refType), new Node.Value(new Long(refId)));
					eventInfo.addChild(refIdInfo);
					
					int sigLen = (int) getVal(packet, index, 4);
					index += 4;
					String signature = getStr(packet, index, sigLen);
					index += sigLen;
					Node signatureInfo = new Node(new Node.Descriptor("RefTypeSignature:"), new Node.Value(signature));
					eventInfo.addChild(signatureInfo);
					
					int classStatus = (int) getVal(packet, index, 4);
					index += 4;
					Node classStatusInfo = new Node(new Node.Descriptor("Status:"), new Node.Value(new Integer(classStatus), ClassStatus.asString(classStatus)));
					eventInfo.addChild(classStatusInfo);
					break;

					//TODO test this case
				case EventKind.CLASS_UNLOAD:
					sigLen = (int) getVal(packet, index, 4);
					index += 4;
					signature = getStr(packet, index, sigLen);
					index += sigLen;
					signatureInfo = new Node(new Node.Descriptor("RefTypeSignature:"), new Node.Value(signature));
					eventInfo.addChild(signatureInfo);
					break;

					//TODO test this case
				case EventKind.FIELD_ACCESS:
					threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					thread = ObjectType.getType(threadId);
					threadInfo = new Node(new Node.Descriptor("AccessorThreadID:", thread), new Node.Value(new Long(threadId)));
					eventInfo.addChild(threadInfo);
					
					location = getLocation(packet, index);
					location.setDecription("AccessLocation:");
					index += locationSize;
					locationInfo = location.toNode();
					eventInfo.addChild(locationInfo);
					
					typeTag = packet.getData()[index++];
					typeTagInfo = new Node(new Node.Descriptor("TypeTag:"), new Node.Value(new Byte(typeTag), TypeTag.asString(typeTag)));
					eventInfo.addChild(typeTagInfo);
					
					refId = getVal(packet, index, referenceTypeIDSize);
					index += referenceTypeIDSize;
					refType = ReferenceType.getType(refId);
					refIdInfo = new Node(new Node.Descriptor("ReferenceID:", refType), new Node.Value(new Long(refId)));
					eventInfo.addChild(refIdInfo);
					
					long fieldId = getVal(packet, index, fieldIDSize);
					index += fieldIDSize;
					FieldType fieldType = FieldType.getType(refId, fieldId);
					Node fieldIdInfo = new Node(new Node.Descriptor("AccessedFieldID:", fieldType), new Node.Value(new Long(fieldId)));
					eventInfo.addChild(fieldIdInfo);

					value = getValue(packet, index);
					index += value.getOffset();
					objectId = (Long) value.getValue();
					objectType = ObjectType.getType(objectId);
					singleValueInfo = new Node(new Node.Descriptor("AccessedObjectID:", objectType), new Node.Value(value));
					eventInfo.addChild(singleValueInfo);
					break;

				case EventKind.FIELD_MODIFICATION:
					threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					thread = ObjectType.getType(threadId);
					threadInfo = new Node(new Node.Descriptor("AccessorThreadID:", thread), new Node.Value(new Long(threadId)));
					eventInfo.addChild(threadInfo);
					
					location = getLocation(packet, index);
					location.setDecription("AccessLocation:");
					index += locationSize;
					locationInfo = location.toNode();
					eventInfo.addChild(locationInfo);
					
					typeTag = packet.getData()[index++];
					typeTagInfo = new Node(new Node.Descriptor("TypeTag:"), new Node.Value(new Byte(typeTag), TypeTag.asString(typeTag)));
					eventInfo.addChild(typeTagInfo);
					
					refId = getVal(packet, index, referenceTypeIDSize);
					index += referenceTypeIDSize;
					refType = ReferenceType.getType(refId);
					refIdInfo = new Node(new Node.Descriptor("ReferenceID:", refType), new Node.Value(new Long(refId)));
					eventInfo.addChild(refIdInfo);
					
					fieldId = getVal(packet, index, fieldIDSize);
					index += fieldIDSize;
					fieldType = FieldType.getType(refId, fieldId);
					fieldIdInfo = new Node(new Node.Descriptor("AccessedFieldID:", fieldType), new Node.Value(new Long(fieldId)));
					eventInfo.addChild(fieldIdInfo);
					
					value = getValue(packet, index);
					index += value.getOffset();
					objectId = (Long) value.getValue();
					objectType = ObjectType.getType(objectId);
					singleValueInfo = new Node(new Node.Descriptor("AccessedObjectID:", objectType), new Node.Value(value));
					eventInfo.addChild(singleValueInfo);
					
					value = getValue(packet, index);
					index += value.getOffset();
					objectId = (Long) value.getValue();
					objectType = ObjectType.getType(objectId);
					singleValueInfo = new Node(new Node.Descriptor("ValueSet:", objectType), new Node.Value(value));
					eventInfo.addChild(singleValueInfo);
					break;
					
				case EventKind.VM_DEATH:
                    //TODO : all streams are closed after this so stop reading
					break;

				default:
					Node errInfo = new Node(new Node.Descriptor("ERR: Unknown eventKind " + eventKind), null);
				eventInfo.addChild(errInfo);
					break;
			}
			
		}
		
		return root;
    }
    
//	public Component getDetailedView(Packet packet) {
//		int index = 0;
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout());
//
//		Vector colData;
//		Vector rowData = new Vector();
//		Vector colNames = new Vector();
//
//		colNames.add("Event Kind");
//		colNames.add("Description");
//		colNames.add("Value");
//
//		colData = new Vector();
//		colData.add("");
//		colData.add("Suspend policy: ");
//		colData.add(SuspendPolicy.asString(packet.getData()[index++]));
//		rowData.add(colData);
//
//		int numEvents = (int) getVal(packet, 1, 4);
//		index += 4;		
//		colData = new Vector();
//		colData.add("");
//		colData.add("Number of events: ");
//		colData.add(String.valueOf(numEvents));
//		rowData.add(colData);
//
//		colData = new Vector();
//		colData.add("");
//		colData.add("");
//		colData.add("");
//		rowData.add(colData);
//
//		for (int i = 0; i < numEvents; i++) {
//			byte command = packet.getData()[index++];
//
//			colData = new Vector();
//			colData.add(EventKind.asString(command));
//			colData.add("");
//			colData.add("");
//			rowData.add(colData);
//
//			colData = new Vector();
//			colData.add("");
//			colData.add("Request ID:");
//			colData.add(String.valueOf(getVal(packet, index, 4)));
//			rowData.add(colData);
//			index += 4;
//
//			switch (command) {
//				
//				case EventKind.VM_START:
////					fallthrough
//				case EventKind.THREAD_DEATH:
////					fallthrough
//				case EventKind.THREAD_START:
//					colData = new Vector();
//					colData.add("");
//					colData.add("ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					rowData.add(colData);
//					index += objectIDSize;
//					break;
//
//
//				case EventKind.SINGLE_STEP:
//					colData = new Vector();
//					colData.add("");
//					colData.add("ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					rowData.add(colData);
//					index += objectIDSize;
//					
//					colData = new Vector();
//					colData.add("");
//					colData.add("Step location:");
//					colData.add(getLocation(packet, index));
//					rowData.add(colData);
//					index += locationSize;
//					break;
//
//				case EventKind.METHOD_ENTRY:
////					fallthrough
//				case EventKind.METHOD_EXIT:
////					fallthrough
//				case EventKind.BREAKPOINT:
//					colData = new Vector();
//					colData.add("");
//					colData.add("ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					colData.add("");
//					rowData.add(colData);
//					index += objectIDSize;
//					
//					colData = new Vector();
//					colData.add("");
//					colData.add("Location:");
//					colData.add(getLocation(packet, index));
//					rowData.add(colData);
//					index += locationSize;
//					break;
//
//				case EventKind.EXCEPTION:
//					colData = new Vector();
//					colData.add("");
//					colData.add("ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					rowData.add(colData);
//					index += objectIDSize;
//					
//					colData = new Vector();
//					colData.add("");
//					colData.add("Throw location:");
//					colData.add(getLocation(packet, index));
//					rowData.add(colData);
//					index += locationSize;
//					
//					colData = new Vector();
//					colData.add("");
//					colData.add("Exception Object ID:");
//					colData.add(taggedObjectIDToString(packet, index));
//					rowData.add(colData);
//					index += 1 + objectIDSize;
//					
//					colData = new Vector();
//					colData.add("");
//					colData.add("Catch location:");
//					colData.add(getLocation(packet, index));
//					rowData.add(colData);
//					index += locationSize;
//					
//					//ps.println("EXCEPTION: reqID = " + getVal(packet, index, 4));
//					//index += objectIDSize + locationSize + objectIDSize + 1;
//					
//					break;
//					
//				case EventKind.CLASS_PREPARE:
//					colData = new Vector();
//					colData.add("");
//					colData.add("Loader ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					rowData.add(colData);
//					index += objectIDSize;
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Ref TypeTag:");
//					colData.add(TypeTag.asString(packet.getData()[index++]));
//					rowData.add(colData);
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Ref Type ID:");
//					colData.add(String.valueOf(getVal(packet, index, referenceTypeIDSize)));
//					rowData.add(colData);
//					index += referenceTypeIDSize;
//
//					int sigLen = (int) getVal(packet, index, 4);
//					index += 4;
//					colData = new Vector();
//					colData.add("");
//					colData.add("RefType signature:");
//					colData.add(getStr(packet, index, sigLen));
//					rowData.add(colData);
//					index += sigLen;
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Status:");
//					colData.add(ClassStatus.asString((int) getVal(packet, index, 4)));
//					rowData.add(colData);
//					index += 4;
//					break;
//
//					//TODO test this case
//				case EventKind.CLASS_UNLOAD:
//					int sigLen2 = (int) getVal(packet, index, 4);
//					index += 4;
//					colData = new Vector();
//					colData.add("");
//					colData.add("RefType signature:");
//					colData.add(getStr(packet, index, sigLen2));
//					rowData.add(colData);
//					index += sigLen2;
//					break;
//
//					//TODO test this case
//				case EventKind.FIELD_ACCESS:
//					colData = new Vector();
//					colData.add("");
//					colData.add("Accessing ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					rowData.add(colData);
//					index += objectIDSize;
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Access location:");
//					colData.add(getLocation(packet, index));
//					rowData.add(colData);
//					index += locationSize;
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Ref TypeTag:");
//					colData.add(TypeTag.asString(packet.getData()[index++]));
//					rowData.add(colData);
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Ref Type ID:");
//					colData.add(String.valueOf(getVal(packet, index, referenceTypeIDSize)));
//					rowData.add(colData);
//					index += referenceTypeIDSize;
//
//					colData = new Vector();
//					colData.add("");
//					colData.add("Accessed Field ID:");
//					colData.add(String.valueOf(getVal(packet, index, fieldIDSize)));
//					rowData.add(colData);
//					index += fieldIDSize;
//
//					//taggedObjectIDToString
//					colData = new Vector();
//					colData.add("");
//					colData.add("Accessed Object ID:");
//					colData.add(taggedObjectIDToString(packet, index));
//					rowData.add(colData);
//					index += 1 + objectIDSize;
//					break;
//
//				case EventKind.FIELD_MODIFICATION:
//					colData = new Vector();
//					colData.add("");
//					colData.add("Accessing ThreadID:");
//					colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//					rowData.add(colData);
//					index += objectIDSize;
//	
//					colData = new Vector();
//					colData.add("");
//					colData.add("Access location:");
//					colData.add(getLocation(packet, index));
//					rowData.add(colData);
//					index += locationSize;
//	
//					colData = new Vector();
//					colData.add("");
//					colData.add("Ref TypeTag:");
//					colData.add(TypeTag.asString(packet.getData()[index++]));
//					rowData.add(colData);
//	
//					colData = new Vector();
//					colData.add("");
//					colData.add("Ref Type ID:");
//					colData.add(String.valueOf(getVal(packet, index, referenceTypeIDSize)));
//					rowData.add(colData);
//					index += referenceTypeIDSize;
//	
//					colData = new Vector();
//					colData.add("");
//					colData.add("Accessed Field ID:");
//					colData.add(String.valueOf(getVal(packet, index, fieldIDSize)));
//					rowData.add(colData);
//					index += fieldIDSize;
//	
//					//taggedObjectIDToString
//					colData = new Vector();
//					colData.add("");
//					colData.add("Accessed Object ID:");
//					colData.add(taggedObjectIDToString(packet, index));
//					rowData.add(colData);
//					index += 1 + objectIDSize;
//
//					byte valueType = packet.getData()[index++];
//					String valueValue = "";
//					switch (valueType) {
//						case Tag.VOID: // VOID
//							valueValue = Tag.asString(valueType);
//							break;
//						case Tag.BYTE: // Byte
//						case Tag.BOOLEAN: // Boolean
//							valueValue = Tag.asString(valueType) + ":" + packet.getData()[index++];
//							break;
//						case Tag.CHAR: // Char
//						case Tag.SHORT: // Short
//							valueValue = Tag.asString(valueType) + ":" + getVal(packet, index, 2);
//							index += 2;
//							break;
//						case Tag.FLOAT: // Float
//						case Tag.INT: // Int
//							valueValue = Tag.asString(valueType) + ":" + getVal(packet, index, 4);
//							index += 4;
//							break;
//						case Tag.DOUBLE: // Double
//						case Tag.LONG: // Long
//							valueValue = Tag.asString(valueType) + ":" + getVal(packet, index, 8);
//							index += 8;
//							break;
//						case Tag.ARRAY: // Array
//						case Tag.OBJECT: // Object
//						case Tag.STRING: // String
//						case Tag.THREAD: // Thread
//						case Tag.THREAD_GROUP: // ThreadGroup
//						case Tag.CLASS_LOADER: // ClassLoader
//						case Tag.CLASS_OBJECT: // ClassObject
//							valueValue = Tag.asString(valueType) + ":" + getVal(packet, index, objectIDSize);
//							index += objectIDSize;
//							break;
//						default:
//							valueValue = "Value type: " + Tag.asString(valueType);
//					}
//					colData = new Vector();
//					colData.add("");
//					colData.add("Value set:");
//					colData.add(valueValue);
//					rowData.add(colData);
//					break;
//					
//				case EventKind.VM_DEATH:
//					break;
//
//				default:
//					throw new RuntimeException("unimplemented command = " + command);
//			}
//		}
//		panel.add(new JScrollPane(new JTable(rowData, colNames)), BorderLayout.CENTER);
//		return panel;
//	}
}
