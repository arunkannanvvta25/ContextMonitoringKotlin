package com.example.contextmonitoring

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.contextmonitoring.Db.HealthData
import com.example.contextmonitoring.Db.HealthDataViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    lateinit var heartRateLiveData: MutableLiveData<String>

    init {
        heartRateLiveData = MutableLiveData()
        heartRateLiveData.value = ".."
    }

    private lateinit var healthDataVM: HealthDataViewModel
    private var accelerometerValue: Int = 0
    private var heartRateValue: Float = 0.0f
    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    var result = computeRespiratoryRate(uri)
                    accelerometerValue=result;
                    var acc_val: TextView = findViewById(R.id.textviewResp)
                    acc_val.text = "Respiratory Rate is $result";
                } else {
                    Toast.makeText(this, "File selection canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadProps()
        super.onCreate(savedInstanceState)
        heartRateLiveData.observe(this) { result ->
            var textView = findViewById<TextView>(R.id.textView)
            textView.text = "Heart Rate is : $result"
        }
        setContentView(R.layout.activity_main)
        val heartRate = findViewById<Button>(R.id.heart)
        heartRate.setOnClickListener(View.OnClickListener {
            getVideo()
        })
        setRespiratoryButtonOnClick()
        val symptomsBtn = findViewById<Button>(R.id.symptoms)
        symptomsBtn.setOnClickListener {
            navigateToSymptomsPage()
        }
    }

    private fun LoadProps() {
        val intent = intent
        if (intent != null && intent.hasExtra("options")) {
            val columns = intent.getSerializableExtra("options") as ArrayList<Float>
            healthDataVM = ViewModelProvider(this).get(HealthDataViewModel::class.java)
            val row = HealthData(
                0,
                columns[0],
                columns[1],
                columns[2],
                columns[3],
                columns[4],
                columns[5],
                columns[6],
                columns[7],
                columns[8],
                columns[9],
                columns[10].toString(),
                columns[11].toInt()
            )
            healthDataVM.insert(row)
            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVideo() {
        var textView = findViewById<TextView>(R.id.textView)
        textView.text = "Loading"
        getHeartRateVideo.launch("video/*")
    }

    private fun navigateToSymptomsPage() {
        var arraylist = ArrayList<Float>()
        arraylist.add(heartRateValue)
        arraylist.add(accelerometerValue.toFloat())
        val intent = Intent(this, SymptomsActivity::class.java)
        intent.putExtra("options", arraylist)
        startActivity(intent)
    }

    private fun setRespiratoryButtonOnClick() {
        val respRate = findViewById<Button>(R.id.resp)
        respRate.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            openDocumentLauncher.launch(intent)
        })
    }

    private fun computeRespiratoryRate(uri: Uri): Int {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        return RespiratoryRateComputer.compute(inputStream,uri);
    }

    private val getHeartRateVideo =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d("URI", uri.toString())
            GlobalScope.launch {
                val result = processVideoFrames(uri)
                heartRateLiveData.postValue(result);
                Log.d("final", result);
            }
        }

    private suspend fun processVideoFrames(videoUri: Uri?): String {

        val retriever = MediaMetadataRetriever()
        var frameList = ArrayList<Bitmap>()
        try {
            retriever.setDataSource(this, videoUri)
            // Calculate the total duration of the video in microseconds.
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    ?: 0
            var aduration = duration!!.toInt()
            var i = 10
            while (i < aduration) {
                val bitmap = retriever.getFrameAtIndex(i)
                frameList.add(bitmap!!)
                i += 5
            }
        } catch (m_e: Exception) {
        } finally {
            retriever?.release()
            var redBucket: Long = 0
            var pixelCount: Long = 0
            val a = mutableListOf<Long>()
            for (i in frameList) {
                redBucket = 0
                for (y in 100 until 200) {
                    for (x in 100 until 200) {
                        val c: Int = i.getPixel(x, y)
                        pixelCount++
                        redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                    }
                }
                a.add(redBucket)
            }
            val b = mutableListOf<Long>()
            for (i in 0 until a.lastIndex - 5) {
                var temp =
                    (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2) + a.elementAt(
                        i + 3
                    ) + a.elementAt(
                        i + 4
                    )) / 4
                b.add(temp)
            }
            var x = b.elementAt(0)
            var count = 0
            for (i in 1 until b.lastIndex) {
                var p = b.elementAt(i.toInt())
                val dif = p - x;
                Log.d("Diff", dif.toString())
                if ((p - x) > 800) {
                    count = count + 1
                }
                x = b.elementAt(i.toInt())
            }
            var rate = ((count.toFloat() / 45) * 60).toInt()
            val temp = (rate / 2);
            heartRateValue = temp.toFloat();
            return temp.toString()
        }
    }


}

