package com.example.lightmote

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    val colors = arrayOf(
        ByteArray(3) { 127; 0; 0 },
        ByteArray(3) { 0; 127; 0 },
        ByteArray(3) { 0; 0; 127 },
        ByteArray(3) { 127; 127; 0 },
        ByteArray(3) { 127; 0; 127 },
        ByteArray(3) { 0; 127; 127 },
        ByteArray(3) { 127; 127; 127 } ,
        ByteArray(3) { 127; 0; 0 },
        ByteArray(3) { 0; 127; 0 },
        ByteArray(3) { 0; 0; 127 },
        ByteArray(3) { 127; 127; 0 },
        ByteArray(3) { 127; 0; 127 },
        ByteArray(3) { 0; 127; 127 },
        ByteArray(3) { 127; 127; 127 } ,
        ByteArray(3) { 127; 0; 0 },
        ByteArray(3) { 0; 127; 0 },
        ByteArray(3) { 0; 0; 127 },
        ByteArray(3) { 127; 127; 0 },
        ByteArray(3) { 127; 0; 127 },
        ByteArray(3) { 0; 127; 127 },
        ByteArray(3) { 127; 127; 127 }
    )

    var ipAddresses = arrayOf(
        "192.168.43.85",
        "192.168.43.35",
        "192.168.43.45"
    )

    fun sendColorChange(ip: String, color: ByteArray){
        val tvHello=findViewById(R.id.mytextView) as TextView;
        tvHello.text=ip
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

    private val myListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            val tag: Any = v.getTag()
            val buttonIndex = tag as Int
            var ipIndex: Int = 0
            when {
                buttonIndex < 7 -> ipIndex = 0
                buttonIndex in 7..13 -> ipIndex = 1
                buttonIndex in 14..20 -> ipIndex = 2
            }
            sendColorChange(ipAddresses[ipIndex], colors[buttonIndex.toInt()])
            // Do something depending on the value of the tag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttons = arrayOf(
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
        }

    }
}

//// TODO: 11/17/20 make a grid of buttons
//        make the grid of buttons
//
//        make long hold on buttons go into color select mode
//        make normal click change the color of the ball on that row
//        make a settings buton to input the IP of the balls for each row
//            or make a button on each row that opens an input for putting in the IP
//        a way to power off the balls
//
//        determine if it is better to use their built in sequence system or make my own
//        a way to record sequences
//        a way to play an mp3 that starts at the same time as recorded sequences
//        a way to load/save sequences