package com.example.languagetranslatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private ImageView micIV;
    private MaterialButton translateBtn;
    private Spinner toSpinner, fromSpinner;
    private TextInputEditText sourceEdt;
    private TextView translatedTV;

    String[] fromLanguages = {"From","English","Arabic","African","Belarusian","Bulgarian","Bengali","Catlan",
    "Czech","Welsh","Hindi","Urdu"};
    String[] toLanguages = {"To","English","Arabic","African","Belarusian","Bulgarian","Bengali","Catlan",
            "Czech","Welsh","Hindi","Urdu"};
    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguageCode, toLanguageCOde = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        micIV = findViewById(R.id.idIVMic);
        translateBtn=findViewById(R.id.idBtnTranslate);
        sourceEdt=findViewById(R.id.idEditSource);
        translatedTV=findViewById(R.id.idTranslatedTV);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode=getLanguageCode(fromLanguages[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCOde = getLanguageCode(toLanguages[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please add text to Translate",Toast.LENGTH_SHORT);
                }
                else if (fromLanguageCode==0){
                    Toast.makeText(MainActivity.this,"Please select source language",Toast.LENGTH_SHORT);
                }
                else if (toLanguageCOde==0){
                    Toast.makeText(MainActivity.this,"Please select conversion language",Toast.LENGTH_SHORT);

                }
                else{
                        translateText(toLanguageCOde,fromLanguageCode,sourceEdt.getText().toString());
                }
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak for conversion");

                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_PERMISSION_CODE){
            if (requestCode==RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));

            }
        }
    }

    private void translateText(int toLanguageCOde, int fromLanguageCode, String source){
        translatedTV.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCOde)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Failed to translate: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(MainActivity.this,"Fail to Download Model"+e.getMessage(),Toast.LENGTH_SHORT);

            }
        });



    }



    public int getLanguageCode(String language) {
        int languageCode=0;
        switch (language){
            case "English":
                languageCode= FirebaseTranslateLanguage.EN;
                break;
            case "Arabic":0
                languageCode= FirebaseTranslateLanguage.AR;
                break;

            case "African":
                languageCode= FirebaseTranslateLanguage.AF;
                break;
            case "Belarusian":
                languageCode= FirebaseTranslateLanguage.BE;
                break;

            case "Bulgarian":
                languageCode= FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode= FirebaseTranslateLanguage.BN;
                break;

            case "Catlan":
                languageCode= FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode= FirebaseTranslateLanguage.CS;
                break;

            case "Welsh":
                languageCode= FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode= FirebaseTranslateLanguage.HI;
                break;

            case "Urdu":
                languageCode= FirebaseTranslateLanguage.UR;
                break;

            default:
                languageCode=0;

        }

        return  languageCode;
    }
}