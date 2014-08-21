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
public interface Command {

    //VirtualMachine Command Set (1)
    public static final byte VirtualMachine_Version = 1;
    public static final byte VirtualMachine_ClassesBySignature = 2;
    public static final byte VirtualMachine_AllClasses = 3;
    public static final byte VirtualMachine_AllThreads = 4;
    public static final byte VirtualMachine_TopLevelThreadGroups = 5;
    public static final byte VirtualMachine_Dispose = 6;
    public static final byte VirtualMachine_IDSizes = 7;
    public static final byte VirtualMachine_Suspend = 8;
    public static final byte VirtualMachine_Resume = 9;
    public static final byte VirtualMachine_Exit = 10;
    public static final byte VirtualMachine_CreateString = 11;
    public static final byte VirtualMachine_Capabilities = 12;
    public static final byte VirtualMachine_ClassPaths = 13;
    public static final byte VirtualMachine_DisposeObjects = 14;
    public static final byte VirtualMachine_HoldEvents = 15;
    public static final byte VirtualMachine_ReleaseEvents = 16;
    public static final byte VirtualMachine_CapabilitiesNew = 17;
    public static final byte VirtualMachine_RedefineClasses = 18;
    public static final byte VirtualMachine_SetDefaultStratum = 19;
    public static final byte VirtualMachine_AllClassesWithGeneric = 20;
    
    //ReferenceType Command Set (2)
    public static final byte ReferenceType_Signature = 1;
    public static final byte ReferenceType_ClassLoader = 2;
    public static final byte ReferenceType_Modifiers = 3;
    public static final byte ReferenceType_Fields = 4;
    public static final byte ReferenceType_Methods = 5;
    public static final byte ReferenceType_GetValues = 6;
    public static final byte ReferenceType_SourceFile = 7;
    public static final byte ReferenceType_NestedTypes = 8;
    public static final byte ReferenceType_Status = 9;
    public static final byte ReferenceType_Interfaces = 10;
    public static final byte ReferenceType_ClassObject = 11;
    public static final byte ReferenceType_SourceDebugExtension = 12;
    public static final byte ReferenceType_SignatureWithGeneric = 13;
    public static final byte ReferenceType_FieldsWithGeneric = 14;
    public static final byte ReferenceType_MethodsWithGeneric = 15;
    
    //ClassType Command Set (3)
    public static final byte ClassType_Superclass = 1;
    public static final byte ClassType_SetValues = 2;
    public static final byte ClassType_InvokeMethod = 3;
    public static final byte ClassType_NewInstance = 4;
    
    //ArrayType Command Set (4)
    public static final byte ArrayType_NewInstance = 1;
    
    //InterfaceType Command Set (5)
    
    //Method Command Set (6)
    public static final byte Method_LineTable = 1;
    public static final byte Method_VariableTable = 2;
    public static final byte Method_Bytecodes = 3;
    public static final byte Method_IsObsolete = 4;
    public static final byte Method_VariableTableWithGeneric = 5;
    //Field Command Set (8)
    
    //ObjectReference Command Set (9)
    public static final byte ObjectReference_ReferenceType = 1;
    public static final byte ObjectReference_GetValues = 2;
    public static final byte ObjectReference_SetValues = 3;
    public static final byte ObjectReference_MonitorInfo = 5;
    public static final byte ObjectReference_InvokeMethod = 6;
    public static final byte ObjectReference_DisableCollection = 7;
    public static final byte ObjectReference_EnableCollection = 8;
    public static final byte ObjectReference_IsCollected = 9;
    
    //StringReference Command Set (10)
    public static final byte StringReference_Value = 1;
    
    //ThreadReference Command Set (11)
    public static final byte ThreadReference_Name = 1;
    public static final byte ThreadReference_Suspend = 2;
    public static final byte ThreadReference_Resume = 3;
    public static final byte ThreadReference_Status = 4;
    public static final byte ThreadReference_ThreadGroup = 5;
    public static final byte ThreadReference_Frames = 6;
    public static final byte ThreadReference_FrameCount = 7;
    public static final byte ThreadReference_OwnedMonitors = 8;
    public static final byte ThreadReference_CurrentContendedMonitor = 9;
    public static final byte ThreadReference_Stop = 10;
    public static final byte ThreadReference_Interrupt = 11;
    public static final byte ThreadReference_SuspendCount = 12;
    
    //ThreadGroupReference Command Set (12)
    public static final byte ThreadGroupReference_Name = 1;
    public static final byte ThreadGroupReference_Parent = 2;
    public static final byte ThreadGroupReference_Children = 3;
    
    //ArrayReference Command Set (13)
    public static final byte ArrayReference_Length = 1;
    public static final byte ArrayReference_GetValues = 2;
    public static final byte ArrayReference_SetValues = 3;
    
    //ClassLoaderReference Command Set (14)
    public static final byte ClassLoaderReference_VisibleClasses = 1;
    
    //EventRequest Command Set (15)
    public static final byte EventRequest_Set = 1;
    public static final byte EventRequest_Clear = 2;
    public static final byte EventRequest_ClearAllBreakpoints = 3;
    
    //StackFrame Command Set (16)
    public static final byte StackFrame_GetValues = 1;
    public static final byte StackFrame_SetValues = 2;
    public static final byte StackFrame_ThisObject = 3;
    public static final byte StackFrame_PopFrames = 4;
    
    //ClassObjectReference Command Set (17)
    public static final byte ClassObjectReference_ReflectedType = 1;
    
    //Event Command Set (64)
    public static final byte Event_Composite = 100;

}
