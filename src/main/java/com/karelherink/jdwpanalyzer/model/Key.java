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

/**
 * @author  karel herink
 */
final class Key {
    public int cmdSet, cmd;
    
    public Key(int cmdSet, int cmd) {
        this.cmdSet = cmdSet;
        this.cmd = cmd;
    }
    
    public int hashCode() {
        return cmdSet *256 + cmd;
    }
    
    public boolean equals(Object key) {
        if (key instanceof Key) {
            return (((Key) key).cmdSet == cmdSet) &&
                (((Key) key).cmd == cmd);
        }
        return false;
    }
    
    public String toString() {
        return "Key[" + cmdSet + ", " + cmd + "]";
    }
}
