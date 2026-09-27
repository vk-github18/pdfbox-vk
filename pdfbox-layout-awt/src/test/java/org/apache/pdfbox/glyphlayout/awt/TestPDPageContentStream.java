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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.Closeable;
import java.io.IOException;

/**
 * Test class to record the written text
 */
public class TestPDPageContentStream implements Closeable {

    private final StringBuilder sb = new StringBuilder();
    private final PDPageContentStream cs;

    public TestPDPageContentStream(PDDocument document, PDPage sourcePage) throws IOException {
        cs = new PDPageContentStream(document, sourcePage);
    }

    public void showText(String s) throws IOException {
        sb.append(s);
        cs.showText(s);
    }

    public void  newLineAtOffset(float x, float y) throws IOException {
        sb.append("\n");
        cs.newLineAtOffset(x, y) ;
    }

    public String getText() {
        return sb.toString().replaceAll(" +"," ").strip();
    }

    public void setGlyphLayoutProcessor(GlyphLayoutProcessorAwt glyphLayoutProcessor) {
        cs.setGlyphLayoutProcessor(glyphLayoutProcessor);
    }

    @Override
    public void close() throws IOException {
        cs.close();
    }

    public void beginText() throws IOException {
        cs.beginText();
    }

    public void setFont(PDType0Font font, float fontSize) throws IOException {
        cs.setFont(font, fontSize);
    }

    public void endText() throws IOException {
        cs.endText();
    }

    public void moveTo(float x, float y) throws IOException {
        cs.moveTo(x, y);
    }

    public void lineTo(float x, float y) throws IOException {
        cs.lineTo(x, y);
    }

    public void stroke() throws IOException {
        cs.stroke();
    }
}
