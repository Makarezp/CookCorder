package fp.cookcorder.screen.record

import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class RecordCellController @Inject constructor(): TypedEpoxyController<List<Int>>() {

    lateinit var viewModel: RecordViewModel

    override fun buildModels(data: List<Int>) {
        data.forEach {
            recordCell {
                rcCellTouchListener { v, event ->

                }
            }
        }

    }
}