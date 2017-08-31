/*
 * Copyright 2017 Fenenko Aleksandr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pro.fenenko.Pechkin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import pro.fenenko.Bitmessage.BMAddressUtils;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinManageSubscribed extends javax.swing.JFrame {

    /**
     * Creates new form PechkinManageSubscribed
     */
    private String[] addressSubscrib = new String[0];
    private int[] rowSelect = new int[0];
    private BMUtils utils = new BMUtils();
    
    public PechkinManageSubscribed() {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/ic_launcher_144.png")).getImage());
        ListSelectionModel selModel = listAddress.getSelectionModel();
        selModel.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                rowSelect = listAddress.getSelectedRows();
                //System.out.println(rowSelect.length);
            }
        });
    }
    
    public void updateList(){
        DefaultTableModel dtm = (DefaultTableModel) listAddress.getModel();
        while (0 < dtm.getRowCount()) {
            dtm.removeRow(0);
        }
        
        String[] subscrib = PechkinPrivateData.getSubscribed();
        addressSubscrib = subscrib;
        for(int i = 0; i < subscrib.length;i++){
            //System.out.println(subscrib[i]);
            Vector<String> row = new Vector<String>(2);
            row.add(subscrib[i]);
            String s = "";
            if(PechkinPrivateData.isAddress(subscrib[i])){
                s = PechkinPrivateData.getName(subscrib[i]);
            }
            row.add(s);
            
            if(dtm != null){
                dtm.addRow(row);
        }
            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jScrollPane1 = new javax.swing.JScrollPane();
        listAddress = new javax.swing.JTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle"); // NOI18N
        setTitle(bundle.getString("SUBSCRIPTION MANAGEMENT")); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setPreferredSize(new java.awt.Dimension(100, 46));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/add-32.png"))); // NOI18N
        jButton1.setToolTipText(bundle.getString("ADD SUBSCRIBED")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        listAddress.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Address", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        listAddress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listAddressMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listAddressMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(listAddress);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createPopupMenu(java.awt.event.MouseEvent evt){
        JMenuItem name = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SET NAME"));
        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String address = addressSubscrib[rowSelect[0]];
                String name = PechkinPrivateData.getName(address);
                createDialogSetName(address,name,name);
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        JMenuItem delete = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELETE SUBSCRIBE"));
        delete.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                deleteSubscribe();
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        JPopupMenu jpu = new JPopupMenu();
        if(this.rowSelect.length == 1){
            jpu.add(name);
        }
        
        if( 0 < this.rowSelect.length){
            jpu.add(delete);
        }
        
        jpu.show(evt.getComponent(), evt.getX(), evt.getY());
    }
    
    private void createDialogSetName(String address, String name,String title) {
        String ret = JOptionPane.showInputDialog("Change name for address " + name,title);
        if ((ret != null)) {
            if (ret.length() != 0) {
                BMLog.LogD(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("PECHKINPRIVATENAME"), "Addressbook " + address + " change name " + ret);
                if(!PechkinPrivateData.isName(ret)){
                    PechkinPrivateData.addOrChangeAddressbok(address, ret);
                    updateList();
                    PechkinMainWindow.updateListMessage();
                } else{
                    JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("THIS NAME IS PRESSED"),java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ERROR ADDRESSBOKK"),JOptionPane.ERROR_MESSAGE);
                    createDialogSetName(address,name,ret);
                }

            }
        }
    }
    
    private void deleteSubscribe(){
        String[] delete = new String[this.rowSelect.length];
        String s = "\n";
        for(int i = 0; i < delete.length;i++){
            //System.out.println(" row "+rowSelect[i]+" "add);
            delete[i] = this.addressSubscrib[rowSelect[i]];
            if(PechkinPrivateData.isAddress(delete[i])){
                s += PechkinPrivateData.getName(delete[i])+"\n";
            } else{
                s += delete[i]+"\n";
            }
            
        }
        //s = s.substring(0,s.length()-1);
        
        int pane = JOptionPane.showConfirmDialog(null, "are you sure unsibscribed " + s, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("MANAGE SUBSCRIBE "), JOptionPane.YES_NO_OPTION);
        if (pane == JOptionPane.YES_OPTION) {
                    //PechkinPrivateData.deleteBMAddress(s);
                    //updateAddressTree();
                    for(int i = 0; i < delete.length;i++){
                        PechkinPrivateData.deleteSubscribe(delete[i]);
                    }
                    updateList();
        } else {
                    BMLog.LogD(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("PECHKINMAINWINDOW"), java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("USER CANCELED DELETE ADDRESS"));
       }
        
        
    }
    
    private void listAddressMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAddressMousePressed
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            createPopupMenu(evt);
        }
    }//GEN-LAST:event_listAddressMousePressed

    private void listAddressMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAddressMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            createPopupMenu(evt);
        }
    }//GEN-LAST:event_listAddressMouseReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String ret = JOptionPane.showInputDialog(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("INPUT BITMESSAGE ADDRESS SUBSCRIBED"));
        if(ret != null){
            if((new BMAddressUtils()).getVersionAddressBM(ret) < 4){
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("THIS VERSION PECHKIN NOT SUPPORTED VERSIONS ADDRESS LESS 4"), java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ERROR MANAGE SUBSCRIBED"), JOptionPane.ERROR_MESSAGE);
                        return;
                        
            }
            
            if(!(new BMAddressUtils().checkAddressBM(ret))){
                JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("INPUT NOT VALID BITMESSAGE ADDRESS"),java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ERROR SUBSCRIB"),JOptionPane.ERROR_MESSAGE);
            } else{
                String[] test = PechkinPrivateData.getSubscribed();
                boolean flag = true;
                for(int i =0; (flag)&&(i < test.length);i++){
                    if(test[i].contains(ret)){
                        flag = false;
                    }
                }
                if(flag){
                    PechkinPrivateData.addSubscribe(ret);
                    updateList();
                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

  
    public static void view() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PechkinManageSubscribed.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PechkinManageSubscribed.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PechkinManageSubscribed.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PechkinManageSubscribed.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        PechkinManageSubscribed manage = new PechkinManageSubscribed();
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                manage.setVisible(true);
                manage.updateList();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTable listAddress;
    // End of variables declaration//GEN-END:variables
}