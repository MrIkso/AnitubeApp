package com.mrikso.anitube.app.utils;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.QuoteSpan;
import android.text.style.StyleSpan;
import androidx.core.text.HtmlCompat;
import java.lang.reflect.Method;
import org.xml.sax.XMLReader;

public class HtmlTextSpanner {

    public static Spanned spanText(String htmlText) {
        Spanned formattedText =
                HtmlCompat.fromHtml(
                        htmlText,
                        HtmlCompat.FROM_HTML_MODE_LEGACY,
                        null,
                        new Html.TagHandler() {
                            boolean inQuote = false;
                            boolean inTitle = false;

                            @Override
                            public void handleTag(
                                    boolean opening,
                                    String tag,
                                    Editable output,
                                    XMLReader xmlReader) {
                                if (tag.equalsIgnoreCase("div") && output != null) {
                                    if (opening) {
                                        String classAttribute =
                                                getAttributeValue(xmlReader, "class");
                                        if ("quote".equalsIgnoreCase(classAttribute)) {
                                            output.setSpan(
                                                    new QuoteSpan(),
                                                    output.length(),
                                                    output.length(),
                                                    Spannable.SPAN_MARK_MARK);
                                            inQuote = true;
                                        } else if ("title_quote".equalsIgnoreCase(classAttribute)) {
                                            inTitle = true;
                                        }
                                    } else {
                                        if (inQuote) {
                                            int spanStart =
                                                    output.getSpanStart(Spannable.SPAN_MARK_MARK);
                                            if (spanStart >= 0) {
                                                output.setSpan(
                                                        new QuoteSpan(),
                                                        spanStart,
                                                        output.length(),
                                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            }
                                            inQuote = false;
                                        } else if (inTitle) {
                                            int spanStart =
                                                    output.getSpanStart(Spannable.SPAN_MARK_MARK);
                                            if (spanStart >= 0) {
                                                output.setSpan(
                                                        new StyleSpan(Typeface.BOLD),
                                                        spanStart,
                                                        output.length(),
                                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            }
                                            inTitle = false;
                                        }
                                    }
                                }
                            }
                        });
        return formattedText;
    }

    private static String getAttributeValue(XMLReader xmlReader, String attributeName) {
        try {
            Method method = xmlReader.getClass().getMethod("getAttributeValue", String.class);
            return (String) method.invoke(xmlReader, attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
