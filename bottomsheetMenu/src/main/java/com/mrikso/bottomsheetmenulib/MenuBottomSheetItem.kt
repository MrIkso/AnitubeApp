package com.mrikso.bottomsheetmenulib

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class MenuBottomSheetItem(
    var title: String,
    var iconRes: Int? = null,
    var iconDrawable: @RawValue Drawable? = null,
    var id: Int? = null
) : Parcelable