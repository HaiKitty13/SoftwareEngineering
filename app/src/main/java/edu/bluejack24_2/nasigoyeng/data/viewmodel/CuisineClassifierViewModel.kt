package edu.bluejack24_2.nasigoyeng.data.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.jvm.Throws

import org.tensorflow.lite.flex.FlexDelegate

class CuisineClassifierViewModel(application: Application): AndroidViewModel(application) {

    companion object {
        private const val TAG = "CuisineViewModel"
        private const val MODEL_FILE = "cuisine_model.tflite"
        private const val CONFIG_FILE = "model_config.json"
    }

    sealed class UiState {
        object Loading : UiState()
        object Ready : UiState()
        object Predicting : UiState()
        data class Error(val message: String) : UiState()
    }

    data class PredictionResult(
        val predictions: List<CuisinePrediction>,
        val processingTime: Long
    )

    data class CuisinePrediction(
        val cuisine: String,
        val confidence: Float,
        val rank: Int
    )

    // Private properties
    private var tflite: Interpreter? = null
    private var wordIndex: Map<String, Int> = emptyMap()
    private var cuisineClasses: List<String> = emptyList()
    private var maxSequenceLength: Int = 0
    private var vocabSize: Int = 0

    // LiveData for UI
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val _predictionResult = MutableLiveData<PredictionResult>()
    val predictionResult: LiveData<PredictionResult> = _predictionResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init{
        initializeModel()
    }

    private fun initializeModel(){
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                withContext(Dispatchers.IO){
                    loadModel()
                    loadConfiguration()
                }

                _uiState.value = UiState.Ready
                Log.d(TAG, "Model initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing model", e)
                _uiState.value = UiState.Error("Failed to load model: ${e.message}")
                _errorMessage.value = "Error loading model: ${e.message}"
            }
        }
    }

    fun predictCuisine (ingredients: String){
        if(ingredients.isBlank()){
            _errorMessage.value = "Please enter ingredients"
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Predicting

                val result = withContext(Dispatchers.Default){
                    performPrediction(ingredients)
                }

                _predictionResult.value = result
                _uiState.value = UiState.Ready
            } catch (e:Exception){
                Log.e(TAG, "Error during prediction", e)
                _uiState.value = UiState.Error("Prediction failed: ${e.message}")
                _errorMessage.value = "Error during prediction: ${e.message}"
            }
        }
    }

    private suspend fun performPrediction(ingredients: String): PredictionResult{
        val startTime = System.currentTimeMillis()

        val sequence = preprocessText(ingredients)

        val input = Array(1) {FloatArray(maxSequenceLength)}
        sequence.forEachIndexed{index, value ->
            if (index < maxSequenceLength){
                input[0][index] = value.toFloat()
            }
        }

        val output = Array(1) { FloatArray(cuisineClasses.size)}

        tflite?.run(input, output)

        val processingTime = System.currentTimeMillis() - startTime

        val predictions = output[0]
            .mapIndexed { index, confidence ->
                CuisinePrediction(
                    cuisine = cuisineClasses[index],
                    confidence = confidence,
                    rank = 0
                )
            }
            .sortedByDescending { it.confidence }
            .mapIndexed { index, cuisinePrediction ->
                cuisinePrediction.copy(rank = index + 1)
            }

        return PredictionResult(predictions, processingTime)
    }

    @Throws(IOException::class)
    private fun loadModel(){
        val tfliteModel = loadModelFile(MODEL_FILE)
//        val delegate =
        val options = Interpreter.Options().apply {
            numThreads = 4
            addDelegate(FlexDelegate())

//            try {
//                addDelegate(GpuDelegate())
//            } catch (e: Exception) {
//                Log.w(TAG, "GPU delegate not supported or failed to initialize: ${e.message}")
//            }
//
//            try {
//                addDelegate(NnApiDelegate())
//            } catch (e: Exception) {
//                Log.w(TAG, "NNAPI delegate not supported or failed to initialize: ${e.message}")
//            }
        }
        tflite = Interpreter(tfliteModel, options)
    }

    @Throws(IOException::class)
    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = getApplication<Application>().assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class, JSONException::class)
    private fun loadConfiguration(){
        val inputStream = getApplication<Application>().assets.open(CONFIG_FILE)
        val json = inputStream.bufferedReader().use {it.readText()}
        val config = JSONObject(json)

        // load tokenizer
        val tokenizerConfig = config.getJSONObject("tokenizer")
        val wordIndexJson = tokenizerConfig.getJSONObject("word_index")
        wordIndex = buildMap {
            wordIndexJson.keys().forEach { key ->
                put(key, wordIndexJson.getInt(key))
            }
        }
    }

    private fun preprocessText(text: String): IntArray {
        val words = text.lowercase().split("\\s+".toRegex())
        val sequence = words.mapNotNull { word ->
            val cleanWord = word.replace("[^a-zA-Z0-9]".toRegex(), "")
            wordIndex[cleanWord]?.takeIf { it < vocabSize }
        }
        return IntArray(maxSequenceLength){ index ->
            if (index < sequence.size) sequence[index] else 0
        }
    }

    fun cleanError(){
        _errorMessage.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        tflite?.close()
    }


}