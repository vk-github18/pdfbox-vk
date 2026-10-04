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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example of formatting for letters defined in: DIN 91379: Characters in Unicode for the electronic
 * processing of names and data exchange in Europe
 * <p>
 * Use of the positioning features of Java.
 *
 * @author Volker Kunert
 */
class GlyphLayoutTransformTest extends TestBase
{
    static String TEXT =
                      "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H\n"
                    + "K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄\n"
                    + "T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀\n"
                    + "k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥̄\n"
                    + "s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ ē̍ Ī́ ī́\n"
                    + "ō̍ Ž̦ Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈\n"
                    ;

    /**
     * Test, no ActualText
     *
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    @Test
    void testGlyphLayoutTransformNoActualText() throws IOException, FontFormatException, URISyntaxException
    {
        testGlyphLayoutTransform(false, "");
    }

    /**
     * Test with ActualText
     *
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    @Test
    void testGlyphLayoutTransformUseActualText() throws IOException, FontFormatException, URISyntaxException
    {
        testGlyphLayoutTransform(true, "_ActualText");
    }

    /**
     * Test GlyphLayoutProcessorAwt with letters and sequences from DIN 91379
     * @param useActualText
     * @throws IOException
     * @throws FontFormatException
     * @throws URISyntaxException
     */
    void testGlyphLayoutTransform(boolean useActualText, String sActualText) throws IOException, FontFormatException, URISyntaxException
    {
        AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions options = new AbstractGlyphLayoutProcessor.GlyphLayoutProcessorOptions();
        if (useActualText)
        {
            options.useActualText();
        }
        GlyphLayoutProcessorAwt glyphLayoutProcessor = new GlyphLayoutProcessorAwt(options);

        String outputBaseName = String.format("GlyphLayoutTransform%s", sActualText);
        String outputPDFFilename = "target/" + outputBaseName + ".pdf";
        String outputTextFilename = "target/" + outputBaseName + ".txt";

        float fontSize = 12.0f;

        try (PDDocument doc = new PDDocument())
        {
            // Works poorly with DejaVu Sans, see discussion on PDFBOX-4951 on 5.7.2026
            InputStream fontStream = GlyphLayoutTransformTest.class.getResourceAsStream("/ttf/Arimo-Regular.ttf");
            // last parameter is just for better code coverage
            PDType0Font font = glyphLayoutProcessor.loadFont(doc, fontStream, true);

            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page))
            {
                cs.setGlyphLayoutProcessor(glyphLayoutProcessor);

                float x = page.getBBox().getLowerLeftX() + fontSize;
                float y = page.getBBox().getUpperRightY() - fontSize;
                String[] lines = TEXT.split("\n");
                showRotating(cs, font, fontSize, x, y, lines[0]);
                showScaled(cs, font, fontSize, x, y, lines[1]);
            }
            doc.save(outputPDFFilename);
        }
//TODO        checkRenderIdent(outputBaseName + ".pdf");

        // Extract text
        try (PDDocument doc = Loader.loadPDF(new File(outputPDFFilename)))
        {
            assertEquals(1, doc.getNumberOfPages());
            
            PDFTextStripper stripper = new PDFTextStripper();
            String s = stripper.getText(doc);
            try (OutputStream os = new FileOutputStream(outputTextFilename))
            {
                os.write (0xEF);
                os.write (0xBB);
                os.write (0xBF);

                try (Writer writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8)))
                {
                    //TODO compare this output with the input, like in TextStripper test
                    // Not yet correct as of 4.7.2026
                    writer.write(s);
                }
            }
        }
    }

    /*
     * break the text into lines and show them
     */
    private void showLines(PDPageContentStream cs, PDType0Font font, float fontSize,
            float x, float y, String s) throws IOException
    {

        s = s.replace("\t", "    ");
        String[] lines = s.split("[\n]");

        for (String line : lines)
        {
            if (!line.isEmpty())
            {
                showOneLine(cs, font, fontSize, x, y, line);
                y -= fontSize * 1.5f;
            }
        }
    }

    /*
     * show one line
     */
    void showOneLine(PDPageContentStream cs, PDType0Font font, float fontSize,
                            float x, float y, String line) throws IOException
    {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(line);
        cs.endText();
    }


    /*
     * show rotating
     */
    void showRotating(PDPageContentStream cs, PDType0Font font, float fontSize,
                      float x, float y, String line) throws IOException
    {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        Matrix m = new Matrix();
        m.translate(300, 300);
        m.rotate(1.55);
        m.scale(2,3);
        cs.setTextMatrix(m);
        String[] words = line.split(" ");
        for (String word: words) {
            cs.showText(word);
        }
        cs.endText();
    }

    /*
     * show scaled
     */
    void showScaled(PDPageContentStream cs, PDType0Font font, float fontSize,
                      float x, float y, String line) throws IOException
    {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        Matrix m = new Matrix();
        m.translate(100, 100);
        m.scale(0.7f, 2f);
        cs.setTextMatrix(m);
        String[] words = line.split(" ");
        for (String word: words) {
            cs.showText(word);
        }
        cs.endText();
    }
}