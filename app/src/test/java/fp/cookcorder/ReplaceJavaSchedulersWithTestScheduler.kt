package fp.cookcorder

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ReplaceJavaSchedulersWithTestScheduler(
        val replacementScheduler: Scheduler = Schedulers.trampoline()
) : TestRule {

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { replacementScheduler }
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { replacementScheduler }
                RxAndroidPlugins.setMainThreadSchedulerHandler { replacementScheduler }

                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                    RxAndroidPlugins.reset()
                }
            }
        }
    }
}