package com.mrikso.anitube.app.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View

fun makeLinks(
    text: String,
    phrase: String,
    /* phraseColor: Int,*/
    listener: View.OnClickListener
): SpannableString {
    val spannableString = SpannableString(text)
    val clickableSpan = object : ClickableSpan() {
        /*override fun updateDrawState(ds: TextPaint) {
           // ds.color = phraseColor      // you can use custom color
            ds.isUnderlineText = false  // this remove the underline
        }*/
        override fun onClick(view: View) {
            listener.onClick(view)
        }
    }
    val start = text.indexOf(phrase)
    val end = start + phrase.length
    spannableString.setSpan(
        clickableSpan,
        start,
        end,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannableString
}
