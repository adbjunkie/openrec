package com.openrec.recorder.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.openrec.recorder.LauncherIcon
import com.openrec.recorder.LauncherIconManager
import com.openrec.recorder.Prefs
import com.openrec.recorder.RecordingState
import com.openrec.recorder.R
import com.openrec.recorder.data.RecordingsRepository
import com.openrec.recorder.databinding.ActivityMainBinding
import com.openrec.recorder.service.RecordingService
import com.openrec.recorder.util.BatteryOptimizationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: RecordingsAdapter
    private var isRecording = false

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != RecordingService.ACTION_STATE_CHANGED) return

            isRecording = intent.getBooleanExtra(RecordingService.EXTRA_IS_RECORDING, false)
            updateRecordingUi()

            intent.getStringExtra(RecordingService.EXTRA_ERROR)?.let { error ->
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
            }

            intent.getStringExtra(RecordingService.EXTRA_FILE_PATH)?.let {
                refreshRecordings()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allGranted = grants.values.all { it }
        if (allGranted) {
            maybePromptBatteryOptimization { startRecordingInternal() }
        } else {
            Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupControls()
        refreshRecordings()
        isRecording = RecordingState.isRecording || RecordingState.isServiceRunning(this)
        updateRecordingUi()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(RecordingService.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stateReceiver, filter)
        }
    }

    override fun onStop() {
        unregisterReceiver(stateReceiver)
        super.onStop()
    }

    private fun setupRecycler() {
        adapter = RecordingsAdapter(
            onShare = { recording -> shareRecording(recording.file) },
            onDelete = { recording -> confirmDelete(recording) },
        )
        binding.recordingsList.layoutManager = LinearLayoutManager(this)
        binding.recordingsList.adapter = adapter
    }

    private fun setupControls() {
        binding.recordButton.setOnClickListener {
            if (isRecording) {
                RecordingService.stop(this)
            } else {
                requestPermissionsAndRecord()
            }
        }

        binding.frontCameraSwitch.isChecked = Prefs.useFrontCamera(this)
        binding.frontCameraSwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setUseFrontCamera(this, checked)
        }

        binding.audioSwitch.isChecked = Prefs.recordAudio(this)
        binding.audioSwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setRecordAudio(this, checked)
        }

        binding.changeIconButton.setOnClickListener { showIconPicker() }

        binding.discreteNotificationSwitch.isChecked = Prefs.discreteNotification(this)
        binding.discreteNotificationSwitch.setOnCheckedChangeListener { _, checked ->
            Prefs.setDiscreteNotification(this, checked)
        }

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.donateButton.setOnClickListener { openDonatePage() }
    }

    private fun openDonatePage() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donate_url))))
    }

    private fun maybePromptBatteryOptimization(onContinue: () -> Unit) {
        if (!BatteryOptimizationHelper.shouldPrompt(this)) {
            onContinue()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.battery_opt_title)
            .setMessage(R.string.battery_opt_message)
            .setPositiveButton(R.string.battery_opt_allow) { _, _ ->
                BatteryOptimizationHelper.requestExemption(this)
                onContinue()
            }
            .setNegativeButton(R.string.battery_opt_later) { _, _ ->
                BatteryOptimizationHelper.markPromptDismissed(this)
                onContinue()
            }
            .show()
    }

    private fun showIconPicker() {
        val icons = LauncherIcon.entries.toTypedArray()
        val labels = icons.map { getString(it.labelRes) }.toTypedArray()
        val currentIndex = icons.indexOf(LauncherIconManager.current(this)).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(R.string.choose_app_icon)
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                val selected = icons[which]
                if (selected != LauncherIconManager.current(this)) {
                    LauncherIconManager.apply(this, selected)
                    Toast.makeText(this, R.string.icon_changed_message, Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun requestPermissionsAndRecord() {
        val permissions = buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isEmpty()) {
            maybePromptBatteryOptimization { startRecordingInternal() }
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startRecordingInternal() {
        if (isRecording) return
        RecordingService.start(this)
        isRecording = true
        updateRecordingUi()
    }

    private fun updateRecordingUi() {
        binding.recordButton.text = getString(
            if (isRecording) R.string.stop_recording else R.string.start_recording
        )
        binding.statusText.text = getString(
            if (isRecording) R.string.status_recording else R.string.status_idle
        )
        binding.statusIndicator.setBackgroundResource(
            if (isRecording) R.drawable.status_recording else R.drawable.status_idle
        )
        binding.frontCameraSwitch.isEnabled = !isRecording
        binding.audioSwitch.isEnabled = !isRecording
        binding.changeIconButton.isEnabled = !isRecording
        binding.discreteNotificationSwitch.isEnabled = !isRecording
    }

    private fun refreshRecordings() {
        val recordings = RecordingsRepository.listRecordings(this)
        adapter.submitList(recordings)
        binding.emptyState.visibility = if (recordings.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun shareRecording(file: java.io.File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_recording)))
    }

    private fun confirmDelete(recording: com.openrec.recorder.data.RecordingFile) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_recording_title)
            .setMessage(recording.name)
            .setPositiveButton(R.string.delete) { _, _ ->
                if (RecordingsRepository.delete(recording.file)) {
                    refreshRecordings()
                } else {
                    Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}