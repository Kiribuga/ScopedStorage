package com.example.scopedstorage.ui.listvideo

import android.app.Activity
import android.app.RemoteAction
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.scopedstorage.R
import com.example.scopedstorage.data.AdapterVideo
import com.example.scopedstorage.data.DetailVideo
import com.example.scopedstorage.databinding.ListVideoFragmentBinding
import com.example.scopedstorage.ui.dialogs.AddFragmentDialog
import com.example.scopedstorage.ui.dialogs.SelectDirDialog
import com.example.scopedstorage.utill.autoCleared
import com.example.scopedstorage.utill.haveQ

class FragmentListVideo : Fragment(R.layout.list_video_fragment), DetailVideo {

    private val vBinding: ListVideoFragmentBinding by viewBinding()
    private var adapterVideo: AdapterVideo by autoCleared()
    private val viewModel: ViewModelVideoList by viewModels()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var recoverableActionLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var createVideoLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPermissionResultListener()
        initRecoverableActionListener()
        initCrateVideoLauncher()
        if (hasPermission().not()) {
            requestPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        observerVM()
        vBinding.downloadButton.setOnClickListener {
            AddFragmentDialog(this).show(parentFragmentManager, "result")
        }
        vBinding.dloadWithDirButton.setOnClickListener {
            createVideoLauncher.launch("title")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionState(hasPermission())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observerVM() {
        viewModel.allVideoLD.observe(viewLifecycleOwner) {
            adapterVideo.updateList(it)
        }
        viewModel.loadLD.observe(viewLifecycleOwner, ::load)
        viewModel.recoverableActionLD.observe(viewLifecycleOwner, ::handlerRecoverableAction)
        viewModel.downloadVideoLD.observe(viewLifecycleOwner) {

        }
    }

    private fun load(result: Boolean) {
        vBinding.loadPB.isVisible = result
        vBinding.listVideoRV.isEnabled = !result
        vBinding.downloadButton.isEnabled = !result
        vBinding.dloadWithDirButton.isEnabled = !result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlerRecoverableAction(action: RemoteAction) {
        val request = IntentSenderRequest.Builder(action.actionIntent.intentSender)
            .build()
        recoverableActionLauncher.launch(request)
    }

    private fun initRecoverableActionListener() {
        recoverableActionLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { activityResult ->
            val isConfirmed = activityResult.resultCode == Activity.RESULT_OK
            if (isConfirmed) {
                viewModel.confirmDelete()
            } else {
                viewModel.declineDelete()
            }
        }
    }

    private fun initList() {
        adapterVideo = AdapterVideo { video ->
            viewModel.deleteVideo(video.id)
        }
        with(vBinding.listVideoRV) {
            adapter = adapterVideo
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun detailVideo(title: String, uri: String) {
        viewModel.saveVideo(title, uri)
    }

    private fun hasPermission(): Boolean {
        return PERMISSIONS.all {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initPermissionResultListener() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionToGrantedMap: Map<String, Boolean> ->
            if (permissionToGrantedMap.values.all { it }) {
                viewModel.permissionGranted()
            } else {
                viewModel.permissionDenied()
            }
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(PERMISSIONS.toTypedArray())
    }

    private fun initCrateVideoLauncher() {
        createVideoLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument()
        ) { uri ->
            viewModel.saveUri(uri)
            SelectDirDialog(this).show(parentFragmentManager, "result")
        }
    }

    override fun detailVideoWithDir(url: String) {
        viewModel.saveDownloadVideo(url)
    }

    companion object {
        private val PERMISSIONS = listOfNotNull(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                .takeIf { haveQ().not() }
        )
    }
}