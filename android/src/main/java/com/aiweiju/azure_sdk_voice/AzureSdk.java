package com.aiweiju.azure_sdk_voice;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognitionResult;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AzureSdk {
    static final AzureSdk instance = new AzureSdk();

    SpeechSynthesizer speechSynthesizer = null;
    TranslationRecognizer continuousTranslationRecognizer = null;

    public void synthesisToSpeaker(String speechKey,String speechRegion,String inputText,Callback callback) throws InterruptedException, ExecutionException {
        stopSynthesisToSpeaker();
        while (true) {
            if (speechSynthesizer == null) {
                break;
            }
            Thread.sleep(100);
        }
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);

        speechSynthesizer = new SpeechSynthesizer(speechConfig);

        Handler handler = new Handler(Looper.getMainLooper());

        Thread synthesisThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SpeechSynthesisResult speechSynthesisResult = null;
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 在这里更新播放状态
                            callback.onCallback("start",false);
                        }
                    });

                    speechSynthesisResult = speechSynthesizer.SpeakSsmlAsync(inputText).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
                    Log.d("flutter_spf", "Speech synthesized to speaker for text [" + inputText + "]");
                }
                else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
                    SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
                    Log.d("flutter_spf", "CANCELED: Reason=" + cancellation.getReason());

                    if (cancellation.getReason() == CancellationReason.Error) {
                        Log.d("flutter_spf", "CANCELED: ErrorCode=" + cancellation.getErrorCode());
                        Log.d("flutter_spf", "CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                        Log.d("flutter_spf", "CANCELED: Did you set the speech resource key and region values?");
                    }
                }
                Log.d("flutter_spf", "speak thread over");
                speechSynthesizer.close();
                speechConfig.close();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里更新播放状态
                        callback.onCallback("stop",true);
                    }
                });
                speechSynthesizer = null;

            }
        });
        synthesisThread.start();
    }

    public void stopSynthesisToSpeaker() throws InterruptedException {
        if (speechSynthesizer != null) {
            speechSynthesizer.StopSpeakingAsync();
        }
    }

    public String translateWav(Context context,String speechKey,String speechRegion,String fileName,String recognitionLanguage,String toLanguage) throws InterruptedException, ExecutionException {
        SpeechTranslationConfig speechTranslationConfig = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion);
//        "en-US"
        speechTranslationConfig.setSpeechRecognitionLanguage(recognitionLanguage);

//        String[] toLanguages = { "it" };
//        for (String language : toLanguages) {
            speechTranslationConfig.addTargetLanguage(toLanguage);
//        }
//        AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        File externalStorageDir = context.getExternalCacheDir();

        if (externalStorageDir == null) {
            return "";
        }
        String filePath = externalStorageDir.getAbsolutePath() + "/" + fileName + ".wav";
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(filePath);
        TranslationRecognizer translationRecognizer = new TranslationRecognizer(speechTranslationConfig, audioConfig);

        Future<TranslationRecognitionResult> task = translationRecognizer.recognizeOnceAsync();
        TranslationRecognitionResult translationRecognitionResult = task.get();

        if (translationRecognitionResult.getReason() == ResultReason.TranslatedSpeech) {
            System.out.println("RECOGNIZED: Text=" + translationRecognitionResult.getText());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("rec", translationRecognitionResult.getText());
                jsonObject.put("trans", translationRecognitionResult.getTranslations().get(toLanguage));
                String jsonData = jsonObject.toString();
                return jsonData;
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            return translationRecognitionResult.getTranslations().get(toLanguage);
//            for (Map.Entry<String, String> pair : translationRecognitionResult.getTranslations().entrySet()) {
//                System.out.printf("Translated into '%s': %s\n", pair.getKey(), pair.getValue());
//            }
        }
        else if (translationRecognitionResult.getReason() == ResultReason.NoMatch) {
            System.out.println("NOMATCH: Speech could not be recognized.");
        }
        else if (translationRecognitionResult.getReason() == ResultReason.Canceled) {
            CancellationDetails cancellation = CancellationDetails.fromResult(translationRecognitionResult);
            System.out.println("CANCELED: Reason=" + cancellation.getReason());

            if (cancellation.getReason() == CancellationReason.Error) {
                System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                System.out.println("CANCELED: Did you set the speech resource key and region values?");
            }
        }

        return "";

    }

    public void startTranslateContinuous(String speechKey,String speechRegion,String recognitionLanguage,String toLanguage,Callback callback) throws InterruptedException, ExecutionException {
        stopTranslateContinuous();
        // Replace the service (Azure) region with your own service region (e.g. "westus").
        String v2EndpointUrl = "wss://" + speechRegion + ".stt.speech.microsoft.com/speech/universal/v2";
        Log.d("flutter_spf"," recognitionLanguage = " + recognitionLanguage );
        // Creates an instance of a speech config with specified endpoint URL and subscription key. Replace with your own subscription key.
        SpeechTranslationConfig speechTranslationConfig = SpeechTranslationConfig.fromEndpoint(URI.create(v2EndpointUrl), speechKey);

        // Change the default from at-start language detection to continuous language detection, since the spoken language in the audio
        // may change.
        speechTranslationConfig.setProperty(PropertyId.SpeechServiceConnection_LanguageIdMode, "Continuous");

        speechTranslationConfig.setSpeechRecognitionLanguage(recognitionLanguage);
        speechTranslationConfig.addTargetLanguage(toLanguage);

        AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        continuousTranslationRecognizer = new TranslationRecognizer(speechTranslationConfig, audioConfig);

        continuousTranslationRecognizer.speechStartDetected.addEventListener((s, e) -> {
            Log.d("flutter_spf"," speechStartDetected");
        });
        continuousTranslationRecognizer.recognized.addEventListener((s, e) -> {
//            AutoDetectSourceLanguageResult autoDetectSourceLanguageResult = AutoDetectSourceLanguageResult.fromResult(e.getResult());
//            String language = autoDetectSourceLanguageResult.getLanguage();
            if (e.getResult().getReason() == ResultReason.TranslatedSpeech) {
                Log.d("flutter_spf"," RECOGNIZED: Text = " + e.getResult().getText());
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("rec", e.getResult().getText());
                    jsonObject.put("trans", e.getResult().getTranslations().get(toLanguage));
                    String jsonData = jsonObject.toString();
                    Log.d("flutter_spf"," fin: jsonData = " + jsonData);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCallback(jsonData,false);
                        }
                    });
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
//                Log.d("flutter_spf"," RECOGNIZED: Language = " + language);
            }
            else if (e.getResult().getReason() == ResultReason.NoMatch) {
                Log.d("flutter_spf"," NOMATCH: Speech could not be recognized.");
            } else {
                Log.d("flutter_spf"," wwwwwwwwwwwwwwwwwwwwwwwwwwwww"+e.getResult().getReason());
            }
        });

        continuousTranslationRecognizer.canceled.addEventListener((s, e) -> {
            Log.d("flutter_spf"," CANCELED: Reason = " + e.getReason());
            if (e.getReason() == CancellationReason.Error) {
                Log.d("flutter_spf"," CANCELED: ErrorCode = " + e.getErrorCode());
                Log.d("flutter_spf"," CANCELED: ErrorDetails = " + e.getErrorDetails());
                Log.d("flutter_spf"," CANCELED: Did you update the subscription info?");
            }
        });

        continuousTranslationRecognizer.sessionStarted.addEventListener((s, e) -> {
            Log.d("flutter_spf","\n Session started event.");

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCallback("start",false);
                }
            });
        });

        continuousTranslationRecognizer.sessionStopped.addEventListener((s, e) -> {
            Log.d("flutter_spf","\n Session stopped event.");

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCallback("stop",true);
                }
            });
        });

        // Starts continuous recognition and wait for processing to end
        Log.d("flutter_spf"," Recognizing from Microphone... please wait");
        continuousTranslationRecognizer.startContinuousRecognitionAsync().get();


