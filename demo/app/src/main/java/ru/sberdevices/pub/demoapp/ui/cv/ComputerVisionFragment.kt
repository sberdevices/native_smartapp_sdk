@file:Suppress("ForbidDefaultCoroutineDispatchers")
package ru.sberdevices.pub.demoapp.ui.cv

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.cv.detection.entity.gesture.Gesture
import ru.sberdevices.cv.detection.entity.humans.Humans
import ru.sberdevices.pub.demoapp.ui.cv.entity.Control
import ru.sberdevices.pub.demoapp.ui.cv.entity.DetectionEvent
import ru.sberdevices.pub.demoapp.ui.cv.entity.GestureDetectionEvent
import ru.sberdevices.pub.demoapp.ui.cv.entity.HumansDetectionEvent
import ru.sberdevices.services.pub.demoapp.databinding.FragmentComputerVisionBinding

private const val GESTURE_SHOWTIME_MS = 1500L
private const val PERMISSION_REQUEST_CODE = 1

class ComputerVisionFragment : Fragment() {
    private lateinit var binding: FragmentComputerVisionBinding
    private val logger by Logger.lazy(javaClass.simpleName)

    @Volatile
    private var mirrorStateJob: Job? = null

    @Volatile
    private var detectionsJob: Job? = null

    private val viewModel: ComputerVisionViewModel by viewModel()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSION_REQUEST_CODE) {
            logger.error { "Requested permissions with wrong requestCode" }
        }

        logger.debug { "permissions: ${permissions.contentToString()}, grantResults: ${grantResults.contentToString()}" }

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            viewModel.permissionsGranted()
            binding.detectionView.cameraPermissionGranted = true
        } else {
            val notGrantedPermissions = permissions
                .zip(grantResults.toList())
                .filter { (_, result) -> result != PackageManager.PERMISSION_GRANTED }
                .map { (permission, _) -> permission }
                .toTypedArray()
            requestPermissions(
                notGrantedPermissions,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermissions(
            arrayOf(
                "android.permission.CAMERA",
                "ru.sberdevices.permission.COMPUTER_VISION_SENSITIVE"
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComputerVisionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        mirrorStateJob = viewModel.mirrorState
            .onEach(::renderMirrorState)
            .launchIn(lifecycleScope)
        detectionsJob = viewModel.detections
            .onEach(::onEvent)
            .launchIn(lifecycleScope + Dispatchers.IO)
        viewModel.resumed()
    }

    private fun onEvent(event: DetectionEvent) {
        when (event) {
            is GestureDetectionEvent -> renderGesture(event.gesture)
            is HumansDetectionEvent -> renderHumans(event.humans)
        }
    }

    private fun renderMirrorState(detected: Boolean?) {
        binding.detectionView.drawMirror(detected)
    }

    private fun renderGesture(gesture: Gesture) {
        binding.detectionView.drawGesture(gesture)
        binding.detectionView.postDelayed({ binding.detectionView.clearGesture() }, GESTURE_SHOWTIME_MS)
    }

    private fun renderHumans(humans: Humans) {
        binding.detectionView.drawHumans(humans)
    }

    private fun setListeners() {
        binding.switchMirror.setOnCheckedChangeListener { _, isChecked ->
            viewModel.mirrorSwitched(isChecked)
        }
        binding.switchFaceBoundingBox.setControlListener(Control.FACE_BOUNDING_BOX)
        binding.switchFaceLandmarks.setControlListener(Control.FACE_LANDMARKS)
        binding.switchBodyBoundingBox.setControlListener(Control.BODY_BOUNDING_BOX)
        binding.switchBodyLandmarksSberdevices.setControlListener(Control.BODY_LANDMARKS_HOMA_NET)
        binding.switchBodyLandmarksVisionlabs.setControlListener(Control.BODY_LANDMARKS_SENTAL_NET)
        binding.switchSegmentation.setControlListener(Control.BODY_SEGMENTATION)
        binding.switchGestures.setOnCheckedChangeListener { _, isChecked ->
            viewModel.gesturesSwitched(isChecked)
        }
    }

    private fun Switch.setControlListener(control: Control) {
        this@setControlListener.setOnCheckedChangeListener { buttonView, isChecked ->
            this@setControlListener.isChecked = isChecked
            logger.verbose { "Switch ${buttonView.text} is checked: $isChecked" }
            viewModel.humansAspectSwitched(isChecked, control)
            binding.detectionView.clearCanvas()
        }
    }

    override fun onPause() {
        viewModel.paused()
        mirrorStateJob?.cancel()
        detectionsJob?.cancel()
        super.onPause()
    }

    companion object {
        fun newInstance() = ComputerVisionFragment()
    }
}
