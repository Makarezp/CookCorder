package fp.cookcorder.screen.record


import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.app.util.minutestToMilliseconds
import javax.inject.Inject

class RecordCellController @Inject constructor() : TypedEpoxyController<List<Int>>() {

    lateinit var viewModel: RecordViewModel

    override fun buildModels(data: List<Int>) {
        data.forEachIndexed { index, it ->
            recordCell {
                id(it)
                rcMinutesToTrigger(it.toString())
                rcCellTouchListener { v ->
                    val parent =v.parent as View
                    viewModel.requestNewRecord(it.minutestToMilliseconds(), parent.left, parent.top)}
            }
        }
    }
}