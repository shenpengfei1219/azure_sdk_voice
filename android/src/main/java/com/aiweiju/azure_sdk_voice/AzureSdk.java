package com.aiweiju.azure_sdk_voice;

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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AzureSdk {
    static final AzureSdk instance = new AzureSdk();

    SpeechSynthesizer speechSynthesizer = null;

    public void synthesisToSpeaker(String speechKey,String speechRegion,String inputText,Callback callback) throws InterruptedException, ExecutionException {
        stopSynthesisToSpeaker();
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
                speechSynthesizer = null;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里更新播放状态
                        callback.onCallback("stop",true);
                    }
                });
            }
        });
        synthesisThread.start();
    }

    public void stopSynthesisToSpeaker() {
        if (speechSynthesizer != null) {
            speechSynthesizer.StopSpeakingAsync();
            speechSynthesizer = null;
        }

    }

    public String translateWav(String speechKey,String speechRegion,String fileName,String recognitionLanguage,String toLanguage) throws InterruptedException, ExecutionException {
        SpeechTranslationConfig speechTranslationConfig = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion);
//        "en-US"
        speechTranslationConfig.setSpeechRecognitionLanguage(recognitionLanguage);

//        String[] toLanguages = { "it" };
//        for (String language : toLanguages) {
            speechTranslationConfig.addTargetLanguage(toLanguage);
//        }
//        AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

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

    public String pronunciationAssessmentWithContentAssessment(String speechKey,String speechRegion,String fileName,String language,String topic) {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
        speechConfig.setSpeechRecognitionLanguage(language);
        PronunciationAssessmentConfig pronunciationConfig = new PronunciationAssessmentConfig("",
                PronunciationAssessmentGradingSystem.HundredMark, PronunciationAssessmentGranularity.Word, false);
        pronunciationConfig.enableProsodyAssessment();
        pronunciationConfig.enableContentAssessmentWithTopic(topic);

        File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

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
