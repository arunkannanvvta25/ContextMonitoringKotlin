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
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.example.med_app.SymptomRating

class SymptomsActivity : ComponentActivity() {
    private lateinit var dropdown: Spinner
    private lateinit var starRating: RatingBar
    private val symptoms = mutableListOf<SymptomRating>()
    private var firstPage = ArrayList<Float>()
    private lateinit var sp: SharedPreferences
    val listOfSymptoms = arrayOf(
        "Nausea", "Headache", "Diarrhea", "Soar Throat", "Fever",
        "Muscle Ache", "Loss of smell or taste", "Cough", "Shortness of Breath", "Feeling Tired"
    )
    private lateinit var healthDatavm: HealthDataViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        sp = getSharedPreferences("SymptomsRatings", Context.MODE_PRIVATE)
        val intent = intent
        if (intent != null && intent.hasExtra("options")) {
            val optionsList = intent.getSerializableExtra("options") as ArrayList<Float>
            firstPage = optionsList;
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptoms)
        dropdown = findViewById(R.id.spinner)
        starRating = findViewById(R.id.ratingBar)
        healthDatavm = ViewModelProvider(this).get(HealthDataViewModel::class.java)        
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
        starRating.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            val symptomSelected = symptoms[dropdown.selectedItemPosition]
            symptomSelected.rating = rating
            sp.edit {
                putFloat(symptomSelected.option, rating)
                apply()
            }
        }
        val second = findViewById<Button>(R.id.go_back)
        second.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val reset = findViewById<Button>(R.id.upload)
        reset.setOnClickListener {
            val size=listOfSymptoms.size - 1
            for (i in 0..size) {
                val optionName = listOfSymptoms[i]
            }
            var ratings = ArrayList<Float>()
            for (i in 1..10) {
                ratings.add(symptoms[i - 1].rating)
            }
            ratings.add(firstPage[0]);
            ratings.add(firstPage[1]);
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("options", ratings)
            sp.edit().clear()
            startActivity(intent)
        }
    }
}