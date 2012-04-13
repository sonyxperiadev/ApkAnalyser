/*
 * Copyright (C) 2012 Sony Mobile Communications AB
 *
 * This file is part of ApkAnalyser.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package analyser.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.ImageIcon;

public class FlagIcon extends ImageIcon
{
    private static final long serialVersionUID = 6083048108163393381L;
    public static ImageIcon ICON_FAIL = createImageIcon("fail.png");
    public static ImageIcon ICON_MODIFIED = createImageIcon("modified.png");
    public static ImageIcon ICON_NATIVE = createImageIcon("native_co.gif");
    public static ImageIcon ICON_UNKNOWN = createImageIcon("unknown.png");
    public static ImageIcon ICON_CONSTRUCTOR = createImageIcon("constr_ovr.gif");
    public static ImageIcon ICON_STATIC = createImageIcon("static_co.gif");
    public static ImageIcon ICON_FINAL = createImageIcon("final_co.gif");
    public static ImageIcon ICON_ANDROID = createImageIcon("android_project.png");

    public static final int FLAG_FAIL = 0x01;
    public static final int FLAG_NATIVE = 0x02;
    public static final int FLAG_UNKNOWN = 0x04;
    public static final int FLAG_CONSTRUCTOR = 0x08;
    public static final int FLAG_MODIFIED = 0x10;
    public static final int FLAG_STATIC = 0x20;
    public static final int FLAG_FINAL = 0x40;
    public static final int FLAG_ANDROID = 0x80;

    protected int m_flags;

    public void setFlags(int flags)
    {
        m_flags = flags;
    }

    public int getFlags()
    {
        return m_flags;
    }

    public FlagIcon(int flags)
    {
        super();
        m_flags = flags;
    }

    /**
     * @param imageData
     */
    public FlagIcon(byte[] imageData, int flags)
    {
        super(imageData);
        m_flags = flags;
    }

    /**
     * @param image
     */
    public FlagIcon(Image image, int flags)
    {
        super(image);
        m_flags = flags;
    }

    /**
     * @param filename
     */
    public FlagIcon(String filename, int flags)
    {
        super(filename);
        m_flags = flags;
    }

    /**
     * @param imageData
     * @param description
     */
    public FlagIcon(byte[] imageData, String description, int flags)
    {
        super(imageData, description);
        m_flags = flags;
    }

    /**
     * @param location
     */
    public FlagIcon(URL location, int flags)
    {
        super(location);
        m_flags = flags;
    }

    /**
     * @param image
     * @param description
     */
    public FlagIcon(Image image, String description, int flags)
    {
        super(image, description);
        m_flags = flags;
    }

    /**
     * @param filename
     * @param description
     */
    public FlagIcon(String filename, String description, int flags)
    {
        super(filename, description);
        m_flags = flags;
    }

    /**
     * @param location
     * @param description
     */
    public FlagIcon(URL location, String description, int flags)
    {
        super(location, description);
        m_flags = flags;
    }

    public synchronized void paintIcon(Rectangle bounds, Graphics g)
    {
        if (bounds.width <= 0 || bounds.height <= 0)
        {
            return;

        }

        int w, h;
        int x = bounds.x;
        int y = bounds.y;

        double iw = getIconWidth();
        double ih = getIconHeight();
        double s = Math.min(bounds.width / iw, bounds.height / ih);
        w = (int) (iw * s);
        h = (int) (ih * s);
        x += (bounds.width - w) / 2;
        y += (bounds.height - h) / 2;

        g.drawImage(getImage()/*scaledImage*/, x, y, w, h, getImageObserver());

        int oiw = (int) (ICON_STATIC.getIconWidth() * s);
        int oih = (int) (ICON_STATIC.getIconHeight() * s);

        if ((m_flags & FLAG_CONSTRUCTOR) > 0)
        {

            g.drawImage(ICON_CONSTRUCTOR.getImage(), x + oiw, y + oih, oiw, oih, getImageObserver());
        }
        if ((m_flags & FLAG_STATIC) > 0)
        {
            g.drawImage(ICON_STATIC.getImage(), x, y, oiw, oih, getImageObserver());
        }
        if ((m_flags & FLAG_FINAL) > 0)
        {
            g.drawImage(ICON_FINAL.getImage(), x + oiw, y, oiw, oih, getImageObserver());
        }
        if ((m_flags & FLAG_UNKNOWN) > 0)
        {
            //g.drawImage(ICON_UNKNOWN.getImage(), x, y, oiw,oih,getImageObserver());
        }
        if ((m_flags & FLAG_FAIL) > 0)
        {
            g.drawImage(ICON_FAIL.getImage(), x, y, oiw, oih, getImageObserver());
        }
        if ((m_flags & FLAG_NATIVE) > 0)
        {
            g.drawImage(ICON_NATIVE.getImage(), x, y + oih, oiw, oih, getImageObserver());
        }
        if ((m_flags & FLAG_MODIFIED) > 0)
        {
            g.drawImage(ICON_MODIFIED.getImage(), x, y, oiw, oih, getImageObserver());
        }
        if ((m_flags & FLAG_ANDROID) > 0)
        {
            g.drawImage(ICON_ANDROID.getImage(), x, y, oiw, oih, getImageObserver());
        }
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y)
    {
        super.paintIcon(c, g, x, y);
        if ((m_flags & FLAG_CONSTRUCTOR) > 0)
        {
            g.drawImage(ICON_CONSTRUCTOR.getImage(), x + ICON_STATIC.getIconWidth(), y + ICON_STATIC.getIconHeight(), getImageObserver());
        }
        if ((m_flags & FLAG_STATIC) > 0)
        {
            g.drawImage(ICON_STATIC.getImage(), x, y, getImageObserver());
        }
        if ((m_flags & FLAG_FINAL) > 0)
        {
            g.drawImage(ICON_FINAL.getImage(), x + ICON_STATIC.getIconWidth(), y, getImageObserver());
        }
        if ((m_flags & FLAG_UNKNOWN) > 0)
        {
            //g.drawImage(ICON_UNKNOWN.getImage(), x, y, getImageObserver());
        }
        if ((m_flags & FLAG_FAIL) > 0)
        {
            g.drawImage(ICON_FAIL.getImage(), x, y, getImageObserver());
        }
        if ((m_flags & FLAG_NATIVE) > 0)
        {
            g.drawImage(ICON_NATIVE.getImage(), x, y + ICON_STATIC.getIconHeight(), getImageObserver());
        }
        if ((m_flags & FLAG_MODIFIED) > 0)
        {
            g.drawImage(ICON_MODIFIED.getImage(), x, y, getImageObserver());
        }
        if ((m_flags & FLAG_ANDROID) > 0)
        {
            g.drawImage(ICON_ANDROID.getImage(), x, y, getImageObserver());
        }
    }

    public static ImageIcon createImageIcon(String path)
    {
        java.net.URL imgURL = Thread.currentThread().getContextClassLoader().getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL);
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
