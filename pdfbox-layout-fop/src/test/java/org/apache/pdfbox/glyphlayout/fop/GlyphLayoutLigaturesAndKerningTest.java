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

package org.apache.pdfbox.glyphlayout.fop;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.AbstractGlyphLayoutProcessor;
import org.junit.jupiter.api.Test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Examples for ligatures and kerning
 * See <a href="https://issues.apache.org/jira/browse/PDFBOX-4951">PDFBOX-4951</a>
 *
 * The default processing of GlyphLayoutProcessor is with ligatures and kerning disabled.
 * You can enable ligatures and kerning using FontOptions, see below.
 *
 * @author Volker Kunert
 */
class GlyphLayoutLigaturesAndKerningTest extends TestBase
{
    static final String FIRACODE_STRING = "!= == === >= <=";
    static final String DEJAVU_STRING =  "AVATAR, effective, affiliation, float, film, affluent";
    static final String BENGALI_STRING =  "আমি কোন পথে ক্ষীরের লক্ষ্মী ষন্ড পুতুল রুপো গঙ্গা ঋষি";
    static final String THAI_STRING =  "กูกินก้งปิ้งอยู่ในถ้ำ";
    static final String BENGALI_STRING2 =  "হ্যালো ওয়ার্ল্ড";

    /**
     * Check that missing glyph is caught like in main pdfbox.
     *
     * @throws IOException
     */
    @Test
    void testMissingGlyph() throws IOException
    {
        GlyphLayoutProcessorFop glyphLayoutProcessor = new GlyphLayoutProcessorFop();

        String lohitBengaliPath = "/ttf/Lohit-Bengali.ttf";

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font lohitBengaliFont = createPdType0Font(glyphLayoutProcessor, doc, lohitBengaliPath);

            PDPage page = new PDPage();
            doc.addPage(page);
            try (TestPDPageContentStream cs = new TestPDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
                        showLines(cs, lohitBengaliFont, 1, 0, 0, "123ABC"));
                assertEquals("Missing glyph in font 'Lohit-Bengali' for the character 'A', codePoint: 65 (U+0041).", ex.getMessage());

                // Ignore the "You did not call endText()" warning, this is because of the premature close
            }
        }
    }

    /**
     * Test, no ActualText
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testLigaturesAndKerningNoActualText() throws IOException, URISyntaxException {
        testLigaturesAndKerning(false, "");
    }

    /**
     * Test with ActualText
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void testLigaturesAndKerningUseActualText() throws IOException, URISyntaxException {
        testLigaturesAndKerning(true, "_ActualText");
    }

    /**
     * Test ligatures and kerning
     * @param useActualText
     * @throws IOException
     * @throws URISyntaxException
     */
    void testLigaturesAndKerning(boolean useActualText, String sActualText) throws IOException, URISyntaxException
    {
        AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions options = new AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions();
        if (useActualText) {
            options.useActualText();
        }
        GlyphLayoutProcessorFop glyphLayoutProcessor = new GlyphLayoutProcessorFop(options);

        String outputBaseName = "GlyphLayoutLigaturesAndKerning" + sActualText;
        String outputPDFFilePath = "target/" + outputBaseName + ".pdf";
        String outputTextFilePath = "target/" + outputBaseName + ".txt";

        String firaPath = "/ttf/FiraCode-Regular.ttf";
        String dejavuPath = "/ttf/DejaVuSans.ttf"; // ligatures not in Liberation nor in Arimo
        String thaiPath = "/ttf/NotoSansThai-Regular.ttf";
        String lohitBengaliPath = "/ttf/Lohit-Bengali.ttf";

        float fontSize = 12.0f;
        String writtenText;

        try (PDDocument doc = new PDDocument())
        {
            PDType0Font firaFont = createPdType0Font(glyphLayoutProcessor, doc, firaPath);
            PDType0Font firaLigFont = createPdType0Font(glyphLayoutProcessor, doc, firaPath);
            
            PDType0Font dejavuFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath);
            
            PDType0Font dejavuLigFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath);
            
            PDType0Font dejavuKernFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath);
            
            PDType0Font dejavuLigKernFont = createPdType0Font(glyphLayoutProcessor, doc, dejavuPath);

            PDType0Font thaiFont = createPdType0Font(glyphLayoutProcessor, doc, thaiPath);

            PDType0Font lohitBengaliFont = createPdType0Font(glyphLayoutProcessor, doc, lohitBengaliPath);
            
            PDPage page = new PDPage();
            doc.addPage(page);
            try (TestPDPageContentStream cs = new TestPDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);
                
                float x = page.getBBox().getLowerLeftX() + fontSize;
                float y = page.getBBox().getUpperRightY() - fontSize;
                y = showLines(cs, firaFont, fontSize, x, y, FIRACODE_STRING);
                y = showLines(cs, firaLigFont, fontSize, x, y, FIRACODE_STRING + " (Ligatures)");
                y = showLines(cs, dejavuFont, fontSize, x, y, DEJAVU_STRING);
                y = showLines(cs, dejavuLigFont, fontSize, x, y, DEJAVU_STRING + " (Ligatures)");
                y = showLines(cs, dejavuKernFont, fontSize, x, y, DEJAVU_STRING + " (Kerning)");
                y = showLines(cs, dejavuLigKernFont, fontSize, x, y, DEJAVU_STRING + " (Ligatures and kerning)");
                y = showLines(cs, thaiFont, fontSize, x, y, THAI_STRING);
                y = showLines(cs, lohitBengaliFont, fontSize, x, y - 5, BENGALI_STRING + " (ভারত)");

                // from the related awt test. Unclear if useful in the future if FOP ever supports bengali
                // and we get access to it.
                cs.beginText();
                cs.setFont(lohitBengaliFont, 20);
                cs.newLineAtOffset(x, y - 20);
                cs.showText(BENGALI_STRING2);
                cs.showText(" ");
                cs.showText(BENGALI_STRING2);
                cs.endText();
                writtenText = cs.getText();
            }
            doc.save(outputPDFFilePath);
        }

        checkRenderIdent(outputBaseName + ".pdf");

        // Extract text
        try (PDDocument doc = Loader.loadPDF(new File(outputPDFFilePath)))
        {
            assertEquals(1, doc.getNumberOfPages());

            String strippedExtractedText = getAndWriteExtractedText(doc, outputTextFilePath);

            assertEquals(writtenText, strippedExtractedText, "Extracted Text should equal the written text");

            assertEquals(writtenText, strippedExtractedText, "Extracted Text should equal the written text");
        }
    }

    /**
     * break the text into lines and show them
     */
    private float showLines(TestPDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String s) throws IOException
    {

        s = s.replace("\t", "    ");
        String[] lines = s.split("\\n");

        float height = font.getBoundingBox().getHeight();

        for (String line : lines)
        {
            if (!line.isEmpty())
            {
                showOneLine(cs, font, fontSize, x, y, line);
                y -= height / 1000f * fontSize;
            }
        }
        return y;
    }
}
