package fp.cookcorder.screen.record

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.view.*
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import org.jetbrains.anko.design.longSnackbar
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        viewModel.permissionGranted = isPermissionGranted()
        if (!viewModel.permissionGranted) requestPermission()
        observeLiveData()
    }

    private fun observeLiveData() {
        observe(viewModel.isRecording) {
            if (it) {

            } else {

            }
        }
        observe(viewModel.requestRecordingPermission) {
            requestPermission()
        }
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



