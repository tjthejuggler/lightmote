package com.example.lightmote

//add buffers after a vibration change
//make buffer shrink as there are more vibrations, and get longer when there are less vibrations
//we need to keep track of the last vibration and the number of vibrations in x seconds
//the buffer time is based on the number of vibrations so we can do clear clicks, but also sped up fast stuff


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*


class SoundMeter {
    private var ar: AudioRecord? = null
    private var minSize = 0
    fun start() {
        minSize = AudioRecord.getMinBufferSize(4000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        Log.d("minSize", minSize.toString());
        ar = AudioRecord(MediaRecorder.AudioSource.MIC, 4000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize)
        ar!!.startRecording()
    }

    fun stop() {
        if (ar != null) {
            ar!!.stop()
        }
    }

    val amplitude: Double
        get() {
            val buffer = ShortArray(minSize)
            ar!!.read(buffer, 0, minSize)
            var max = 0
            for (s in buffer) {
                if (Math.abs(s.toInt()) > max) {
                    max = Math.abs(s.toInt())
                }
            }
            return max.toDouble()
        }
}

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    var settingsButtons = arrayOf<Button>()
    var colorButtons = arrayOf<Button>()
    var micButtons = arrayOf<ImageButton>()
    var micButtonXs = arrayOf<ImageView>()

//237,35,85,172,19,81
    var ipAddresses = arrayOf(
            "192.168.43.85",
            "192.168.43.19",
            "192.168.43.81",
            "192.168.43.23",
            "192.168.43.240"
    )

    val colors: Array<ByteArray> = arrayOf(
            byteArrayOf(127, 0, 0),
            byteArrayOf(0, 127, 0),
            byteArrayOf(0, 0, 127),
            byteArrayOf(127, 127, 0),
            byteArrayOf(127, 0, 127),
            byteArrayOf(0, 127, 127),
            byteArrayOf(127, 127, 127),
            byteArrayOf(127, 0, 0),
            byteArrayOf(0, 127, 0),
            byteArrayOf(0, 0, 127),
            byteArrayOf(127, 127, 0),
            byteArrayOf(127, 0, 127),
            byteArrayOf(0, 127, 127),
            byteArrayOf(127, 127, 127),
            byteArrayOf(127, 0, 0),
            byteArrayOf(0, 127, 0),
            byteArrayOf(0, 0, 127),
            byteArrayOf(127, 127, 0),
            byteArrayOf(127, 0, 127),
            byteArrayOf(0, 127, 127),
            byteArrayOf(127, 127, 127)
    )

    var selectedIndex = arrayOf(0, 7, 14)

    var currentVibration = 1.0F;
    var micOn = arrayOf(true, true, true)
    var vibOn = arrayOf(true, true, true)

    fun sendColorChange(ip: String, color: ByteArray, buttonIndex: Int){
        val tvHello=findViewById(R.id.mytextView) as TextView;
        tvHello.text= buttonIndex.toString()
        val buffer = ByteArray(12)
        val buf: ByteBuffer =
            ByteBuffer.wrap(buffer)
        buf.put(0, 66)
        buf.putInt(1, 0)
        buf.put(5, 0)
        buf.putShort(6, 0)
        buf.put(8, 0x0a)
        buf.put(9, color[0])
        buf.put(10, color[1])
        buf.put(11, color[2])
        val address: InetAddress = InetAddress.getByName(ip)
        val packet = DatagramPacket(
                buffer, buffer.size, address, 41412
        )
        val datagramSocket = DatagramSocket()
        thread{datagramSocket.send(packet)}
    }

    fun getRgbFromHex(hex: String): ByteArray {
        var noalpha = hex.drop(2)
        var fullhex = "#$noalpha"
        val initColor = Color.parseColor(fullhex)
        val r = Color.red(initColor)/2
        val g = Color.green(initColor)/2
        val b = Color.blue(initColor)/2
        return byteArrayOf(r.toByte(), g.toByte(), b.toByte())
    }

    fun setSelectedIndex(buttonIndex: Int){
        when {
            buttonIndex < 7 -> selectedIndex[0] = buttonIndex
            buttonIndex in 7..13 -> selectedIndex[1] = buttonIndex
            buttonIndex in 14..20 -> selectedIndex[2] = buttonIndex
        }
        var ipIndex = 0
        showSelectedIndex()
        when {
            buttonIndex < 7 -> ipIndex = 0
            buttonIndex in 7..13 -> ipIndex = 1
            buttonIndex in 14..20 -> ipIndex = 2
        }
        sendColorChange(ipAddresses[ipIndex], colors[buttonIndex], buttonIndex)
    }

