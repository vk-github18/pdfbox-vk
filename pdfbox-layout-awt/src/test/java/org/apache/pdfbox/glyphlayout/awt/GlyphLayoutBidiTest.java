/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.glyphlayout.awt;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.AbstractGlyphLayoutProcessor;
import org.junit.jupiter.api.Test;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Examples for bidirectional text with GlyphLayoutProcessorAwt
 *
 * @author Volker Kunert
 */
public class GlyphLayoutBidiTest extends TestBase
{
    public static final String TEXT1 = "نحن الآن في شهر رمضان 1447 هجري";
    public static final String TEXT2 = "Guten Tag ";
    public static final String TEXT3 = "السلام عليكم";
    public static final String TEXT4 = " Good afternoon";

    /*
     * show one line
     */
    private float showLine(PDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String text) throws IOException
    {
        return showLine(cs, new PDType0Font[]{font}, fontSize, x, y, new String[]{text});
    }

    /*
     * show one line
     */
    private float showLine(PDPageContentStream cs, PDType0Font[] fonts, float fontSize,
            float x, float y, String[] texts) throws IOException
    {
        cs.beginText();
        cs.newLineAtOffset(x, y);

        if (fonts.length != texts.length)
        {
            throw new IllegalArgumentException("Size of fonts and texts is different");
        }
        for (int i = 0; i < texts.length; i++)
        {
            cs.setFont(fonts[i], fontSize);
            cs.showText(texts[i]);
        }
        cs.endText();

        float height = fonts[0].getBoundingBox().getHeight();
        y -= height / 1000f * fontSize;
        return y;
    }

    /**
     * Test, no ActualText
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    @Test
    void testGlyphLayoutBidiNoActualText() throws IOException, FontFormatException, URISyntaxException {
        testGlyphLayoutBidi(false, "");
    }

    /**
     * Test with ActualText
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    @Test
    void testGlyphLayoutBidiUseActualText() throws IOException, FontFormatException, URISyntaxException {
        testGlyphLayoutBidi(true, "_ActualText");
    }

    /**
     * Test ligatures and kerning
     * @param useActualText
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    void testGlyphLayoutBidi(boolean useActualText, String sActualText) throws IOException, FontFormatException, URISyntaxException
    {
        AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions options = new AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions();
        if (useActualText) {
            options.useActualText();
        }
        GlyphLayoutProcessorAwt glyphLayoutProcessorAwt = new GlyphLayoutProcessorAwt(options);

        String outputBaseName = "GlyphLayoutBidi" + sActualText;
        String outputPDFFilePath = "target/" + outputBaseName + ".pdf";
        String outputTextFilePath = "target/" + outputBaseName + ".txt";

        String arabicPath = "/ttf/NotoSansArabic-Regular.ttf";
        String lgcPath = "/ttf/DejaVuSans.ttf";

        float fontSize = 12.0f;
        String writtenText;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font arabicFont = createPdType0Font(glyphLayoutProcessorAwt, doc, arabicPath);
            PDType0Font lgcFont = createPdType0Font(glyphLayoutProcessorAwt, doc, lgcPath);

            PDPage page = new PDPage();
            doc.addPage(page);
            try (TestPDPageContentStream cs = new TestPDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessorAwt);
                
                float x = page.getBBox().getLowerLeftX() + fontSize;
                float y = page.getBBox().getUpperRightY() - fontSize;
                
                y = showLine(cs, arabicFont, fontSize, x, y, TEXT1);
                printStringAsHex("TEXT1", TEXT1);
                writtenText = cs.getText();
                printStringAsHex("writtenText", writtenText);
                assertEquals(TEXT1, writtenText, "writtenText should equal writtenText");

                //DBG showLine(cs, new PDType0Font[]{ lgcFont, arabicFont, lgcFont }, fontSize, x, y, new String[]{ TEXT2, TEXT3, TEXT4 });
            }
            doc.save(outputPDFFilePath);
        }

        checkRenderIdent(outputBaseName + ".pdf");

        // Extract text
        try (PDDocument doc = Loader.loadPDF(new File(outputPDFFilePath)))
        {
            assertEquals(1, doc.getNumberOfPages());

            String strippedExtractedText = getAndWriteExtractedText(doc, outputTextFilePath);
            printStringAsHex("strippedExtractedText", strippedExtractedText); // ActualText looks good, why not extracted as is?

            assertEquals(writtenText, strippedExtractedText, "Extracted Text should equal the written text");
        }
    }

    @Test
    public void testUTF16StringToString() {
        // hex string extracted from ActualText in written PDF file
        String hexString = "FEFF0646062D06460020062706440622064600200641064A00200634064706310020063106450636062706460020003100340034003700200647062C0631064A";
        assertEquals("نحن الآن في شهر رمضان 1447 هجري", utf16HexToString(hexString), "String from hex numbers in ActualText should equal written Text");
    }
}
