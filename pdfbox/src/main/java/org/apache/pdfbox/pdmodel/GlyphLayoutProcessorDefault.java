package org.apache.pdfbox.pdmodel;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;

public class GlyphLayoutProcessorDefault extends AbstractGlyphLayoutProcessor implements GlyphLayoutProcessorInterface {
    @Override
    public boolean supportsFont(PDFont font) {
        return true;
    }


    /**
     * Returns the given font as GlyphLayoutProcessorDefault does not change the default behavior
     *
     * @param font given font
     * @return font the same font
     */
    @Override
    public PDFont getFont(PDFont font) {
        return font;
    }



    @Override
    protected float getStringWidthUni(PDType0Font font, float fontSize, String text, int bidiLevel) throws IOException {
        return font.getStringWidth(text) * font.getFontMatrix().getScaleX() * fontSize;
    }

    @Override
    protected void showTextUni(ContentStreamForGlyphLayoutInterface contentStream, PDType0Font font, float fontSize, String text, int bidiLevel) throws IOException {
        contentStream.showTextDefault(text);
    }
}