    fun showSelectedIndex(){
        val selectedSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90f, resources.displayMetrics)
        val unselectedSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics)
        for (i in 0..20) {
            if (selectedIndex[0] == i || selectedIndex[1] == i || selectedIndex[2] == i) {
                colorButtons[i].rotation = 45F
                colorButtons[i].layoutParams.height=selectedSize.toInt();
                colorButtons[i].layoutParams.width=selectedSize.toInt();
                colorButtons[i].bringToFront();

            }else{
                colorButtons[i].setText(" ")
                colorButtons[i].rotation = 0F
                colorButtons[i].layoutParams.height=unselectedSize.toInt();
                colorButtons[i].layoutParams.width=unselectedSize.toInt();
            }
        }
    }

    private val colorButtonListener: View.OnClickListener = View.OnClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int
        setSelectedIndex(buttonIndex)
    }

    private val settingsButtonListener: View.OnClickListener = View.OnClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int



    }

    private val colorButtonLongClickListener: View.OnLongClickListener = View.OnLongClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int
        var ipIndex: Int = 0
        when {
            buttonIndex < 7 -> ipIndex = 0
            buttonIndex in 7..13 -> ipIndex = 1
            buttonIndex in 14..20 -> ipIndex = 2
        }

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                    colors[buttonIndex] = getRgbFromHex(Integer.toHexString(selectedColor))
                    setButtonColor(buttonIndex)
                    Log.i("selectedColor", selectedColor.toString())
                }
                .setNegativeButton("ok") { dialog, which -> }
                .build()
                .show()
        true
    }

    private val micButtonListener: View.OnClickListener = View.OnClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int
        micOn[buttonIndex] = !micOn[buttonIndex]
        if (micOn[buttonIndex]) {
            micButtonXs[buttonIndex].visibility = View.GONE
        }else{
            micButtonXs[buttonIndex].visibility = View.VISIBLE
        }
    }
