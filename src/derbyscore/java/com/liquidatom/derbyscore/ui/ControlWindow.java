package com.liquidatom.derbyscore.ui;

import com.liquidatom.derbyscore.PicoContainerFactory;
import com.liquidatom.derbyscore.domain.Bout;
import com.liquidatom.derbyscore.domain.BoutListener;
import com.liquidatom.derbyscore.domain.Team;
import com.liquidatom.derbyscore.theme.Theme;
import com.liquidatom.derbyscore.theme.ThemeBuilder;
import com.liquidatom.derbyscore.theme.ThemeFactory;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public class ControlWindow extends JFrame implements BoutListener {

    static private final Logger log = LoggerFactory.getLogger(ControlWindow.class);

    private ScoreboardWindow scoreboardWindow;
    final private CountDownLatch latch = new CountDownLatch(1);

    public ControlWindow() throws IOException {
        initComponents();
        setLocationRelativeTo(null);

        final Runnable r = new Runnable() {

        public void run() {
        // Setup icons and logos.
        URL appLogo = ControlWindow.class.getResource("/gfx/brrg.png");
        URL themeIcon = ControlWindow.class.getResource("/gfx/theme.png");
        URL exitIcon = ControlWindow.class.getResource("/gfx/exit.png");
        URL aboutIcon = ControlWindow.class.getResource("/gfx/question.png");

        try {
            setIconImage(ImageIO.read(appLogo));
            loadThemeMenuItem.setIcon(new ImageIcon(ImageIO.read(themeIcon).getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
            quitMenuItem.setIcon(new ImageIcon(ImageIO.read(exitIcon).getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
            aboutMenuItem.setIcon(new ImageIcon(ImageIO.read(aboutIcon).getScaledInstance(22, 22, Image.SCALE_SMOOTH)));
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        BufferedImage teamALogo = getBout().getTeamA().getImage();
        if (teamALogo != null) {
            teamALogoButton.setIcon(new ImageIcon(teamALogo));
        }

        BufferedImage teamBLogo = getBout().getTeamB().getImage();
        if (teamBLogo != null) {
            teamBLogoButton.setIcon(new ImageIcon(teamBLogo));
        }

        startJamButton.setIcon(new ImageIcon(getClass().getResource("/gfx/ledgreen.png")));
        lineupButton.setIcon(new ImageIcon(getClass().getResource("/gfx/ledorange.png")));
        endJamButton.setIcon(new ImageIcon(getClass().getResource("/gfx/ledred.png")));
        timeoutButton.setIcon(new ImageIcon(getClass().getResource("/gfx/player_pause.png")));
        officialTimeoutButton.setIcon(new ImageIcon(getClass().getResource("/gfx/zebra_watch.png")));
        showScoreboardButton.setIcon(new ImageIcon(getClass().getResource("/gfx/scoreboard_32x32.png")));
        fullscreenButton.setIcon(new ImageIcon(getClass().getResource("/gfx/windows_fullscreen.png")));

        periodComboBox.addActionListener(new PeriodActionListener(getBout()));

        KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                boolean consumeKeyEvent = false;
                if (KeyEvent.KEY_PRESSED == e.getID()) {
                    int keyCode = e.getKeyCode();
                    if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
                        consumeKeyEvent = true;
                    }

                    if (e.isControlDown() && !e.isAltDown() && teamAJamPointsSpinner.isEnabled()) {
                        if (keyCode == KeyEvent.VK_UP) {
                            getBout().getTeamA().setJamPoints(getBout().getTeamA().getJamPoints() + 1);
                        }
                        else if (keyCode == KeyEvent.VK_DOWN) {
                            getBout().getTeamA().setJamPoints(Math.max(0, getBout().getTeamA().getJamPoints() - 1));
                        }
                        else if (keyCode == KeyEvent.VK_L) {
                            if (getBout().isTeamALead()) {
                                getBout().setLead(null);
                            }
                            else {
                                getBout().setLead(getBout().getTeamA());
                            }
                        }                    }
                    else if (!e.isControlDown() && e.isAltDown() && teamBJamPointsSpinner.isEnabled()) {
                        if (keyCode == KeyEvent.VK_UP) {
                            getBout().getTeamB().setJamPoints(getBout().getTeamB().getJamPoints() + 1);
                        }
                        else if (keyCode == KeyEvent.VK_DOWN) {
                            getBout().getTeamB().setJamPoints(Math.max(0, getBout().getTeamB().getJamPoints() - 1));
                        }
                        else if (keyCode == KeyEvent.VK_L) {
                            if (getBout().isTeamBLead()) {
                                getBout().setLead(null);
                            }
                            else {
                                getBout().setLead(getBout().getTeamB());
                            }
                        }
                    }
                    else if (keyCode == KeyEvent.VK_F1 && startJamButton.isEnabled()) {
                        startJam();
                    }
                    else if (keyCode == KeyEvent.VK_F2 && endJamButton.isEnabled()) {
                        endJam();
                    }
                }
                
                return !consumeKeyEvent ? super.dispatchKeyEvent(e) : true;
            }
        });

        jamTimeTextField.addKeyListener(new TimeTextFieldKeyListener(jamTimeTextField, getBout().getJamClock()));
        periodTimeTextField.addKeyListener(new TimeTextFieldKeyListener(periodTimeTextField, getBout().getPeriodClock()));

        teamAScoreSpinner.addChangeListener(new ScoreChangeListener(getBout().getTeamA()));
        teamAJamPointsSpinner.addChangeListener(new JamPointsChangeListener(getBout().getTeamA()));
        teamATimeoutSpinner.addChangeListener(new TimeoutChangeListener(getBout().getTeamA()));
        teamBScoreSpinner.addChangeListener(new ScoreChangeListener(getBout().getTeamB()));
        teamBJamPointsSpinner.addChangeListener(new JamPointsChangeListener(getBout().getTeamB()));
        teamBTimeoutSpinner.addChangeListener(new TimeoutChangeListener(getBout().getTeamB()));


        getBout().getTeamA().addListener(
                new ControlWindowTeamListener(teamALogoButton, teamALabel, teamAScoreSpinner, teamAJamPointsSpinner, teamATimeoutSpinner));
        getBout().getTeamB().addListener(
                new ControlWindowTeamListener(teamBLogoButton, teamBLabel, teamBScoreSpinner, teamBJamPointsSpinner, teamBTimeoutSpinner));
        getBout().markConfigured();

        }
        };

        getBout().addListener(this);
        EventQueue.invokeLater(r);
    }

    protected void windowClosed() {
        if (scoreboardWindow != null) {
            scoreboardWindow.dispose();
        }
        latch.countDown();
    }

    protected void changeLogo(Team team) {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

                BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                BufferedImage big = defaultConfiguration.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
                big.getGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), this);
                team.setImage(big);
            }
            catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Unable to set icon: " + e.getMessage(), e);
                }
            }
        }
    }

    protected void lineup() {
        getBout().lineup();
    }

    protected void startJam() {
        getBout().beginJam();
    }

    protected void endJam() {
        getBout().endJam();
    }

    protected void teamTimeout() {
        getBout().timeout();
    }

    protected void officialTimeout() {
        getBout().officialTimeout();
    }

    protected void showScoreboard() {
        if (scoreboardWindow == null) {
            showScoreboardButton.setText("Hide Scoreboard");
            scoreboardWindow = new ScoreboardWindow(getBout());
            scoreboardWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent evt) {
                    showScoreboardButton.setText("Show Scoreboard");
                    scoreboardWindow = null;
                }
            });
            scoreboardWindow.setVisible(true);
            getBout().addListener(scoreboardWindow);
        }
        else {
            scoreboardWindow.dispose();
        }
    }

    protected void fullscreenScoreboard() {
        if (scoreboardWindow != null) {
            if (!"Restore".equals(fullscreenButton.getText())) {
                fullscreenButton.setText("Restore");
                scoreboardWindow.setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
            else {
                fullscreenButton.setText("Maximize");
                scoreboardWindow.setExtendedState(JFrame.NORMAL);
            }
        }
    }

    public void awaitClosed() throws InterruptedException {
        latch.await();
    }

    protected void loadTheme() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.showOpenDialog(this);
            File selectedFile = chooser.getSelectedFile();

            // Create and register our global theme.
            if (selectedFile != null && selectedFile.isFile() && selectedFile.canRead()) {
                ThemeBuilder themeBuilder = new ThemeBuilder();
                Theme theme = themeBuilder.buildTheme(selectedFile.toURI().toURL());
                ThemeFactory.getInstance().setCurrent(theme);
            }
        }
        catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Unable to load suggested theme: " + e.getMessage(), e);
            }
        }
    }
    
    protected void displayAboutDialog() {
        final AboutDialog dialog = new AboutDialog();
        dialog.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainToolbar = new javax.swing.JToolBar();
        startJamButton = new javax.swing.JButton();
        lineupButton = new javax.swing.JButton();
        endJamButton = new javax.swing.JButton();
        timeoutButton = new javax.swing.JButton();
        officialTimeoutButton = new javax.swing.JButton();
        showScoreboardButton = new javax.swing.JButton();
        fullscreenButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        commonPanel = new javax.swing.JPanel();
        periodTimeTextField = new javax.swing.JTextField();
        jamTimeTextField = new javax.swing.JTextField();
        periodTimeLabel = new javax.swing.JLabel();
        jamTimeLabel = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();
        periodComboBox = new javax.swing.JComboBox();
        teamAPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        teamALabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        teamAScoreSpinner = new javax.swing.JSpinner();
        jPanel6 = new javax.swing.JPanel();
        teamALogoButton = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        teamATimeoutsLabel = new javax.swing.JLabel();
        teamATimeoutSpinner = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        teamAJamPointsSpinner = new javax.swing.JSpinner();
        teamBPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        teamBLabel = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        teamBScoreSpinner = new javax.swing.JSpinner();
        jPanel11 = new javax.swing.JPanel();
        teamBLogoButton = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        teamBTimeoutsLabel = new javax.swing.JLabel();
        teamBTimeoutSpinner = new javax.swing.JSpinner();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        teamBJamPointsSpinner = new javax.swing.JSpinner();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadThemeMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Derby Score");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        mainToolbar.setRollover(true);
        mainToolbar.setDoubleBuffered(true);

        startJamButton.setText("[F1] Start Jam");
        startJamButton.setDoubleBuffered(true);
        startJamButton.setEnabled(false);
        startJamButton.setFocusable(false);
        startJamButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startJamButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        startJamButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startJamButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(startJamButton);

        lineupButton.setText("Lineup");
        lineupButton.setDoubleBuffered(true);
        lineupButton.setEnabled(false);
        lineupButton.setFocusable(false);
        lineupButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lineupButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lineupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineupButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(lineupButton);

        endJamButton.setText("[F2] End Jam");
        endJamButton.setDoubleBuffered(true);
        endJamButton.setEnabled(false);
        endJamButton.setFocusable(false);
        endJamButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        endJamButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        endJamButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endJamButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(endJamButton);

        timeoutButton.setText("Timeout");
        timeoutButton.setDoubleBuffered(true);
        timeoutButton.setFocusable(false);
        timeoutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        timeoutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        timeoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeoutButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(timeoutButton);

        officialTimeoutButton.setText("Official Timeout");
        officialTimeoutButton.setDoubleBuffered(true);
        officialTimeoutButton.setEnabled(false);
        officialTimeoutButton.setFocusable(false);
        officialTimeoutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        officialTimeoutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        officialTimeoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                officialTimeoutButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(officialTimeoutButton);

        showScoreboardButton.setText("Show Scoreboard");
        showScoreboardButton.setDoubleBuffered(true);
        showScoreboardButton.setFocusable(false);
        showScoreboardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        showScoreboardButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        showScoreboardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoreboardToggleButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(showScoreboardButton);

        fullscreenButton.setText("Fullscreen");
        fullscreenButton.setFocusable(false);
        fullscreenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fullscreenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fullscreenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullscreenButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(fullscreenButton);

        getContentPane().add(mainToolbar, java.awt.BorderLayout.PAGE_START);

        mainPanel.setLayout(new java.awt.BorderLayout());

        periodTimeTextField.setEditable(false);
        periodTimeTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        periodTimeTextField.setText("30:00");
        periodTimeTextField.setDoubleBuffered(true);

        jamTimeTextField.setEditable(false);
        jamTimeTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jamTimeTextField.setText("02:00");
        jamTimeTextField.setDoubleBuffered(true);

        periodTimeLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 18));
        periodTimeLabel.setText("Period");
        periodTimeLabel.setDoubleBuffered(true);

        jamTimeLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 18));
        jamTimeLabel.setText("Jam");
        jamTimeLabel.setDoubleBuffered(true);

        messageLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 18));
        messageLabel.setForeground(new java.awt.Color(0, 0, 250));
        messageLabel.setText("Get Ready!");
        messageLabel.setDoubleBuffered(true);

        periodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Period 1", "Period 2", "Overtime" }));
        periodComboBox.setDoubleBuffered(true);

        javax.swing.GroupLayout commonPanelLayout = new javax.swing.GroupLayout(commonPanel);
        commonPanel.setLayout(commonPanelLayout);
        commonPanelLayout.setHorizontalGroup(
            commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commonPanelLayout.createSequentialGroup()
                .addGroup(commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, commonPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jamTimeLabel)
                            .addComponent(periodTimeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jamTimeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addComponent(periodTimeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, commonPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(periodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, commonPanelLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)))
                .addContainerGap())
        );
        commonPanelLayout.setVerticalGroup(
            commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commonPanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(periodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(periodTimeLabel)
                    .addComponent(periodTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(commonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jamTimeLabel)
                    .addComponent(jamTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(messageLabel)
                .addGap(231, 231, 231))
        );

        mainPanel.add(commonPanel, java.awt.BorderLayout.CENTER);

        teamAPanel.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.Y_AXIS));

        teamALabel.setText("Team A");
        teamALabel.setDoubleBuffered(true);
        jPanel1.add(teamALabel);

        jPanel7.add(jPanel1);

        teamAScoreSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        teamAScoreSpinner.setToolTipText("The number of points currently earned by team A in all finalized jams.");
        teamAScoreSpinner.setDoubleBuffered(true);
        teamAScoreSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(teamAScoreSpinner, ""));
        teamAScoreSpinner.setEnabled(false);
        teamAScoreSpinner.setMinimumSize(new java.awt.Dimension(120, 28));
        teamAScoreSpinner.setPreferredSize(new java.awt.Dimension(120, 28));
        jPanel4.add(teamAScoreSpinner);

        jPanel7.add(jPanel4);

        teamALogoButton.setDoubleBuffered(true);
        teamALogoButton.setMaximumSize(new java.awt.Dimension(120, 120));
        teamALogoButton.setMinimumSize(new java.awt.Dimension(120, 120));
        teamALogoButton.setPreferredSize(new java.awt.Dimension(120, 120));
        teamALogoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teamALogoButtonActionPerformed(evt);
            }
        });
        jPanel6.add(teamALogoButton);

        jPanel7.add(jPanel6);

        jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        teamATimeoutsLabel.setText("Timeouts");
        teamATimeoutsLabel.setDoubleBuffered(true);
        jPanel13.add(teamATimeoutsLabel);

        teamATimeoutSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 0, 3, 1));
        teamATimeoutSpinner.setDoubleBuffered(true);
        teamATimeoutSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(teamATimeoutSpinner, ""));
        teamATimeoutSpinner.setEnabled(false);
        jPanel13.add(teamATimeoutSpinner);

        jPanel7.add(jPanel13);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Jam Points");
        jLabel1.setDoubleBuffered(true);
        jPanel3.add(jLabel1);

        teamAJamPointsSpinner.setDoubleBuffered(true);
        teamAJamPointsSpinner.setEnabled(false);
        teamAJamPointsSpinner.setMinimumSize(new java.awt.Dimension(50, 26));
        teamAJamPointsSpinner.setNextFocusableComponent(teamBJamPointsSpinner);
        teamAJamPointsSpinner.setPreferredSize(new java.awt.Dimension(50, 26));
        jPanel3.add(teamAJamPointsSpinner);

        jPanel7.add(jPanel3);

        teamAPanel.add(jPanel7, java.awt.BorderLayout.NORTH);

        mainPanel.add(teamAPanel, java.awt.BorderLayout.WEST);

        teamBPanel.setLayout(new java.awt.BorderLayout());

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.Y_AXIS));

        teamBLabel.setText("Team B");
        teamBLabel.setDoubleBuffered(true);
        jPanel2.add(teamBLabel);

        jPanel9.add(jPanel2);

        teamBScoreSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        teamBScoreSpinner.setDoubleBuffered(true);
        teamBScoreSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(teamBScoreSpinner, ""));
        teamBScoreSpinner.setEnabled(false);
        teamBScoreSpinner.setMinimumSize(new java.awt.Dimension(120, 28));
        teamBScoreSpinner.setPreferredSize(new java.awt.Dimension(120, 28));
        jPanel10.add(teamBScoreSpinner);

        jPanel9.add(jPanel10);

        teamBLogoButton.setDoubleBuffered(true);
        teamBLogoButton.setMaximumSize(new java.awt.Dimension(120, 120));
        teamBLogoButton.setMinimumSize(new java.awt.Dimension(120, 120));
        teamBLogoButton.setPreferredSize(new java.awt.Dimension(120, 120));
        teamBLogoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teamBLogoButtonActionPerformed(evt);
            }
        });
        jPanel11.add(teamBLogoButton);

        jPanel9.add(jPanel11);

        jPanel12.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        teamBTimeoutsLabel.setText("Timeouts");
        teamBTimeoutsLabel.setDoubleBuffered(true);
        jPanel12.add(teamBTimeoutsLabel);

        teamBTimeoutSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 0, 3, 1));
        teamBTimeoutSpinner.setDoubleBuffered(true);
        teamBTimeoutSpinner.setEnabled(false);
        jPanel12.add(teamBTimeoutSpinner);

        jPanel9.add(jPanel12);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel2.setText("Jam Points");
        jLabel2.setDoubleBuffered(true);
        jPanel8.add(jLabel2);

        teamBJamPointsSpinner.setDoubleBuffered(true);
        teamBJamPointsSpinner.setEnabled(false);
        teamBJamPointsSpinner.setMinimumSize(new java.awt.Dimension(50, 26));
        teamBJamPointsSpinner.setNextFocusableComponent(teamBJamPointsSpinner);
        teamBJamPointsSpinner.setPreferredSize(new java.awt.Dimension(50, 26));
        jPanel8.add(teamBJamPointsSpinner);

        jPanel9.add(jPanel8);

        teamBPanel.add(jPanel9, java.awt.BorderLayout.NORTH);

        mainPanel.add(teamBPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        jMenuBar1.setDoubleBuffered(true);

        fileMenu.setText("File");
        fileMenu.setDoubleBuffered(true);

        loadThemeMenuItem.setText("Load Theme");
        loadThemeMenuItem.setToolTipText("Load a theme for the scoreboard from the system.");
        loadThemeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadThemeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadThemeMenuItem);
        fileMenu.add(jSeparator1);

        quitMenuItem.setText("Quit");
        quitMenuItem.setToolTipText("Exit the application.");
        quitMenuItem.setDoubleBuffered(true);
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitMenuItem);

        jMenuBar1.add(fileMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.setToolTipText("Information about the application and usage.");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        windowClosed();
    }//GEN-LAST:event_formWindowClosed

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuItemActionPerformed
        dispose();
    }//GEN-LAST:event_quitMenuItemActionPerformed

    private void teamBLogoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teamBLogoButtonActionPerformed
        changeLogo(getBout().getTeamB());
    }//GEN-LAST:event_teamBLogoButtonActionPerformed

    private void teamALogoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teamALogoButtonActionPerformed
        changeLogo(getBout().getTeamA());
    }//GEN-LAST:event_teamALogoButtonActionPerformed

    private void startJamButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startJamButtonActionPerformed
        startJam();
    }//GEN-LAST:event_startJamButtonActionPerformed

    private void endJamButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endJamButtonActionPerformed
        endJam();
    }//GEN-LAST:event_endJamButtonActionPerformed

    private void officialTimeoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_officialTimeoutButtonActionPerformed
        officialTimeout();
    }//GEN-LAST:event_officialTimeoutButtonActionPerformed

    private void timeoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeoutButtonActionPerformed
        teamTimeout();
    }//GEN-LAST:event_timeoutButtonActionPerformed

    private void scoreboardToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoreboardToggleButtonActionPerformed
        showScoreboard();
    }//GEN-LAST:event_scoreboardToggleButtonActionPerformed

    private void lineupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineupButtonActionPerformed
        lineup();
    }//GEN-LAST:event_lineupButtonActionPerformed

    private void loadThemeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadThemeMenuItemActionPerformed
        loadTheme();
    }//GEN-LAST:event_loadThemeMenuItemActionPerformed

    private void fullscreenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullscreenButtonActionPerformed
        fullscreenScoreboard();
    }//GEN-LAST:event_fullscreenButtonActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        displayAboutDialog();
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel commonPanel;
    private javax.swing.JButton endJamButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton fullscreenButton;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel jamTimeLabel;
    private javax.swing.JTextField jamTimeTextField;
    private javax.swing.JButton lineupButton;
    private javax.swing.JMenuItem loadThemeMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton officialTimeoutButton;
    private javax.swing.JComboBox periodComboBox;
    private javax.swing.JLabel periodTimeLabel;
    private javax.swing.JTextField periodTimeTextField;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JButton showScoreboardButton;
    private javax.swing.JButton startJamButton;
    private javax.swing.JSpinner teamAJamPointsSpinner;
    private javax.swing.JLabel teamALabel;
    private javax.swing.JButton teamALogoButton;
    private javax.swing.JPanel teamAPanel;
    private javax.swing.JSpinner teamAScoreSpinner;
    private javax.swing.JSpinner teamATimeoutSpinner;
    private javax.swing.JLabel teamATimeoutsLabel;
    private javax.swing.JSpinner teamBJamPointsSpinner;
    private javax.swing.JLabel teamBLabel;
    private javax.swing.JButton teamBLogoButton;
    private javax.swing.JPanel teamBPanel;
    private javax.swing.JSpinner teamBScoreSpinner;
    private javax.swing.JSpinner teamBTimeoutSpinner;
    private javax.swing.JLabel teamBTimeoutsLabel;
    private javax.swing.JButton timeoutButton;
    // End of variables declaration//GEN-END:variables

    public void onChanged() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (getBout().isOvertime()) {
                    periodComboBox.getModel().setSelectedItem(periodComboBox.getModel().getElementAt(2));
                } else {
                    periodComboBox.getModel().setSelectedItem(periodComboBox.getModel().getElementAt(getBout().getPeriod()-1));
                }

                if (!periodTimeTextField.isEditable()) {
                    periodTimeTextField.setText(getBout().getPeriodClock().getDisplayTime());
                }

                if (!jamTimeTextField.isEditable()) {
                    jamTimeTextField.setText(getBout().getJamClock().getDisplayTime());
                }

                switch (getBout().getBoutState()) {
                    case UNCONFIGURED:
                        setUnconfiguredState();
                        break;
                    case CONFIGURED:
                        setConfiguredState();
                        break;
                    case JAMMING:
                        setJammingState();
                        break;
                    case LINEUP:
                        setLineupState();
                        break;
                    case TEAM_TIMEOUT:
                        setTeamTimeoutState();
                        break;
                    case OFFICIAL_TIMEOUT:
                        setOfficialTimeoutState();
                        break;
//                    case INTERMISSION:
//                        setIntermissionState();
//                        break;
//                    case OVERTIME_LINEUP:
//                        setOvertimeLineupState();
//                        break;
//                    case FINISHED:
//                        setFinishedState();
//                        break;
                    default:
                        messageLabel.setText("?");
                        break;
                }
            }
        });
    }
    
    protected void setUnconfiguredState() {
        startJamButton.setEnabled(false);
        endJamButton.setEnabled(false);
        lineupButton.setEnabled(false);
        timeoutButton.setEnabled(false);
        officialTimeoutButton.setEnabled(false);
        jamTimeTextField.setEditable(true);
        periodTimeTextField.setEditable(true);

        messageLabel.setText("Unconfigured");
    }

    protected void setConfiguredState() {
        teamALogoButton.setEnabled(true);
        teamATimeoutSpinner.setEnabled(true);
        teamAScoreSpinner.setEnabled(true);
        teamAJamPointsSpinner.setEnabled(true);

        teamBLogoButton.setEnabled(true);
        teamBTimeoutSpinner.setEnabled(true);
        teamBScoreSpinner.setEnabled(true);
        teamBJamPointsSpinner.setEnabled(true);

        lineupButton.setEnabled(true);
        startJamButton.setEnabled(true);
        endJamButton.setEnabled(false);
        timeoutButton.setEnabled(false);
        officialTimeoutButton.setEnabled(true);
        jamTimeTextField.setEditable(true);
        periodTimeTextField.setEditable(true);

        messageLabel.setText("Configured");
    }

    protected void setJammingState() {
        lineupButton.setEnabled(false);
        startJamButton.setEnabled(false);
        endJamButton.setEnabled(true);
        timeoutButton.setEnabled(false);
        officialTimeoutButton.setEnabled(true);
        jamTimeTextField.setEditable(false);
        periodTimeTextField.setEditable(false);
        teamALogoButton.setEnabled(false);
        teamBLogoButton.setEnabled(false);

        messageLabel.setText("Jamming" + (getBout().isOvertime() ? " Overtime" : ""));
    }

    protected void setLineupState() {
        startJamButton.setEnabled(true);
        lineupButton.setEnabled(false);
        endJamButton.setEnabled(false);
        timeoutButton.setEnabled(true);
        officialTimeoutButton.setEnabled(true);
        jamTimeTextField.setEditable(false);
        periodTimeTextField.setEditable(false);
        teamALogoButton.setEnabled(false);
        teamBLogoButton.setEnabled(false);

        messageLabel.setText("Lineup" + (getBout().isOvertime() ? " Overtime" : ""));
    }

    protected void setTeamTimeoutState() {
        startJamButton.setEnabled(true);
        lineupButton.setEnabled(true);
        endJamButton.setEnabled(false);
        timeoutButton.setEnabled(false);
        officialTimeoutButton.setEnabled(true);
        jamTimeTextField.setEditable(false);
        periodTimeTextField.setEditable(true);
        teamALogoButton.setEnabled(false);
        teamBLogoButton.setEnabled(false);

        messageLabel.setText("Team Timeout" + (getBout().isOvertime() ? " Overtime" : ""));
    }

    protected void setOfficialTimeoutState() {
        lineupButton.setEnabled(true);
        startJamButton.setEnabled(true);
        endJamButton.setEnabled(false);
        timeoutButton.setEnabled(true);
        officialTimeoutButton.setEnabled(false);
        jamTimeTextField.setEditable(true);
        periodTimeTextField.setEditable(true);
        teamALogoButton.setEnabled(true);
        teamBLogoButton.setEnabled(true);

        messageLabel.setText("Official Timeout" + (getBout().isOvertime() ? " Overtime" : ""));
    }

    protected void setIntermissionState() {
        startJamButton.setEnabled(true);
        endJamButton.setEnabled(false);
        lineupButton.setEnabled(false);
        officialTimeoutButton.setEnabled(false);
        jamTimeTextField.setEditable(false);
        periodTimeTextField.setEditable(false);
        teamALogoButton.setEnabled(false);
        teamBLogoButton.setEnabled(false);

        messageLabel.setText("Intermission");
    }

    protected void setOvertimeLineupState() {
        startJamButton.setEnabled(true);
        endJamButton.setEnabled(false);
        lineupButton.setEnabled(false);
        timeoutButton.setEnabled(true);
        officialTimeoutButton.setEnabled(true);
        jamTimeTextField.setEditable(false);
        periodTimeTextField.setEditable(false);
        teamALogoButton.setEnabled(false);
        teamBLogoButton.setEnabled(false);

        messageLabel.setText("Overtime Lineup");
    }

    protected void setFinishedState() {
        startJamButton.setEnabled(false);
        endJamButton.setEnabled(false);
        lineupButton.setEnabled(false);
        timeoutButton.setEnabled(false);
        officialTimeoutButton.setEnabled(false);
        jamTimeTextField.setEditable(false);
        periodTimeTextField.setEditable(false);
        teamALogoButton.setEnabled(false);
        teamBLogoButton.setEnabled(false);

        messageLabel.setText("Finished");
    }

    protected Bout getBout() {
        PicoContainerFactory picoContainerFactory = PicoContainerFactory.getInstance();
        MutablePicoContainer pico = picoContainerFactory.get();
        return pico.getComponent(Bout.class);
    }
}
