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

/*
 * $Id: EditorActions.java,v 1.35 2011-02-14 15:45:58 gaudenz Exp $
 * Copyright (c) 2001-2010, Gaudenz Alder, David Benson
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package andreflect.gui.chart;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;

public class GraphActions
{
    public static final GraphPanel getEditor(ActionEvent e)
    {
        if (e.getSource() instanceof Component)
        {
            Component component = (Component) e.getSource();

            while (component != null
                    && !(component instanceof GraphPanel))
            {
                component = component.getParent();
            }

            return (GraphPanel) component;
        }

        return null;
    }

    public static class RefreshAction extends AbstractAction
    {

        private static final long serialVersionUID = -7536610768780568409L;

        @Override
        public void actionPerformed(ActionEvent e) {
            GraphPanel editor = getEditor(e);
            editor.refresh();
        }

    }

    @SuppressWarnings("serial")
    public static class ZoomAction extends AbstractAction
    {
        protected boolean isZoomIn;

        public ZoomAction(boolean isZoomIn)
        {
            this.isZoomIn = isZoomIn;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() instanceof mxGraphComponent)
            {
                mxGraphComponent graphComponent = (mxGraphComponent) e
                        .getSource();
                double scale = graphComponent.getGraph().getView().getScale();
                double percent = Math.round(100 * scale);
                if (isZoomIn) {
                    percent *= 1.2;
                } else {
                    percent *= 0.8;
                }

                scale = Math.min(8, Math.max(0.05, percent / 100));
                graphComponent.zoomTo(scale, graphComponent
                        .isCenterZoom());
            }
        }
    }

    @SuppressWarnings("serial")
    public static class SaveAction extends AbstractAction
    {
        protected String lastDir = null;

        @Override
        public void actionPerformed(ActionEvent e)
        {
            GraphPanel editor = getEditor(e);

            if (editor != null)
            {
                mxGraphComponent graphComponent = editor.getGraphComponent();
                mxGraph graph = graphComponent.getGraph();
                FileFilter selectedFilter = null;
                String filename = null;

                String wd;

                if (lastDir != null)
                {
                    wd = lastDir;
                }
                else
                {
                    wd = System.getProperty("user.dir");
                }

                JFileChooser fc = new JFileChooser(wd);

                // Adds a filter for each supported image format
                Object[] imageFormats = ImageIO.getReaderFormatNames();

                // Finds all distinct extensions
                HashSet<String> formats = new HashSet<String>();

                boolean png = false;
                boolean gif = false;
                for (int i = 0; i < imageFormats.length; i++)
                {
                    String ext = imageFormats[i].toString().toLowerCase();
                    if (ext.equals("png")) {
                        png = true;
                    } else if (ext.equals("gif")) {
                        gif = true;
                    }
                    formats.add(ext);
                }

                if (png) {
                    imageFormats = new Object[] { new String("png") };
                } else if (gif) {
                    imageFormats = new Object[] { new String("gif") };
                } else {
                    imageFormats = formats.toArray();
                }

                DefaultFileFilter firstFileFilter = null;

                for (int i = 0; i < imageFormats.length; i++)
                {
                    String ext = imageFormats[i].toString();

                    DefaultFileFilter filter = new DefaultFileFilter("."
                            + ext, ext.toUpperCase() + " "
                            + "file" + " (." + ext + ")");

                    if (firstFileFilter == null) {
                        firstFileFilter = filter;
                    }
                    fc.addChoosableFileFilter(filter);
                }

                fc.setFileFilter(firstFileFilter);
                int rc = fc.showDialog(null, "save");

                if (rc != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }
                else
                {
                    lastDir = fc.getSelectedFile().getParent();
                }

                filename = fc.getSelectedFile().getAbsolutePath();
                selectedFilter = fc.getFileFilter();

                if (selectedFilter instanceof DefaultFileFilter)
                {
                    String ext = ((DefaultFileFilter) selectedFilter)
                            .getExtension();

                    if (!filename.toLowerCase().endsWith(ext))
                    {
                        filename += ext;
                    }
                }

                if (new File(filename).exists()
                        && JOptionPane.showConfirmDialog(graphComponent,
                                "overwriteExistingFile") != JOptionPane.YES_OPTION)
                {
                    return;
                }

                try
                {
                    String ext = filename
                            .substring(filename.lastIndexOf('.') + 1);
                    BufferedImage image = mxCellRenderer
                            .createBufferedImage(graph, null, 1, null,
                                    graphComponent.isAntiAlias(), null,
                                    graphComponent.getCanvas());

                    if (image != null)
                    {
                        ImageIO.write(image, ext, new File(filename));
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(graphComponent,
                                "No diagram to save. Select apk files and start analysis");
                    }
                } catch (Throwable ex)
                {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(graphComponent,
                            ex.toString(), "error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

}
