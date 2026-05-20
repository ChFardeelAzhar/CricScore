package com.cricscore.app.core.extensions

import android.view.View
import java.util.Locale

// Number formatting Extensions
fun Float.formatSR(): String = String.format(Locale.US, "%.1f", this)
fun Float.formatEcon(): String = String.format(Locale.US, "%.2f", this)
fun Float.formatRR(): String = String.format(Locale.US, "%.2f", this)

// View Visibility Extensions
fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

// Click Debouncing Extension
class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeClick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (System.currentTimeMillis() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = System.currentTimeMillis()
        onSafeClick(v)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
