package fp.cookcorder.screen.play

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import fp.cookcorder.R
import fp.cookcorder.app.util.onClick
import fp.cookcorder.model.Task
import fp.cookcorder.screen.utils.elevateOnTouch
import javax.inject.Inject

class TaskAdapter @Inject constructor(
        private val taskClickListener: TaskClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    interface TaskClickListener {
        fun onPlay(task: Task)
        fun onDelete(task: Task)
    }

    var taskList: List<Task> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val upperContainer: View by lazy { view.findViewById<View>(R.id.constraintLayout) }
        val text: TextView by lazy { view.findViewById<TextView>(R.id.itemTaskTV) }
        val deleteIV: ImageView by lazy { view.findViewById<ImageView>(R.id.deleteIV) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            upperContainer.setOnTouchListener(
                    elevateOnTouch  {
                        taskClickListener.onPlay(taskList[position])
                    })
            deleteIV.setOnTouchListener( elevateOnTouch {
                taskClickListener.onDelete(taskList[position])
            })
            text.text = taskList[position].name
        }
    }
}