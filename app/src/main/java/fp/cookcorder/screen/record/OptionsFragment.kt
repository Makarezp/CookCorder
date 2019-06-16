package fp.cookcorder.screen.record

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Rect
import android.icu.text.AlphabeticIndex.Record
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.intentmodel.EventObservable
import fp.cookcorder.intentmodel.StateSubscriber
import fp.cookcorder.intentmodel.record.RecordModelStore
import fp.cookcorder.intentmodel.record.RecordViewEvent
import fp.cookcorder.intentmodel.record.RecordViewEvent.RepeatsChanged
import fp.cookcorder.intentmodel.record.RecordViewEvent.TitleTextChanged
import fp.cookcorder.intentmodel.record.RecordViewProcessor
import fp.cookcorder.intentmodel.record.RecorderState
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.utils.observe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.options_fragment.*
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import javax.inject.Inject

class OptionsFragment : DaggerFragment(),
        EventObservable<RecordViewEvent>,
        StateSubscriber<RecorderState> {

    private val disposable = CompositeDisposable()

    @Inject
    lateinit var recordModelStore: RecordModelStore
    @Inject
    lateinit var recordViewEventProcessor: RecordViewProcessor

    private val repeatsRelay = PublishRelay.create<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.options_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        removeEditTextFocusOnDone()
        activity?.let {
            if (it is MainActivity) it.onTouchListener = { clearFocusOnTouchOutside(it) }
        }
        setupSeekBar()
    }

    override fun events(): Observable<RecordViewEvent> =
            Observable.merge(
                    titleET.textChanges().map { TitleTextChanged(it.toString()) },
                    repeatsRelay.map { RepeatsChanged(it) }
            )


    override fun Observable<RecorderState>.subscribeToState(): Disposable = subscribe {
        repeatsNumTV.text = it.repeats.toString()
    }

    private fun removeEditTextFocusOnDone() {
        titleET.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocusAndHideKeyboard(v)
                true
            } else false
        }
    }

    fun clearFocusOnTouchOutside(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = activity!!.currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    clearFocusAndHideKeyboard(v)
                }
            }
        }
    }

    private fun clearFocusAndHideKeyboard(v: View) {
        v.clearFocus()
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun setupSeekBar() {
        seekBar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                repeatsRelay.accept(value)
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                repeatsTV.animate()
                        .setDuration(100L)
                        .alpha(0F)
                        .start()
                repeatsNumTV.animate()
                        .setDuration(100L)
                        .alpha(0f)
                        .start()
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                repeatsTV.animate()
                        .setDuration(100L)
                        .alpha(1F)
                        .start()
                repeatsNumTV.animate()
                        .setDuration(100L)
                        .alpha(1f)
                        .start()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        disposable += recordModelStore.modelState().subscribeToState()
        disposable += events().subscribe(recordViewEventProcessor::process)
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }
}