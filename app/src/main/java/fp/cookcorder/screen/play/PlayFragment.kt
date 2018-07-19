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
import kotlinx.android.synthetic.main.play_fragment.*
import javax.inject.Inject

class PlayFragment : DaggerFragment() {

    companion object {
        fun newInstance() = PlayFragment()
    }

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<PlayViewModel>

    @Inject
    lateinit var taskAdapter: TaskAdapter

    private lateinit var viewModel: PlayViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.play_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory).get(PlayViewModel::class.java)
        setupRecycler()
        observeTasks()
    }

    private fun setupRecycler() {
        playFragRV.layoutManager = LinearLayoutManager(context!!)
        playFragRV.adapter = taskAdapter
    }

    private fun observeTasks() {
        observe(viewModel.tasks) { taskAdapter.taskList = it }
    }
}