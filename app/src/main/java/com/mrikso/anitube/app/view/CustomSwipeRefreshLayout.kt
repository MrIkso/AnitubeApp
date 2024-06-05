package com.mrikso.anitube.app.view

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.MaterialColors

class CustomSwipeRefreshLayout : SwipeRefreshLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        setProgressBackgroundColorSchemeColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface))
        setColorSchemeColors(MaterialColors.getColor(this, com.google.android.material.R.attr.colorControlNormal))
    }
}