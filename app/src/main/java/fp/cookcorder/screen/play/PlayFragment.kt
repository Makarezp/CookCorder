package fp.cookcorder.screen.play

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import fp.cookcorder.screen.editdialog.EditDialog
import kotlinx.android.synthetic.main.play_fragment.*
import javax.inject.Inject

class PlayFragment : DaggerFragment() {

    companion object {
        const val KEY_IS_CURRENT = "KEY_IS_CURRENT"

        fun newInstance(isCurrent: Boolean): PlayFragment {
            val fragment = PlayFragment()
            val bundle = Bundle()
            bundle.putBoolean(KEY_IS_CURRENT, isCurrent)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<PlayViewModel>

    private lateinit var viewModel: PlayViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.play_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory).get(PlayViewModel::class.java)
        setNoTaskText()
        observe(viewModel.showNoTasks) {
            playFragmentTVNoTasks.animate().alpha(if (it) 1F else 0F)
                    .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                    .start()
        }
        setupRecycler()
        observeShowEditTask()
    }

    private fun setNoTaskText() {
        if (arguments!!.getBoolean(KEY_IS_CURRENT)) {
            playFragmentTVNoTasks.setText(R.string.you_don_t_have_any_scheduled_tasks)
        } else {
            playFragmentTVNoTasks.setText(R.string.you_don_t_haby_any_snoozed_tasks)
        }
    }

    private fun setupRecycler() {
        playFragRV.layoutManager = LinearLayoutManager(context!!)
        playFragRV.adapter = viewModel.adapter
    }

    private fun observeShowEditTask() {
        observe(viewModel.editTaskCmd) {
            showEditDialog()
        }
    }

    private fun showEditDialog() {
        EditDialog().show(fragmentManager, "EditDialog")
    }
}