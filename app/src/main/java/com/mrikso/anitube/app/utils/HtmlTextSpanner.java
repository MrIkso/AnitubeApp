package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.text.Spanned;
import android.text.util.Linkify;

import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.html.CustomHtmlToSpannedConverter;
import com.mrikso.anitube.app.utils.html.ImageGetterUtils;
import com.mrikso.anitube.app.utils.html.ListTagHandler;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class HtmlTextSpanner {

    public static Spanned formatContent(String source, Context context) {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, new HTMLSchema());
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        CustomHtmlToSpannedConverter converter = new CustomHtmlToSpannedConverter(
                source,  ImageGetterUtils.getImageGetter(context), new ListTagHandler(), parser, ApiClient.BASE_URL, context);

        return CustomHtmlToSpannedConverter.linkifySpanned(converter.convert(), Linkify.ALL);
    }


}
