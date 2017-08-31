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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import pro.fenenko.Bitmessage.BMConstant;

/**
 *
 * @author fenenko
 */
public class PechkinListMessageRenderer extends JTable {
    
    public synchronized Component prepareRenderer(TableCellRenderer renderer,int rowIndex, int vColIndex) {
        Component rComp = super.prepareRenderer(renderer, rowIndex, vColIndex);
        long id = Long.valueOf((String)super.getModel().getValueAt(rowIndex, 5));
        PechkinMessage msg = PechkinPrivateData.getMessage(id);
        if(msg.getStatus() == BMConstant.MESSAGE_RECEIV_UNREAD){
            Font font = rComp.getFont();
            font = font.deriveFont(Font.BOLD);
            rComp.setFont(font);
        } 
        return rComp;
    
    }
    
}
