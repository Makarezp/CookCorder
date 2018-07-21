package fp.cookcorder.screen.play

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import fp.cookcorder.R

@EpoxyModelClass(layout = R.layout.item_task)
abstract class PlayCell : EpoxyModelWithHolder<PlayCell.Holder>() {


    @EpoxyAttribute
    var pcTitle = ""

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcOnPlayClicked: () -> Unit = {}

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var pcOnDeleteClicked: () -> Unit = {}


    override fun bind(holder: Holder) {
        with(holder) {
            title.text = pcTitle
            upperContainer.setOnClickListener { pcOnPlayClicked() }
            deleteIV.setOnClickListener { pcOnDeleteClicked() }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var upperContainer: View
        lateinit var title: TextView
        lateinit var deleteIV: ImageView

        override fun bindView(itemView: View) {
            upperContainer = itemView.findViewById<View>(R.id.constraintLayout)
            title = itemView.findViewById(R.id.itemTaskTV)
            deleteIV = itemView.findViewById(R.id.deleteIV)
        }
    }
}