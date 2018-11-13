package fp.cookcorder.screen.record

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import kotlinx.android.synthetic.main.time_fragment.*
import javax.inject.Inject

class TimeFragment : DaggerFragment() {

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.time_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        viewModel.currentMinutesToSchedule.value = minutePicker.hours * 60 + minutePicker.minutes
        minutePicker.onValueChangedListener = { hours, minutes, _ ->
            viewModel.currentMinutesToSchedule.value = hours * 60 + minutes
        }
    }

}