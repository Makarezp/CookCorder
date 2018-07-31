package fp.cookcorder.screen.record


import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class RecordCellController @Inject constructor() : TypedEpoxyController<List<Int>>() {

    lateinit var viewModel: RecordViewModel

    override fun buildModels(data: List<Int>) {
        data.forEachIndexed { index, it ->
            val data1 = it
            recordCell {
                id(it)
                rcMinutesToTrigger(it.toString())
                rcSuccess(viewModel.successObservable.filter {
                    it == data1
                }.map { Any() })
                rcCellTouchListener { v ->
                    val parent =v.parent as View
                    viewModel.requestNewRecord(it, parent.left, parent.top)}
            }
        }
    }
}