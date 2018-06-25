package fp.cookcorder.screen.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import fp.cookcorder.R
import fp.cookcorder.app.util.onClick
import fp.cookcorder.model.Task
import javax.inject.Inject

class TaskAdapter @Inject constructor(
        private val taskClickListener: TaskClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    interface TaskClickListener {
        fun onTaskClicked(task: Task)
    }

    var taskList: List<Task> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView by lazy { view.findViewById<TextView>(R.id.itemTaskTV) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = taskList.size

    override fun onBindViewHolder(holder: TaskAdapter.ViewHolder, position: Int) {
        with(holder) {
            text.text = taskList[position].name
            text.onClick { taskClickListener.onTaskClicked(taskList[position]) }
        }
    }
}