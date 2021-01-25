package com.example.lightmote


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.Image
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

class MainActivity : AppCompatActivity() {

    var settingsButtons = arrayOf<Button>()
    var colorButtons = arrayOf<Button>()
    var micButtons = arrayOf<ImageButton>()
    var micButtonXs = arrayOf<ImageView>()

    var ipAddresses = arrayOf(
            "192.168.43.85",
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

    var micOn = arrayOf(true, true, true)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("onCreate", "onCreate");
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("Build.PRODUCT", Build.PRODUCT);

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
        showSelectedIndex()
        if (Build.PRODUCT != "sdk_gphone_x86_arm") {
            val mySoundMeter = SoundMeter()
            mySoundMeter.start()
            var indicesCanIncreaseFromMic = true
                thread {
                    while (true) {
                        if (micOn[0] || micOn[1] || micOn[2]) {
                            try {
                                Thread.sleep(50)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            if (mySoundMeter != null) {
                                val amplitude = mySoundMeter.amplitude
                                Log.i("AMPLITUDE", amplitude.toString())
                                if (amplitude > 800) {
                                    if (indicesCanIncreaseFromMic) {
                                        indicesCanIncreaseFromMic = false
                                        Log.i("INCREASE", "increase")
                                        for (i in 0..2) {
                                            if (micOn[i]) {

                                                increaseSelectedIndexByOne(i)
                                            }
                                        }
                                    }
                                } else {
                                    indicesCanIncreaseFromMic = true
                                }
                            }
                        }
                    }
                }
        }
    }
}




