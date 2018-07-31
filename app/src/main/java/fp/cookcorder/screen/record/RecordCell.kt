package fp.cookcorder.screen.record

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.lottie.LottieAnimationView
import fp.cookcorder.R
import fp.cookcorder.app.util.visible
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

@EpoxyModelClass(layout = R.layout.item_record)
abstract class RecordCell : EpoxyModelWithHolder<RecordCell.Holder>() {

    @EpoxyAttribute
    var rcMinutesToTrigger = ""

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var rcCellTouchListener: (View) -> Unit

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var rcSuccess: Observable<Any>

    private var disposable: Disposable? = null

    override fun bind(holder: Holder) {
        with(holder) {
            minutesText.text = rcMinutesToTrigger
            minutesText.setOnClickListener(rcCellTouchListener)
            disposable = rcSuccess
                    .doAfterNext {
                        lottie.visible()
                        lottie.playAnimation()
                    }
                    .delay(5, TimeUnit.SECONDS, Schedulers.io())
                    .subscribe(
                            {
                                lottie.pauseAnimation()
                                lottie.visibility = View.GONE
                            },
                            {
                                Timber.e(it)
                            }
                    )
        }
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        holder.lottie.visibility = View.GONE
        disposable?.dispose()
    }

    class Holder : EpoxyHolder() {

        lateinit var container: View
        lateinit var minutesText: TextView
        lateinit var lottie: LottieAnimationView

        override fun bindView(itemView: View) {
            container = itemView
            minutesText = itemView.findViewById(R.id.itemRecordTV)
            lottie = itemView.findViewById(R.id.success)
        }
    }
}



