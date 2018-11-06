package fp.cookcorder.screen.play

import android.arch.lifecycle.MutableLiveData
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import fp.cookcorder.R
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.app.util.invisible
import fp.cookcorder.app.util.setTextHideIfNull
import fp.cookcorder.app.util.visible
import fp.cookcorder.domain.managetaskusecase.ManageTaskUseCase
import fp.cookcorder.domain.play.PlayUseCase
import fp.cookcorder.model.Task
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.screen.utils.calculateTimeDifference
import fp.cookcorder.screen.utils.getDateTimeFromEpoch
import fp.cookcorder.screen.utils.getTimeFromEpoch
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.properties.Delegates


class PlayViewModel @Inject constructor(
        private val playUseCase: PlayUseCase,
        private val manageTaskUseCase: ManageTaskUseCase,
        private val playAdapter: PlayAdapter,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean
) : BaseViewModel() {

    val adapter = playAdapter
    val showNoTasks = MutableLiveData<Boolean>()
    val editTaskCmd = SingleLiveEvent<Long>()

    var isVisible: Boolean by Delegates.observable(false) {
        property, oldValue, newValue ->
        tasks = tasks
    }
    private var tasks: List<Task> by Delegates.observable(emptyList()) { property, oldValue, newValue ->
        if (isVisible) {
            playAdapter.tasks = newValue
        }
    }


    @Inject
    fun init() {
        playAdapter.viewModel = this
        val taskObs = if (isCurrent) manageTaskUseCase.getCurrentTasks() else manageTaskUseCase.getPastTasks()
        exe(taskObs) {
            showNoTasks.value = it.isEmpty()
            tasks = it.sortedBy { it.scheduleTime }
        }
    }

    fun play(task: Task): Observable<Pair<Int, Int>> {
        return playUseCase.playTask(task)
    }

    fun editTask(taskId: Long) {
        editTaskCmd.value = taskId
    }

    fun delete(task: Task) {
        exe(manageTaskUseCase.deleteTask(task)) {
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
            upperContainer = itemView.findViewById<View>(R.id.constraintLayout)
            title = itemView.findViewById(R.id.itemTaskTitle)
            subTitle = itemView.findViewById(R.id.itemTaskTVTime)
            details = itemView.findViewById(R.id.itemTaskTvTimePlayed)
            playButton = itemView.findViewById(R.id.itemTaskPlayIB)
            editButton = itemView.findViewById(R.id.itemTaskIBEdit)
            deleteButton = itemView.findViewById(R.id.itemTaskDeleteIB)
            seekBar = itemView.findViewById(R.id.itemTaskSeekBar)
        }
        return holder
    }

    override fun getItemCount(): Int = differ.currentList.size


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = differ.currentList[position]
        with(holder) {
            title.setTextHideIfNull(task.title)
            playButton.setOnClickListener { _ ->
                viewModel.play(task)
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui()).let { progressToMax ->
                            recordCompositeDisposable.clear()
                            seekBar.visible()
                            recordCompositeDisposable.addAll(progressToMax
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
            editButton.setOnClickListener { viewModel.editTask(task.id) }
            deleteButton.setOnClickListener { viewModel.delete(task) }
            subTitle.text = upperContainer.context.getString(R.string.at_time,
                    if (isCurrent) getTimeFromEpoch(task.scheduleTime) else getDateTimeFromEpoch(task.scheduleTime))
            if (isCurrent) {
                compositeDisposable.addAll(timer.subscribe(
                        {
                            details.text = makeHourString(task.scheduleTime)
                        },
                        {
                            Timber.d(it)
                        }
                ))
            }
        }
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        super.onViewRecycled(holder)
        holder.compositeDisposable.clear()
        holder.recordCompositeDisposable.clear()
        holder.seekBar.invisible()
    }

    private fun makeHourString(timeToCompare: Long): String {
        with(calculateTimeDifference(timeToCompare)) {
            val hours = if (first != 0L) first.toString() + ":" else ""
            val minutes = String.format("%02d", second) + ":"
            val seconds = String.format("%02d", third)
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
}
