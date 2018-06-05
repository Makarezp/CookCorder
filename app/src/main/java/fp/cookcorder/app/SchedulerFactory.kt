package fp.cookcorder.app

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SchedulerFactory @Inject() constructor() {

    fun io() = Schedulers.io()

    fun ui(): Scheduler = AndroidSchedulers.mainThread()
}