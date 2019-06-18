package fp.cookcorder.screen

import android.os.Bundle
import android.view.MotionEvent
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.screen.record.RecordFragment


class MainActivity : DaggerAppCompatActivity() {

    var onTouchListener: ((MotionEvent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().run {
                replace(R.id.container, RecordFragment.newInstance(), "record")
                commit()
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        onTouchListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val KEY_LAUNCH_PAGE = "KEY_LAUNCH_PAGE"
    }
}
