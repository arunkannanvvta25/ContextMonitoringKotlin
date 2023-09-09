package com.example.contextmonitoring

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.View
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    lateinit var heartRateVal: MutableLiveData<String>

    init {
        heartRateVal = MutableLiveData()
        heartRateVal.value = ".."
    }

    private lateinit var healthDataVM: HealthDataViewModel
    private var accelerometerValue: Float = 0.0f
    private var heartRateValue: Float = 0.0f
    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    var result = computeRespiratoryRate(uri).toString();
                    var acc_val: TextView = findViewById(R.id.textviewResp)
                    acc_val.text = "Respiratory Rate is +$result";
                } else {
                    Toast.makeText(this, "File selection canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        LoadProps()
        super.onCreate(savedInstanceState)
        heartRateVal.observe(this) { result ->
            var tvBloodRate = findViewById<TextView>(R.id.textView)
            tvBloodRate.text = "Heart Rate is : $result"
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
            val optionsList = intent.getSerializableExtra("options") as ArrayList<Float>
            healthDataVM = ViewModelProvider(this).get(HealthDataViewModel::class.java)
            val newEntry = HealthData(
                0,
                optionsList[0],
                optionsList[1],
                optionsList[2],
                optionsList[3],
                optionsList[4],
                optionsList[5],
                optionsList[6],
                optionsList[7],
                optionsList[8],
                optionsList[9],
                optionsList[10].toString(),
                optionsList[11].toInt()
            )
            healthDataVM.insert(newEntry)

            Toast.makeText(this, "saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVideo() {
        var tvBloodRate = findViewById<TextView>(R.id.textView)
        tvBloodRate.text = "Loading"
        getHeartRateVideo.launch("video/*")
    }

    private fun navigateToSymptomsPage() {
        var arraylist = ArrayList<Float>()
        arraylist.add(heartRateValue)
        arraylist.add(accelerometerValue)
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

    private fun computeRespiratoryRate(uri: Uri): Float {
        var result = 0.00
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                // Split the CSV row by commas
                val values = line?.split(",")
                var temp = 0;
                if (values != null) {
                    var k = 0
                    var previousValue: Double = 0.0
                    for (value in values) {
                        val currentValue: Double = value.toDouble()
                        if (abs(currentValue - previousValue) > 0.15) {
                            k++
                        }
                        previousValue = currentValue
                        temp += 1;
                        if (temp == 1280)
                            break;
                    }
                    result = k / 45.0
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading CSV file", Toast.LENGTH_SHORT).show()
        }
        accelerometerValue = (result * 30).toFloat();
        return accelerometerValue;
    }

    private val getHeartRateVideo =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d("URI", uri.toString())
            GlobalScope.launch {
                val result = processVideoFrames(uri)
                heartRateVal.postValue(result);
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

