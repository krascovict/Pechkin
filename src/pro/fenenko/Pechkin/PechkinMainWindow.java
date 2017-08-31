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

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.crypto.Data;
import pro.fenenko.Bitmessage.BMAddress;
import pro.fenenko.Bitmessage.BMAddressUtils;
import pro.fenenko.Bitmessage.BMConstant;
import pro.fenenko.Bitmessage.BMLog;
import pro.fenenko.Bitmessage.BMParse;
import pro.fenenko.Bitmessage.BMUtils;

/**
 *
 * @author Fenenko Aleksandr
 */
public class PechkinMainWindow extends javax.swing.JFrame {

    private final String label_inbox = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("INBOX");
    private final String label_outbox = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("OUTBOX");
    private final String label_subscribe = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBSCRIBTIONS");

    public DefaultMutableTreeNode addressTree;
    public TreePath selectPath = null;
    private String selectAddress = "";
    public String[] selectPathAddress = new String[4];
    public int countSelectPathAddress = 0;
    private static Object lock = new byte[0];
    private String[] sSelectPath = new String[0];
    private PechkinTextClipboardTransfer clipboardTransfer = new PechkinTextClipboardTransfer();
    private TableColumn columnSubjection = null;
    private TableColumn columnAddressFrom = null;
    private TableColumn columnAddressTo = null;
    private TableColumn columnID = null;
    private boolean hideColumnAddressFrom = false;
    private boolean hideColumnAddressTo = false;
    private long selectIdMessage = 0;
    private boolean flagConnect = true;
    private String filteringStr = "";
    private int countUnread = 0;

