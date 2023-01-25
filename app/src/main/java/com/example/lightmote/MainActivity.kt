package com.example.lightmote

//add buffers after a vibration change
//make buffer shrink as there are more vibrations, and get longer when there are less vibrations
//we need to keep track of the last vibration and the number of vibrations in x seconds
//the buffer time is based on the number of vibrations so we can do clear clicks, but also sped up fast stuff

//there should be a sequence or action of some sort that togels between my control and the automatic control. Would be great if the automatic was based on whatever I do.
//
//it should be easy to calibrate



import AccelerationUtil
import CompassUtil
import OrientationUtil
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log



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

    val player = MediaPlayer()

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

    fun getByteArrayFromRGB(r: Int, g: Int, b: Int): ByteArray {
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
        Log.d("button color", ipAddresses[ipIndex]+", "+colors[buttonIndex].toString()+", "+buttonIndex.toString())
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

    //make a function called getTimestamp that takes two inputs, a line number and a bunch of text
    //the function will return the timestamp of the line number
    fun getTimestamp(lineNumber: Int, text: String): Int{
        Log.i("text", text.toString())
        var lines = text.split("\n")
        //log lineNumber here
        Log.i("lineNumber", lineNumber.toString())

        var line = lines[lineNumber]
        Log.i("linexxx", line.toString())
        //the timestamp is in this format       "1794": "x;255,255,0;x",
        //we want to get the 1794
        var timestamp = line.substring(1, line.indexOf(":"))
        //remove the quotes
        timestamp = timestamp.replace("\"", "")
        //remove the space
        timestamp = timestamp.replace(" ", "")
        return timestamp.toInt()
    }
//make a void function called send_sequence_color_change
    //this function will take in a line number and a bunch of text



    fun send_sequence_color_change(lineNumber: Int, text: String) {
        //get the line
        var lines = text.split("\n")
        var line = lines[lineNumber]
        //the line will look like this  "1794": "255,0,0;255,255,0;0,0,255",
        //it reprsents three different colors. ignore the timestamp and get teh three colors from these
        var colors = line.substring(line.indexOf(":") + 2, line.length - 1).split(";").toMutableList()
        //remove the quotes
        colors[0] = colors[0].replace("\"", "")
        colors[1] = colors[1].replace("\"", "")
        colors[2] = colors[2].replace("\"", "")


        //Log the three colors here
        Log.i("color1", colors[0])
        Log.i("color2", colors[1])
        Log.i("color3", colors[2])
        //send the color change
        //sendColorChange(ipAddresses[buttonIndex], rgb, buttonIndex)

        //use this
        //loop through colors with an index
        for (i in 0..colors.size - 1) {
            //check if color == "x"
            if (colors[i] != "x") {
                //if it is x then do nothing
                //get the color
                var color = colors[i]
                //get the rgb
                var byte_rgb = getByteArrayFromRGB(
                    color.split(",")[0].toInt()/2,
                    color.split(",")[1].toInt()/2,
                    color.split(",")[2].toInt()/2
                )
                //send the color change
                sendColorChange(ipAddresses[0], byte_rgb, i)
            }

            //todo i need to send continuous color changes based on phone position sensors
            //  the color set by the timestamps will be recorded and become the new base color for that ball
            //  (an alternative to this would be to only do color changes when the timestamps say to,
            //   but then at those times take into account the phone position sensors)
            //todo highlight current line
            //todo autoscroll lines
            //todo make it work with screen off
            //todo hook up movements to color changes


        }
    }





    fun playMP3(context: Context) {
        var sequenceEditText = findViewById<View>(R.id.sequenceText) as EditText
        var sequenceEditTextInput = sequenceEditText.text.toString()
        var fileName = sequenceEditTextInput.split("\n")[0]
        val path: String = "/storage/emulated/0/" + fileName + ".mp3"
        //val player = MediaPlayer()
        try {
            player.setDataSource(path)
            player.prepare()
            player.start()
        } catch (e: IOException) {
            Log.e("MainActivity", "prepare() failed")
        }
        //while the song is playing we want to run this code


        var cur_color_line_number = 2
        while (player.isPlaying) {
            //we want to get the current position of the song

            var currentPosition = player.currentPosition
            if (currentPosition >= getTimestamp(cur_color_line_number, sequenceEditTextInput)) {
                send_sequence_color_change(cur_color_line_number, sequenceEditTextInput)
                cur_color_line_number += 1
            }
            //
            //todo while the song is playing we should be looking for the next timestamp and sending the color
            //we always


//        try {
//            Log.d("seqBtn path", path);
//            player.setDataSource(path)
//            player.prepare()
//            player.setOnPreparedListener(OnPreparedListener { mp -> mp.start() })
//            player.prepareAsync()
//            var music_is_playing = true
//            player.setOnCompletionListener(OnCompletionListener {
//                music_is_playing = false
//            })
//            while (music_is_playing){
//                playColorSequence(this, player.getCurrentPosition())
//            }
//        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
//        } catch (e: Exception) {
//            println("Exception of type : $e")
//            e.printStackTrace()
//        }
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
        if (player.isPlaying()) {
            player.stop()
            player.reset()
        }else{
            playMP3(this)
        }
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

    fun map(oldValue: Float, oldMin: Float, oldMax: Float, newMin: Float, newMax: Float): Float {
        return (((oldValue - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin
    }

    fun mapToColor(oldValue: Float, oldMax: Float): ByteArray {
        val mappedValue = map(oldValue, 0f, oldMax, 0f, 360f)
        val color = Color.HSVToColor(floatArrayOf(mappedValue, 1.0f, 1.0f))
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        val red = (hsv[0] * 128).toInt()
        val green = (hsv[1] * 128).toInt()
        val blue = (hsv[2] * 128).toInt()
        //create a bytearray to hold the rgb values
        var rgb = getByteArrayFromRGB(
            red,
            green,
           blue
        )

        //rgb = (red shl 24) or (green shl 16) or (blue shl 8) or 0xff
        return rgb

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
        val usingMic = false
        val usingAcceleration = false
        val usingCompass = true
        val usingOrientation = false
        //TODO figure out why this and the other sequenceButton place is making it crash
        sequenceButton.setOnClickListener(sequenceButtonListener)
        showSelectedIndex()
        if (Build.PRODUCT != "sdk_gphone_x86_arm") {
            val mySoundMeter = SoundMeter()
            mySoundMeter.start()
            val myCompassUtil = CompassUtil(this)
            myCompassUtil.start()
            val myOrientationUtil = OrientationUtil(this)
            myOrientationUtil.start()
            val myAccelerationUtil = AccelerationUtil(this)
            myAccelerationUtil.start()
                var indicesCanIncreaseFromMic = true
                var indicesCanIncreaseFromVib = true
                var compassDirection = 0.0f
                var acceleration = myAccelerationUtil.getAcceleration()
                var orientation = myOrientationUtil.getOrientation()
                var highest_azimuth = 0.0f
                var lowest_azimuth = 0.0f
                thread {
                    while (true) {
                        if (usingCompass) {
                            compassDirection = myCompassUtil.getDirection()
                            //log compass direction
                            Log.d("compass direction", compassDirection.toString())

                            val maxValue: Float = 719f
                            val color = mapToColor(compassDirection, maxValue)
                            Log.d("compass color", ipAddresses[0] +", "+color.toString())
                            //192.168.43.85, , 2
                            //var temp_byte_array = "[B@bb44aa2"
                            runOnUiThread {
                                sendColorChange(ipAddresses[0], color, 0)
                            }
                            //todo figure out why the compass color is flickering so much and seems not to be hooked up to the compass
                            //todo try out other sensors

                            //todo maybe the color selected on the app should be the base color

                            //wait a second
                            Thread.sleep(10)
                            //todo if i am doing this continuously then i need to make sure that i am not doing it too fast
                            //  one option is to only send color changes if there has been a large enough sensor change
                            //  another option is to send lots of color changes.

                            //this should use the current base color and then adjust it based on the compass direction
                            //todo save the 3 base colors so you can use them here in the sensors

                            //todo app shouldnt crash when the timestamps run out. it should just go back to normal mode
                        }

                        if (usingAcceleration) {
                            acceleration = myAccelerationUtil.getAcceleration()
                            //log acceleration
                            //split acceleration into x, y, and z
                            val x = acceleration[0]
                            val y = acceleration[1]
                            val z = acceleration[2]
                            //log x, y, and z in a single log
                            Log.d("acceleration", "x: $x, y: $y, z: $z")
                        }

                        if (usingOrientation) {
                            orientation = myOrientationUtil.getOrientation()
                            //log orientation
                            //split orientation into azimuth, pitch, and roll
                            val azimuth = orientation[0]
                            val pitch = orientation[1]
                            val roll = orientation[2]
                            //log azimuth, pitch, and roll in a single log
                            Log.d("azimuth, pitch, roll", "$azimuth, $pitch, $roll")
                            //keep track of the highest and lowest azimuth
                            if (azimuth > highest_azimuth) {
                                highest_azimuth = azimuth
                            }
                            if (azimuth < lowest_azimuth) {
                                lowest_azimuth = azimuth
                            }
                            //use the highest and lowest azimuth to convert the range to integers 0 through 6
                            val azimuth_range = highest_azimuth - lowest_azimuth
                            val azimuth_range_per_number = azimuth_range / 7
                            val azimuth_number = ((azimuth - lowest_azimuth) / azimuth_range_per_number).toInt()
                            //Log.d("azimuthInt", azimuth_number.toString())

                        }
                        //todo sequence files
                        //mp3 should be playable with a text sequence file


                        //todo move this azimuth stuff into the orientation util
                        //todo keep track of previous azimuth int and change the color of he balls based on if a change happens
                        //todo make azimuth be a smooth transition through colors instead of just changing colors

                        if (usingMic) {
                            if (micOn[0] || micOn[1] || micOn[2]) { //todo right now vibration stuff is under this mic stuff and it should be independent
                                try {
                                    Thread.sleep(50)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                                if (mySoundMeter != null) { //todo get this mic stuff turned back on
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
                                }
                            }
                        }

                        if (usingAcceleration){
//                                val vibration_rate = mySoundMeter.amplitude
//                                Log.i("vibration_rate", vibration_rate.toString())
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




