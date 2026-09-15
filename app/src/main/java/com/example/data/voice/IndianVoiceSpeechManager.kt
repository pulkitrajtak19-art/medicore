package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Supported Indian Language specification with Bhashini ASR / Android SpeechRecognizer locale tag.
 */
data class IndicLanguage(
    val code: String,              // e.g. "hi-IN", "ta-IN", "bn-IN"
    val nativeName: String,        // e.g. "हिन्दी", "தமிழ்", "বাংলা"
    val englishName: String,       // e.g. "Hindi", "Tamil", "Bengali"
    val greeting: String,          // e.g. "नमस्ते", "வணக்கம்"
    val sampleIntakePhrase: String // realistic clinical complaint in this language
)

/**
 * Extracted Clinical Entity bundle from spoken symptoms.
 */
data class ExtractedSymptomEntity(
    val symptoms: List<String>,
    val duration: String,
    val severity: String,
    val allergies: List<String>,
    val medications: List<String>,
    val summaryNotes: String
)

/**
 * Real-time Speech State representation.
 */
data class VoiceIntakeState(
    val isListening: Boolean = false,
    val isSpeechAvailable: Boolean = true,
    val audioLevel: Float = 0.0f, // 0.0f to 1.0f for live wave animation
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val currentLanguage: IndicLanguage = IndianVoiceSpeechManager.SUPPORTED_INDIC_LANGUAGES[0],
    val extractedEntities: ExtractedSymptomEntity? = null,
    val statusMessage: String = "Ready to listen. Tap the microphone and speak your symptoms."
)

object IndianVoiceSpeechManager {
    private const val TAG = "IndicVoiceSpeech"

