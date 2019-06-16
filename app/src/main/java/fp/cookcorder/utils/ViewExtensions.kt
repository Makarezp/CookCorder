package fp.cookcorder.utils

import android.content.res.Resources
import android.view.View
import android.widget.TextView

fun View.onClick(onClickListener: (View) -> Unit) = this.setOnClickListener(onClickListener)

fun View.visibleOrGone(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun TextView.setTextHideIfNull(charSequence: CharSequence?) {
    if (charSequence != null && charSequence.isNotEmpty()) {
        this.visibility = View.VISIBLE
        this.text = charSequence
    } else {
        this.visibility = View.GONE
    }
}

fun TextView.setTextInvisibleIfEmptyOrNull(charSequence: CharSequence?) {
    if (charSequence != null && charSequence.isNotEmpty()) {
        this.visibility = View.VISIBLE
        this.text = charSequence
    } else {
        this.visibility = View.INVISIBLE
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)