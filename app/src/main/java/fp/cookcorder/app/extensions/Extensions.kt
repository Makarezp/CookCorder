package fp.cookcorder.app.extensions

import android.view.View

fun View.onClick(onClickListener: (View) -> Unit) = this.setOnClickListener(onClickListener)