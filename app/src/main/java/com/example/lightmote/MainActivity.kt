package com.example.lightmote

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorChangedListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    var ipAddresses = arrayOf(
            "192.168.43.85",
            "192.168.43.35",
            "192.168.43.240"
    )

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
        // Do something depending on the value of the tag
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
                    .setOnColorSelectedListener { selectedColor -> Toast(this) }

                    .setNegativeButton("cancel") { dialog, which -> }
                    .build()
                    .show()
        true



    }
//set each button as the color it is set as
    //make clicking buttons 'select' them by giving them some sort of border
    //resize/position buttons so they are horizontal and leave space
    //clean up todos



//    private val myLongListener: View.OnLongClickListener = object : View.OnLongClickListener {
//        override fun onLongClick(v: View?) {
//            val tag: Any = v.getTag()
//            val buttonIndex = tag as Int
//            var ipIndex: Int = 0
//            when {
//                buttonIndex < 7 -> ipIndex = 0
//                buttonIndex in 7..13 -> ipIndex = 1
//                buttonIndex in 14..20 -> ipIndex = 2
//            }
//            sendColorChange(ipAddresses[ipIndex], colors[buttonIndex], buttonIndex)
//            // Do something depending on the value of the tag
//        }
//
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("onCreate", "onCreate");
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
            buttons[i].setBackgroundColor(Color.rgb(colors[i][0].toInt(),colors[i][1].toInt(),colors[i][2].toInt()))
        }

        for (i in 0..20) {
            buttons[i].setOnLongClickListener(myLongClickListener)
            buttons[i].tag = i
        }

//        val colorwheels = arrayOf(
//                findViewById(R.id.color_picker_view),
//                findViewById(R.id.color_picker_view2),
//                findViewById<ColorPickerView>(R.id.color_picker_view3)
//        )
//        for (i in 0..2) {
//            colorwheels[i].addOnColorChangedListener(OnColorChangedListener { selectedColor ->
//                sendColorChange(ipAddresses[i], getRgbFromHex(Integer.toHexString(selectedColor)), i)
//                ColorPickerDialogBuilder
//                        .with(this)
//                        .setTitle("Choose color")
//
//                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//                        .density(12)
//                        .setOnColorSelectedListener { selectedColor -> Toast(this) }
//
//                        .setNegativeButton("cancel") { dialog, which -> }
//                        .build()
//                        .show()
//            })
//        }


        //bring buttons back, but less room form them so we can add extra modifier buttons for things like sound, strobe, load presets
        //make longhold on color buttons open color picker dialog



//        val recorder = MediaRecorder()
//
//        Log.d("amp2", "amp2");
//
//        while (true){
////            val msg: Message = mHandler.obtainMessage()
//            val b = Bundle()
//            try {
//                sleep(250)
//            } catch (e: InterruptedException) {
//                // TODO Auto-generated catch block
//                e.printStackTrace()
//            }
//            if (recorder != null) {
//                val amplitude = recorder.maxAmplitude
//                b.putLong("currentTime", amplitude.toLong())
//                Log.i("AMPLITUDE", amplitude.toString())
//            } else {
//                b.putLong("currentTime", 0)
//            }
////            msg.data = b
////            mHandler.sendMessage(msg)
//
//        }
    }
}

//get old buttons back
//make longhold got into another window that is color selection

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

//WISHES
//        color change based on juggler location
//        color change based on ball speed
//        color change based on music
//            even if this is super simple and just changes to the next color in a cycle everytime it hears a ball slap against my hand
//
//        lots of different color option
//            1)color wheel
//        ability to change colors automatically on loop through X colors
//
//        ability to make keyboard hotkeys for colors
//        ability to make onscreen buttons for colors
//            1)while in edit mode, pushing a key or a button makes that key/button become the currently selected color
//
//        a way to record sequences
//        a way to play an mp3 that starts at the same time as recorded sequences
//        a way to load/save sequences
//
//        a way to power off the balls
//        a way to turn off balls lights, but leave it on
//        a way to return to default colors
//
//        Possible button/key setup
//        -an X that shown up in edit mode that allows you to set a button or key as off
//        -a way to tell it the number of buttons/keys you want it to cycle through, the first 3 butttons, (q,w,e) for example.
//            also a way to tell it how fast to cycle through
//        button have letters on them so they match up with keyboard(this could be an option)
//
//          a way to fade
//          a way to strobe