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

package andreflect.gui.chart.shape;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import analyser.gui.ClassTreeRenderer;
import analyser.gui.FlagIcon;
import analyser.logic.RefFolder;
import andreflect.gui.chart.GraphPanel;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxBasicShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class FolderShape extends mxBasicShape {
    @Override
    public void paintShape(mxGraphics2DCanvas canvas, mxCellState state)
    {
        Rectangle tmp = state.getRectangle();

        mxIGraphModel model = state.getView().getGraph().getModel();
        Object reference = model.getValue(state.getCell());

        String name;

        if (reference == null) {
            name = "unknown folder";
        } else if (reference instanceof RefFolder) {
            name = ((RefFolder) reference).getName();
        } else {
            name = reference.toString();
        }

        double scale = canvas.getScale();

        int charspacing = (int) (GraphPanel.CHAR_SPACING * scale);

        Rectangle fontRect = mxUtils.getSizeForString(name, mxUtils.getFont(state
                .getStyle()), scale).getRectangle();

        // workaround to caclulate font y
        int fonty = tmp.y;

        Font scaledFont = mxUtils.getFont(state.getStyle(), scale);
        canvas.getGraphics().setFont(scaledFont);

        int fontSize = mxUtils.getInt(state.getStyle(), mxConstants.STYLE_FONTSIZE,
                mxConstants.DEFAULT_FONTSIZE);
        FontMetrics fm = canvas.getGraphics().getFontMetrics();
        int scaledFontSize = scaledFont.getSize();
        double fontScaleFactor = ((double) scaledFontSize) / ((double) fontSize);
        // This factor is the amount by which the font is smaller/
        // larger than we expect for the given scale. 1 means it's
        // correct, 0.8 means the font is 0.8 the size we expected
        // when scaled, etc.
        double fontScaleRatio = fontScaleFactor / scale;
        // The y position has to be moved by (1 - ratio) * height / 2
        fonty += 2 * fm.getMaxAscent() - fm.getHeight()
                + 5/*mxConstants.LABEL_INSET*/* scale;

        double vertAlignProportion = 1.0; //0 top 1.0 bottom 0.5 middle align

        fonty += (1.0 - fontScaleRatio) * fontRect.height * vertAlignProportion;

        // end workaround

        FlagIcon im = ClassTreeRenderer.ICON_PACKAGE;

        int imageheight = Math.min((int) (im.getIconHeight() * scale), fontRect.height);
        int imagewidth = (int) (im.getIconWidth() * scale); //ratio is fixed it will changed in canvas.drawImage()

        if (configureGraphics(canvas, state, true))
        {
            canvas.fillShape(new Rectangle(tmp.x, tmp.y, tmp.width,
                    tmp.height));
        }

        if (configureGraphics(canvas, state, false))
        {

            Rectangle imagerect = new Rectangle(tmp.x + charspacing, tmp.y + fontRect.height - imageheight, imagewidth, imageheight);
            im.paintIcon(imagerect, canvas.getGraphics());
            canvas.getGraphics().drawString(name, (tmp.x + charspacing + imagewidth + charspacing), fonty);

            canvas.getGraphics().drawRect(tmp.x, tmp.y, tmp.width,
                    tmp.height);
        }

    }
}
