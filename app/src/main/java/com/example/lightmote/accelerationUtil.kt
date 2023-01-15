import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class AccelerationUtil(context: Context) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var acceleration = FloatArray(3)

    fun start() {
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun getAcceleration(): FloatArray {
        return acceleration
    }

    override fun onSensorChanged(event: SensorEvent) {
        acceleration = event.values.clone()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used in this example
    }
}
