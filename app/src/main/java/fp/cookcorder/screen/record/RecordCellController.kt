package fp.cookcorder.screen.record


import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.app.util.minutestToMilliseconds
import fp.cookcorder.screen.utils.handleCancellableTouch
import javax.inject.Inject

class RecordCellController @Inject constructor() : TypedEpoxyController<List<Int>>() {

    lateinit var viewModel: RecordViewModel

    override fun buildModels(data: List<Int>) {
        data.forEach {
            recordCell {
                id(it)
                rcMinutesToTrigger(it.toString())
                rcCellTouchListener(handleCancellableTouch(
                        { viewModel.requestNewRecord() },
                        { viewModel.finishRecording(it.minutestToMilliseconds()) },
                        { viewModel.cancelRecording() }
                ))
            }
        }
    }
}