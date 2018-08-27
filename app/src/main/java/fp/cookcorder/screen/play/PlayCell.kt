package fp.cookcorder.screen.play

import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.airbnb.epoxy.*
import fp.cookcorder.R
import fp.cookcorder.app.util.invisible
import fp.cookcorder.app.util.setTextHideIfNull
import fp.cookcorder.app.util.visible
import fp.cookcorder.screen.utils.calculateTimeDifference
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
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
    var pcOnPlayClicked: (() -> Observable<Pair<Int, Int>>)? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcOnEditClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcOnDeleteClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcTimer: Observable<Long>? = null

    @EpoxyAttribute
    var pcScheduleTime: Long = 0


    override fun bind(holder: Holder) {
        with(holder) {
            title.setTextHideIfNull(pcTitle)
            playButton.setOnClickListener { _ ->
                pcOnPlayClicked?.let { progressToMax ->
                    recordCompositeDisposable.clear()
                    seekBar.visible()
                    recordCompositeDisposable.addAll(progressToMax()
                            .subscribe(
                                    {
                                        seekBar.max = it.second
                                        seekBar.progress = it.first

                                    },
                                    { Timber.d(it) },
                                    {
                                        seekBar.visibility = View.INVISIBLE
                                        seekBar.progress = 0
                                    })
                    )
                }
            }
            editButton.setOnClickListener { pcOnEditClicked() }
            deleteButton.setOnClickListener { pcOnDeleteClicked() }
            subTitle.text = upperContainer.context.getString(R.string.at_time, pcTimePlayed)
            pcTimer?.let {
                compositeDisposable.addAll(it.subscribe(
                        {
                            details.text = makeHourString(pcScheduleTime)
                        },
                        {
                            Timber.d(it)
                        }
                ))
            }
        }
    }


    override fun bind(holder: Holder, previouslyBoundModel: EpoxyModel<*>) {
        super.bind(holder, previouslyBoundModel)
        holder.compositeDisposable.clear()
        bind(holder)
    }

    private fun makeHourString(timeToCompare: Long): String {
        with(calculateTimeDifference(timeToCompare)) {
            val hours = if (first != 0L) first.toString() + ":" else ""
            val minutes = String.format("%02d", second) + ":"
            val seconds = String.format("%02d", third)
            return "in $hours$minutes$seconds"
        }
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        holder.compositeDisposable.clear()
        holder.recordCompositeDisposable.clear()
        holder.seekBar.invisible()
    }

    class Holder : EpoxyHolder() {

        lateinit var upperContainer: View
        lateinit var title: TextView
        lateinit var subTitle: TextView
        lateinit var details: TextView
        lateinit var playButton: ImageButton
        lateinit var editButton: ImageButton
        lateinit var deleteButton: ImageButton
        lateinit var seekBar: SeekBar
        val compositeDisposable = CompositeDisposable()
        val recordCompositeDisposable = CompositeDisposable()

        override fun bindView(itemView: View) {
            upperContainer = itemView.findViewById<View>(R.id.constraintLayout)
            title = itemView.findViewById(R.id.itemTaskTitle)
            subTitle = itemView.findViewById(R.id.itemTaskTVTime)
            details = itemView.findViewById(R.id.itemTaskTvTimePlayed)
            playButton = itemView.findViewById(R.id.itemTaskPlayIB)
            editButton = itemView.findViewById(R.id.itemTaskIBEdit)
            deleteButton = itemView.findViewById(R.id.itemTaskDeleteIB)
            seekBar = itemView.findViewById(R.id.itemTaskSeekBar)
        }
    }
}