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
import java.awt.Font;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author fenenko
 */
public class PechkinTreeAddressRender extends DefaultTreeCellRenderer {
    private final String label_inbox = java.util.ResourceBundle.getBundle("pro/fenenko/Pechkin/internalization/Bundle").getString("INBOX");
    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);

        String s = value.toString();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        boolean flag = (s.contains("("))&&(s.contains(")"));
        if(0 < node.getChildCount()){
            s = node.getChildAt(0).toString();
            if(s.contains(label_inbox)){
                flag = flag || s.contains("(");
            }
        }
        if(flag){
            Font f = this.getFont();
            f = f.deriveFont(Font.BOLD);
            this.setFont(f);
        } else{
            Font f = this.getFont();
            f = f.deriveFont(Font.PLAIN);
            this.setFont(f);
            
        }

        

        return this;
    }
    
}
