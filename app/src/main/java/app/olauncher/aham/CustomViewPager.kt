package app.olauncher.aham

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    var isPagingEnabled = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(ev)
    }
}