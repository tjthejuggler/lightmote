package com.example.lightmote


import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.concurrent.thread


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

    private val myListener: View.OnClickListener = View.OnClickListener { v ->
        val tag: Any = v.getTag()
        val buttonIndex = tag as Int
        var ipIndex: Int = 0
        when {
            buttonIndex < 7 -> ipIndex = 0
            buttonIndex in 7..13 -> ipIndex = 1
            buttonIndex in 14..20 -> ipIndex = 2
        }
        sendColorChange(ipAddresses[ipIndex], colors[buttonIndex], buttonIndex)

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
                        buttons[buttonIndex].setBackgroundColor(Color.rgb(colors[buttonIndex][0].toInt()*2, colors[buttonIndex][1].toInt()*2, colors[buttonIndex][2].toInt()*2))
                        Log.i("selectedColor", selectedColor.toString())
                    }
                    .setNegativeButton("ok") { dialog, which -> }
                    .build()
                    .show()
        true
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
            buttons[i].tag = i
            buttons[i].setBackgroundColor(Color.rgb(colors[i][0].toInt()*2, colors[i][1].toInt()*2, colors[i][2].toInt()*2))
        }

        for (i in 0..20) {
            buttons[i].setOnLongClickListener(myLongClickListener)
            buttons[i].tag = i
        }
    }
}