    /**
     * Creates new form PechkinMainWindow
     */
    public PechkinMainWindow() {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/ic_launcher_144.png")).getImage());
        for (int i = 0; i < selectPathAddress.length; i++) {
            selectPathAddress[i] = "";
        }
        columnSubjection = listMessageTable.getColumnModel().getColumn(0);
        columnAddressFrom = listMessageTable.getColumnModel().getColumn(1);
        columnAddressTo = listMessageTable.getColumnModel().getColumn(2);
        columnID = listMessageTable.getColumnModel().getColumn(5);
        listMessageTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = listMessageTable.rowAtPoint(e.getPoint());
                int col = listMessageTable.columnAtPoint(e.getPoint());
                System.out.println("click in row=" + row + " col=" + col + " ");
                if (row != -1) {
                    selectIdMessage = Long.valueOf((String) listMessageTable.getValueAt(row, 5));
                    PechkinMessage msg = PechkinPrivateData.getMessage(selectIdMessage);
                    if (msg.getStatus() == BMConstant.MESSAGE_RECEIV_UNREAD) {
                        msg.setStatus(BMConstant.MESSAGE_RECEIV_READ);
                        PechkinPrivateData.updateMessage(msg);
                        updateMessage();
                    }
                    addressFrom.setText(PechkinPrivateData.getName(msg.getAddressFrom()));
                    addressTo.setText(PechkinPrivateData.getName(msg.getAddressTo()));
                    titleText.setText(msg.getSubject());
                    dateText.setText(formatDate(msg.getTime()));
                    textMessage.setText(msg.getBody());
                    replyMessage.setEnabled(true);
                    forwardMessage.setEnabled(true);
                    deleteMessage.setEnabled(true);
                    listMessageTable.setRowSelectionInterval(row, row);
                }
                AddressTree.updateUI();
            }
            //AddressTree.
            //this.Ad
        });
        listMessageTable.setRowSorter(new TableRowSorter(listMessageTable.getModel()));

    }

    public boolean isConnect() {
        return flagConnect;
    }

    private synchronized String formatStatus(int status) {
        String ret = "";
        switch (status) {
            case BMConstant.MESSAGE_RECEIV_UNREAD:
                ret = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("UNREAD");
                break;
            case BMConstant.MESSAGE_CREATE:
                ret = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("START CREATE");
                break;
            case BMConstant.MESSAGE_SEND:
                ret = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SEND");
                break;
            case BMConstant.MESSAGE_FIND_PUBKEY:
                ret = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("FIND KEY");
                break;
            case BMConstant.MESSAGE_DELIVERY:
                ret = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELIVERED");
                break;
            case BMConstant.MESSAGE_RECEIV_READ:
                break;
        }
        return ret;
    }

    private synchronized String formatDate(long date) {
        if (date < 9999999999L) {
            date = date * 1000L;
        }
        Date d = new Date(date);
        Date t = new Date((System.currentTimeMillis() - d.getTime()));
        SimpleDateFormat format = null;
        if (t.getTime() < 7 * 24 * 60 * 60 * 1000L) {
            format = new SimpleDateFormat("EEE HH:mm:ss");
        } else {
            if (t.getTime() < 30 * 60 * 60 * 1000L) {
                format = new SimpleDateFormat("MMM d HH:mm:ss");
            } else {
                format = new SimpleDateFormat("YY MMM d HH:mm:ss");
            }
        }
        return format.format(date);
    }

    public synchronized void updateMessage() {
        countUnread = 0;
        synchronized (lock) {
            //updateAddressTreeUnread(0);
            String selectAddressFrom = "";
            String selectAddressTo = "";
            String t = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ALL");
            boolean selectAll = false;

            for (int i = 0; i < sSelectPath.length; i++) {
                System.out.println("select path " + i + " " + sSelectPath[0]);

                if (!sSelectPath[i].contains(",")) {//(sSelectPath[i].contains(t+"]"))||(sSelectPath[i].contains(t+") "))){
                    System.out.println("select all");
                    selectAll = true;
                } else {
                    String s = sSelectPath[i];
                    int t1 = s.indexOf(", ") + 2;
                    System.out.println(sSelectPath[i].substring(t1));
                    s = sSelectPath[i].substring(t1);
                    boolean f1 = s.contains(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("INBOX"));
                    boolean f2 = s.contains(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("OUTBOX"));
                    if ((f1 == false) && (f2 == false)) {
                        f1 = true;
                        f2 = true;
                    }
                    if (f1) {
                        hideColumnAddressFrom = false;
                    } else {
                        hideColumnAddressFrom = true;
                    }
                    if (f2) {
                        hideColumnAddressTo = false;
                    } else {
                        hideColumnAddressTo = true;
                    }
                    t1 = s.indexOf(",");
                    if (t1 == -1) {
                        t1 = s.indexOf("]");
                        if (t1 == -1) {
                            t1 = s.length();
                        }
                    }
                    s = s.substring(0, t1);
                    System.out.println("address: " + s);
                    if (f1) {
                        selectAddressFrom += s;
                    }
                    if (f2) {
                        selectAddressTo += s;
                    }
                }
                if(sSelectPath[i].contains(label_subscribe)){
                    selectAddressFrom += BMConstant.ADDRESS_BROADCAST;
                    
                }
                    
            }
            if(selectAll){
                hideColumnAddressFrom = false;
                hideColumnAddressTo = false;
            }
            DefaultTableModel dtm = (DefaultTableModel) listMessageTable.getModel();
            //region clear message table
            while (0 < dtm.getRowCount()) {
                dtm.removeRow(0);
            }
            //endregion
            long[] ids = PechkinPrivateData.getMessageIDs();
            String[] addresses = new String[PechkinPrivateData.getCountBMAddress() + 1];
            int[] countUnreadAddr = new int[addresses.length];
            for (int i = 0; i < countUnreadAddr.length - 1; i++) {
                addresses[i] = PechkinPrivateData.getName(PechkinPrivateData.getBMAddress(i).getAddress());
                countUnreadAddr[i] = 0;
            }
            addresses[addresses.length - 1] = BMConstant.ADDRESS_BROADCAST;
            countUnreadAddr[addresses.length - 1] = 0;
            for (int i = 0; i < ids.length; i++) {
                PechkinMessage msg = PechkinPrivateData.getMessage(ids[i]);
                if (msg.getStatus() == BMConstant.MESSAGE_RECEIV_UNREAD) {
                    countUnread++;
                    for (int j = 0; j < addresses.length; j++) {
                        if (PechkinPrivateData.getAddress(addresses[j]).contains(msg.getAddressTo())) {
                            countUnreadAddr[j]++;
                        }
                    }

                }
                boolean flag = (selectAll) || (selectAddressFrom.contains(PechkinPrivateData.getName(msg.getAddressTo())))
                        || (selectAddressTo.contains(PechkinPrivateData.getName(msg.getAddressFrom())));
                if ((textFilter.getText().length() != 0) && (flag)) {
                    String s = textFilter.getText();
                    if (msg.getSubject().contains(s)
                            || msg.getAddressFrom().contains(s)
                            || msg.getAddressTo().contains(s)
                            || msg.getBody().contains(s)
                            || PechkinPrivateData.getName(msg.getAddressFrom()).contains(s)
                            || PechkinPrivateData.getName(msg.getAddressTo()).contains(s)) {
                        flag = true;
                    } else {
                        flag = false;
                    }

                }
                if (flag) {
                    Vector<String> row = new Vector<String>(4);
                    row.add(msg.getSubject());
                    row.add(PechkinPrivateData.getName(msg.getAddressFrom()));
                    row.add(PechkinPrivateData.getName(msg.getAddressTo()));
                    String s = formatDate(msg.getTime());
                    row.add(s);
                    row.add(formatStatus(msg.getStatus()));
                    row.add(String.valueOf(ids[i]));

                    if (dtm != null) {
                        dtm.addRow(row);
                    }
                }
            }
            if (hideColumnAddressFrom) {
                columnAddressFrom.setMaxWidth(0);
                columnAddressFrom.setMinWidth(0);
                columnAddressFrom.setPreferredWidth(0);
                listMessageTable.updateUI();
            } else {
                columnAddressFrom.setMaxWidth(columnSubjection.getMaxWidth());
                columnAddressFrom.setMinWidth(columnSubjection.getMinWidth());
                columnAddressFrom.setPreferredWidth(columnSubjection.getPreferredWidth());
                listMessageTable.updateUI();
            }
            if (hideColumnAddressTo) {
                columnAddressTo.setMaxWidth(0);
                columnAddressTo.setMinWidth(0);
                columnAddressTo.setPreferredWidth(0);
                listMessageTable.updateUI();
            } else {
                columnAddressTo.setMaxWidth(columnSubjection.getMaxWidth());
                columnAddressTo.setMinWidth(columnSubjection.getMinWidth());
                columnAddressTo.setPreferredWidth(columnSubjection.getPreferredWidth());
                listMessageTable.updateUI();
            }

            for (int i = 0; i < addressTree.getChildCount(); i++) {
                ((DefaultMutableTreeNode) addressTree.getChildAt(i).getChildAt(0)).setUserObject(label_inbox);
                boolean flag = true;
                for (int j = 0; (j < addresses.length) && flag; j++) {
                    if ((addressTree.getChildAt(i).toString().contains(addresses[j])) && (countUnreadAddr[j] != 0)) {
                        ((DefaultMutableTreeNode) addressTree.getChildAt(i).getChildAt(0)).setUserObject(label_inbox + " (" + countUnreadAddr[j] + ")");
                        flag = false;
                    }
                    if(addressTree.getChildAt(i).toString().contains(this.label_subscribe)&&(countUnreadAddr[j] != 0)&&(addresses[j].contains(BMConstant.ADDRESS_BROADCAST))){
                        ((DefaultMutableTreeNode) addressTree.getChildAt(i).getChildAt(0)).setUserObject(label_inbox + " (" + countUnreadAddr[j] + ")");
                        flag = false;
                        
                    }
                }

            }

            if (countUnread != 0) {
                addressTree.setUserObject(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ALL") + " (" + String.valueOf(countUnread) + ")");

            } else {
                addressTree.setUserObject(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ALL"));

            }
            //AddressTree.updateUI();

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
        newMessage = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        buttonConnectDisconnect = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        addressTree = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ALL"));
        AddressTree = new javax.swing.JTree(addressTree);
        PechkinTreeAddressRender renderer = new PechkinTreeAddressRender();
        AddressTree.setCellRenderer(renderer);
        jScrollPane2 = new javax.swing.JScrollPane();
        listMessageTable = new pro.fenenko.Pechkin.PechkinListMessageRenderer();
        titleText = new javax.swing.JTextField();
        addressFrom = new javax.swing.JTextField();
        dateText = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        textMessage = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jToolBar2 = new javax.swing.JToolBar();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(1, 0), new java.awt.Dimension(1, 0), new java.awt.Dimension(1, 32767));
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        replyMessage = new javax.swing.JButton();
        forwardMessage = new javax.swing.JButton();
        deleteMessage = new javax.swing.JButton();
        textFilter = new javax.swing.JTextField();
        addressTo = new javax.swing.JTextField();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        label1 = new java.awt.Label();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        fileNewAddress = new javax.swing.JMenuItem();
        addChan = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        importMenu = new javax.swing.JMenuItem();
        exportMenu = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        menuAddressbook = new javax.swing.JMenuItem();
        menuSubscriptions = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        menuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Pechkin");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jToolBar1.setFloatable(false);

        newMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/mail-message-new-32.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle"); // NOI18N
        newMessage.setToolTipText(bundle.getString("NEW MESSAGE")); // NOI18N
        newMessage.setEnabled(false);
        newMessage.setFocusable(false);
        newMessage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newMessage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMessageActionPerformed(evt);
            }
        });
        jToolBar1.add(newMessage);
        jToolBar1.add(filler2);

        buttonConnectDisconnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/internet-online-32.png"))); // NOI18N
        buttonConnectDisconnect.setToolTipText(bundle.getString("DISCONNECT")); // NOI18N
        buttonConnectDisconnect.setFocusable(false);
        buttonConnectDisconnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonConnectDisconnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonConnectDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConnectDisconnectActionPerformed(evt);
            }
        });
        jToolBar1.add(buttonConnectDisconnect);

        AddressTree.setAutoscrolls(true);
        AddressTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AddressTreeMousePressed(evt);
            }
        });
        AddressTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                AddressTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(AddressTree);

        listMessageTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Subject", "From", "To", "Date", "", "ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Long.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        listMessageTable.setToolTipText("");
        listMessageTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        listMessageTable.setFillsViewportHeight(true);
        listMessageTable.getTableHeader().setReorderingAllowed(false);
        listMessageTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                listMessageTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMessageTableMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listMessageTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listMessageTable);
        listMessageTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (listMessageTable.getColumnModel().getColumnCount() > 0) {
            listMessageTable.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("SUBJECT")); // NOI18N
            listMessageTable.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("FROM")); // NOI18N
            listMessageTable.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("TO")); // NOI18N
            listMessageTable.getColumnModel().getColumn(3).setHeaderValue(bundle.getString("DATE")); // NOI18N
            listMessageTable.getColumnModel().getColumn(5).setMinWidth(0);
            listMessageTable.getColumnModel().getColumn(5).setPreferredWidth(0);
            listMessageTable.getColumnModel().getColumn(5).setMaxWidth(0);
        }

        titleText.setEditable(false);

        addressFrom.setEditable(false);

        dateText.setEditable(false);

        textMessage.setEditable(false);
        jScrollPane3.setViewportView(textMessage);

        jLabel1.setText(bundle.getString("FROM")); // NOI18N

        jLabel2.setText(bundle.getString("SUBJECT")); // NOI18N

        jLabel3.setText(bundle.getString("TO")); // NOI18N

        jToolBar2.setFloatable(false);
        jToolBar2.setPreferredSize(new java.awt.Dimension(100, 46));
        jToolBar2.add(filler3);
        jToolBar2.add(filler8);
        jToolBar2.add(filler4);

        replyMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/mail-reply-sender-32.png"))); // NOI18N
        replyMessage.setToolTipText(bundle.getString("REPLY")); // NOI18N
        replyMessage.setEnabled(false);
        replyMessage.setFocusable(false);
        replyMessage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        replyMessage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        replyMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replyMessageActionPerformed(evt);
            }
        });
        jToolBar2.add(replyMessage);

        forwardMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/mail-forward-32.png"))); // NOI18N
        forwardMessage.setToolTipText(bundle.getString("FORWARD")); // NOI18N
        forwardMessage.setEnabled(false);
        forwardMessage.setFocusable(false);
        forwardMessage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        forwardMessage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        forwardMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardMessageActionPerformed(evt);
            }
        });
        jToolBar2.add(forwardMessage);

        deleteMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/delete-32.png"))); // NOI18N
        deleteMessage.setToolTipText(bundle.getString("DELETE")); // NOI18N
        deleteMessage.setEnabled(false);
        deleteMessage.setFocusable(false);
        deleteMessage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteMessage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMessageActionPerformed(evt);
            }
        });
        jToolBar2.add(deleteMessage);

        textFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFilterKeyReleased(evt);
            }
        });

        addressTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressToActionPerformed(evt);
            }
        });

        label1.setText("Messages filtering");

        jMenu2.setText(bundle.getString("FILE")); // NOI18N

        fileNewAddress.setText(bundle.getString("NEW ADDRESS")); // NOI18N
        fileNewAddress.setToolTipText(bundle.getString("GENERATE NEW RANDOM ADDRESS")); // NOI18N
        fileNewAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNewAddressActionPerformed(evt);
            }
        });
        jMenu2.add(fileNewAddress);

        addChan.setText(bundle.getString("ADD CHAN")); // NOI18N
        addChan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChanActionPerformed(evt);
            }
        });
        jMenu2.add(addChan);
        jMenu2.add(jSeparator1);

        importMenu.setText(bundle.getString("IMPORT PYBITMESSAGE KEYS")); // NOI18N
        importMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuActionPerformed(evt);
            }
        });
        jMenu2.add(importMenu);

        exportMenu.setText(bundle.getString("EXPORT PYBITMESSAGE KEYS")); // NOI18N
        exportMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuActionPerformed(evt);
            }
        });
        jMenu2.add(exportMenu);

        jMenuBar1.add(jMenu2);

        jMenu1.setText(bundle.getString("SETTINGS")); // NOI18N

        menuAddressbook.setText(bundle.getString("ADDRESSBOOK")); // NOI18N
        menuAddressbook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAddressbookActionPerformed(evt);
            }
        });
        jMenu1.add(menuAddressbook);

        menuSubscriptions.setText(bundle.getString("SUBSCIPTIONS")); // NOI18N
        menuSubscriptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSubscriptionsActionPerformed(evt);
            }
        });
        jMenu1.add(menuSubscriptions);

        jMenuItem1.setText(bundle.getString("SETTINGS")); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        menuHelp.setText(bundle.getString("HELP")); // NOI18N

        menuAbout.setText(bundle.getString("ABOUT")); // NOI18N
        menuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAboutActionPerformed(evt);
            }
        });
        menuHelp.add(menuAbout);

        jMenuBar1.add(menuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(292, 292, 292)
                        .addComponent(textFilter))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(filler7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressFrom)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleText, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateText, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE))
                            .addComponent(addressTo)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(filler7, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addressFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(titleText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(addressTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AddressTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_AddressTreeValueChanged
        newMessage.setEnabled(false);
        try {
            sSelectPath = new String[AddressTree.getSelectionPaths().length];
            for (int i = 0; i < sSelectPath.length;
                    sSelectPath[i] = AddressTree.getSelectionPaths()[i].toString(), System.out.println("select path " + i + " " + sSelectPath[i]), i++);
            if((sSelectPath.length == 1)&&(sSelectPath[0].contains(","))&&(!sSelectPath[0].contains(this.label_subscribe))){
                newMessage.setEnabled(true);
            }
        } catch (java.lang.NullPointerException ex) {

        }
        

        this.updateMessage();

    }//GEN-LAST:event_AddressTreeValueChanged

    public synchronized void updateAddressTree() {
        if (0 == PechkinPrivateData.getCountBMAddress()) {
            newMessage.setEnabled(false);
        } else {
            newMessage.setEnabled(true);
        }

        addressTree.removeAllChildren();
        for (int i = 0; i < PechkinPrivateData.getCountBMAddress(); i++) {
            String s = PechkinPrivateData.getName(PechkinPrivateData.getBMAddress(i).getAddress());

            DefaultMutableTreeNode addr = new DefaultMutableTreeNode(
                    s);
            addr.add(new DefaultMutableTreeNode(label_inbox));
            addr.add(new DefaultMutableTreeNode(label_outbox));
            addressTree.add(addr);

        }
        DefaultMutableTreeNode subs = new DefaultMutableTreeNode(label_subscribe);
        subs.add(new DefaultMutableTreeNode(" "));
        addressTree.add(subs);
        if (selectPath != null) {
            AddressTree.setAnchorSelectionPath(selectPath);
        }
        updateMessage();
        AddressTree.updateUI();
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
        System.err.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("WINDOW CLOSED"));
    }//GEN-LAST:event_formWindowClosed

    private void fileNewAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNewAddressActionPerformed
        
            // TODO add your handling code here:
            System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("NEW RANDOM ADDRESS"));
            BMAddress address = (new BMAddressUtils()).genBMKeyAddress(20);
            System.out.println(address.getAddress());
            PechkinPrivateData.addBMAddress(address);
           
            this.updateAddressTree();
        
    }//GEN-LAST:event_fileNewAddressActionPerformed

    private void listMessageTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMessageTableMouseClicked
        // TODO add your handling code here:
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("RIGHT CLICK"));

        }
    }//GEN-LAST:event_listMessageTableMouseClicked

    private void createPopupMenuTableMessage(java.awt.event.MouseEvent evt) {
        JMenuItem copyAddressFrom = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("COPY ADDRESS FROM"));
        JMenuItem nameAddressFrom = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("NAME ADDRESS FROM"));
        JMenuItem subscribeAddressFrom = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBCRIBE ON ADDRESS FROM"));
        JMenuItem copyAddressTo = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("COPY ADDRESS TO"));
        JMenuItem nameAddressTo = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("NAME ADDRESS TO"));
        JMenuItem subscribeAddressTo = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBCRIBE ON ADDRESS TO"));
        JMenuItem delete = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELETE SELECT MESSAGE"));
        JMenuItem resend = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("RESEND MESSAGE"));

        nameAddressFrom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String name = addressFrom.getText();
                String address = PechkinPrivateData.getAddress(name);
                BMLog.LogD("PechkinMainWindow", "SET NAME FOR ADDRESS " + name + " " + address);
                name = name + "(" + address + ")";
                String t = name;
                createDialogSetName(address, name, t);
            }
        });

        nameAddressTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String name = addressTo.getText();
                String address = PechkinPrivateData.getAddress(name);
                BMLog.LogD("PechkinMainWindow", "SET NAME FOR ADDRESS " + name + " " + address);
                name = name + "(" + address + ")";
                String t = name;
                createDialogSetName(address, name, t);
            }
        });

        copyAddressFrom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String s = addressFrom.getText();
                s = PechkinPrivateData.getAddress(s);
                BMLog.LogD(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("PECHKINMAINWINDOW"), "copy addressFrom " + s);
                clipboardTransfer.sendData(s);
            }
        });
        subscribeAddressFrom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String s = addressFrom.getText();
                s = PechkinPrivateData.getAddress(s);
                if ((new BMAddressUtils()).checkAddressBM(s)) {
                    BMLog.LogD("PechkinMainWindow", "SUBSCRIBE ON " + s);
                    PechkinPrivateData.addSubscribe(s);
                }
            }
        });
        subscribeAddressTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String s = addressTo.getText();
                s = PechkinPrivateData.getAddress(s);
                if ((new BMAddressUtils()).checkAddressBM(s)) {
                    BMLog.LogD("PechkinMainWindow", "SUBSCRIBE ON " + s);
                    PechkinPrivateData.addSubscribe(s);
                }
            }
        });

        copyAddressTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String s = addressTo.getText();
                s = PechkinPrivateData.getAddress(s);
                BMLog.LogD("PechkinMainWindow", "copy addressTo " + s);
                clipboardTransfer.sendData(s);
            }
        });
        resend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("RESEND MESSAGE"));
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                funcDeleteMessage();
            }
        });
        JPopupMenu jpu = new JPopupMenu();
        if (1 == listMessageTable.getSelectedRows().length) {
            if (!hideColumnAddressFrom) {
                jpu.add(nameAddressFrom);
                jpu.add(copyAddressFrom);
                jpu.add(subscribeAddressFrom);

            }
            if (!hideColumnAddressTo) {
                jpu.add(nameAddressTo);
                jpu.add(copyAddressTo);
                jpu.add(subscribeAddressTo);

            }
            //jpu.add(resend);

            if (0 < listMessageTable.getSelectedRows().length) {
                jpu.add(delete);
            }

        }
        jpu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void funcDeleteMessage() {
        System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELETE MESSAGES"));
        int[] selectMessage = listMessageTable.getSelectedRows();
        long[] IDs = new long[selectMessage.length];
        for (int i = 0; i < selectMessage.length; i++) {
            IDs[i] = Long.valueOf((String) listMessageTable.getValueAt(selectMessage[i], 5));
        }
        int pane = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ARE YOU SURE DELETED MESSAGE "), java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELETE MESSAGES "), JOptionPane.YES_NO_OPTION);
        if (pane == JOptionPane.YES_OPTION) {

            PechkinPrivateData.deleteMessages(IDs);
            updateMessage();
        } else {
            BMLog.LogD("PechkinMainWindow", "USER CANCELED");
        }

    }

    private void listMessageTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMessageTableMousePressed
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            createPopupMenuTableMessage(evt);
        }
    }//GEN-LAST:event_listMessageTableMousePressed

    private void listMessageTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMessageTableMouseReleased
        // TODO add your handling code here:
        if (evt.isPopupTrigger()) {
            createPopupMenuTableMessage(evt);
        }
    }//GEN-LAST:event_listMessageTableMouseReleased
    private void createDialogSetName(String address, String name, String title) {
        String ret = JOptionPane.showInputDialog("Change name for address " + name, title);
        if ((ret != null)) {
            if (ret.length() != 0) {
                BMLog.LogD("PechkinPrivateName", "Addressbook " + address + " change name " + ret);
                if (!PechkinPrivateData.isName(ret)) {
                    PechkinPrivateData.addOrChangeAddressbok(address, ret);
                    updateAddressTree();
                } else {
                    JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("THIS NAME IS PRESSED"), java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ERROR ADDRESSBOKK"), JOptionPane.ERROR_MESSAGE);
                    createDialogSetName(address, name, ret);
                }

            }
        }
    }

    private void createPopupMenuAddressTree(java.awt.event.MouseEvent evt) {
        JMenuItem nameAddress = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SET NAME THIS ADDRESS"));
        nameAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String address = PechkinPrivateData.getAddress(selectAddress);
                BMLog.LogD("PechkinMainWindow", "SET NAME FOR ADDRESS " + selectAddress + " " + address);

                String name = selectAddress;
                if (address.length() == 0) {
                    address = selectAddress;
                    name = "";
                }
                name = name + "(" + address + ")";
                String t = selectAddress;
                createDialogSetName(address, name, t);

                //System.out.println(ret);
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        JMenuItem addSubscribe = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("SUBSCRIPTION MANAGEMENT"));
        addSubscribe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                PechkinManageSubscribed.view();
                //JOptionPane.showInputDialog("Input address new subscribe");
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        JMenuItem copyAddress = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("COPY ADDRESS"));
        copyAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String s = sSelectPath[0];
                s = s.substring(s.indexOf(",") + 2);
                int i = s.indexOf(",");
                if (i == -1) {
                    i = s.indexOf("]");
                }
                s = s.substring(0, i);

                s = PechkinPrivateData.getAddress(s);
                BMLog.LogD("PechkinMainWindow", " COPY ADDRESS" + s);
                clipboardTransfer.sendData(s);
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        JMenuItem deleteAddress = new JMenuItem(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELETE ADDRESS"));
        deleteAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String s = sSelectPath[0];
                s = s.substring(s.indexOf(",") + 2);
                int i = s.indexOf(",");
                if (i == -1) {
                    i = s.indexOf("]");
                }
                s = s.substring(0, i);
                s = PechkinPrivateData.getAddress(s);
                System.out.println("DELETE ADDRESS " + s);
                int pane = JOptionPane.showConfirmDialog(null, "are you sure deleted " + s, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DELETE ADDRESS "), JOptionPane.YES_NO_OPTION);
                if (pane == JOptionPane.YES_OPTION) {
                    PechkinPrivateData.deleteBMAddress(s);
                    updateAddressTree();
                } else {
                    BMLog.LogD("PechkinMainWindow", "USER CANCELED DELETE ADDRESS");
                }

                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        JPopupMenu jpu = new JPopupMenu();
        if (this.sSelectPath != null) {
            //System.out.println("SSELECT  "+sSelectPath[0]);
            if ((1 == this.sSelectPath.length) && (sSelectPath[0].contains(",")) && (!sSelectPath[0].contains(label_subscribe))) {
                String s = sSelectPath[0];
                s = s.substring(s.indexOf("All,") + 4, s.length());
                int c = s.indexOf(",");
                if (c == -1) {
                    c = s.indexOf("]");
                }
                if (c != -1) {
                    s = s.substring(0, c);
                }
                System.out.println("" + s);
                while (s.indexOf(" ") == 0) {
                    s = s.substring(1);
                }
                selectAddress = s;
                jpu.add(nameAddress);
                jpu.add(copyAddress);
                jpu.add(deleteAddress);

            }
            if (sSelectPath[0].contains(this.label_subscribe)) {
                selectAddress = "";
                jpu.add(addSubscribe);

            }

        }
        jpu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void AddressTreeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AddressTreeMousePressed
        // TODO add your handling code here:
        //System.out.println("POPPUP MENU");
        if (evt.isPopupTrigger()) {
            createPopupMenuAddressTree(evt);
        }
    }//GEN-LAST:event_AddressTreeMousePressed

    private void importMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuActionPerformed
        // TODO add your handling code here:
        JFileChooser fc;
        File f = new File(PechkinConfigDir.getPyBitmessagDir());
        if (f.exists()) {
            System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("IMPORT PYBITMESSAGE DIR"));
            fc = new JFileChooser(f);
        } else {
            System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("IMPORT HOME DIR"));
            fc = new JFileChooser();
        }
        int retVal;
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("PYBITMESAGE KEY"), "dat");
        fc.setFileFilter(filter);

        retVal = fc.showOpenDialog(this);
        if (retVal != JFileChooser.ABORT) {
            System.out.println(retVal + fc.getSelectedFile().getAbsolutePath());
            try {
                BufferedReader read = new BufferedReader(new FileReader(fc.getSelectedFile()));
                String s = read.readLine();
                String address = "";
                String sign_key = "";
                String ciph_key = "";
                String info = "";
                while (s != null) {
                    if (s.contains("[")) {
                        s = s.substring(1);
                        s = s.substring(0, s.indexOf("]"));
                        address = s;
                        sign_key = "";
                        ciph_key = "";
                        BMLog.LogD("PechkinMainWindow", java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("IMPORT KEY FIND ADDRESS ") + address);
                    }
                    if (address.length() != 0) {
                        if (s.contains("privsigningkey = ")) {
                            s = s.substring("privsigningkey = ".length());
                            sign_key = s;
                        }
                        if (s.contains("privencryptionkey = ")) {
                            s = s.substring("privencryptionkey = ".length());
                            ciph_key = s;
                        }
                        if (sign_key.length() != 0 && ciph_key.length() != 0) {
                            BMUtils utils = new BMUtils();
                            BMAddressUtils aUtils = new BMAddressUtils();
                            byte[] sKey = utils.importKeyBitmessageFormat(sign_key);
                            byte[] cKey = utils.importKeyBitmessageFormat(ciph_key);
                            BMAddress addr = new BMAddress(sKey, cKey);
                            System.out.println(addr.getAddress() + " " + address);
                            if (!address.contains(addr.getAddress())) {
                                JOptionPane.showMessageDialog(null, fc.getSelectedFile().getAbsolutePath() + java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString(" IS NOT VALID PYBITMESSAGE FILE"), java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ERROR IMPORT KEYS"), JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            boolean flag = true;
                            for (int i = 0; (flag) && (i < PechkinPrivateData.getCountBMAddress()); i++) {
                                if (PechkinPrivateData.getBMAddress(i).getAddress().contains(address)) {
                                    flag = false;
                                }
                            }
                            if (flag) {
                                if (4 <= aUtils.getVersionAddressBM(addr.getAddress())) {
                                    PechkinPrivateData.addBMAddress(addr);
                                    info += addr.getAddress() + "\n";
                                } else {
                                    //JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString(" ADDRESS ")+addr.getAddress()+java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString(" NOT IMPORTING\N ADDRESS VERSION NOT SUPPORTED"),java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("IMPORT KEYS"),JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            address = "";
                            sign_key = "";
                            ciph_key = "";
                        }

                    }
                    s = read.readLine();
                }
                //JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString(" IMPORTING ADDRESS\N")+info,java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("IMPORT KEYS"),JOptionPane.INFORMATION_MESSAGE);
                this.updateAddressTree();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PechkinMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PechkinMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_importMenuActionPerformed

    private void exportMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuActionPerformed
        // TODO add your handling code here:
        JFileChooser fc;
        fc = new JFileChooser();

        int retVal;
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PyBitmesage key", "dat");
        fc.setFileFilter(filter);
        fc.setToolTipText(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("EXPORT KEYS IN PYBITMESSAGE FORMAT"));

        retVal = fc.showSaveDialog(this);
        if (retVal != JFileChooser.ABORT) {
            try {
                BufferedWriter write = new BufferedWriter(new FileWriter(fc.getSelectedFile()));
                String info = "";
                BMUtils utils = new BMUtils();
                for (int i = 0; i < PechkinPrivateData.getCountBMAddress(); i++) {
                    BMAddress addr = PechkinPrivateData.getBMAddress(i);
                    write.write("[" + addr.getAddress() + "]\n");
                    info += addr.getAddress() + "\n";
                    write.write("privsigningkey = " + utils.createKeyBitmessageFormat(addr.getPrivateSigningKey()) + "\n");
                    write.write("privencryptionkey = " + utils.createKeyBitmessageFormat(addr.getPrivateCipherKey()) + "\n");

                }
                write.close();
                //JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString(" EXPORT ADDRESS\N")+info+java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString(" IN FILE ")+fc.getSelectedFile().getAbsolutePath(),java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("EXPORT KEYS"),JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(PechkinMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }//GEN-LAST:event_exportMenuActionPerformed

    private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
        // TODO add your handling code here:
        PechkinAbout.view();
    }//GEN-LAST:event_menuAboutActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        PechkinSetting.view();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void menuSubscriptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSubscriptionsActionPerformed
        // TODO add your handling code here:
        PechkinManageSubscribed.view();
    }//GEN-LAST:event_menuSubscriptionsActionPerformed

    private void buttonConnectDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConnectDisconnectActionPerformed
        // TODO add your handling code here:
        this.flagConnect = !this.flagConnect;
        if (flagConnect) {
            buttonConnectDisconnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/internet-online-32.png")));
            buttonConnectDisconnect.setToolTipText(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("CONNECTED TO BITMESSAGE NETWORK"));
        } else {
            buttonConnectDisconnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/internet-offline-32.png")));
            buttonConnectDisconnect.setToolTipText(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("DISCONNECTED  TO BITMESSAGE NETWORK"));
        }
    }//GEN-LAST:event_buttonConnectDisconnectActionPerformed

    private void newMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMessageActionPerformed
        // TODO add your handling code here:
        System.err.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("START NEW MESSAGE"));
        System.out.println("select address "+this.selectAddress+" "+sSelectPath[0]);
        PechkinNewMessage.view(sSelectPath[0]);
    }//GEN-LAST:event_newMessageActionPerformed

    private void menuAddressbookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAddressbookActionPerformed
        // TODO add your handling code here:
        PechkinAddressbook.view();
    }//GEN-LAST:event_menuAddressbookActionPerformed

    private void addChanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChanActionPerformed
        // TODO add your handling code here:
        String ret = JOptionPane.showInputDialog(null, java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("ADD CHAN INPUT PASSERPHASE "), "");
        if (ret != null) {
            String passerphase = ret;
            BMAddressUtils aUtils = new BMAddressUtils();
            BMAddress address = aUtils.createChan("", ret);

            System.out.println(java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("CREATE CHAN ") + address.getAddress());
            PechkinPrivateData.addBMAddress(address);
            PechkinPrivateData.addOrChangeAddressbok(address.getAddress(), "Chan:\"" + ret + "\"");
            PechkinPrivateData.addPubKey(address.getAddress(), address.getPublicCipherKey(), 1000, 1000);
            updateAddress();
            updateAddressTree();

        }
    }//GEN-LAST:event_addChanActionPerformed

    private void replyMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replyMessageActionPerformed
        // TODO add your handling code here:
        PechkinNewMessage.view(selectIdMessage);
    }//GEN-LAST:event_replyMessageActionPerformed

    private void deleteMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMessageActionPerformed
        // TODO add your handling code here:
        funcDeleteMessage();
    }//GEN-LAST:event_deleteMessageActionPerformed

    private void forwardMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardMessageActionPerformed
        // TODO add your handling code here:
        PechkinNewMessage.viewForward(selectIdMessage);
    }//GEN-LAST:event_forwardMessageActionPerformed

    private static String oldTextFilter = "";

    private void textFilterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFilterKeyReleased
        // TODO add your handling code here:
        String t = textFilter.getText();
        if (!t.equals(oldTextFilter)) {
            oldTextFilter = t;
            updateMessage();
        }
    }//GEN-LAST:event_textFilterKeyReleased

    private void addressToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressToActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addressToActionPerformed

    /**
     * @param args the command line arguments
     */
    private static PechkinClient client;
    private static PechkinServer server;
    private static PechkinMainWindow window;

    public static synchronized void updateListMessage() {
        window.updateMessage();
    }

    public static synchronized void updateAddress() {
        window.updateAddressTree();
    }

    public static void startConnect() {
        server = new PechkinServer(PechkinConfig.getServerPort(), PechkinConfig.getServerConnect());
        server.start();

        client = new PechkinClient(PechkinConfig.getClientConnect());
        client.start();
    }

    public static void Disconnect() {
        client.Stop();
        server.Stop();
    }

    public static void main(String args[]) {
        //final PechkinMainWindow Main = new PechkinMainWindow();
        //.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/internet-online-32.png")));
        //Main.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/internet-online-32.png")));
        BMLog.setLogLevel(1);
        PechkinConfig.init();
        PechkinPrivateData.init();
        PechkinNodeAddress.init();

        try {
            PechkinData.init();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PechkinMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        PechkinTaskCreateMessage createMessage = new PechkinTaskCreateMessage();
        createMessage.start();
        PechkinWork work = new PechkinWork();
        work.start();

        startConnect();

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
            java.util.logging.Logger.getLogger(PechkinMainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PechkinMainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PechkinMainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PechkinMainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        window = new PechkinMainWindow();
        //
        //window.setIconImage(new Image(getClass().getResource("/pechkin/res/internet-online-32.png")));
        /* Create and display the form */
        //window.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pechkin/res/internet-online-32.png")));
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                System.out.println("MainWindow");

                window.setVisible(true);

                window.updateAddressTree();
                window.setVisible(true);
                window.updateAddressTree();
                window.AddressTree.setSelectionRow(0);

            }
        });

        System.out.println("MainWindowFinish");

        long targetMemoryGC = 100 * 1024 * 1024;
        boolean flagTriger = true;
        long timeout = System.currentTimeMillis();
        boolean flagLastStateConnect = window.isConnect();
        while (true) {
            //window.updateAddressTree();
            if (flagLastStateConnect != window.isConnect()) {
                if (window.isConnect()) {
                    startConnect();
                } else {
                    Disconnect();
                }
                flagLastStateConnect = window.isConnect();
            }
            try {
                //System.out.println("tick");
                Thread.sleep(500);

            } catch (InterruptedException ex) {
                //System.err.println("111");
                //Logger.getLogger(PechkinMainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTree AddressTree;
    private javax.swing.JMenuItem addChan;
    public javax.swing.JTextField addressFrom;
    private javax.swing.JTextField addressTo;
    private javax.swing.JButton buttonConnectDisconnect;
    public javax.swing.JTextField dateText;
    private javax.swing.JButton deleteMessage;
    private javax.swing.JMenuItem exportMenu;
    private javax.swing.JMenuItem fileNewAddress;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.JButton forwardMessage;
    private javax.swing.JMenuItem importMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private java.awt.Label label1;
    public javax.swing.JTable listMessageTable;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenuItem menuAddressbook;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuSubscriptions;
    public javax.swing.JButton newMessage;
    private javax.swing.JButton replyMessage;
    private javax.swing.JTextField textFilter;
    public javax.swing.JTextPane textMessage;
    public javax.swing.JTextField titleText;
    // End of variables declaration//GEN-END:variables
}
