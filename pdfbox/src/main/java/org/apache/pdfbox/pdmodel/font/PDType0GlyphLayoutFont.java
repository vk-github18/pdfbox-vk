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
package org.apache.pdfbox.pdmodel.font;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.GlyphLayoutProcessorInterface;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.io.InputStream;

/**
 * A Composite (Type 0) font with GlyphLayoutManager
 *
 * @author Volker Kunert
 */
public class PDType0GlyphLayoutFont extends PDType0Font implements PDVectorFont
{
    protected final GlyphLayoutProcessorInterface glyphLayoutProcessor;

    protected PDType0GlyphLayoutFont(GlyphLayoutProcessorInterface glyphLayoutProcessor,
                                  PDDocument doc, RandomAccessRead randomAccessRead,
                                  boolean embedSubset, boolean vertical)
            throws java.io.IOException
    {
        super(doc, new TTFParser().parse(randomAccessRead), embedSubset, true,
                vertical);
        this.glyphLayoutProcessor = glyphLayoutProcessor;

    }


    /**
     * Loads a TTF to be embedded into a document as a Type 0 font.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param input An input stream of a TrueType font. It will be closed before returning.
     * @param embedSubset True if the font will be subset before embedding. Set this to false when
     * creating a font for AcroForm.
     * @return A Type0 font with a CIDFontType2 descendant.
     * @throws IOException If there is an error reading the font stream.
     */
    public static PDType0GlyphLayoutFont load(GlyphLayoutProcessorInterface glyphLayoutProcessor,
                                              PDDocument doc, InputStream input, boolean embedSubset)
            throws IOException
    {
        return new PDType0GlyphLayoutFont(glyphLayoutProcessor, doc, RandomAccessReadBuffer.createBufferFromStream(input),
                embedSubset, false);
    }

    @Override
    public float getStringWidth(String text) throws IOException {
        return glyphLayoutProcessor.getStringWidth(this, 1.0f, text) / getFontMatrix().getScaleX();
    }

}
