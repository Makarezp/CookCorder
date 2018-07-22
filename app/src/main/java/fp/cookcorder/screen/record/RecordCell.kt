package fp.cookcorder.screen.record

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import fp.cookcorder.R

@EpoxyModelClass(layout = R.layout.item_record)
abstract class RecordCell : EpoxyModelWithHolder<RecordCell.Holder>() {

    @EpoxyAttribute
    var rcMinutesToTrigger = ""

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var rcCellTouchListener: View.OnTouchListener

    override fun bind(holder: Holder) {
        with(holder) {
            minutesText.text = rcMinutesToTrigger
            container.setOnTouchListener(rcCellTouchListener)
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var container: View
        lateinit var minutesText: TextView

        override fun bindView(itemView: View) {
            container = itemView
            minutesText = itemView.findViewById(R.id.itemRecordTV)
        }
    }
}



