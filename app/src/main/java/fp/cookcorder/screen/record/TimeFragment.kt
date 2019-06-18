package fp.cookcorder.screen.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.intentmodel.EventObservable
import fp.cookcorder.intentmodel.record.RecordViewEvent
import fp.cookcorder.intentmodel.record.RecordViewEvent.MinsToScheduleChanged
import fp.cookcorder.intentmodel.record.RecordViewProcessor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.time_fragment.*
import javax.inject.Inject

class TimeFragment : DaggerFragment(),
        EventObservable<RecordViewEvent> {

    @Inject
    lateinit var recordViewEventProcessor: RecordViewProcessor

    private val minsToSheduleRelay = PublishRelay.create<Int>()

    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.time_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        minutePicker.onValueChangedListener = { hours, minutes, _ ->
            minsToSheduleRelay.accept(hours * 60 + minutes)
        }
    }

    override fun events(): Observable<RecordViewEvent> =
            minsToSheduleRelay.map { MinsToScheduleChanged(it) }


    override fun onResume() {
        super.onResume()
        disposable += events().subscribe(recordViewEventProcessor::process)
        minsToSheduleRelay.accept(minutePicker.hours * 60 + minutePicker.minutes)
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }
}