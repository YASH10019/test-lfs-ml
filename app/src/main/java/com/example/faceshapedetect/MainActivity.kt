package com.example.faceshapedetect
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.faceshapedetect.ml.FaceShapeRecognizer
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.util.Collections.copy
import java.util.jar.Manifest
import android.content.Intent as Intent1

class MainActivity : AppCompatActivity() {

    lateinit var select_image_button : Button
    lateinit var make_prediction : Button
    lateinit var img_view : ImageView
    lateinit var text_view : TextView
    var bitmap:Bitmap? = null
    lateinit var camerabtn : Button
    var uri:String = ""

    @RequiresApi(Build.VERSION_CODES.M)
    public fun checkandGetpermissions(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
        else{
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        select_image_button = findViewById(R.id.button)
        make_prediction = findViewById(R.id.button2)
        img_view = findViewById(R.id.imageView2)
        text_view = findViewById(R.id.textView)
        camerabtn = findViewById<Button>(R.id.camerabtn)

        // handling permissions
        checkandGetpermissions()


        select_image_button.setOnClickListener(View.OnClickListener {
            Log.d("mssg", "button pressed")
            var intent : Intent1 = Intent1(Intent1.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 250)
        })

        camerabtn.setOnClickListener(View.OnClickListener {
            var camera : Intent1 = Intent1(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(camera, 200)
        })


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent1?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 250){
            img_view.setImageURI(data?.data)

            val uri : Uri ?= data?.data
            uri!!
//            uri = uuri.toString()
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
        else if(requestCode == 200 && resultCode == Activity.RESULT_OK){
            bitmap = data?.extras?.get("data") as Bitmap
            img_view.setImageBitmap(bitmap)
        }
        fun ARGBBitmap(img: Bitmap): Bitmap {
            return img.copy(Bitmap.Config.ARGB_8888, true)
        }
//        val labels = application.assets.open("labels.txt").bufferedReader().use { it.readText() }.split("\n")
val filename  = "labels.txt"
        val inputString = application.assets.open(filename).bufferedReader().use { it.readText() }
        val townlost = inputString.split("\n")
        make_prediction.setOnClickListener(View.OnClickListener {
            var resized = Bitmap.createScaledBitmap(ARGBBitmap(bitmap!!), 250, 190, true)
//            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri.toUri())
            val model = FaceShapeRecognizer.newInstance(this)
//            var tbuffer = TensorImage.fromBitmap(resized)
            if (bitmap != null) {
                var tbuffer = TensorImage.fromBitmap(ARGBBitmap(bitmap!!))



//                var byteBuffer = tbuffer.buffer
// Creates inputs for reference.
                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 250, 190, 1), DataType.FLOAT32)
//                inputFeature0.loadBuffer(byteBuffer)
                val scaled = Bitmap.createScaledBitmap(tbuffer.bitmap, 250, 190, true)
                var byteBuffer = ByteBuffer.allocate(scaled.height * scaled.rowBytes)
                scaled.copyPixelsToBuffer(byteBuffer)
                inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer


//                var max = getMax(outputFeature0.floatArray)
//                val classes = arrayOf("Heart", "Oblong", "Oval", "Round", "Square")
                text_view.setText(outputFeature0.toString())




// Releases model resources if no longer used.
                model.close()}
        }
        )


    }




}