package fp.cookcorder.screen.play

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.editdialog.EditDialog
import fp.cookcorder.screen.record.RecordFragment
import fp.cookcorder.screen.utils.observe
import fp.cookcorder.screen.utils.px
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

    lateinit var layoutManager: RecyclerView.LayoutManager

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
        addRecyclerLayoutPositionListener()
    }

    private fun addRecyclerLayoutPositionListener() {
        parentFragment?.let {
            (it as RecordFragment)
                    .addSlidingPanelListener(object : SlidingUpPanelLayout.PanelSlideListener {
                        override fun onPanelSlide(panel: View?, slideOffset: Float) {
                            playFragmentTVNoTasks.translationY = ((1 - slideOffset) * -210).px

                            for (i in 0 until layoutManager.childCount) {
                                val card = layoutManager.getChildAt(i) as CardView
                                card.cardElevation = slideOffset * 5
                            }
                        }

                        override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
                            (playFragRV.layoutManager as ScrollEnabledLinearLayoutManager)
                                    .apply {
                                        scrollingEnabled = newState != SlidingUpPanelLayout.PanelState.COLLAPSED
                                    }

                            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                playFragRV.scrollToPosition(0)
                                viewModel.isPanelPeeked = false
                            } else {
                                viewModel.isPanelPeeked = true
                            }
                        }
                    })
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser) {
            parentFragment?.let {
                if(it is RecordFragment) {
                    it.setSlidingPanelScrollViewListener(playFragRV)
                }
            }
        }
    }

    private fun setNoTaskText() {
        if (arguments!!.getBoolean(KEY_IS_CURRENT)) {
            playFragmentTVNoTasks.setText(R.string.you_don_t_have_any_scheduled_tasks)
        } else {
            playFragmentTVNoTasks.setText(R.string.you_don_t_haby_any_snoozed_tasks)
        }
    }

    private fun setupRecycler() {
        layoutManager = ScrollEnabledLinearLayoutManager(context!!)
        playFragRV.layoutManager = layoutManager
        playFragRV.adapter = viewModel.adapter
    }

    private fun observeShowEditTask() {
        observe(viewModel.editTaskCmd) {
            showEditDialog(it)
        }
    }

    private fun showEditDialog(taskId: Long) {
        EditDialog.newInstance(taskId).show(fragmentManager, "EditDialog")
    }
}

class ScrollEnabledLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

    var scrollingEnabled = false

    override fun canScrollVertically(): Boolean {
        return scrollingEnabled
    }
}