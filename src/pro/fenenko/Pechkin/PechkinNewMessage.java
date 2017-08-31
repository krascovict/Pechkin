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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import pro.fenenko.Bitmessage.BMAddressUtils;
import pro.fenenko.Bitmessage.BMConstant;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMMessage;
import pro.fenenko.Bitmessage.BMParse;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinNewMessage extends javax.swing.JFrame {

    private static final ResourceBundle pechkinNewMessageInter = ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle");
    private BMUtils utils = new BMUtils();
    private BMAddressUtils aUtils = new BMAddressUtils();
    private final String strSubscription = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBSCRIPTION");
    private final String strRMsg = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("REDIRECT MESSAGE");
    private final String strSubject = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBJECT");
    private final String strDate = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DATE");
    private final String strFrom = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("FROM");
    private final String strAnswer = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ANSWER");
    private final String strTo = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("TO");
    private final String strWrites = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("WRITES");
    private final String strFwd = "Fwd:";
    /**
     * Creates new form PechkinNewMessage
     */
    private void createAddressFrom(String address){
        AddressFrom.removeAllItems();
        int count = PechkinPrivateData.getCountBMAddress();
        String s;
        int number = 0;
        System.out.println("address "+address);
        for(int i = 0; i < count;i++){
            
            s = PechkinPrivateData.getBMAddress(i).getAddress();
            s = PechkinPrivateData.getName(s);
            if(address.contains(s)){
                number = i;
            }
            AddressFrom.addItem(s);
            
        }
        //count = AddressFrom.getItemCount();
        //AddressFrom.addItem(PechkinPrivateData.getName(address));
        AddressFrom.setSelectedIndex(number);
        AddressFrom.updateUI();
    }
    private void createAddressTo(String address){
        String addAddress = "";
        String a;
        int add = 0;
        for(int i = 0; i < PechkinPrivateData.getCountAddressbook();i++){
                    a = PechkinPrivateData.getAddress(i);
                    if((!addAddress.contains(a))&&(!address.contains(a))){
                        addAddress += a;
                        AddressTo.addItem(PechkinPrivateData.getName(a));
                        add++;
                    }
                }
                for(int i = 0; i < PechkinPrivateData.getCountPubKey();i++){
                    a = PechkinPrivateData.getPubkeyAddress(i);
                    if((!addAddress.contains(a))&&(!address.contains(a))){
                        AddressTo.addItem(a);
                        add++;
                    }
                }
                
                AddressTo.addItem(strSubscription);
                add++;
                
                int count = AddressTo.getItemCount();
                for(int i = 0; i < count-add;i++){
                    AddressTo.removeItemAt(0);
                    
                }
                if(address.length() != 0){
                    int i = AddressTo.getItemCount();
                    AddressTo.addItem(PechkinPrivateData.getName(address));
                    AddressTo.setSelectedIndex(i);
                
                }
        
    }
    private void createAddressLists(String address){
        createAddressFrom(address);
        createAddressTo("");
    }
    public PechkinNewMessage(String address) {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/ic_launcher_144.png")).getImage());
        createAddressLists(address);
       System.out.println();
               
        
    }
    
    public PechkinNewMessage(long id, boolean flagForward){
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/ic_launcher_144.png")).getImage());
        PechkinMessage msg = PechkinPrivateData.getMessage(id);
        String title = "";
        if(msg.getSubject().indexOf(strFwd) != 0 ){
            title = strFwd+msg.getSubject();
        }
        textTitle.setText(title);
        createAddressFrom(msg.getAddressFrom());
        createAddressTo("     ");
        Date d = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("YYYY MM d HH:mm:ss");
        String s = "-----"+strRMsg+"-----\n";
        s = s+strSubject+":\t"+ msg.getSubject()+"\n";
        s += strDate+":\t"+format.format(d)+"\n";
        s += strFrom+":\t"+msg.getAddressFrom()+"\n";
        s += strAnswer+":\t"+msg.getAddressFrom()+"\n";
        s += strTo+":\t"+msg.getAddressTo()+"\n\n";
        s += msg.getBody();
        textBody.setText(s);
    }
    
    public PechkinNewMessage(long id) {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/ic_launcher_144.png")).getImage());
        PechkinMessage msg = PechkinPrivateData.getMessage(id);
        createAddressLists(PechkinPrivateData.getName(msg.getAddressFrom()));
        
        String title = "";
        
        title = "Re:"+msg.getSubject();
       
        textTitle.setText(title);
        boolean f = false;
        BMLog.LogD("PechkinNewMessage",msg.getAddressFrom()+" -> "+msg.getAddressTo());
        for(int i = 0; (i < PechkinPrivateData.getCountBMAddress())&&(!f);i++){
            if(msg.getAddressTo().contains(PechkinPrivateData.getBMAddress(i).getAddress())){
                f = true;
            } else{
                //System.out.println(msg.getAddressTo()+" "+PechkinPrivateData.getBMAddress(i).getAddress());
            }
        }
        String s;
        if(f){
            //BMLog.LogD("PechkinNewMessage",msg.getAddressFrom()+" -> "+msg.getAddressTo());
            createAddressFrom(msg.getAddressTo());
            createAddressTo(msg.getAddressFrom());
             Date d = new Date(System.currentTimeMillis());
             SimpleDateFormat format = new SimpleDateFormat("YYYY MM d HH:mm:ss");
             s = format.format(msg.getTime()*1000);
             s = s + " "+msg.getAddressFrom();
             s = (s+" "+strWrites+"\n\n");
          
        } else{
            //BMLog.LogD("PechkinNewMessage",msg.getAddressFrom()+" -> "+msg.getAddressTo());
            createAddressFrom(msg.getAddressFrom());
            createAddressTo(msg.getAddressTo());
             Date d = new Date(System.currentTimeMillis());
             SimpleDateFormat format = new SimpleDateFormat("YYYY MM d HH:mm:ss");
             s = format.format(msg.getTime()*1000);
             s = s + " "+msg.getAddressFrom();
             s = (s+" "+strWrites+"\n\n");
        }
        String text = msg.getBody();
             
             int c = text.indexOf("\n");
             if(c == -1){
                 s += "> "+text;
             }
             while(c != -1){
                 s += ">  "+text.substring(0,c)+"\n";
                 text = text.substring(c+1,text.length());
                 c = text.indexOf("\n");
             }
             textBody.setText(s);
        
       
               
        
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jToolBar1 = new javax.swing.JToolBar();
        sendMessage = new javax.swing.JButton();
        labelAddressFrom = new javax.swing.JLabel();
        labelAddressTo = new javax.swing.JLabel();
        AddressFrom = new javax.swing.JComboBox<>();
        AddressTo = new javax.swing.JComboBox<>();
        labelTitle = new javax.swing.JLabel();
        textTitle = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        textBody = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        ttlDay = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();

        setTitle(pechkinNewMessageInter.getString("NEW MESSAGE")); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMinimumSize(new java.awt.Dimension(125, 46));

        sendMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/mail-message-send-32.png"))); // NOI18N
        sendMessage.setToolTipText(pechkinNewMessageInter.getString("SEND MESSAGE")); // NOI18N
        sendMessage.setEnabled(false);
        sendMessage.setFocusable(false);
        sendMessage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sendMessage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sendMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendMessageActionPerformed(evt);
            }
        });
        jToolBar1.add(sendMessage);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle"); // NOI18N
        labelAddressFrom.setText(bundle.getString("FROM")); // NOI18N

        labelAddressTo.setText(pechkinNewMessageInter.getString("TO")); // NOI18N

        AddressFrom.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        AddressTo.setEditable(true);
        AddressTo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        AddressTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddressToActionPerformed(evt);
            }
        });

        labelTitle.setText(pechkinNewMessageInter.getString("SUBJECT")); // NOI18N
        labelTitle.setToolTipText("");

        textTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textTitleActionPerformed(evt);
            }
        });

        textBody.setColumns(20);
        textBody.setRows(5);
        jScrollPane1.setViewportView(textBody);

        jLabel1.setText(bundle.getString("TTL")); // NOI18N

        ttlDay.setModel(new javax.swing.SpinnerNumberModel(1, 1, 28, 1));

        jLabel2.setText(pechkinNewMessageInter.getString("DAY")); // NOI18N
        jLabel2.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelAddressTo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelAddressFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AddressFrom, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(AddressTo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ttlDay, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(labelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(326, 326, 326))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(textTitle))))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AddressFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelAddressFrom))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AddressTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(ttlDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(labelAddressTo)
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelTitle)
                            .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AddressToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddressToActionPerformed
        // TODO add your handling code here:
        String s = AddressTo.getSelectedItem().toString();
        if(s.contains(strSubscription)||PechkinPrivateData.isName(s)){
            sendMessage.setEnabled(true);
            return;
        }
        sendMessage.setEnabled((new BMAddressUtils()).checkAddressBM(s));
    }//GEN-LAST:event_AddressToActionPerformed

    private void sendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendMessageActionPerformed
        // TODO add your handling code here:
        BMLog.LogD("PechkinNewMessage", "SendMessage");
        BMParse parse = new BMParse();
        String address_from = AddressFrom.getSelectedItem().toString();
        address_from = PechkinPrivateData.getAddress(address_from);
        String address_to = AddressTo.getSelectedItem().toString();
        if(address_to.contains(strSubscription)){
            address_to = BMConstant.ADDRESS_BROADCAST;
        }
        address_to = PechkinPrivateData.getAddress(address_to);
        while(address_to.indexOf(" ") == 0){
            address_to.substring(1);
        }
        while(address_to.indexOf(" ") == address_to.length()-1){
            address_to.substring(address_to.length()-1);
        }
        while(address_from.indexOf(" ") == 0){
            address_from.substring(1);
        }
        while(address_from.indexOf(" ") == address_from.length()-1){
            address_from.substring(address_from.length()-1);
        }
        //while(address_fr)
        if((!aUtils.checkAddressBM(address_from)))
        {
            BMLog.LogE("PechkinNewMessage", "WRONG ADDRESS  "+address_from);
            return;
        }
        if(!((aUtils.checkAddressBM(address_to))||(address_to.contains(BMConstant.ADDRESS_BROADCAST))))
        {
            BMLog.LogE("PechkinNewMessage", "WRONG BMADDRESS "+address_to);
            return;
        }
        if(address_to.contains(strSubscription)){
            address_to = BMConstant.ADDRESS_BROADCAST;
        }
        BMMessage msg = new BMMessage(address_from
                ,address_to
                ,PechkinMessage.SUBJECT+textTitle.getText()+"\n"+PechkinMessage.BODY+textBody.getText()+"\n");
        msg.time = System.currentTimeMillis()/1000L+((int)ttlDay.getValue())*3600*24;
        PechkinPrivateData.addMessage(msg, BMConstant.MESSAGE_CREATE);
        BMLog.LogD("PechkinNewMessage", "add message");
        PechkinMainWindow.updateListMessage();
        this.dispose();
    }//GEN-LAST:event_sendMessageActionPerformed

    private void textTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textTitleActionPerformed

   
    public static void view(String address) {
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
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PechkinNewMessage window = new PechkinNewMessage(address);
                System.out.println("select address "+address);
                window.setVisible(true);
                
                
                
                
            }
        });
    }
    
    public static void view(long id) {
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
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PechkinNewMessage window = new PechkinNewMessage(id);
                window.setVisible(true);
                
                
                
                
            }
        });
    }
    public static void viewForward(long id) {
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
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PechkinNewMessage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PechkinNewMessage window = new PechkinNewMessage(id,true);
                window.setVisible(true);
                
                
                
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> AddressFrom;
    private javax.swing.JComboBox<String> AddressTo;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel labelAddressFrom;
    private javax.swing.JLabel labelAddressTo;
    private javax.swing.JLabel labelTitle;
    private javax.swing.JButton sendMessage;
    private javax.swing.JTextArea textBody;
    private javax.swing.JTextField textTitle;
    private javax.swing.JSpinner ttlDay;
    // End of variables declaration//GEN-END:variables
}
