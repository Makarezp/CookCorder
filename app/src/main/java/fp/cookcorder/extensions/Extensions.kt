package fp.cookcorder.extensions

import android.view.View

fun View.onClick(onClickListener: (View) -> Unit) = this.setOnClickListener(onClickListener)