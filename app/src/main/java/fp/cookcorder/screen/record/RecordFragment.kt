package fp.cookcorder.screen.record

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Button
import com.airbnb.epoxy.EpoxyViewHolder
import com.airbnb.lottie.LottieAnimationView
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import fp.cookcorder.screen.utils.circularHide
import fp.cookcorder.screen.utils.circularReval
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.record_dialog.*
import org.jetbrains.anko.design.longSnackbar
import timber.log.Timber
import javax.inject.Inject

class RecordFragment : DaggerFragment() {

    companion object {
        const val TITLE = "Record"

        const val RECORDING_PERMISSION_REQUEST = 1

        fun newInstance() = RecordFragment()
    }


    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel

    private val dialogView: View by lazy { View.inflate(context, R.layout.record_dialog, null) }

    private val dialog: Dialog by lazy { Dialog(context, R.style.AlertDialogStyle) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        viewModel.permissionGranted = isPermissionGranted()
        if (!viewModel.permissionGranted) requestPermission()
        prepareDialog()
        observeLiveData()
        setupRecycler()
        setupNumberPicker()
    }

    private fun setupNumberPicker() {

    }

    private fun setupRecycler() {
        mainFragmentRV.layoutManager = GridLayoutManager(context, 3)
        mainFragmentRV.adapter = viewModel.adapter
    }

    private fun observeLiveData() {
        observe(viewModel.isRecording) {
            if (it) {
                showRecordDialog(viewModel.recordViewPosition!!)
            } else {
                circularHide(dialogView)
                dialog.dismiss()
            }
        }
        observe(viewModel.requestRecordingPermission) {
            requestPermission()
        }
    }

    private fun prepareDialog() {
        dialog.setContentView(dialogView)
        dialogView.findViewById<Button>(R.id.button).setOnClickListener {
            viewModel.finishRecording()
        }
        dialogView.findViewById<Button>(R.id.button2).setOnClickListener {
            viewModel.cancelRecording()
        }
        dialog.setOnKeyListener(DialogInterface.OnKeyListener { dialogInterface, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK) {

                viewModel.cancelRecording()
                return@OnKeyListener true
            }
            false
        })

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showRecordDialog(startRevalFrom: Pair<Int, Int>) {
        dialog.setOnShowListener { circularReval(dialogView, startRevalFrom) }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog.dismiss()
    }

    private fun requestPermission() {

        fun request() = ActivityCompat
                .requestPermissions(activity!!, arrayOf(RECORD_AUDIO), RECORDING_PERMISSION_REQUEST)

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, RECORD_AUDIO))
            longSnackbar(view!!,
                    "We cannot continue without your permission ",
                    "Grant permission"
            ) { request() }
        else request()
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context!!, RECORD_AUDIO) == PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == RecordFragment.RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == Manifest.permission.RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        viewModel.permissionGranted =
                                grantResults[index] == PermissionChecker.PERMISSION_GRANTED
                    }
        }
    }
}



