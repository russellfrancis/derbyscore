package com.liquidatom.derbyscore.ui;

import com.liquidatom.derbyscore.domain.Bout;
import com.liquidatom.derbyscore.domain.BoutListener;
import com.liquidatom.derbyscore.theme.ImageElement;
import com.liquidatom.derbyscore.theme.TextElement;
import com.liquidatom.derbyscore.theme.Theme;
import com.liquidatom.derbyscore.theme.ThemeElement;
import com.liquidatom.derbyscore.theme.ThemeFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Russell Francis (russell.francis@gmail.com)
 * @version $Id: ScoreboardWindow.java 4 2010-03-12 11:40:07Z russ $
 */
public class ScoreboardWindow extends JFrame implements BoutListener, ComponentListener {

    static private final Logger log = LoggerFactory.getLogger(ScoreboardWindow.class);
    static private final int DEFAULT_WINDOW_WIDTH = 800;
    static private final int DEFAULT_WINDOW_HEIGHT = 600;
    
    // cached copies for quick drawing and other useful rendering information.
    private GraphicsConfiguration graphicsConfiguration =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    private AffineTransform xform = AffineTransform.getTranslateInstance(0.0, 0.0);
    private Bout bout;
    private Map<String, Object> scriptScope = new LinkedHashMap<String, Object>();

    public ScoreboardWindow(Bout bout) {
        super(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        setLocationByPlatform(true);
        setFocusTraversalKeysEnabled(false);
        setIgnoreRepaint(true);
        addComponentListener(this);

        try {
            URL appLogo = getClass().getResource("/gfx/brrg.png");
            setIconImage(ImageIO.read(appLogo));
        }
        catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Unable to set window icon for scoreboard.");
            }
        }
        
        setBout(bout);
    }

    @Override
    public void paint(Graphics parentGraphics) {
        BufferStrategy strategy = getBufferStrategy();
        if (strategy != null) {
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            try {
                super.paint(g);
                g.transform(xform);
                render(g);
                strategy.show();
            }
            finally {
                g.dispose();
            }
        }
    }

    protected void render(Graphics2D g) {
        getBout().getPeriodClock().readLock().lock();
        try {
            getBout().getJamClock().readLock().lock();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                Collection<ThemeElement> elements = getTheme().getThemeElements();
                for (ThemeElement element : elements) {
                    drawThemeElement(g, element);
                }
            }
            finally {
                getBout().getJamClock().readLock().unlock();
            }
        }
        finally {
            getBout().getPeriodClock().readLock().unlock();
        }
    }

    protected void drawThemeElement(Graphics2D g, ThemeElement element) {
        if (element.isVisible(getScriptScope())) {
            if (element instanceof TextElement) {
                drawTextElement(g, (TextElement)element);
            }
            else if (element instanceof ImageElement) {
                drawImageElement(g, (ImageElement)element);
            }
        }
    }

    protected void drawTextElement(Graphics2D g, TextElement element) {
        String text = element.getText(getScriptScope());
        Font font = element.getFont();
        Color color = element.getColor();

        g.setFont(font);
        g.setColor(color);
        FontMetrics fontMetrics = g.getFontMetrics();
        Rectangle2D bounds = fontMetrics.getStringBounds(text, g);
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();
        if (width > 0 && height > 0) {
            BufferedImage textImage = getDefaultConfiguration().createCompatibleImage(width, height, Transparency.BITMASK);
            Graphics2D textG = textImage.createGraphics();
            try {
                textG.setFont(font);
                textG.setColor(color);
                textG.drawString(text, 0, fontMetrics.getAscent());

                Rectangle position = element.getPosition();
                g.drawImage(textImage,
                        (int)position.getX(),
                        (int)position.getY(),
                        (int)position.getWidth(),
                        (int)position.getHeight(),
                        null);
            }
            finally {
                textG.dispose();
            }
        }
    }

    protected void drawImageElement(Graphics2D g, ImageElement element) {
        BufferedImage image = getTheme().getImage(element.getImgRef());
        if (image != null) {
            Rectangle position = element.getPosition();
            int x = (int) position.getX();
            int y = (int) position.getY();
            int width = (int) position.getWidth();
            int height = (int) position.getHeight();
            g.drawImage(image, x, y, width, height, null);
        }
    }

    protected GraphicsConfiguration getDefaultConfiguration() {
        return graphicsConfiguration;
    }

    protected void setScriptScope(Map<String, Object> scriptScope) {
        if (scriptScope == null) {
            throw new IllegalArgumentException("The parameter scriptScope must be non-null.");
        }
        this.scriptScope = scriptScope;
    }

    protected Map<String, Object> getScriptScope() {
        return scriptScope;
    }

    protected Theme getTheme() {
        return ThemeFactory.getInstance().getCurrent();
    }

    protected void setBout(Bout bout) {
        this.bout = bout;
        if (bout == null) {
            scriptScope.remove("bout");
        }
        else {
            scriptScope.put("bout", bout);
        }
    }

    protected Bout getBout() {
        return bout;
    }

    public void onChanged() {
        repaint();
    }

    public void componentResized(ComponentEvent e) {
        // Determine the translation which we must apply to render away from the window controls.
        Point parentPoint = getLocationOnScreen();
        Point childPoint = getContentPane().getLocationOnScreen();
        double tx = childPoint.getX() - parentPoint.getX();
        double ty = childPoint.getY() - parentPoint.getY();
        AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);

        // Determine the scaling which must be applied to the text and images.
        BufferedImage original = getTheme().getBackdrop();
        int width = getContentPane().getWidth();
        int height = getContentPane().getHeight();
        double sx = (double)width / (double)original.getWidth();
        double sy = (double)height / (double)original.getHeight();
        transform.concatenate(AffineTransform.getScaleInstance(sx, sy));

        // Apply this transformation matrix to be used during rendering.
        this.xform =transform;

        // Instruct the application to rerender the scoreboard.
        repaint();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        createBufferStrategy(2);
    }

    public void componentHidden(ComponentEvent e) {
    }
}
