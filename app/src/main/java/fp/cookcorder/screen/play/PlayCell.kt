package fp.cookcorder.screen.play

import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import fp.cookcorder.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber

@EpoxyModelClass(layout = R.layout.item_task)
abstract class PlayCell : EpoxyModelWithHolder<PlayCell.Holder>() {


    @EpoxyAttribute
    var pcTitle = ""

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcOnPlayClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcOnDeleteClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcTimer: Observable<Long>? = null

    @EpoxyAttribute
    var pcScheduleTime: Long = 0

    private var timerDisposable: Disposable? = null


    override fun bind(holder: Holder) {
        with(holder) {
            title.text = pcTitle
            upperContainer.setOnClickListener { pcOnPlayClicked() }
            deleteIV.setOnClickListener { pcOnDeleteClicked() }
            timerDisposable = pcTimer?.subscribe(
                    {
                        subTitle.text = (pcScheduleTime - System.currentTimeMillis()).toString()
                    },
                    {
                        Timber.d(it)
                    }
            )

        }
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        timerDisposable?.dispose()
    }

    class Holder : EpoxyHolder() {

        lateinit var upperContainer: View
        lateinit var title: TextView
        lateinit var subTitle: TextView
        lateinit var deleteIV: ImageView
        lateinit var chronoMeter: Chronometer

        override fun bindView(itemView: View) {
            upperContainer = itemView.findViewById<View>(R.id.constraintLayout)
            title = itemView.findViewById(R.id.itemTaskTV)
            subTitle = itemView.findViewById(R.id.itemTaskTVSubtitle)
            deleteIV = itemView.findViewById(R.id.deleteIV)
            chronoMeter = itemView.findViewById(R.id.itemTaskChrono)
        }
    }
}