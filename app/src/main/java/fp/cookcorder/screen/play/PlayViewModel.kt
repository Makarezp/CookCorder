package fp.cookcorder.screen.play

import android.arch.lifecycle.MutableLiveData
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import fp.cookcorder.R
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.interactors.play.PlayerInteractor
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.utils.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.properties.Delegates


class PlayViewModel @Inject constructor(
        private val playerInteractor: PlayerInteractor,
        private val taskInteractor: TaskInteractor,
        private val playAdapter: PlayAdapter,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean
) : BaseViewModel() {

    val adapter = playAdapter
    val showNoTasks = MutableLiveData<Boolean>()
    val editTaskCmd = SingleLiveEvent<Long>()
    var isPanelPeeked: Boolean by Delegates.observable(false) { _, _, newValue ->
        adapter.isPanelPeeked = newValue
    }

    @Inject
    fun init() {
        playAdapter.viewModel = this
        val taskObs = if (isCurrent) taskInteractor.getCurrentTasks() else taskInteractor.getPastTasks()
        exe(taskObs) {
            showNoTasks.value = it.isEmpty()
            playAdapter.tasks = it.sortedBy { it.scheduleTime }.let { if (isCurrent) it else it.reversed() }
        }
    }

    fun play(task: Task): Observable<Pair<Int, Int>> {
        return playerInteractor.playTask(task)
    }

    fun stopPlaying(task: Task, onSuccess: () -> Unit) {
        exe(playerInteractor.stopPlayingTask(task)) { onSuccess() }
    }

    fun editTask(taskId: Long) {
        editTaskCmd.value = taskId
    }

    fun delete(task: Task) {
        exe(taskInteractor.deleteTask(task)) {
            Timber.d("Task with id ${task.name} has been deleted")
        }
    }
}

class PlayAdapter @Inject constructor(
        private val schedulerFactory: SchedulerFactory,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean
) : RecyclerView.Adapter<TaskViewHolder>() {
    lateinit var viewModel: PlayViewModel

    var tasks: List<Task> = emptyList()
        set(value) {
            field = value
            differ.submitList(value)
        }

    var isPanelPeeked = false

    private val timer = Observable.interval(100, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerFactory.single())
            .observeOn(schedulerFactory.ui())
            .publish()

    private val differ = AsyncListDiffer<Task>(this, DiffCallback)

    init {
        differ.submitList(emptyList())
        timer.connect()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_task, parent, false)

        val holder = TaskViewHolder(view)
        with(holder) {

            cardView = itemView.findViewById(R.id.cardView)
            title = itemView.findViewById(R.id.itemTaskTitle)
            subTitle = itemView.findViewById(R.id.itemTaskTVTime)
            details = itemView.findViewById(R.id.itemTaskTvTimePlayed)
            playButton = itemView.findViewById(R.id.itemTaskPlayIB)
            moreButton = itemView.findViewById(R.id.itemTaskMoreIB)
            seekBar = itemView.findViewById(R.id.itemTaskSeekBar)
            progressView = itemView.findViewById(R.id.taskProgress)
        }
        return holder
    }

    override fun getItemCount(): Int = differ.currentList.size


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = differ.currentList[position]
        var isPlaying = false
        with(holder) {
            if (isPanelPeeked) cardView.cardElevation = 5f
            if (isCurrent) progressView.visible()
            playButton.setImageResource(R.drawable.ic_play)
            title.setTextInvisibleIfEmptyOrNull(task.title)
            playButton.setOnClickListener { _ ->
                if (!isPlaying) {
                    viewModel.play(task)
                            .subscribeOn(schedulerFactory.io())
                            .observeOn(schedulerFactory.ui()).let { progressToMax ->
                                recordCompositeDisposable.clear()
                                seekBar.visible()
                                isPlaying = true
                                playButton.setImageResource(R.drawable.ic_stop)
                                recordCompositeDisposable.addAll(progressToMax
                                        .subscribe(
                                                {
                                                    seekBar.max = it.second
                                                    seekBar.progress = it.first

                                                },
                                                { Timber.d(it) },
                                                {
                                                    seekBar.progress = seekBar.max
                                                    seekBar.visibility = View.INVISIBLE
                                                    seekBar.progress = 0
                                                    isPlaying = false
                                                    playButton.setImageResource(R.drawable.ic_play)
                                                })
                                )
                            }
                } else {
                    viewModel.stopPlaying(task) {
                        seekBar.visibility = View.INVISIBLE
                        seekBar.progress = 0
                        isPlaying = false
                        playButton.setImageResource(R.drawable.ic_play)
                    }

                }
            }
            moreButton.setOnClickListener { v -> showPopUp(v, task) }
            subTitle.text = if (isCurrent) getTimeFromEpoch(task.scheduleTime)
            else getDateTimeFromEpoch(task.scheduleTime)
            if (isCurrent) {
                compositeDisposable.addAll(timer.subscribe(
                        {
                            val dayLiteral =   itemView.context.getString(
                                    if(task.scheduleTime.getTimeInstant().isToday())
                                        R.string.today else R.string.tomorrow
                            )
                            details.text = "$dayLiteral ${makeHourString(task.scheduleTime)}"
                        },
                        {
                            Timber.d(it)
                        }
                ))
            }
        }
    }

    private fun showPopUp(view: View, task: Task) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.task_menu)
        popupMenu.setOnMenuItemClickListener { menuItem -> onMoreItemClicked(menuItem.itemId, task) }
        popupMenu.show()
    }

    private fun onMoreItemClicked(itemId: Int, task: Task): Boolean {
        when (itemId) {
            R.id.editMenuItem -> viewModel.editTask(task.id)
            R.id.removeMenuItem -> viewModel.delete(task)
        }

        return true
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        super.onViewRecycled(holder)
        holder.compositeDisposable.clear()
        holder.recordCompositeDisposable.clear()
        holder.seekBar.invisible()
    }

    private fun makeHourString(timeToCompare: Long): String {
        with(calculateTimeDifference(timeToCompare)) {
            val hours = if (first != 0L) first.toString() + "h " else ""
            val minutes = if (second > 0) String.format("%2d", second) + " min " else ""
            val seconds = if (second < 5) String.format("%2d", third) + " sec" else ""
            return "in $hours$minutes$seconds"
        }
    }
}


object DiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var cardView: CardView
    lateinit var title: TextView
    lateinit var subTitle: TextView
    lateinit var details: TextView
    lateinit var playButton: ImageButton
    lateinit var moreButton: ImageButton
    lateinit var seekBar: SeekBar
    lateinit var progressView: View
    val compositeDisposable = CompositeDisposable()
    val recordCompositeDisposable = CompositeDisposable()
}
