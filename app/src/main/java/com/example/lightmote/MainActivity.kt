package com.example.lightmote

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorChangedListener
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    var ipAddresses = arrayOf(
            "192.168.43.85",
            "192.168.43.35",
            "192.168.43.45"
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
        return byteArrayOf(r.toByte(), g.toByte(), b.toByte() )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("onCreate", "onCreate");
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val colorwheels = arrayOf(
                findViewById(R.id.color_picker_view),
                findViewById(R.id.color_picker_view2),
                findViewById<ColorPickerView>(R.id.color_picker_view3)
        )
        for (i in 0..2) {
            colorwheels[i].addOnColorChangedListener(OnColorChangedListener { selectedColor ->
                sendColorChange(ipAddresses[i], getRgbFromHex(Integer.toHexString(selectedColor)), i)
            })
        }
    }
}

//// TODO: 11/17/20 make a grid of buttons
//        make the grid of buttons
//          make color widget control ball colors
//          make 3 color widgets
//
//        make long hold on buttons go into color select mode
//        make normal click change the color of the ball on that row
//        make a settings buton to input the IP of the balls for each row
//            or make a button on each row that opens an input for putting in the IP
//        a way to power off the balls
//        a way to save button colors
//          a way to return to default colors
//
//        determine if it is better to use their built in sequence system or make my own
//        a way to record sequences
//        a way to play an mp3 that starts at the same time as recorded sequences
//        a way to load/save sequences