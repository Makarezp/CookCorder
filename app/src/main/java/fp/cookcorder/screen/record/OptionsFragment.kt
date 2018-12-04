package fp.cookcorder.screen.record

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Rect
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
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.MainActivity
import kotlinx.android.synthetic.main.options_fragment.*
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import javax.inject.Inject

class OptionsFragment : DaggerFragment() {

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.options_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        setupEditText()
        activity?.let {
            if (it is MainActivity) it.onTouchListener = { clearFocusOnTouchOutside(it) }
        }
        setupSeekBar()

    }

    private fun setupEditText() {
        removeEditTextFocusOnDone()

        titleET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setTitle(s.toString())
            }
        })
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
                viewModel.repeats.value = value
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {
                repeatsTV.animate()
                        .setDuration(100L)
                        .alpha(0F)
                        .start()
            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {
                repeatsTV.animate()
                        .setDuration(100L)
                        .alpha(1F)
                        .start()
            }
        })
    }

}