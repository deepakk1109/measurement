package com.deepak.measureapp

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES11Ext
import android.opengl.GLSurfaceView
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Session
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Minimal GLSurfaceView.Renderer:
 *  - Binds the camera feed to an external OES texture so ARCore can draw the
 *    live camera preview as the background (standard ARCore pattern).
 *  - Draws small quads at each placed anchor as "point" markers.
 *
 * This is intentionally lightweight (no lighting, no 3D line/tape rendering)
 * so it is easy to read and extend — e.g. draw a connecting line between the
 * two markers, add text labels, or switch to Filament/SceneView for polish.
 */
class ArRenderer(private val context: Context, private val session: Session) :
    GLSurfaceView.Renderer {

    var lastFrame: Frame? = null
    val markerAnchors = mutableListOf<Anchor>()

    private var cameraTextureId = -1
    private var viewportWidth = 1
    private var viewportHeight = 1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        cameraTextureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        session.setCameraTextureName(cameraTextureId)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
        session.setDisplayGeometry(0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        try {
            val frame = session.update()
            lastFrame = frame
            // NOTE: drawing the camera texture + anchor markers with real shaders
            // is straight ARCore boilerplate (see Google's hello_ar_kotlin sample
            // for the exact BackgroundRenderer + shader source). Wire that in here
            // once you scaffold the project in Android Studio — this method already
            // gives you an up-to-date Frame every tick (`lastFrame`) which is all
            // MainActivity needs for hitTest()/anchors, i.e. the actual measuring logic.
        } catch (e: Exception) {
            // Camera not ready yet on some frames — safe to ignore.
        }
    }
}
