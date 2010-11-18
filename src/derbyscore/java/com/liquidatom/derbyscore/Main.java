package com.liquidatom.derbyscore;

import com.liquidatom.derbyscore.domain.Bout;
import com.liquidatom.derbyscore.domain.Team;
import com.liquidatom.derbyscore.theme.Theme;
import com.liquidatom.derbyscore.theme.ThemeBuilder;
import com.liquidatom.derbyscore.theme.ThemeFactory;
import com.liquidatom.derbyscore.ui.ControlWindow;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Characteristics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public class Main implements Runnable {

    static private final Logger log = LoggerFactory.getLogger(Main.class);
    static private final int SUCCESS = 0;
    static private final int FAILURE = 1;

    static public void main(String[] args) throws Exception {
        // Ensure we are not in a headless environment.
        if (GraphicsEnvironment.isHeadless()) {
            if (log.isErrorEnabled()) {
                log.error("This application requires a graphical display, mouse and keyboard to operate.");
            }
            // Exit with a failure status
            System.exit(FAILURE);
        }

        // Try to set the default look and feel of the application.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Unable to set the native look and feel: " + e.getMessage());
            }

            JOptionPane.showMessageDialog(
                    null,
                    "Unable to set the native look and feel: " + e.getMessage(),
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        JFrame.setDefaultLookAndFeelDecorated(true);

        MutablePicoContainer pico = PicoContainerFactory.getInstance().get();
        pico.addComponent(Team.class);
        pico.as(Characteristics.CACHE).addComponent(Bout.class);

        // Create and register our global theme.
        ThemeBuilder themeBuilder = new ThemeBuilder();
        URL themeXml = Main.class.getResource("/themes/default/theme.xml");
        Theme theme = themeBuilder.buildTheme(themeXml);
        ThemeFactory.getInstance().setCurrent(theme);

        // Construct and show the control window.
        ControlWindow controlWindow = new ControlWindow();

        Main main = new Main(controlWindow);

        // Set the controlWindow to be visible in the AWT Event Thread.
        EventQueue.invokeLater(main);

        // Wait for it to exit and the close the application.
        controlWindow.awaitClosed();

        // Exit the application.
        System.exit(SUCCESS);
    }
    
    final private ControlWindow controlWindow;

    public Main(ControlWindow controlWindow) {
        if (controlWindow == null) {
            throw new NullPointerException("The parameter controlWindow must be non-null.");
        }
        this.controlWindow = controlWindow;
    }

    public void run() {
        controlWindow.setVisible(true);
    }
}
