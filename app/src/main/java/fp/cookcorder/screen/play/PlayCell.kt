package fp.cookcorder.screen.play

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import fp.cookcorder.R
import fp.cookcorder.app.util.setTextHideIfNull
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber


@EpoxyModelClass(layout = R.layout.item_task)
abstract class PlayCell : EpoxyModelWithHolder<PlayCell.Holder>() {


    @EpoxyAttribute
    var pcTitle: String? = ""

    @EpoxyAttribute
    var pcTimePlayed = ""

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
            title.setTextHideIfNull(pcTitle)
            upperContainer.setOnClickListener { pcOnPlayClicked() }
            deleteIV.setOnClickListener { pcOnDeleteClicked() }
            subTitle.text = upperContainer.context.getString(R.string.at_time, pcTimePlayed)
            timerDisposable = pcTimer?.subscribe(
                    {
                        details.text = makeHourString(pcScheduleTime)
                    },
                    {
                        Timber.d(it)
                    }
            )

        }
    }

    private fun makeHourString(timeToCompare: Long): String {
        with(calculateTimeDifference(timeToCompare)) {
            val hours = if(first != 0L) first.toString() + ":" else ""
            val minutes = String.format("%02d", second) + ":"
            val seconds = String.format("%02d", third)
            return "in $hours$minutes$seconds"
        }
    }

    private fun calculateTimeDifference(timeToCompare: Long): Triple<Long, Long, Long> {
        val time1 = Instant.ofEpochMilli(timeToCompare)
        val time2 = Instant.now()
        val hours = ChronoUnit.HOURS.between(time2, time1)
        val minutes = ChronoUnit.MINUTES.between(time2, time1) % 60
        val seconds = ChronoUnit.SECONDS.between(time2, time1) % 60
        return Triple(hours, minutes, seconds)
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
            title = itemView.findViewById(R.id.itemTaskTitle)
            subTitle = itemView.findViewById(R.id.itemTaskTVTime)
            details = itemView.findViewById(R.id.itemTaskTvTimePlayed)
            deleteIV = itemView.findViewById(R.id.deleteIV)
        }
    }
}