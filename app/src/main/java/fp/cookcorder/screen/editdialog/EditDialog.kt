package fp.cookcorder.screen.editdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerAppCompatDialogFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import javax.inject.Inject

class EditDialog : DaggerAppCompatDialogFragment() {

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<EditDialogViewModel>

    private lateinit var viewModel: EditDialogViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_dialog, container, false)
    }
}