package com.example.imagerecognitionapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ibm.cloud.sdk.core.security.Authenticator
import com.ibm.cloud.sdk.core.security.IamAuthenticator
import com.ibm.watson.language_translator.v3.LanguageTranslator
import com.ibm.watson.language_translator.v3.model.TranslateOptions
import com.ibm.watson.language_translator.v3.model.TranslationResult
import com.ibm.watson.visual_recognition.v3.VisualRecognition
import com.ibm.watson.visual_recognition.v3.model.ClassifiedImages
import com.ibm.watson.visual_recognition.v3.model.ClassifyOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

val scope = CoroutineScope(Dispatchers.Default)

class MainActivity : AppCompatActivity() {

    lateinit var photoFile : File
    private val READ_REQUEST_CODE: Int = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //画像選択ボタン
        val selectbutton = findViewById<Button>(R.id.selectButton)
        selectbutton.setOnClickListener {
            selectPhoto()
        }

        //画像判定ボタン
        val judgebutton = findViewById<Button>(R.id.judgeButton)
        judgebutton.setOnClickListener {
            var judge_en : String? = null
            var judge_ja : String? = null
            scope.launch {
                val WT = WatsonTask()
                judge_en = WT.visualRecognition(photoFile)
                judge_ja = WT.languageTranslator(judge_en.toString())
            }
            while(judge_ja == null){
                Thread.sleep(10)
            }
            val judge_jatextview = findViewById<TextView>(R.id.judge_jaTextView)
            judge_jatextview.setText(judge_ja)
            val judge_entextview = findViewById<TextView>(R.id.judge_enTextView)
            judge_entextview.setText(judge_en)
        }
    }

    //画像が選択された時の処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                var uri :Uri= Uri.parse(data.getData().toString())
                try {
                    photoFile = File(GetPathFromUri.getPathFromUri( applicationContext , uri))
                    val image = BitmapFactory.decodeStream(contentResolver?.openInputStream(uri))
                    val imageView = findViewById<ImageView>(R.id.imageView)
                    imageView.setImageBitmap(image)
                } catch (e: Exception) {
                    Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //画像の選択
    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }
}

//Watsonの非同期処理
class WatsonTask(){
    //VisualRecognitionのAPIキー
    val VR_API_KEY = ""
    val vrauthenticator : Authenticator = IamAuthenticator(VR_API_KEY)
    val vrService :VisualRecognition = VisualRecognition("2018-03-19",vrauthenticator)

    //LanguageTranslaterのAPIキー
    val LT_API_KEY = ""
    val ltauthenticator : Authenticator = IamAuthenticator(LT_API_KEY)
    val ltService :LanguageTranslator = LanguageTranslator("2018-05-01",ltauthenticator)

    //VisualRecognizerの処理
    fun visualRecognition(photoFile : File) : String?{
        try{
            var vroptions: ClassifyOptions = ClassifyOptions.Builder()
                .imagesFile(photoFile)
                .build()
            var vrResult : ClassifiedImages = vrService.classify(vroptions).execute().getResult()
            System.out.println(vrResult)
            val vrResultJson = JSONObject(vrResult.toString())
            val judge = vrResultJson.getJSONArray("images").getJSONObject(0).getJSONArray("classifiers").getJSONObject(0).getJSONArray("classes").getJSONObject(0).getString("class")
            System.out.println(judge)
            return  judge
        } catch(e: Exception){
            android.util.Log.e("tag","ng",e)
            return null
        }
        return null
    }

    //LanguageTranslatorの処理
    fun languageTranslator(judgeText : String) : String?{
        try{
            var tloptions : TranslateOptions = TranslateOptions.Builder()
                .addText(judgeText)
                .modelId("en-ja")
                .build()
            var ltResult : TranslationResult = ltService.translate(tloptions).execute().getResult()
            System.out.println(ltResult)
            val ltResultJson = JSONObject(ltResult.toString())
            var transitionData = ltResultJson.getJSONArray("translations").getJSONObject(0).getString("translation")
            return transitionData
        } catch(e: Exception){
            android.util.Log.e("tag","ng",e)
            return null
        }
        return null
    }
}