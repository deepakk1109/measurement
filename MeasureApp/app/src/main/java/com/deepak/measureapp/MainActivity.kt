package com.deepak.measureapp

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableException
import kotlin.math.sqrt

/**
 * Measure App - basic AR distance measuring tool (like iPhone's Measure app).
 *
 * Flow:
 *  1. Check/request camera permission + ARCore availability.
 *  2. Start an ARCore Session, feed it into a GLSurfaceView (camera preview + plane detection).
 *  3. On tap, do a hitTest() against a detected plane -> create an Anchor there.
 *  4. Keep last 2 anchors; compute the straight-line distance between them in meters.
 *  5. Show the result in a TextView overlay (converted to cm too).
 *
 * This is a minimal, working skeleton — enough to build, run, and extend
 * (multi-segment measuring, AR labels in 3D space, area/volume mode, etc).
 */
class MainActivity : AppCompatActivity() {

    private var arSession: Session? = null
    private lateinit var surfaceView: GLSurfaceView
    private lateinit var resultText: TextView
    private lateinit var renderer: ArRenderer

    private val anchors = mutableListOf<Anchor>()

    private val cameraPermissionRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        resultText = findViewById(R.id.resultText)

        if (hasCameraPermission()) {
            setupArSession()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode
            )
        }

        surfaceView.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                onTap(event.x, event.y)
            }
            true
        }
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode && hasCameraPermission()) {
            setupArSession()
        } else {
            Toast.makeText(this, "Camera permission தேவை AR measure பண்ண", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupArSession() {
        try {
            when (ArCoreApk.getInstance().requestInstall(this, true)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> return // will re-enter onResume
                ArCoreApk.InstallStatus.INSTALLED -> { /* continue */ }
            }
            arSession = Session(this).apply {
                val config = Config(this)
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.focusMode = Config.FocusMode.AUTO
                configure(config)
            }
            renderer = ArRenderer(this, arSession!!)
            surfaceView.preserveEGLContextOnPause = true
            surfaceView.setEGLContextClientVersion(2)
            surfaceView.setRenderer(renderer)
            surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        } catch (e: UnavailableException) {
            Toast.makeText(this, "ARCore install pandrathukku problem: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Session error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun onTap(x: Float, y: Float) {
        val frame = renderer.lastFrame ?: return
        val hits = frame.hitTest(x, y)
        val hit = hits.firstOrNull {
            val trackable = it.trackable
            trackable is Plane && trackable.isPoseInPolygon(it.hitPose)
        } ?: return

        val anchor = hit.createAnchor()
        anchors.add(anchor)
        renderer.markerAnchors.add(anchor)

        if (anchors.size >= 2) {
            val a = anchors[anchors.size - 2].pose
            val b = anchors[anchors.size - 1].pose
            val dx = a.tx() - b.tx()
            val dy = a.ty() - b.ty()
            val dz = a.tz() - b.tz()
            val distanceMeters = sqrt(dx * dx + dy * dy + dz * dz)
            resultText.text = "Distance: %.2f m  (%.1f cm)".format(distanceMeters, distanceMeters * 100)
        } else {
            resultText.text = "Point 1 vachaachu. Rendu vadhu point tap pannunga."
        }
    }

    /** Long-press or a menu action could call this to start a fresh measurement. */
    fun resetMeasurement() {
        anchors.forEach { it.detach() }
        anchors.clear()
        renderer.markerAnchors.clear()
        resultText.text = "Tap on a surface to start measuring"
    }

    override fun onResume() {
        super.onResume()
        if (arSession == null && hasCameraPermission()) setupArSession()
        try {
            arSession?.resume()
            surfaceView.onResume()
        } catch (e: Exception) {
            Toast.makeText(this, "AR resume failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        surfaceView.onPause()
        arSession?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSession?.close()
        arSession = null
    }
}
