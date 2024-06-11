package com.mrikso.anitube.app.utils.html;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.ParagraphStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;
import com.google.android.material.color.MaterialColors;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CustomHtmlToSpannedConverter implements ContentHandler {

    private static final float[] HEADER_SIZES = {1.5f, 1.4f, 1.3f, 1.2f, 1.1f,
            1f,};
    private static final String MONOSPACE = "monospace";

    private static final HashMap<String, Integer> COLORS = buildColorMap();
    private static int titleQuoteColor;
    private static int quoteColor;
    private static int quoteBackgroundColor;

    private final String mSource;
    private final XMLReader mReader;
    private final SpannableStringBuilder mSpannableStringBuilder;
    private final Html.ImageGetter mImageGetter;
    private final Html.TagHandler mTagHandler;
    private final String mBaseUri;
    private boolean isCode;
    private boolean isQuote;
    private boolean isTitleQuote;

    public CustomHtmlToSpannedConverter(String source,
                                        Html.ImageGetter imageGetter, Html.TagHandler tagHandler,
                                        XMLReader parser, String baseUri, Context context) {
        mSource = source;
        mSpannableStringBuilder = new SpannableStringBuilder();
        mImageGetter = imageGetter;
        mTagHandler = tagHandler;
        mReader = parser;
        mBaseUri = baseUri;
        titleQuoteColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, null);
        quoteColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorTertiary, null);
        quoteBackgroundColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, null);
    }

    private static void handleP(SpannableStringBuilder text) {
        int len = text.length();

        if (len >= 1 && text.charAt(len - 1) == '\n') {
            if (len >= 2 && text.charAt(len - 2) == '\n') {
                return;
            }

            text.append("\n");
            return;
        }

        if (len != 0) {
            text.append("\n\n");
        }
    }

    private static void handleBr(SpannableStringBuilder text) {
        text.append("\n");
    }

    private static Object getLast(Spanned text, Class kind) {
        /*
         * This knows that the last returned object from getSpans() will be the
         * most recently added.
         */
        Object[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }

    private static void start(SpannableStringBuilder text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void endMultiple(SpannableStringBuilder text, Class kind,
                                    Object[] replArray) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            for (Object repl : replArray) {
                text.setSpan(repl, where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

    }

    private static void end(SpannableStringBuilder text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

    }

    private static void startImg(SpannableStringBuilder text,
                                 Attributes attributes, Html.ImageGetter img) {
        String cssClass = attributes.getValue("class");
        String src = cssClass != null && attributes.getValue("", "src") == null ? cssClass.substring(cssClass.indexOf("-") + 1) : attributes.getValue("", "src");
        Drawable d = null;

        if (img != null) {
            d = img.getDrawable(src);
        }

        if (d == null) {
            // don't draw anything
            return;
        }

        int len = text.length();
        text.append("\uFFFC");

        text.setSpan(new ImageSpan(d, src), len, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void startFont(SpannableStringBuilder text,
                                  Attributes attributes) {
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        int len = text.length();
        text.setSpan(new Font(color, face), len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void endFont(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Font.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Font f = (Font) obj;

            if (!TextUtils.isEmpty(f.mColor)) {
                if (f.mColor.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = f.mColor.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        ColorStateList colors = res.getColorStateList(colorRes);
                        text.setSpan(new TextAppearanceSpan(null, 0, 0, colors,
                                        null), where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    int c = getHtmlColor(f.mColor);
                    if (c != -1) {
                        text.setSpan(new ForegroundColorSpan(c | 0xFF000000),
                                where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            if (f.mFace != null) {
                text.setSpan(new TypefaceSpan(f.mFace), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void processQuote(SpannableStringBuilder text, int color, boolean isHeader, boolean isClose) {
        if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') {
            text.append("\n");
        }

        if(!isClose){
            start(text, new Blockquote());
        }
        else {
            if(!isHeader) {
                handleP(text);
            }
            end(text, Blockquote.class, new CustomQuoteSpan(quoteBackgroundColor, color));
        }
    }

    private static void startA(SpannableStringBuilder text,
                               Attributes attributes, String baseUri) {
        String href = attributes.getValue("", "href");

        if (href != null && !href.startsWith("http")) {
            String prefix;
            if (!href.startsWith("/")) {
                prefix = baseUri + "/";
            } else {
                prefix = baseUri;
            }
            href = prefix + href;
        }

        int len = text.length();
        text.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
    }

    private static void endA(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Href.class);
        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        if (where != len) {
            Href h = (Href) obj;

            if (h.mHref != null) {
                text.setSpan(new URLSpan(h.mHref), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void endHeader(SpannableStringBuilder text) {
        int len = text.length();
        Object obj = getLast(text, Header.class);

        int where = text.getSpanStart(obj);

        text.removeSpan(obj);

        // Back off not to change only the text, not the blank line.
        while (len > where && text.charAt(len - 1) == '\n') {
            len--;
        }

        if (where != len) {
            Header h = (Header) obj;

            text.setSpan(new RelativeSizeSpan(HEADER_SIZES[h.mLevel]), where,
                    len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static HashMap<String, Integer> buildColorMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("aqua", 0x00FFFF);
        map.put("black", 0x000000);
        map.put("blue", 0x0000FF);
        map.put("fuchsia", 0xFF00FF);
        map.put("green", 0x008000);
        map.put("grey", 0x808080);
        map.put("lime", 0x00FF00);
        map.put("maroon", 0x800000);
        map.put("navy", 0x000080);
        map.put("olive", 0x808000);
        map.put("purple", 0x800080);
        map.put("red", 0xFF0000);
        map.put("silver", 0xC0C0C0);
        map.put("teal", 0x008080);
        map.put("white", 0xFFFFFF);
        map.put("yellow", 0xFFFF00);
        return map;
    }

    /**
     * Converts an HTML color (named or numeric) to an integer RGB value.
     *
     * @param color Non-null color string.
     * @return A color value, or {@code -1} if the color string could not be
     * interpreted.
     */
    private static int getHtmlColor(String color) {
        Integer i = COLORS.get(color.toLowerCase(Locale.US));
        if (i != null) {
            return i;
        } else {
            try {
                return convertValueToInt(color, -1);
            } catch (NumberFormatException nfe) {
                return -1;
            }
        }
    }

    /**
     * Copied from com.android.internal.util.XmlUtils from Android source
     */
    private static int convertValueToInt(CharSequence charSeq,
                                         int defaultValue) {
        if (null == charSeq)
            return defaultValue;

        String nm = charSeq.toString();

        // XXX This code is copied from Integer.decode() so we don't
        // have to instantiate an Integer!

        int value;
        int sign = 1;
        int index = 0;
        int len = nm.length();
        int base = 10;

        if ('-' == nm.charAt(0)) {
            sign = -1;
            index++;
        }

        if ('0' == nm.charAt(index)) {
            // Quick check for a zero by itself
            if (index == (len - 1))
                return 0;

            char c = nm.charAt(index + 1);

            if ('x' == c || 'X' == c) {
                index += 2;
                base = 16;
            } else {
                index++;
                base = 8;
            }
        } else if ('#' == nm.charAt(index)) {
            index++;
            base = 16;
        }

        return Integer.parseInt(nm.substring(index), base) * sign;
    }

    /**
     * <<<<<<< e370c9bc00e8f6b33b1d12a44b4c70a7f063c8b9
     * Parses Spanned text for existing html links and reapplies them after the text has been Linkified
     * =======
     * Parses Spanned text for existing html links and reapplies them.
     * >>>>>>> Issue #168 bugfix for links not being clickable, adds autoLink feature including web, phone, map and email
     *
     * @param spann {@link Spanned} text which has to be Linkified
     * @param mask  bitmask to define which kinds of links will be searched and applied (e.g. <a href="https://developer.android.com/reference/android/text/util/Linkify.html#ALL">Linkify.ALL</a>)
     * @return Linkified {@link Spanned} text
     * @see <a href="https://developer.android.com/reference/android/text/util/Linkify.html">Linkify</a>
     */
    public static Spanned linkifySpanned(@NonNull final Spanned spann, final int mask) {
        URLSpan[] existingSpans = spann.getSpans(0, spann.length(), URLSpan.class);
        List<Pair<Integer, Integer>> links = new ArrayList<>();

        for (URLSpan urlSpan : existingSpans) {
            links.add(new Pair<>(spann.getSpanStart(urlSpan), spann.getSpanEnd(urlSpan)));
        }

        Linkify.addLinks((Spannable) spann, mask);

        // add the links back in
        for (int i = 0; i < existingSpans.length; i++) {
            ((Spannable) spann).setSpan(existingSpans[i], links.get(i).first, links.get(i).second, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spann;
    }

    public Spanned convert() {

        mReader.setContentHandler(this);
        try {
            mReader.parse(new InputSource(new StringReader(mSource)));
        } catch (IOException | SAXException e) {
            // We are reading from a string. There should not be IO problems.
            throw new RuntimeException(e);
        }

        // Fix flags and range for paragraph-type markup.
        Object[] obj = mSpannableStringBuilder.getSpans(0,
                mSpannableStringBuilder.length(), ParagraphStyle.class);
        for (int i = 0; i < obj.length; i++) {
            int start = mSpannableStringBuilder.getSpanStart(obj[i]);
            int end = mSpannableStringBuilder.getSpanEnd(obj[i]);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0
                    && mSpannableStringBuilder.charAt(end - 1) == '\n'
                    && mSpannableStringBuilder.charAt(end - 2) == '\n') {
                end--;
            }

            if (end == start) {
                mSpannableStringBuilder.removeSpan(obj[i]);
            } else {
                mSpannableStringBuilder.setSpan(obj[i], start, end,
                        Spannable.SPAN_PARAGRAPH);
            }
        }

        return mSpannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a
            // </br> for each <br>
            // so we can safely emite the linebreaks when we handle the close
            // tag.
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            String cssClass = attributes.getValue("class");
            if ("title_quote".equals(cssClass)) {
                processQuote(mSpannableStringBuilder, titleQuoteColor, true, false);
                isTitleQuote = true;
            } else if ("quote".equals(cssClass)) {
                processQuote(mSpannableStringBuilder, quoteColor, false, false);
                isQuote = true;
            } else {
                handleP(mSpannableStringBuilder);
            }
        } else if (tag.equalsIgnoreCase("strong")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("em")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("cite")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("i")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("big")) {
            start(mSpannableStringBuilder, new Big());
        } else if (tag.equalsIgnoreCase("small")) {
            start(mSpannableStringBuilder, new Small());
        } else if (tag.equalsIgnoreCase("font")) {
            startFont(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(mSpannableStringBuilder);
            start(mSpannableStringBuilder, new Blockquote());
        } else if (tag.equalsIgnoreCase("tt")) {
            start(mSpannableStringBuilder, new Monospace());
        } else if (tag.equalsIgnoreCase("a")) {
            startA(mSpannableStringBuilder, attributes, mBaseUri);
        } /*else if (tag.equalsIgnoreCase("span")) {
            startSpan(mSpannableStringBuilder, attributes);
        }*/ else if (tag.equalsIgnoreCase("u")) {
            start(mSpannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("sup")) {
            start(mSpannableStringBuilder, new Super());
        } else if (tag.equalsIgnoreCase("sub")) {
            start(mSpannableStringBuilder, new Sub());
        } else if (tag.equalsIgnoreCase("code")) {
            isCode = true;
            start(mSpannableStringBuilder, new InlineCode());
        } else if (tag.equalsIgnoreCase("pre")) {
            isCode = true;
            start(mSpannableStringBuilder, new CodeBlock());
        } else if (tag.equalsIgnoreCase("del")) {
            start(mSpannableStringBuilder, new StrikeThrough());
        } else if (tag.equalsIgnoreCase("s")) {
            start(mSpannableStringBuilder, new StrikeThrough());
        } else if (tag.equalsIgnoreCase("strike")) {
            start(mSpannableStringBuilder, new StrikeThrough());
        } else if (tag.length() == 2
                && Character.toLowerCase(tag.charAt(0)) == 'h'
                && tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(mSpannableStringBuilder);
            start(mSpannableStringBuilder, new Header(tag.charAt(1) - '1'));
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(mSpannableStringBuilder, attributes, mImageGetter);
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(true, tag, mSpannableStringBuilder, mReader);
        }
    }

    private void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            handleP(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            if (isTitleQuote) {
                processQuote(mSpannableStringBuilder, titleQuoteColor, true, true);
                isTitleQuote = false;
            } else if (isQuote) {
                processQuote(mSpannableStringBuilder, quoteColor, false, true);
                isQuote = false;
            } else {
                handleP(mSpannableStringBuilder);
            }
        } else if (tag.equalsIgnoreCase("strong")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(
                    Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("b")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(
                    Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("em")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(
                    Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("cite")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(
                    Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(
                    Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("i")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(
                    Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("big")) {
            end(mSpannableStringBuilder, Big.class, new RelativeSizeSpan(1.25f));
        } else if (tag.equalsIgnoreCase("small")) {
            end(mSpannableStringBuilder, Small.class,
                    new RelativeSizeSpan(0.8f));
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            handleP(mSpannableStringBuilder);
            end(mSpannableStringBuilder, Blockquote.class,
                    new CustomQuoteSpan());
        } else if (tag.equalsIgnoreCase("tt")) {
            end(mSpannableStringBuilder, Monospace.class, new TypefaceSpan(
                    MONOSPACE));
        } else if (tag.equalsIgnoreCase("a")) {
            endA(mSpannableStringBuilder);
        } /*else if (tag.equalsIgnoreCase("span")) {
            endSpan(mSpannableStringBuilder);
        }*/ else if (tag.equalsIgnoreCase("u")) {
            end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            end(mSpannableStringBuilder, Super.class, new SuperscriptSpan());
        } else if (tag.equalsIgnoreCase("sub")) {
            end(mSpannableStringBuilder, Sub.class, new SubscriptSpan());
        } else if (tag.equalsIgnoreCase("code")) {
            endMultiple(mSpannableStringBuilder, InlineCode.class,
                    new Object[]{new TypefaceSpan(MONOSPACE),
                            new ForegroundColorSpan(0xffdd1144),
                            new BackgroundColorSpan(0xfff7f7f9)
                    });
            isCode = false;
        } else if (tag.equalsIgnoreCase("pre")) {
            endMultiple(mSpannableStringBuilder, CodeBlock.class, new Object[]{
                    new TypefaceSpan(MONOSPACE),
                    new ForegroundColorSpan(0xff000000),
                    new CodeBlockLine(0xfff5f5f5)
            });
            isCode = false;
        } else if (tag.equalsIgnoreCase("del")) {
            end(mSpannableStringBuilder, StrikeThrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("s")) {
            end(mSpannableStringBuilder, StrikeThrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("strike")) {
            end(mSpannableStringBuilder, StrikeThrough.class, new StrikethroughSpan());
        } else if (tag.length() == 2
                && Character.toLowerCase(tag.charAt(0)) == 'h'
                && tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            handleP(mSpannableStringBuilder);
            endHeader(mSpannableStringBuilder);
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(false, tag, mSpannableStringBuilder, mReader);
        }
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        handleStartTag(localName, attributes);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        handleEndTag(localName);
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace; newlines
         * count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = mSpannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (!isCode) {
                    if (pred != ' ' && pred != '\n') {
                        sb.append(' ');
                    }
                } else {
                    if ((pred != ' ' && c == ' ') || (pred != '\n' && c == '\n')) {
                        sb.append(c);
                    }
                }
            } else {
                sb.append(c);
            }
        }

        mSpannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    private static class Bold {
    }

    private static class Italic {
    }

    private static class Underline {
    }

    private static class Big {
    }

    private static class Small {
    }

    private static class Monospace {
    }

    private static class Blockquote {
    }

    private static class Super {
    }

    private static class Sub {
    }

    private static class InlineCode {
    }

    private static class CodeBlock {
    }

    private static class StrikeThrough {
    }

    private static class Font {
        public String mColor;
        public String mFace;

        public Font(String color, String face) {
            mColor = color;
            mFace = face;
        }
    }

    private static class Href {
        public String mHref;

        public Href(String href) {
            mHref = href;
        }
    }

    private static class Header {
        private final int mLevel;

        public Header(int level) {
            mLevel = level;
        }
    }

    private static class CodeBlockLine implements LineBackgroundSpan {
        private final int fillColor;

        public CodeBlockLine(int fillColor) {
            this.fillColor = fillColor;
        }

        @Override
        public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline,
                                   int bottom, CharSequence text, int start, int end, int lnum) {
            int paddingInPx = ConvertUtils.dp2px(12);
            Rect rect = new Rect(left - paddingInPx, top - paddingInPx,
                    right + paddingInPx, bottom + paddingInPx);
            final int paintColor = p.getColor();
            p.setColor(this.fillColor);
            c.drawRect(rect, p);
            p.setColor(paintColor);
        }
    }

    public static class CustomQuoteSpan implements LeadingMarginSpan, LineBackgroundSpan {
        private final int backgroundColor;
        private final int stripeColor;
        private int stripeWidth = 10;
        private int gap = 20;

        public CustomQuoteSpan() {
            this.backgroundColor = 0xffffffff;
            this.stripeColor = 0xfff7f7f9;
        }

        public CustomQuoteSpan(int backgroundColor, int stripeColor) {
            this.backgroundColor = backgroundColor;
            this.stripeColor = stripeColor;
        }

        public CustomQuoteSpan(int backgroundColor, int stripeColor, int stripeWidth, int gap) {
            this.backgroundColor = backgroundColor;
            this.stripeColor = stripeColor;
            this.stripeWidth = stripeWidth;
            this.gap = gap;
        }

        @Override
        public int getLeadingMargin(boolean first) {
            return stripeWidth + gap;
        }

        @Override
        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
            Paint.Style style = p.getStyle();
            int color = p.getColor();

            p.setStyle(Paint.Style.FILL);
            p.setColor(stripeColor);

            c.drawRect(x, top, x + dir * stripeWidth, bottom, p);

            p.setStyle(style);
            p.setColor(color);
        }

        @Override
        public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
            Paint.Style style = p.getStyle();
            int color = p.getColor();

            p.setStyle(Paint.Style.FILL);
            p.setColor((backgroundColor));

            c.drawRect(left, top, right, bottom, p);

            p.setStyle(style);
            p.setColor(color);
        }
    }
}