//when we click the seqBtn we want to make sure mic are off
    //then we want to make a toast that says you need something in the text field if there is nothing
    fun playMP3(context: Context) {
        var sequenceEditText = findViewById<View>(R.id.sequenceText) as EditText
        var sequenceEditTextInput = sequenceEditText.text.toString()
        var fileName = sequenceEditTextInput.split("\n")[0]
        val path: String = "/storage/emulated/0/" + fileName + ".mp3"
        val player = MediaPlayer()
        try {
            Log.d("seqBtn path", path);
            player.setDataSource(path)
            player.prepare()
            player.setOnPreparedListener(OnPreparedListener { mp -> mp.start() })
            player.prepareAsync()
            var music_is_playing = true
            player.setOnCompletionListener(OnCompletionListener {
                music_is_playing = false
            })
            while (music_is_playing){
                playColorSequence(this, player.getCurrentPosition())
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Exception) {
            println("Exception of type : $e")
            e.printStackTrace()
        }
    }

    fun playColorSequence(context: Context, currentTime: Int){
        var sequenceEditText = findViewById<View>(R.id.sequenceText) as EditText
        var sequenceEditTextInput = sequenceEditText.text.toString()
        for (line in sequenceEditTextInput.split("\n")){
            var this_timestamp = line.split('"')[0]
            //compare this timestamp to the current time that gets passed in

            //split on the : and get the color keys

            //convert the color keys to rgb or hex codes

            //send commands and change the angle of the button for the ball that corresponds
        }
    }

    private val sequenceButtonListener: View.OnClickListener = View.OnClickListener { v ->
        Log.d("seqBtn pressed", "seqBtn pressed");
        playMP3(this)
        //playColorSequence(this)
    }

    class CustomDialogClass(context: Context) : Dialog(context) {
        init {
            setCancelable(true)
        }
        private val yesButtonListener: View.OnClickListener = View.OnClickListener { v ->
            this.cancel()
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.mic_longhold_dialog_layout)
            val yesButton=findViewById(R.id.btn_yes) as Button;
            yesButton.setOnClickListener(yesButtonListener)
        }
    }

    private val micButtonLongClickListener: View.OnLongClickListener = View.OnLongClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int
        CustomDialogClass(this).show()
        Log.d("color_longhold", Build.PRODUCT);
        true
    }

    fun setButtonColor(buttonIndex: Int) {
        var redValue = colors[buttonIndex][0].toInt()*2
        var greenValue = colors[buttonIndex][1].toInt()*2
        var blueValue = colors[buttonIndex][2].toInt()*2
        colorButtons[buttonIndex].setBackgroundColor(Color.rgb(redValue, greenValue, blueValue))
        if (redValue+greenValue+blueValue > 600) {
            colorButtons[buttonIndex].setTextColor(Color.BLACK)
        }else{
            colorButtons[buttonIndex].setTextColor(Color.WHITE)
        }
    }

    private fun increaseSelectedIndexByOne(i: Int) {
        runOnUiThread {
            selectedIndex[i] = selectedIndex[i] + 1
            if (selectedIndex[i] - i * 7 > 6) {
                selectedIndex[i] = selectedIndex[i] - 7
            }
            setSelectedIndex(selectedIndex[i])
        }
    }

    //an array to hold all current_time logs
    var current_time_logs = ArrayList<String>()

    private fun log_current_time() {
        //val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        //unformated just number of milliseconds
        val current_time = System.currentTimeMillis()
        //add this time to current_time_logs
        current_time_logs.add(current_time.toString())
        //get all other items in array and get rid of any that start with a number that is more than 10 seconds ago


        //val currentDateandTime = sdf.format(Date())
        //Log.d("current time", current_time.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //a textview called vibration_tv

        Log.d("Build.PRODUCT", Build.PRODUCT);
        val sequenceButton = findViewById<Button>(R.id.seqBtn)
        settingsButtons = arrayOf(
                findViewById(R.id.settings_button1),
                findViewById(R.id.settings_button2),
                findViewById<Button>(R.id.settings_button3)
        )
        colorButtons = arrayOf(
                findViewById(R.id.btnA1),
                findViewById(R.id.btnA2),
                findViewById(R.id.btnA3),
                findViewById(R.id.btnA4),
                findViewById(R.id.btnA5),
                findViewById(R.id.btnA6),
                findViewById(R.id.btnA7),
                findViewById(R.id.btnB1),
                findViewById(R.id.btnB2),
                findViewById(R.id.btnB3),
                findViewById(R.id.btnB4),
                findViewById(R.id.btnB5),
                findViewById(R.id.btnB6),
                findViewById(R.id.btnB7),
                findViewById(R.id.btnC1),
                findViewById(R.id.btnC2),
                findViewById(R.id.btnC3),
                findViewById(R.id.btnC4),
                findViewById(R.id.btnC5),
                findViewById(R.id.btnC6),
                findViewById<Button>(R.id.btnC7)
        )
        micButtons = arrayOf(
            findViewById(R.id.micBtnA),
            findViewById(R.id.micBtnB),
            findViewById<ImageButton>(R.id.micBtnC)
        )
        for (i in 0..2) {
            micButtons[i].tag = i
            micButtons[i].setOnClickListener(micButtonListener)
            micButtons[i].setOnLongClickListener(micButtonLongClickListener)
        }
        micButtonXs = arrayOf(
            findViewById(R.id.micBtnAX),
            findViewById(R.id.micBtnBX),
            findViewById<ImageView>(R.id.micBtnCX)
        )
        for (i in 0..20) {
            colorButtons[i].setOnClickListener(colorButtonListener)
            colorButtons[i].setOnLongClickListener(colorButtonLongClickListener)
            colorButtons[i].tag = i
            setButtonColor(i)
        }
        for (i in 0..2) {
            settingsButtons[i].setOnClickListener(settingsButtonListener)
            settingsButtons[i].tag = i
            setButtonColor(i)
        }
        //TODO figure out why this and the other sequenceButton place is making it crash
        sequenceButton.setOnClickListener(sequenceButtonListener)
        showSelectedIndex()
        if (Build.PRODUCT != "sdk_gphone_x86_arm") {
            val mySoundMeter = SoundMeter()
            mySoundMeter.start()
                var indicesCanIncreaseFromMic = true
                var indicesCanIncreaseFromVib = true
                thread {
                    while (true) {
                        if (micOn[0] || micOn[1] || micOn[2]) {
                            try {
                                Thread.sleep(50)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            if (mySoundMeter != null) {
//                                val amplitude = mySoundMeter.amplitude
//                                Log.i("AMPLITUDE", amplitude.toString())
//                                if (amplitude > 800) {
//                                    if (indicesCanIncreaseFromMic) {
//                                        indicesCanIncreaseFromMic = false
//                                        Log.i("INCREASE", "increase")
//                                        for (i in 0..2) {
//                                            if (micOn[i]) {
//                                                increaseSelectedIndexByOne(i)
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    indicesCanIncreaseFromMic = true
//                                }

                                val vibration_rate = mySoundMeter.amplitude
                                Log.i("vibration_rate", vibration_rate.toString())
                                if (currentVibration > 1.11) {
                                    if (indicesCanIncreaseFromVib) {
                                        indicesCanIncreaseFromVib = false
                                        Log.i("INCREASE", "vib increase")
                                        for (i in 0..2) {
                                            if (vibOn[i]) {
                                                increaseSelectedIndexByOne(i)
                                                log_current_time()
                                            }
                                        }
                                    }
                                } else {
                                    indicesCanIncreaseFromVib = true
                                }
                            }
                        }
                    }
                }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]


            // Use a threshold to determine if the device is being shaken
            var acceleration = ((x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
            //only use 2 decimal places and make everything from .98 to 1.02 be equal to 1.00
            if (acceleration >= 0.99 && acceleration <= 1.001) {
                //set acceleration to 1
                acceleration = 1.0F

            }
            //val accelerationRounded = String.format("%.2f", acceleration).toDouble()
            val accelerationFormatted = String.format("%.3f", acceleration)

            val textView: TextView = findViewById(R.id.vibration_tv) as TextView
            //set textview text to acceleration
            textView.text = accelerationFormatted.toString()
            currentVibration = acceleration
            if (acceleration > 1.02) {
                //  Toast.makeText(this, "Device is shaking!", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}




