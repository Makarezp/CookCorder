package fp.cookcorder.screen.play

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import fp.cookcorder.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
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
                        val minutesToSeconds = calculateTimeDifference(pcScheduleTime)
                        subTitle.text = minutesToSeconds.first.toString()
                        details.text = minutesToSeconds.second.toString()
                    },
                    {
                        Timber.d(it)
                    }
            )

        }
    }

    private fun calculateTimeDifference(timeToCompare: Long): Pair<Long, Long> {
        val time1 = Instant.ofEpochMilli(timeToCompare)
        val time2 = Instant.now()

        val minutes = ChronoUnit.MINUTES.between(time2, time1)
        val seconds = ChronoUnit.SECONDS.between(time2, time1) % 60
        return minutes to seconds
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        timerDisposable?.dispose()
    }

    class Holder : EpoxyHolder() {

        lateinit var upperContainer: View
        lateinit var title: TextView
        lateinit var subTitle: TextView
        lateinit var details: TextView
        lateinit var deleteIV: ImageView

        override fun bindView(itemView: View) {
            upperContainer = itemView.findViewById<View>(R.id.constraintLayout)
            title = itemView.findViewById(R.id.itemTaskTV)
            subTitle = itemView.findViewById(R.id.itemTaskTVSubtitle)
            details = itemView.findViewById(R.id.itemTaskTvDetails)
            deleteIV = itemView.findViewById(R.id.deleteIV)
        }
    }
}