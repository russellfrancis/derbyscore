package com.liquidatom.derbyscore.theme;

import com.liquidatom.derbyscore.PicoContainerFactory;
import com.liquidatom.derbyscore.domain.Bout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.concurrent.GuardedBy;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.picocontainer.MutablePicoContainer;

/**
 *
 * @author Russell Francis (russ@metro-six.com)
 */
public class Theme {

    final private URL themeDefinitionURL;

    @GuardedBy("itself")
    final private Map<String, Font> fonts = Collections.synchronizedMap(new HashMap<String, Font>());

    // *** We must always synchronize images before fonts if we need to synchronize on both.
    @GuardedBy("itself")
    final private Map<String, BufferedImage> images = Collections.synchronizedMap(new HashMap<String, BufferedImage>());

    // *** We must always synchronize elements before images / fonts if we require synchronization on both.
    @GuardedBy("itself")
    final private List<ThemeElement> elements = Collections.synchronizedList(new ArrayList<ThemeElement>());

    @GuardedBy("javascriptEngineLock")
    final private ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    final private Lock javascriptEngineLock = new ReentrantLock();

    protected Theme() {
        this(null);
    }

    public Theme(final URL themeDefinitionURL) {
        this.themeDefinitionURL = themeDefinitionURL;
    }
    
    protected GraphicsConfiguration getDefaultConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    protected BufferedImage toCompatibleImage(final BufferedImage img, final int transparency, GraphicsConfiguration gc) {
        if (gc == null) {
            gc = getDefaultConfiguration();
        }
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage result = gc.createCompatibleImage(w, h, transparency);
        Graphics2D g2 = result.createGraphics();
        g2.drawRenderedImage(img, null);
        g2.dispose();
        return result;
    }

    public Collection<ThemeElement> getThemeElements() {
        return Collections.unmodifiableCollection(elements);
    }

    public BufferedImage getBackdrop() {
        synchronized (elements) {
            for (ThemeElement e : elements) {
                if (e instanceof ImageElement) {
                    return images.get(((ImageElement)e).getImgRef());
                }
            }
        }
        return null;
    }

    public Font getFont(final String id) {
        return fonts.get(id);
    }

    public BufferedImage getImage(String id) {
        if (id.equals("team-a-logo")) {
            return getBout().getTeamA().getImage();
        }
        else if (id.equals("team-b-logo")) {
            return getBout().getTeamB().getImage();
        }

        return images.get(id);
    }

    protected Bout getBout() {
        PicoContainerFactory picoContainerFactory = PicoContainerFactory.getInstance();
        MutablePicoContainer pico = picoContainerFactory.get();
        return pico.getComponent(Bout.class);
    }

    public void addResource(final String id, final Font font) {
        synchronized (images) {
            synchronized (fonts) {
                if (fonts.containsKey(id) || images.containsKey(id)) {
                    throw new IllegalStateException("The id's for resources must be unique '" + id + "' is duplicated!");
                }
                fonts.put(id, font.deriveFont((float)300));
            }
        }
    }

    public void addResource(final String id, final BufferedImage img) {
        addResource(id, img, Transparency.OPAQUE);
    }

    public void addResource(final String id, final BufferedImage img, final int transparency) {
        synchronized (images) {
            synchronized (fonts) {
                if (fonts.containsKey(id) || images.containsKey(id)) {
                    throw new IllegalStateException("The id's for resources must be unique '" + id + "' is duplicated!");
                }

                images.put(id, toCompatibleImage(img, transparency, getDefaultConfiguration()));
            }
        }
    }

    public boolean addElement(final ThemeElement element) {
        return elements.add(element);
    }

    public URL getThemeDefinitionURL() {
        return themeDefinitionURL;
    }

    public String getBase() {
        String externalForm = getThemeDefinitionURL().toExternalForm();
        int lastIndexOfSlash = externalForm.lastIndexOf("/");
        return externalForm.substring(0, lastIndexOfSlash) + "/";
    }

    protected ScriptEngine acquireJavascriptEngine(final Map<String, Object> scope) {
        javascriptEngineLock.lock();

        if (scope != null) {
            for (final Entry<String,Object> entry : scope.entrySet()) {
                final String key = entry.getKey();
                if (key != null) {
                    final Object value = entry.getValue();
                    javascriptEngine.put(key, value);
                }
            }
        }

        return javascriptEngine;
    }

    protected void releaseJavascriptEngine() {
        javascriptEngine.setBindings(javascriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE);
        javascriptEngineLock.unlock();
    }

}
