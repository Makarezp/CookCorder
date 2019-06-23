package fp.cookcorder.screen.play

import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fp.cookcorder.R
import fp.cookcorder.intentmodel.StateSubscriber
import fp.cookcorder.intentmodel.play.PlayerModelStore
import fp.cookcorder.intentmodel.play.PlayerState
import fp.cookcorder.intentmodel.play.TaskState
import fp.cookcorder.intentmodel.play.TaskStatus.NotPlaying
import fp.cookcorder.intentmodel.play.TaskStatus.Playing
import fp.cookcorder.utils.invisible
import fp.cookcorder.utils.setTextInvisibleIfEmptyOrNull
import fp.cookcorder.utils.visible
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.android.synthetic.main.options_fragment.view.*
import javax.inject.Inject


class TaskAdapter constructor(val isCurrent: Boolean,
                              val playerModelStore: PlayerModelStore
) : Adapter<TaskViewHolderMVI>(), StateSubscriber<PlayerState> {

    val disposable = CompositeDisposable()

    var tasks: List<TaskState> = emptyList()
        set(value) {
            field = value
            differ.submitList(value)
        }

    private val differ = AsyncListDiffer<TaskState>(this, DiffTaskStateCallback)

    init {
        setHasStableIds(true)
        differ.submitList(emptyList())
    }


    override fun Observable<PlayerState>.subscribeToState(): Disposable = subscribe {
        tasks = if(isCurrent) it.currentTaskStates else it.pastTaskStates
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        disposable += playerModelStore.modelState().subscribeToState()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposable.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolderMVI {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_task, parent, false)
        return TaskViewHolderMVI(view)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: TaskViewHolderMVI, position: Int) {
        holder.bind(differ.currentList[position])
    }
}


class TaskViewHolderMVI(view: View) : ViewHolder(view) {

    fun bind(taskState: TaskState) {
        fun setNotPlaying() {
            itemView.itemTaskPlayIB.setImageResource(R.drawable.ic_play)
            itemView.seekBar.invisible()
            itemView.seekBar.progress = 0
        }

        fun setIsPlaying() {
            itemView.itemTaskPlayIB.setImageResource(R.drawable.ic_stop)
            itemView.seekBar.visible()
        }

        with(itemView) {
            if (taskState.isCurrent) taskProgress.visible()
            itemTaskTitle.setTextInvisibleIfEmptyOrNull(taskState.task.title)

            when (taskState.taskStatus) {
                is NotPlaying -> setNotPlaying()
                is Playing -> setIsPlaying()
            }
        }
    }
}

object DiffTaskStateCallback : DiffUtil.ItemCallback<TaskState>() {
    override fun areItemsTheSame(oldItem: TaskState, newItem: TaskState): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TaskState, newItem: TaskState): Boolean {
        return oldItem == newItem
    }
}