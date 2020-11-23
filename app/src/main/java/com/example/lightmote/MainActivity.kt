package com.example.lightmote


import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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

    var buttons = arrayOf<Button>()

    var ipAddresses = arrayOf(
            "192.168.43.85",
            "192.168.43.35",
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

    var useMic = arrayOf(true, true, true)

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
        for (i in 0..20) {
            if (selectedIndex[0] == i || selectedIndex[1] == i || selectedIndex[2] == i) {
                buttons[i].setText("O")
            }else{
                buttons[i].setText(" ")
            }
        }
    }

    private val myListener: View.OnClickListener = View.OnClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int
        setSelectedIndex(buttonIndex)
    }

    fun setButtonColor(buttonIndex: Int) {
        var redValue = colors[buttonIndex][0].toInt()*2
        var greenValue = colors[buttonIndex][1].toInt()*2
        var blueValue = colors[buttonIndex][2].toInt()*2
        buttons[buttonIndex].setBackgroundColor(Color.rgb(redValue, greenValue, blueValue))
        if (redValue+greenValue+blueValue > 600) {
            buttons[buttonIndex].setTextColor(Color.BLACK)
        }else{
            buttons[buttonIndex].setTextColor(Color.WHITE)
        }
    }

    private val myLongClickListener: View.OnLongClickListener = View.OnLongClickListener { v ->
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

        buttons = arrayOf(
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
        for (i in 0..20) {
            buttons[i].setOnClickListener(myListener)
            buttons[i].setOnLongClickListener(myLongClickListener)
            buttons[i].tag = i
            setButtonColor(i)
        }
//        for (i in 0..20) {
//            buttons[i].setOnLongClickListener(myLongClickListener)
//            buttons[i].tag = i
//        }
        showSelectedIndex()
        val mySoundMeter = SoundMeter()
        mySoundMeter.start()
        var timeSinceLastMicIndexChange = System.currentTimeMillis()
        var indicesCanIncreaseFromMic = true
        thread {
            while (true) {
                if (useMic[0] == true || useMic[1] == true || useMic[2] == true) {
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    if (mySoundMeter != null) {
                        val amplitude = mySoundMeter.amplitude
                        Log.i("AMPLITUDE", amplitude.toString())
                        if (amplitude > 800){//&& System.currentTimeMillis() - timeSinceLastMicIndexChange > 100) {
                            if (indicesCanIncreaseFromMic) {
                                timeSinceLastMicIndexChange = System.currentTimeMillis()
                                indicesCanIncreaseFromMic = false
                                Log.i("INCREASE", "increase")
                                for (i in 0..2) {
                                    if (useMic[i]) {

                                        increaseSelectedIndexByOne(i)
                                    }
                                }
                            }
                        }else{
                            indicesCanIncreaseFromMic = true
                        }
                    }
                }
            }
        }
    }
}




