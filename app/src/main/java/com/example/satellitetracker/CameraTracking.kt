package com.example.satellitetracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.floor


class CameraTracking : ComponentActivity(), SensorEventListener {

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private lateinit var sensorManager: SensorManager
    private lateinit var vib: Vibrator
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val alpha = 0.3F
    private var isVibrationDone = false

    private var azimuth = 0F
    private var elevation = 0F
    private val viewAngle = 120

    private lateinit var horizontalMarker: ImageView
    private lateinit var verticalMarker: ImageView
    private lateinit var userAzimuthText: TextView
    private lateinit var userElevationText: TextView
    private lateinit var targetElevationText: TextView
    private lateinit var targetAzimuthText: TextView
    private lateinit var circleIndicator: ImageView
    private lateinit var buttonBack: Button

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_tracking)

        azimuth = intent.getFloatExtra("azimuth", 0F)
        elevation = intent.getFloatExtra("elevation", 0F)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        vib = getSystemService(VIBRATOR_SERVICE) as Vibrator

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

        horizontalMarker = findViewById(R.id.horizontalMarker)
        verticalMarker = findViewById(R.id.verticalMarker)
        buttonBack = findViewById(R.id.btn_back_preview)
        userAzimuthText = findViewById(R.id.user_azimuth_text)
        userElevationText = findViewById(R.id.user_elevation_text)
        targetAzimuthText = findViewById(R.id.target_azimuth_text)
        targetElevationText = findViewById(R.id.target_elevation_text)
        circleIndicator = findViewById(R.id.circleIndicator)

        targetAzimuthText.text = azimuth.toString()
        targetElevationText.text = elevation.toString()

        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder()
            .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val previewView = findViewById<PreviewView>(R.id.previewView)

        preview.setSurfaceProvider(previewView.surfaceProvider)

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nic nie rob, jak sie rozkalibruje to trudno. :(
    }

    override fun onResume() {
        super.onResume()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerReading[0] = event.values[0] * alpha + (1-alpha) * accelerometerReading[0]
            accelerometerReading[1] = event.values[1] * alpha + (1-alpha) * accelerometerReading[1]
            accelerometerReading[2] = event.values[2] * alpha + (1-alpha) * accelerometerReading[2]
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerReading[0] = event.values[0] * alpha + (1 - alpha) * magnetometerReading[0]
            magnetometerReading[1] = event.values[1] * alpha + (1 - alpha) * magnetometerReading[1]
            magnetometerReading[2] = event.values[2] * alpha + (1 - alpha) * magnetometerReading[2]
        }

        updateOrientationAngles()

        val azim = normalize(orientationAngles[0])
        val elev = normalize(-orientationAngles[1])

        userAzimuthText.text = floor(azim).toString()
        userElevationText.text = floor(elev).toString()

        val horizontal = calculateBias(azim, azimuth)
        val vertical = calculateBias(elev, elevation)

        horizontalMarker.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = horizontal
        }

        verticalMarker.updateLayoutParams<ConstraintLayout.LayoutParams> {
            verticalBias = vertical
        }

        indicator(vertical, horizontal)
    }

    private fun calculateBias(value: Float, azi: Float): Float {
        var ret: Float
        var distance = azi - value
        if (distance >= 180) distance -= 360
        else if(distance < -180) distance += 360

        ret = (distance + viewAngle / 2) / viewAngle

        if (ret >= 1) ret = 1F
        else if (ret <= 0) ret = 0F

        return ret
    }

    private fun indicator(vertical: Float, horizontal: Float) {
        if (vertical in 0.47..0.53 && horizontal in 0.47..0.53) {
            circleIndicator.setColorFilter(Color.GREEN)
            if (!isVibrationDone) {
                vib.vibrate(
                    VibrationEffect.createOneShot(300, 100)
                )
                isVibrationDone = true
            }
        }
        else {
            circleIndicator.setColorFilter(Color.RED)
            isVibrationDone = false
        }
    }

    private fun normalize(value: Float): Float {
        var degree = Math.toDegrees(value.toDouble()).toFloat()
        degree = (degree + 360) % 360

        return degree
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private fun updateOrientationAngles() {

        val localRotationMatrix = FloatArray(9)
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            localRotationMatrix,
            inclinationMatrix,
            accelerometerReading,
            magnetometerReading
        )

        SensorManager.remapCoordinateSystem(localRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix)
        // "rotationMatrix" now has up-to-date information according to remapped cord system.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        // "orientationAngles" now has up-to-date information.
    }
}