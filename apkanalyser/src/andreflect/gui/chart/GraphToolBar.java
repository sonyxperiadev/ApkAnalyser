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

package andreflect.gui.chart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import andreflect.gui.chart.GraphActions.RefreshAction;
import andreflect.gui.chart.GraphActions.SaveAction;
import andreflect.gui.chart.GraphActions.ZoomAction;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraphView;

public class GraphToolBar extends JToolBar
{

    private static final long serialVersionUID = -8015443128436394471L;

    private boolean ignoreZoomChange = false;

    private final JLabel label = new JLabel();
    JToolBar sp = new JToolBar();

    public void setText(String text, Icon icon) {
        label.setText(text);
        label.setIcon(icon);
    }

    public void setZoom(boolean enabled) {
        sp.setVisible(enabled);
    }

    public GraphToolBar(final GraphPanel editor, String text)
    {
        super(JToolBar.HORIZONTAL);
        setBorder(BorderFactory.createEmptyBorder());
        setFloatable(false);

        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(0, 30));

        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(3, 3, 3, 3), getBorder()));
        label.setText(text);
        add(label, BorderLayout.CENTER);

        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setFloatable(false);

        sp.add(editor.bind("Refresh", new RefreshAction(), "refresh.gif"));

        sp.add(editor.bind("Zoom In", new ZoomAction(true),
                "zoomin.gif"));

        sp.add(editor.bind("Zoom out", new ZoomAction(false),
                "zoomout.gif"));

        final mxGraphView view = editor.getGraphComponent().getGraph()
                .getView();
        final JComboBox zoomCombo = new JComboBox(new String[] { "400%",
                "200%", "150%", "100%", "75%", "50%" });
        zoomCombo.setEditable(true);
        zoomCombo.setMinimumSize(new Dimension(75, 25));
        zoomCombo.setPreferredSize(new Dimension(75, 25));
        zoomCombo.setMaximumSize(new Dimension(75, 25));
        zoomCombo.setMaximumRowCount(6);
        sp.add(zoomCombo);

        sp.add(editor.bind("Save", new SaveAction(), "save.gif"));

        add(sp, BorderLayout.EAST);

        // Sets the zoom in the zoom combo the current value
        mxIEventListener scaleTracker = new mxIEventListener()
        {
            @Override
            public void invoke(Object sender, mxEventObject evt)
            {
                ignoreZoomChange = true;

                try
                {
                    zoomCombo.setSelectedItem((int) Math.round(100 * view
                            .getScale())
                            + "%");
                }
                finally
                {
                    ignoreZoomChange = false;
                }
            }
        };

        // Installs the scale tracker to update the value in the combo box
        // if the zoom is changed from outside the combo box
        view.getGraph().getView().addListener(mxEvent.SCALE, scaleTracker);
        view.getGraph().getView().addListener(mxEvent.SCALE_AND_TRANSLATE,
                scaleTracker);

        // Invokes once to sync with the actual zoom value
        scaleTracker.invoke(null, null);

        zoomCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                mxGraphComponent graphComponent = editor.getGraphComponent();

                // Zoomcombo is changed when the scale is changed in the diagram
                // but the change is ignored here
                if (!ignoreZoomChange)
                {
                    String zoom = zoomCombo.getSelectedItem().toString();

                    try
                    {
                        zoom = zoom.replace("%", "");
                        double scale = Math.min(16, Math.max(0.01,
                                Double.parseDouble(zoom) / 100));
                        graphComponent.zoomTo(scale, graphComponent
                                .isCenterZoom());
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(editor, ex
                                .getMessage());
                    }
                }
            }
        });
        setMinimumSize(new Dimension(0, 25));
        setPreferredSize(new Dimension(0, 25));
        setMaximumSize(new Dimension(0, 25));
    }
}
