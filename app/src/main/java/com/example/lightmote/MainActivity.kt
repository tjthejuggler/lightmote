package com.example.lightmote

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

    val colors: Array<ByteArray> = arrayOf(
        byteArrayOf (127, 0, 0 ),
        byteArrayOf (0, 127, 0 ),
        byteArrayOf (0, 0, 127 ),
        byteArrayOf (127, 127, 0 ),
        byteArrayOf (127, 0, 127 ),
        byteArrayOf (0, 127, 127 ),
        byteArrayOf (127, 127, 127 ) ,
        byteArrayOf (127, 0, 0 ),
        byteArrayOf (0, 127, 0 ),
        byteArrayOf (0, 0, 127 ),
        byteArrayOf (127, 127, 0 ),
        byteArrayOf (127, 0, 127 ),
        byteArrayOf (0, 127, 127 ),
        byteArrayOf (127, 127, 127 ) ,
        byteArrayOf (127, 0, 0 ),
        byteArrayOf (0, 127, 0 ),
        byteArrayOf (0, 0, 127 ),
        byteArrayOf (127, 127, 0 ),
        byteArrayOf (127, 0, 127 ),
        byteArrayOf (0, 127, 127 ),
        byteArrayOf (127, 127, 127 )
    )

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




    private val myListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            Log.d("onClick", "onClick");
            val tag: Any = v.getTag()
            val buttonIndex = tag as Int
            var ipIndex: Int = 0
//            when {
//                buttonIndex < 7 -> ipIndex = 0
//                buttonIndex in 7..13 -> ipIndex = 1
//                buttonIndex in 14..20 -> ipIndex = 2
//            }
            sendColorChange(ipAddresses[buttonIndex], colors[buttonIndex], buttonIndex)
        }
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

        //todo
        //hook up this colorchangelistener to ever wheel
        //make them change ball colors

        colorwheels[0].addOnColorChangedListener(OnColorChangedListener { selectedColor -> // Handle on color change
            Log.d(
                "ColorPicker",
                "onColorChanged: 0x" + Integer.toHexString(selectedColor)
            )
        })

        for (i in 0..2) {
            colorwheels[i].setOnClickListener(myListener)
            colorwheels[i].tag = i
//            colorwheels[i].setBackgroundColor(Color.rgb(colors[i][0].toInt(),colors[i][1].toInt(),colors[i][2].toInt()))

        }
//        val buttons = arrayOf(
//            findViewById(R.id.btnA1),
//            findViewById(R.id.btnA2),
//            findViewById(R.id.btnA3),
//            findViewById(R.id.btnA4),
//            findViewById(R.id.btnA5),
//            findViewById(R.id.btnA6),
//            findViewById(R.id.btnA7),
//            findViewById(R.id.btnB1),
//            findViewById(R.id.btnB2),
//            findViewById(R.id.btnB3),
//            findViewById(R.id.btnB4),
//            findViewById(R.id.btnB5),
//            findViewById(R.id.btnB6),
//            findViewById(R.id.btnB7),
//            findViewById(R.id.btnC1),
//            findViewById(R.id.btnC2),
//            findViewById(R.id.btnC3),
//            findViewById(R.id.btnC4),
//            findViewById(R.id.btnC5),
//            findViewById(R.id.btnC6),
//            findViewById<Button>(R.id.btnC7)
//        )
//
//        for (i in 0..20) {
//            buttons[i].setOnClickListener(myListener)
//            buttons[i].tag = i
//            buttons[i].setBackgroundColor(Color.rgb(colors[i][0].toInt(),colors[i][1].toInt(),colors[i][2].toInt()))
//            buttons[i].visibility = View.INVISIBLE
//        }


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