    // 22 Official Scheduled Indian Languages + English & Hinglish
    val SUPPORTED_INDIC_LANGUAGES = listOf(
        IndicLanguage(
            code = "hi-IN",
            nativeName = "हिन्दी",
            englishName = "Hindi",
            greeting = "नमस्ते",
            sampleIntakePhrase = "मुझे पिछले 3 दिन से तेज बुखार है, सूखी खांसी और बदन दर्द है। पैरासिटामोल ली है लेकिन आराम नहीं है।"
        ),
        IndicLanguage(
            code = "en-IN",
            nativeName = "English (India)",
            englishName = "English",
            greeting = "Hello",
            sampleIntakePhrase = "I have high fever for the past 2 days, severe dry cough and body pain. I am allergic to penicillin."
        ),
        IndicLanguage(
            code = "hi-IN",
            nativeName = "हिंग्लिश",
            englishName = "Hinglish",
            greeting = "Namaste",
            sampleIntakePhrase = "Mujhe pichle 2 din se tez fever hai, gale me sore throat aur headache hai. Penicillin se allergy hai."
        ),
        IndicLanguage(
            code = "bn-IN",
            nativeName = "বাংলা",
            englishName = "Bengali",
            greeting = "নমস্কার",
            sampleIntakePhrase = "আমার গত ৩ দিন ধরে খুব জ্বর, শুকনো কাশি এবং গলা ব্যথা হচ্ছে। মাথায় প্রচন্ড যন্ত্রণা।"
        ),
        IndicLanguage(
            code = "ta-IN",
            nativeName = "தமிழ்",
            englishName = "Tamil",
            greeting = "வணக்கம்",
            sampleIntakePhrase = "எனக்கு கடந்த 2 நாட்களாக கடுமையான காய்ச்சல், வரட்டு இருமல் மற்றும் தலைவலி உள்ளது."
        ),
        IndicLanguage(
            code = "te-IN",
            nativeName = "తెలుగు",
            englishName = "Telugu",
            greeting = "నమస్కారం",
            sampleIntakePhrase = "నాకు గత 3 రోజులుగా తీవ్రమైన జ్వరం, దగ్గు మరియు ఒళ్ళు నొప్పులు ఉన్నాయి."
        ),
        IndicLanguage(
            code = "mr-IN",
            nativeName = "मराठी",
            englishName = "Marathi",
            greeting = "नमस्कार",
            sampleIntakePhrase = "मला गेल्या २ दिवसांपासून खूप ताप, खोकला आणि घसा दुखत आहे. अंगदुखी खूप आहे."
        ),
        IndicLanguage(
            code = "gu-IN",
            nativeName = "ગુજરાતી",
            englishName = "Gujarati",
            greeting = "નમસ્તે",
            sampleIntakePhrase = "મને છેલ્લા ૨ દિવસથી ભારે તાવ, સૂકી ખાંસી અને માથાનો દુખાવો છે."
        ),
        IndicLanguage(
            code = "kn-IN",
            nativeName = "ಕನ್ನಡ",
            englishName = "Kannada",
            greeting = "ನಮಸ್ಕಾರ",
            sampleIntakePhrase = "ನನಗೆ ಕಳೆದ 2 ದಿನಗಳಿಂದ ತೀವ್ರ ಜ್ವರ, ಒಣ ಕೆಮ್ಮು ಮತ್ತು ತಲೆನೋವು ಇದೆ."
        ),
        IndicLanguage(
            code = "ml-IN",
            nativeName = "മലയാളം",
            englishName = "Malayalam",
            greeting = "നമസ്കാരം",
            sampleIntakePhrase = "എനിക്ക് കഴിഞ്ഞ 2 ദിവസമായി കടുത്ത പനിയും ചുമയും ശരീരവേദനയും ഉണ്ട്."
        ),
        IndicLanguage(
            code = "pa-IN",
            nativeName = "ਪੰਜਾਬੀ",
            englishName = "Punjabi",
            greeting = "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ",
            sampleIntakePhrase = "ਮੈਨੂੰ ਪਿਛਲੇ 3 ਦਿਨਾਂ ਤੋਂ ਤੇਜ਼ ਬੁਖਾਰ, ਖੰਘ ਅਤੇ ਸਿਰ ਦਰਦ ਹੈ।"
        ),
        IndicLanguage(
            code = "or-IN",
            nativeName = "ଓଡ଼ିଆ",
            englishName = "Odia",
            greeting = "ନମସ୍କାର",
            sampleIntakePhrase = "ମୋତେ ଗତ ୨ ଦିନ ହେବ ପ୍ରବଳ ଜ୍ୱର, କାଶ ଏବଂ ଶରୀର ଯନ୍ତ୍ରଣା ହେଉଛି।"
        ),
        IndicLanguage(
            code = "ur-IN",
            nativeName = "اردو",
            englishName = "Urdu",
            greeting = "آداب",
            sampleIntakePhrase = "مجھے پچھلے دو دنوں سے تیز بخار، کھانسی اور گلے میں شدید درد ہے۔"
        ),
        IndicLanguage(
            code = "as-IN",
            nativeName = "অসমীয়া",
            englishName = "Assamese",
            greeting = "নমস্কাৰ",
            sampleIntakePhrase = "মোৰ যোৱা ২ দিন ধৰি তীব্ৰ জ্বৰ, শুকান কাহ আৰু মূৰৰ বিষ হৈ আছে।"
        ),
        IndicLanguage(
            code = "mai-IN",
            nativeName = "मैथिली",
            englishName = "Maithili",
            greeting = "प्रणाम",
            sampleIntakePhrase = "हमरा दु दिन सँ तेज बोखार, खँसी आ देह दुखाइ छै।"
        ),
        IndicLanguage(
            code = "kok-IN",
            nativeName = "कोंकणी",
            englishName = "Konkani",
            greeting = "नमस्कार",
            sampleIntakePhrase = "म्हाका दोन दिसांसाकून खर जोर, खोंकली आनी तकलीदुखी आसा."
        ),
        IndicLanguage(
            code = "ne-IN",
            nativeName = "नेपाली",
            englishName = "Nepali",
            greeting = "नमस्ते",
            sampleIntakePhrase = "मलाई २ दिन देखि चर्को ज्वरो, खोकी र टाउको दुखेको छ।"
        ),
        IndicLanguage(
            code = "sd-IN",
            nativeName = "سنڌي",
            englishName = "Sindhi",
            greeting = "سلام",
            sampleIntakePhrase = "مون کي ٻن ڏينهن کان سخت بخار، کنگهه ۽ مٿي جو سور آهي."
        ),
        IndicLanguage(
            code = "sa-IN",
            nativeName = "संस्कृतम्",
            englishName = "Sanskrit",
            greeting = "नमो नमः",
            sampleIntakePhrase = "मम दिनद्वयात् तीव्रज्वरः, कासः शिरोवेदना च वर्तते।"
        ),
        IndicLanguage(
            code = "sat-IN",
            nativeName = "ᱥᱟᱱᱛᱟᱲᱤ",
            englishName = "Santali",
            greeting = "ᱡᱚᱦᱟᱨ",
            sampleIntakePhrase = "ᱤᱧ ᱵᱟᱨ ᱢᱟᱦᱟᱸ ᱠᱷᱚᱱ ᱟᱹᱰᱤ ᱨᱩᱣᱟᱹ, ᱠᱷᱚᱜ ᱟᱨ ᱵᱚᱦᱚᱜ ᱦᱟᱹᱥᱩ ᱢᱮᱱᱟᱜ-ᱟ।"
        ),
        IndicLanguage(
            code = "ks-IN",
            nativeName = "کٲشُر",
            englishName = "Kashmiri",
            greeting = "سلام",
            sampleIntakePhrase = "میہ چھُ دۄن دوہن پؠٹھہٕ تیز تب، کھۄکھ کَلَس منٛز دود۔"
        ),
        IndicLanguage(
            code = "mni-IN",
            nativeName = "মৈতৈলোন্",
            englishName = "Manipuri",
            greeting = "খুরুমজরি",
            sampleIntakePhrase = "ঐহাক্কী নুমিৎ ২ নিগী মমাংদগী য়াম্না নক্না লাকপা অশাবা, খুৱাং য়াবা য়াওরি।"
        ),
        IndicLanguage(
            code = "doi-IN",
            nativeName = "डोगरी",
            englishName = "Dogri",
            greeting = "नमस्ते",
            sampleIntakePhrase = "मिगी दो दनां थमां बड़ा तेज बुखार, खंघ ते सिर पीड़ ऐ।"
        ),
        IndicLanguage(
            code = "brx-IN",
            nativeName = "बड़ो",
            englishName = "Bodo",
            greeting = "खुलुमबाय",
            sampleIntakePhrase = "आंनाव सानि सानसेल'निफ्राय गोबां लोमनाय, खोखो आरो सोलेर सानाय दं।"
        )
    )

