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

package com.karelherink.jdwpanalyzer.ui;

import com.karelherink.jdwpanalyzer.entity.Type;
import com.karelherink.jdwpanalyzer.model.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * @author karel herink
 */
public class View {

    private JFrame mainFrame;
    
    private JPanel mainViewPanel;
    private JPanel topPanel;
    private JPanel detailPanel;
    private JPanel detailPacketPanel;
    private JPanel detailTypePanel;
    private JSplitPane topHorizontalSplit;
    private JSplitPane detailVerticalSplit;
    private JTable packetTable;
    private JTable detailPacketTable;
    private JDWPTableModel packetTableModel;
    private JScrollPane packetTableScroll;
    
    private JList sequenceList;
    private JDWPListModel sequenceListModel;
    private JScrollPane sequenceListScroll;
    private JSplitPane mainVerticalSplit;
    
    private JDWPProxy proxy;
    
    public View(JDWPProxy proxy, List sequentialRequests) {
    	this.proxy = proxy;
        this.mainFrame = new JFrame("ZeroEffort JDWP Analyzer");
        
        this.packetTableModel = new JDWPTableModel(sequentialRequests);
        this.packetTable = new JTable(packetTableModel);
        this.packetTable.setDefaultRenderer(Packet.class, new TablePacketRenderer());
        this.packetTable.setCellSelectionEnabled(true);
        PacketTableUIListener listener = new PacketTableUIListener();
        this.packetTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        this.packetTable.getSelectionModel().addListSelectionListener(listener);
        this.packetTable.addFocusListener(listener);
        this.packetTableScroll = new JScrollPane(this.packetTable);
        
        
        this.detailPanel = new JPanel();
        this.detailPanel.setLayout(new BorderLayout());
        this.detailVerticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.detailPacketPanel = new JPanel();
        this.detailTypePanel = new JPanel();
        this.detailPacketPanel.setLayout(new BorderLayout());
        this.detailTypePanel.setLayout(new BorderLayout());
        this.detailPacketPanel.add(new JTextField("No detail available."), BorderLayout.CENTER);
        this.detailTypePanel.add(new JTextField("No detail available."), BorderLayout.CENTER);
        //this.detailTypePanel.setBackground(Color.WHITE);
        this.detailVerticalSplit.setDividerSize(2);
        this.detailVerticalSplit.add(this.detailPacketPanel);
        this.detailVerticalSplit.add(this.detailTypePanel);
        this.detailVerticalSplit.setDividerLocation(250);
        this.detailPanel.add(this.detailVerticalSplit, BorderLayout.CENTER);
        
        
        this.topPanel = new JPanel();
        this.topPanel.setLayout(new BorderLayout());
        this.topHorizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.topHorizontalSplit.setDividerSize(2);
        this.topHorizontalSplit.add(this.packetTableScroll);
        this.topHorizontalSplit.add(this.detailPanel);
        this.topPanel.add(this.topHorizontalSplit);

        this.sequenceListModel = new JDWPListModel();
        this.sequenceList = new JList(sequenceListModel);
        this.sequenceList.setCellRenderer(new ListPacketRenderer());
        this.sequenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.sequenceList.addListSelectionListener(new SequenceListUIListener());
        this.sequenceList.addFocusListener(new SequenceListUIListener());
        this.sequenceListScroll = new JScrollPane(this.sequenceList);
        
        this.mainVerticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.mainVerticalSplit.setDividerSize(2);
        this.mainVerticalSplit.add(this.topPanel);
        this.mainVerticalSplit.add(this.sequenceListScroll);
        
        this.mainViewPanel = new JPanel();
        this.mainViewPanel.setLayout(new BorderLayout());
        this.mainViewPanel.add(this.mainVerticalSplit, BorderLayout.CENTER);
        
        Container container = this.mainFrame.getContentPane();
        container.setLayout(new GridLayout(1, 1));
        container.add(this.mainViewPanel);
        
        this.mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                View.this.proxy.shutDown();
            }
        });
        
        this.mainFrame.setSize(900, 600);
        this.mainFrame.validate();
        this.mainFrame.setVisible(true);
    }
            
    public void updateView(Packet packet) {
        EventQueue.invokeLater(new ViewUpdater(packet));
    }
    
    private class ViewUpdater implements Runnable {
        private Packet packet;
        ViewUpdater(Packet packet) {
            this.packet = packet;
        }
        public void run() {
            int[] cellPosition = View.this.packetTableModel.update(packet);
            int position = View.this.sequenceListModel.update(packet);
            
            Rectangle tableRect = View.this.packetTable.getCellRect(cellPosition[0], cellPosition[1], true);        
            Rectangle listRect = View.this.sequenceList.getCellBounds(position, position);
            
            View.this.packetTableScroll.getViewport().scrollRectToVisible(tableRect);
            View.this.sequenceListScroll.getViewport().scrollRectToVisible(listRect);
        }
    }
    
    private class PacketTableUIListener implements ListSelectionListener, FocusListener {
        private void updateDetailedViewForPacket() {
            int colIndex = View.this.packetTable.getSelectedColumn();
            int rowIndex = View.this.packetTable.getSelectedRow();
            Packet packet = (Packet) View.this.packetTable.getModel().getValueAt(rowIndex, colIndex);
            if (packet == null)
                return;
            if (packet.isResponse()) {
            	Response resp = (Response) packet;
            	if (resp.isError())
            		return;
            }
            View.this.detailPacketTable = getDetailPacketTable(packet);
            EventQueue.invokeLater(new DetailPacketViewUpdater(View.this.detailPacketTable));
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty())
                return;
            this.updateDetailedViewForPacket();
        }

        public void focusGained(FocusEvent e) {
            this.updateDetailedViewForPacket();
        }
        public void focusLost(FocusEvent e) {
        }
    }
        
    private class SequenceListUIListener implements ListSelectionListener, FocusListener {
        private void updateDetailedViewForPacket(int selectedIndex) {
            Packet packet = (Packet) View.this.sequenceList.getModel().getElementAt(selectedIndex);
            if (packet.isResponse()) {
            	Response resp = (Response) packet;
            	if (resp.isError())
            		return;
            }
            View.this.detailPacketTable = getDetailPacketTable(packet);
            EventQueue.invokeLater(new DetailPacketViewUpdater(View.this.detailPacketTable));
        }
        //ListSelectionListener
        public void valueChanged(ListSelectionEvent e) {
            int selectedIndex = ((JList) e.getSource()).getSelectedIndex();
            if (e.getValueIsAdjusting() || selectedIndex == -1)
                return;
            this.updateDetailedViewForPacket(selectedIndex);
        }        
        //FocusListener
        public void focusGained(FocusEvent e) {
            int selectedIndex = View.this.sequenceList.getSelectedIndex();
            if (selectedIndex == -1)
                return;
            this.updateDetailedViewForPacket(selectedIndex);
        }
        public void focusLost(FocusEvent e) {
        }
    }
    
    private class DetailPacketViewUpdater implements Runnable {
        private Component detailedView;
        public DetailPacketViewUpdater(Component detailedView) {
            this.detailedView = detailedView;
        }
        public void run() {
            //System.out.println("Setting detiled view");
            View.this.detailPacketPanel.removeAll();
            JScrollPane packetTableScroll = new JScrollPane(this.detailedView);
            View.this.detailPacketPanel.add(packetTableScroll);
            View.this.mainFrame.validate();
        }
    }

    
	private JTable getDetailPacketTable(Packet packet) {
		PacketAnalyzer pa = AnalyzerManager.createPacketAnalyzer(packet);
		Node info = pa.getPacketInfo(packet);
		PacketDetailTableModel tableModel = new PacketDetailTableModel(info);
		
		JTable table = new JTable(tableModel);
		DetailPacketTableUIListener listener = new DetailPacketTableUIListener();
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        table.getSelectionModel().addListSelectionListener(listener);
        table.addFocusListener(listener);
		
		return table;
	}
	
    private class DetailPacketTableUIListener implements ListSelectionListener, FocusListener {

      private void updateDetailedViewForType() {
          int rowIndex = View.this.detailPacketTable.getSelectedRow();
          //Packet packet = (Packet) View.this.table.getModel().getValueAt(rowIndex, colIndex);
          Node.Descriptor descriptor = (Node.Descriptor) View.this.detailPacketTable.getValueAt(rowIndex, 0);
          if (descriptor == null || descriptor.getAssociatedType() == null)
              return;
          Type type = descriptor.getAssociatedType();
          final Component typeView = type.getTypeDetailedView();
          EventQueue.invokeLater(new Runnable() {
			public void run() {
				View.this.detailTypePanel.removeAll();
				JScrollPane typeTableScroll = new JScrollPane(typeView);
				//typeTableScroll.setBackground(Color.WHITE);
		        View.this.detailTypePanel.add(typeTableScroll, BorderLayout.CENTER);
		        View.this.mainFrame.validate();
			}          	
          });
      }
  	
		public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting())
              return;
          ListSelectionModel lsm = (ListSelectionModel) e.getSource();
          if (lsm.isSelectionEmpty())
              return;
          //System.out.println("valueChanged : " + e.toString());
          this.updateDetailedViewForType();
		}

		public void focusGained(FocusEvent e) {
          //System.out.println("focusGained : " + e.toString());
          this.updateDetailedViewForType();
		}

		public void focusLost(FocusEvent e) {
		}
  }	

}
