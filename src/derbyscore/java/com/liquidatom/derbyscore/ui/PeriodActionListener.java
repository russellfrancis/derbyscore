package com.liquidatom.derbyscore.ui;

import com.liquidatom.derbyscore.domain.Bout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * A class which listens for user input events on the period combo box.
 *
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: PeriodActionListener.java 11 2010-04-03 03:43:27Z russ $
 */
@Immutable
@ThreadSafe
public class PeriodActionListener implements ActionListener {
    
    final private Bout bout;

    public PeriodActionListener(final Bout bout) {
        super();
        if (bout == null) {
            throw new NullPointerException("The parameter bout must be non-null.");
        }
        this.bout = bout;
    }

    public void actionPerformed(final ActionEvent e) {
        assertDispatchThread();
        Object eventSource = e.getSource();
        if (eventSource != null) {
            if (eventSource instanceof JComboBox) {
                ComboBoxModel model = ((JComboBox)eventSource).getModel();
                Object selectedItem = model.getSelectedItem();
                int i = 0;
                for (; i < model.getSize(); ++i) {
                    if (selectedItem.equals(model.getElementAt(i))) {
                        if (i == 0) {
                            bout.setOvertime(false);
                            bout.setPeriod(1);
                        }
                        else if (i == 1) {
                            bout.setOvertime(false);
                            bout.setPeriod(2);
                        }
                        else if (i == 2) {
                            bout.setOvertime(true);
                            bout.setPeriod(2);
                        }
                        break;
                    }
                }
            }
            else {
                throw new IllegalArgumentException("We are only capable of handling actions on JComboBox instances.");
            }
        }
    }

    protected void assertDispatchThread() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("This method must be invoked from within the AWT Event Dispatch Thread!");
        }
    }
}