//        Future<TranslationRecognitionResult> task = translationRecognizer.recognizeOnceAsync();
//        TranslationRecognitionResult translationRecognitionResult = task.get();
//
//        if (translationRecognitionResult.getReason() == ResultReason.TranslatedSpeech) {
//            System.out.println("RECOGNIZED: Text=" + translationRecognitionResult.getText());
//            try {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("rec", translationRecognitionResult.getText());
//                jsonObject.put("trans", translationRecognitionResult.getTranslations().get(toLanguage));
//                String jsonData = jsonObject.toString();
//                return jsonData;
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        else if (translationRecognitionResult.getReason() == ResultReason.NoMatch) {
//            System.out.println("NOMATCH: Speech could not be recognized.");
//        }
//        else if (translationRecognitionResult.getReason() == ResultReason.Canceled) {
//            CancellationDetails cancellation = CancellationDetails.fromResult(translationRecognitionResult);
//            System.out.println("CANCELED: Reason=" + cancellation.getReason());
//
//            if (cancellation.getReason() == CancellationReason.Error) {
//                System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
//                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
//                System.out.println("CANCELED: Did you set the speech resource key and region values?");
//            }
//        }

    }

    public void stopTranslateContinuous() {
        if (continuousTranslationRecognizer != null) {
            continuousTranslationRecognizer.stopContinuousRecognitionAsync();
        }
    }

    public String pronunciationAssessmentWithContentAssessment(Context context,String speechKey, String speechRegion, String fileName, String language, String topic) {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
        speechConfig.setSpeechRecognitionLanguage(language);
        PronunciationAssessmentConfig pronunciationConfig = new PronunciationAssessmentConfig("",
                PronunciationAssessmentGradingSystem.HundredMark, PronunciationAssessmentGranularity.Word, false);
        pronunciationConfig.enableProsodyAssessment();
        pronunciationConfig.enableContentAssessmentWithTopic(topic);

        File externalStorageDir = context.getExternalCacheDir();

        if (externalStorageDir == null) {
            return "";
        }
        String filePath = externalStorageDir.getAbsolutePath() + "/" + fileName + ".wav";
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(filePath);

        SpeechRecognizer speechRecognizer = new SpeechRecognizer(
                speechConfig,
                audioConfig);

        pronunciationConfig.applyTo(speechRecognizer);
        Future<SpeechRecognitionResult> future = speechRecognizer.recognizeOnceAsync();
        SpeechRecognitionResult speechRecognitionResult = null;
        try {
            speechRecognitionResult = future.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

// The pronunciation assessment result as a Speech SDK object
//        PronunciationAssessmentResult pronunciationAssessmentResult =
//                PronunciationAssessmentResult.fromResult(speechRecognitionResult);

// The pronunciation assessment result as a JSON string
        String pronunciationAssessmentResultJson = speechRecognitionResult.getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonResult);

        speechRecognizer.close();
        speechConfig.close();
        audioConfig.close();
        pronunciationConfig.close();
        speechRecognitionResult.close();

        return pronunciationAssessmentResultJson;
    }

}