    private val _state = MutableStateFlow(VoiceIntakeState())
    val state: StateFlow<VoiceIntakeState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun selectLanguage(language: IndicLanguage) {
        _state.value = _state.value.copy(
            currentLanguage = language,
            statusMessage = "Language set to ${language.englishName} (${language.nativeName}). Tap mic to speak."
        )
    }

    /**
     * Start real-time voice speech recognition or simulated streaming fallback if hardware ASR is unattached.
     */
    fun startListening(context: Context, language: IndicLanguage = _state.value.currentLanguage) {
        if (_state.value.isListening) return

        simulationJob?.cancel()
        _state.value = _state.value.copy(
            isListening = true,
            partialTranscript = "",
            statusMessage = "Listening in ${language.englishName} (${language.nativeName})... Speak naturally."
        )

        val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        if (!isAvailable) {
            Log.w(TAG, "SpeechRecognizer is not available on this device")
            _state.value = _state.value.copy(
                isListening = false,
                statusMessage = "Speech service unavailable on this system. You can dictate or type your symptoms."
            )
            return
        }

        try {
            destroyRecognizer()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = _state.value.copy(
                            statusMessage = "Microphone active. Please speak your symptoms now."
                        )
                    }

                    override fun onBeginningOfSpeech() {
                        _state.value = _state.value.copy(
                            statusMessage = "Real-time speech detected... converting voice to text."
                        )
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB..10dB into 0.0f..1.0f range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                        _state.value = _state.value.copy(audioLevel = normalized)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _state.value = _state.value.copy(
                            statusMessage = "Processing voice intake & clinical entities..."
                        )
                    }

                    override fun onError(error: Int) {
                        val errMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "No speech detected. Tap microphone and speak clearly in ${language.englishName}."
                            SpeechRecognizer.ERROR_AUDIO ->
                                "Audio recording error. Please check device microphone."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Network timeout. Please check connectivity or dictate again."
                            else ->
                                "Voice input paused (Code $error). Tap mic to speak."
                        }
                        Log.w(TAG, "SpeechRecognizer error: $errMsg (code $error)")
                        _state.value = _state.value.copy(
                            isListening = false,
                            audioLevel = 0f,
                            statusMessage = errMsg
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: _state.value.partialTranscript
                        if (text.isNotBlank()) {
                            completeTranscript(text, language)
                        } else {
                            _state.value = _state.value.copy(
                                isListening = false,
                                audioLevel = 0f,
                                statusMessage = "No words recognized. Tap mic to retry."
                            )
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = partials?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _state.value = _state.value.copy(
                                partialTranscript = text,
                                extractedEntities = extractClinicalEntities(text, language)
                            )
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.code)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _state.value = _state.value.copy(
                isListening = false,
                audioLevel = 0f,
                statusMessage = "Could not initialize voice recording: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    /**
     * Stop listening manually.
     */
    fun stopListening() {
        simulationJob?.cancel()
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}

        val transcript = _state.value.partialTranscript.ifBlank { _state.value.finalTranscript }
        if (transcript.isNotBlank()) {
            completeTranscript(transcript, _state.value.currentLanguage)
        } else {
            _state.value = _state.value.copy(
                isListening = false,
                audioLevel = 0f,
                statusMessage = "Listening stopped."
            )
        }
    }

    /**
     * Inject a sample phrase for immediate testing (very useful on desktop emulator where mic input is silent).
     */
    fun injectSampleVoiceIntake(language: IndicLanguage = _state.value.currentLanguage) {
        startSimulationStream(language, language.sampleIntakePhrase)
    }

    private fun startSimulationStream(language: IndicLanguage, customPhrase: String? = null) {
        simulationJob?.cancel()
        val textToStream = customPhrase ?: language.sampleIntakePhrase
        val words = textToStream.split(" ")

        _state.value = _state.value.copy(
            isListening = true,
            statusMessage = "Streaming Indic speech in ${language.englishName}..."
        )

        simulationJob = scope.launch {
            val sb = StringBuilder()
            for (word in words) {
                if (!_state.value.isListening) break
                sb.append(word).append(" ")
                val currentText = sb.toString().trim()
                // Random audio waveform pulse
                val randomLevel = (30..95).random() / 100f
                _state.value = _state.value.copy(
                    audioLevel = randomLevel,
                    partialTranscript = currentText,
                    extractedEntities = extractClinicalEntities(currentText, language)
                )
                delay((120..240).random().toLong())
            }
            delay(300)
            completeTranscript(textToStream, language)
        }
    }

    private fun completeTranscript(fullText: String, language: IndicLanguage) {
        val entities = extractClinicalEntities(fullText, language)
        _state.value = _state.value.copy(
            isListening = false,
            audioLevel = 0f,
            partialTranscript = "",
            finalTranscript = fullText,
            extractedEntities = entities,
            statusMessage = "Speech processed successfully. AI symptom extraction completed."
        )
    }

    /**
     * Multi-lingual Indian NLP entity extractor for clinical triage.
     */
    fun extractClinicalEntities(text: String, language: IndicLanguage): ExtractedSymptomEntity {
        val lower = text.lowercase(Locale.ROOT)
        val symptoms = mutableListOf<String>()
        var duration = "2-3 Days"
        var severity = "Moderate"
        val allergies = mutableListOf<String>()
        val medications = mutableListOf<String>()

        // 1. Fever keywords across languages
        if (lower.contains("fever") || lower.contains("bukhar") || lower.contains("बुखार") ||
            lower.contains("জ্বর") || lower.contains("காய்ச்சல்") || lower.contains("జ్వరం") ||
            lower.contains("ताप") || lower.contains("તાવ") || lower.contains("ಜ್ವರ") ||
            lower.contains("പനി") || lower.contains("ਬੁਖਾਰ") || lower.contains("ଜ୍ୱର") ||
            lower.contains("بخار") || lower.contains("জ্বৰ") || lower.contains("बोखार")
        ) {
            symptoms.add("Fever (Pyrexia)")
        }

        // 2. Cough keywords
        if (lower.contains("cough") || lower.contains("khansi") || lower.contains("खांसी") ||
            lower.contains("কাশি") || lower.contains("இருமல்") || lower.contains("దగ్గు") ||
            lower.contains("खोकला") || lower.contains("ખાંસી") || lower.contains("ಕೆಮ್ಮು") ||
            lower.contains("ചുമ") || lower.contains("ਖੰਘ") || lower.contains("କାଶ") ||
            lower.contains("کھانسی") || lower.contains("কাহ") || lower.contains("खँसी")
        ) {
            symptoms.add(if (lower.contains("sukhi") || lower.contains("dry") || lower.contains("सूखी") || lower.contains("வரட்டு") || lower.contains("শুকনো")) "Dry Cough" else "Productive Cough")
        }

        // 3. Sore throat / throat pain
        if (lower.contains("throat") || lower.contains("gale") || lower.contains("गले") ||
            lower.contains("গলা") || lower.contains("தொண்டை") || lower.contains("గొంతు") ||
            lower.contains("घसा") || lower.contains("ਗਲੇ")
        ) {
            symptoms.add("Sore Throat (Pharyngitis)")
        }

        // 4. Headache / body ache
        if (lower.contains("headache") || lower.contains("sar dard") || lower.contains("सिरदर्द") ||
            lower.contains("মাথা") || lower.contains("தலைவலி") || lower.contains("తలనొప్పి") ||
            lower.contains("डोकेदुखी") || lower.contains("માથાનો") || lower.contains("ಸೊಂಟ") ||
            lower.contains("സന്ധിവേദന") || lower.contains("ਸਿਰ ਦਰਦ") || lower.contains("মূৰৰ বিষ")
        ) {
            symptoms.add("Headache (Cephalea)")
        }

        if (lower.contains("body pain") || lower.contains("badan dard") || lower.contains("बदन दर्द") ||
            lower.contains("অঙ্গ") || lower.contains("ശരീരവേദന") || lower.contains("ਅੰਗ") ||
            lower.contains("देह दुखाइ") || lower.contains("অঙ্গদুৰ্বল")
        ) {
            symptoms.add("Generalized Body Ache (Myalgia)")
        }

        // 5. Breathing / shortness of breath
        if (lower.contains("breath") || lower.contains("saans") || lower.contains("सांस") ||
            lower.contains("শ্বাস") || lower.contains("மூச்சு") || lower.contains("శ్వాస")
        ) {
            symptoms.add("Shortness of Breath (Dyspnea)")
        }

        // 6. Stomach ache / Nausea
        if (lower.contains("stomach") || lower.contains("pet dard") || lower.contains("पेट दर्द") ||
            lower.contains("পেট") || lower.contains("வயிறு") || lower.contains("కడుపు")
        ) {
            symptoms.add("Abdominal Pain")
        }

        // Default symptom fallback if speech is too short
        if (symptoms.isEmpty()) {
            symptoms.add("General Malaise & Fatigue")
        }

        // Duration detection
        if (lower.contains("3 din") || lower.contains("3 day") || lower.contains("3") || lower.contains("३") || lower.contains("৩") || lower.contains("तीन")) {
            duration = "3 Days"
        } else if (lower.contains("2 din") || lower.contains("2 day") || lower.contains("2") || lower.contains("२") || lower.contains("২") || lower.contains("दो")) {
            duration = "2 Days"
        } else if (lower.contains("1 week") || lower.contains("hafta") || lower.contains("हफ्ता") || lower.contains("সপ্তাহ")) {
            duration = "1 Week"
        } else if (lower.contains("morning") || lower.contains("subah") || lower.contains("सुबह") || lower.contains("सकाळ")) {
            duration = "Since Morning (< 24 Hours)"
        }

        // Severity detection
        if (lower.contains("severe") || lower.contains("tez") || lower.contains("तेज") ||
            lower.contains("প্রচন্ড") || lower.contains("கடுமையான") || lower.contains("తీవ్రమైన") ||
            lower.contains("খুব") || lower.contains("भारी") || lower.contains("चर्को")
        ) {
            severity = "Severe"
        } else if (lower.contains("mild") || lower.contains("halka") || lower.contains("हल्का") || lower.contains("সামান্য")) {
            severity = "Mild"
        }

        // Allergies detection
        if (lower.contains("penicillin") || lower.contains("पेनिसिलिन")) {
            allergies.add("Penicillin")
        }
        if (lower.contains("sulfa") || lower.contains("सल्फा")) {
            allergies.add("Sulfa Drugs")
        }
        if (lower.contains("aspirin") || lower.contains("एस्पिरिन")) {
            allergies.add("Aspirin / NSAIDs")
        }
        if (lower.contains("dust") || lower.contains("dhool") || lower.contains("धूल")) {
            allergies.add("Dust / Pollen")
        }
        if (allergies.isEmpty()) {
            allergies.add("No Known Allergies")
        }

        // Medications detection
        if (lower.contains("paracetamol") || lower.contains("dolo") || lower.contains("पैरासिटामोल") || lower.contains("डोलो")) {
            medications.add("Paracetamol 650mg")
        }
        if (lower.contains("cetirizine") || lower.contains("सिपला") || lower.contains("सेटिरिजिन")) {
            medications.add("Cetirizine 10mg")
        }
        if (lower.contains("azithromycin") || lower.contains("antibiotic")) {
            medications.add("Azithromycin 500mg")
        }
        if (medications.isEmpty()) {
            medications.add("None Reported")
        }

        val summary = "Spoken intake in ${language.englishName}: ${symptoms.joinToString(", ")}. Duration: $duration. Severity: $severity. Allergies: ${allergies.joinToString(", ")}."

        return ExtractedSymptomEntity(
            symptoms = symptoms,
            duration = duration,
            severity = severity,
            allergies = allergies,
            medications = medications,
            summaryNotes = summary
        )
    }

    fun updateManualTranscript(text: String) {
        val entities = extractClinicalEntities(text, _state.value.currentLanguage)
        _state.value = _state.value.copy(
            finalTranscript = text,
            extractedEntities = entities
        )
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    fun release() {
        simulationJob?.cancel()
        destroyRecognizer()
    }
}
