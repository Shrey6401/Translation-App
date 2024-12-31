package com.example.translatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;


/** @noinspection ALL*/
public class MainActivity extends AppCompatActivity {

    private Spinner fromspinner, tospinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    String[] fromLanguages = {"from","English","Afrikaans","Arabic","Hindi","Japanese","French","Korean","Italian","Bengali","Farsi","Czech","welsh","Russian"};
    String[] toLanguages = {"To","English","Afrikaans","Arabic","Hindi","Japanese","French","Korean","Italian","Bengali","Farsi","Czech","welsh","Russian"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode,fromLanguageCode,toLanguageCode=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        fromspinner = findViewById(R.id.idFromSpinner);
        tospinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEdtSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslatedTV);
        fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromAdapter  = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        fromspinner.setAdapter(fromAdapter);

        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        tospinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();
                }else if (fromLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguageCode==0) {
                    Toast.makeText(MainActivity.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
                }else{
                    translateText(fromLanguageCode,toLanguageCode,sourceEdt.getText().toString());

                }
            }
        });
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                // Set the recognized text to the TextInputEditText
                sourceEdt.setText(result.get(0));
            } else {
                // Handle the case when no speech was recognized
                Toast.makeText(this, "No speech detected. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle cases where the result is not OK or data is null
            Toast.makeText(this, "Failed to capture speech. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void translateText(int fromLanguageCode, int toLanguageCode, String source){
        translatedTV.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to get message"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public int getLanguageCode(String language){
        int languageCode=0;
        switch (language){
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Farsi":
                languageCode = FirebaseTranslateLanguage.FA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;

        }
        return languageCode;

    }

}