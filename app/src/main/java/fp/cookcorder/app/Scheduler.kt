package fp.cookcorder.app

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class Scheduler @Inject() constructor() {

    fun io() = Schedulers.io()

    fun ui() = AndroidSchedulers.mainThread()
}