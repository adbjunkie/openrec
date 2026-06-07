package com.openrec.recorder.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executor

class CameraRecorder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val mainExecutor: Executor,
) {
    interface Listener {
        fun onRecordingStarted(outputFile: File)
        fun onRecordingStopped(outputFile: File)
        fun onError(message: String)
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var currentOutputFile: File? = null
    private var listener: Listener? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun bind(useFrontCamera: Boolean, recordAudio: Boolean, outputFile: File) {
        currentOutputFile = outputFile
        val cameraSelector = if (useFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()

                val capture = VideoCapture.withOutput(recorder)
                videoCapture = capture

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    capture
                )

                startRecording(outputFile, recordAudio)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera", e)
                listener?.onError(e.message ?: "Camera setup failed")
            }
        }, mainExecutor)
    }

    private fun startRecording(outputFile: File, recordAudio: Boolean) {
        val capture = videoCapture ?: return

        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        val pending = capture.output.prepareRecording(context, outputOptions)
        val recording = if (recordAudio) pending.withAudioEnabled() else pending

        activeRecording = recording.start(mainExecutor) { event ->
            when (event) {
                is androidx.camera.video.VideoRecordEvent.Start -> {
                    listener?.onRecordingStarted(outputFile)
                }
                is androidx.camera.video.VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        listener?.onError("Recording failed: ${event.error}")
                        if (outputFile.exists() && outputFile.length() == 0L) {
                            outputFile.delete()
                        }
                    } else {
                        listener?.onRecordingStopped(outputFile)
                    }
                    activeRecording = null
                }
            }
        }
    }

    fun stop() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun release() {
        stop()
        cameraProvider?.unbindAll()
        cameraProvider = null
        videoCapture = null
        currentOutputFile = null
    }

    companion object {
        private const val TAG = "CameraRecorder"
    }
}