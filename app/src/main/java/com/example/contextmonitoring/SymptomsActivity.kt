package com.example.contextmonitoring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RatingBar
import android.widget.Spinner
import androidx.activity.ComponentActivity
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.example.contextmonitoring.Db.HealthDataViewModel
import com.example.contextmonitoring.Db.SymptomRating

class SymptomsActivity : ComponentActivity() {
    private lateinit var dropdown: Spinner
    private lateinit var starRating: RatingBar
    private val symptoms = mutableListOf<SymptomRating>()
    private var firstPage = ArrayList<Float>()
    private lateinit var sp: SharedPreferences
    val listOfSymptoms = SymptomsOptions.listOfSymptoms
    private lateinit var healthDataVm: HealthDataViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SymptomsRatings", Context.MODE_PRIVATE)
        val intent = intent
        if (intent != null && intent.hasExtra("options")) {
            val optionsList = intent.getSerializableExtra("options") as ArrayList<Float>
            firstPage = optionsList;
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms)
        healthDataVm = ViewModelProvider(this).get(HealthDataViewModel::class.java)
        dropdown = findViewById(R.id.spinner)
        starRating = findViewById(R.id.ratingBar)
        starRating.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            val symptomSelected = symptoms[dropdown.selectedItemPosition]
            symptomSelected.rating = rating
            sp.edit {
                putFloat(symptomSelected.option, rating)
                apply()
            }
        }

        val size = listOfSymptoms.size - 1
        for (i in 0..size) {
            val symptom = listOfSymptoms[i]
            val rating = sp.getFloat(symptom, 0.0f)
            symptoms.add(SymptomRating(symptom, rating))
        }

        val dropdownadapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, symptoms.map { it.option })
        dropdownadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = dropdownadapter
        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val symptomSelected = symptoms[position]
                starRating.rating = symptomSelected.rating
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
        val reset = findViewById<Button>(R.id.upload)
        reset.setOnClickListener {
            var ratings = ArrayList<Float>()
            for (i in 1..10) {
                ratings.add(symptoms[i - 1].rating)
            }
            ratings.add(firstPage[0]);
            ratings.add(firstPage[1]);
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("options", ratings)
            val size = listOfSymptoms.size - 1
            for (i in 0..size) {
                sp.edit {
                    putFloat(listOfSymptoms[i], 0.0f)
                    apply()
                }
            }
            startActivity(intent)
        }
    }
}