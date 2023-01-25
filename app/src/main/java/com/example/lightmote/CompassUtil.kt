import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class CompassUtil(context: Context) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var currentDirection = 0.0f

    fun start() {
        val compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun getDirection(): Float {
        return currentDirection
    }

    override fun onSensorChanged(event: SensorEvent) {
        val values = event.values
        val azimuth = Math.atan2((-values[0]).toDouble(), values[1].toDouble())
        currentDirection = Math.toDegrees(azimuth).toFloat()
        //if (currentDirection < 0) {
            currentDirection += 360
        //}
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used in this example
    }
}
