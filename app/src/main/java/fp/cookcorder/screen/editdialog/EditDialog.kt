package fp.cookcorder.screen.editdialog

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerAppCompatDialogFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.utils.observe
import kotlinx.android.synthetic.main.edit_dialog.*
import javax.inject.Inject

class EditDialog : DaggerAppCompatDialogFragment() {


    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<EditDialogViewModel>

    private val viewModel: EditDialogViewModel by lazy {
        ViewModelProviders.of(this, vmFactory).get(EditDialogViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        observeLiveData()
        setupClickListeners()
        observeDimissDialog()
    }

    private fun observeDimissDialog() {
        observe(viewModel.dismissDialogCmd) {
            dismiss()
        }
    }

    private fun setupClickListeners() {
        editDialogTVOk.setOnClickListener {
            viewModel.editTaskTask(getMinutesToSchedule(), editDialogETTitle.text.toString())
        }
    }

    private fun observeLiveData() {
        observe(viewModel.title) {
            editDialogETTitle.setText(it)
        }
        observe(viewModel.hoursToMinutes) {
            editDialogPicker.hours = it.first
            editDialogPicker.minutes = it.second
        }
    }

    private fun getMinutesToSchedule(): Int {
        val hours = editDialogPicker.hours * 60
        val minutes = editDialogPicker.minutes
        return hours + minutes
    }

    companion object {

        const val KEY_TASK_ID = "KEY_TASK_ID"

        fun newInstance(taskId: Long): EditDialog {
            return EditDialog().apply {
                arguments = Bundle().apply {
                    putLong(KEY_TASK_ID, taskId)
                }
            }
        }
    }
}