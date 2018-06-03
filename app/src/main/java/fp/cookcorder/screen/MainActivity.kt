package fp.cookcorder.screen

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.screen.main.MainFragment

class MainActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }
}
