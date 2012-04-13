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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import mereflect.MEClass;
import mereflect.MEField;
import mereflect.MEMethod;
import analyser.gui.ClassTreeRenderer;
import analyser.gui.FlagIcon;
import andreflect.Util;
import andreflect.gui.chart.ClassComponent;
import andreflect.gui.chart.GraphPanel;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.shape.mxBasicShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class ClassShape extends mxBasicShape {

    private int drawOneLine(int y, FlagIcon fIcon, String text, boolean align_center, mxGraphics2DCanvas canvas, mxCellState state) {

        Rectangle tmp = state.getRectangle();
        double scale = canvas.getScale();

        int charspacing = (int) (GraphPanel.CHAR_SPACING * scale);

        Rectangle fontRect = mxUtils.getSizeForString(text, mxUtils.getFont(state
                .getStyle()), scale).getRectangle();

        FlagIcon im = fIcon;

        int imageheight = Math.min((int) (im.getIconHeight() * scale), fontRect.height);
        int imagewidth = (int) (im.getIconWidth() * scale); //ratio is fixed it will changed in canvas.drawImage()

        // workaround to caclulate font y
        int fonty = y;

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

        int dx = 0;

        if (align_center)
        {
            int sw = fm.stringWidth(text) + charspacing * 2 + imagewidth;
            dx = (tmp.width - sw) / 2;
        }

        Rectangle imagerect = new Rectangle(tmp.x + dx + charspacing, y + fontRect.height - imageheight, imagewidth, imageheight);
        im.paintIcon(imagerect, canvas.getGraphics());
        canvas.getGraphics().drawString(text, (tmp.x + dx + charspacing + imagewidth + charspacing), fonty);

        return y + fontRect.height + mxConstants.LINESPACING;
    }

    private void draw(ClassComponent split, mxGraphics2DCanvas canvas, mxCellState state) {
        Rectangle tmp = state.getRectangle();
        int y = tmp.y;

        MEClass clazz = split.getMEClass();

        FlagIcon ci;
        if (clazz.isInterface()) {
            ci = ClassTreeRenderer.ICON_INTERFACE;
        } else {
            ci = ClassTreeRenderer.ICON_CLASS;
        }

        if (state.getView().getGraph().isCellCollapsed(state.getCell())) {
            y = drawOneLine(y, ci, clazz.getClassName(), false, canvas, state);
            return;
        }

        y = drawOneLine(y, ci, clazz.getClassName(), true, canvas, state);

        canvas.getGraphics().drawLine(tmp.x, y, tmp.x + tmp.width, y);

        boolean hasField = false;

        for (MEField field : split.getFields()) {
            hasField = true;
            FlagIcon fIcon = null;
            int flags = 0;
            if (field.isPublic()) {
                fIcon = ClassTreeRenderer.ICON_PUBLICFIELD;
            } else if (field.isProtected()) {
                fIcon = ClassTreeRenderer.ICON_PROTECTEDFIELD;
            } else if (field.isPrivate()) {
                fIcon = ClassTreeRenderer.ICON_PRIVATEFIELD;
            } else {
                fIcon = ClassTreeRenderer.ICON_PACKAGEMFIELD;
            }
            flags |= field.isStatic() ? FlagIcon.FLAG_STATIC : 0;
            flags |= field.isFinal() ? FlagIcon.FLAG_FINAL : 0;

            fIcon.setFlags(flags);
            String text = field.getName() + " : " + Util.shortenClassName(field.getType().toString());
            y = drawOneLine(y, fIcon, text, false, canvas, state);
        }

        if (!hasField) {
            double scale = canvas.getScale();
            y += (int) (GraphPanel.LINE_SPACING * scale);
        }

        boolean hasMethod = false;

        canvas.getGraphics().drawLine(tmp.x, y, tmp.x + tmp.width, y);
        for (MEMethod method : split.getMethods()) {
            hasMethod = true;
            FlagIcon fIcon = null;
            int flags = 0;
            if (method.isPublic()) {
                fIcon = ClassTreeRenderer.ICON_PUBLICMETHOD;
            } else if (method.isProtected()) {
                fIcon = ClassTreeRenderer.ICON_PROTECTEDMETHOD;
            } else if (method.isPrivate()) {
                fIcon = ClassTreeRenderer.ICON_PRIVATEMETHOD;
            } else {
                fIcon = ClassTreeRenderer.ICON_PACKAGEMETHOD;
            }
            flags |= method.isNative() ? FlagIcon.FLAG_NATIVE : 0;
            flags |= method.isConstructor() ? FlagIcon.FLAG_CONSTRUCTOR : 0;
            flags |= method.isStatic() ? FlagIcon.FLAG_STATIC : 0;
            flags |= method.isFinal() ? FlagIcon.FLAG_FINAL : 0;

            fIcon.setFlags(flags);
            String text = method.getFormattedName() + "(" + method.getArgumentsStringUml() + ") : " + Util.shortenClassName(method.getReturnClassString());
            y = drawOneLine(y, fIcon, text, false, canvas, state);
        }

        if (!hasMethod) {
            canvas.getGraphics().drawLine(tmp.x, y, tmp.x + tmp.width, y);
        }

    }

    private static final Color COLOR_FOCUS = Color.CYAN.brighter().brighter();
    private static final Color COLOR_DEPENDENCY = Color.ORANGE.brighter().brighter();
    private static final Color COLOR_DEPSUPER = Color.GREEN.brighter().brighter();

    @Override
    public void paintShape(mxGraphics2DCanvas canvas, mxCellState state)
    {

        Rectangle tmp = state.getRectangle();

        mxIGraphModel model = state.getView().getGraph().getModel();
        Object val = model.getValue(state.getCell());

        assert val instanceof ClassComponent;
        ClassComponent split = (ClassComponent) val;

        if (configureGraphics(canvas, state, true))
        {
            if (split.isFocus()) {
                canvas.getGraphics().setColor(COLOR_FOCUS);
            } else if (split.isDepSuper()) {
                canvas.getGraphics().setColor(COLOR_DEPSUPER);
            } else if (split.isDependency()) {
                canvas.getGraphics().setColor(COLOR_DEPENDENCY);
            }

            canvas.fillShape(new Rectangle(tmp.x, tmp.y, tmp.width, tmp.height));
        }

        if (configureGraphics(canvas, state, false))
        {
            draw(split, canvas, state);
            canvas.getGraphics().drawRect(tmp.x, tmp.y, tmp.width, tmp.height);
        }
    }
}
