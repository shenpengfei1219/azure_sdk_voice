package com.aiweiju.azure_sdk_voice;

import android.os.Environment;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognitionResult;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AzureSdk {
    public static void synthesisToSpeaker(String speechKey,String speechRegion,String inputText) throws InterruptedException, ExecutionException {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);

        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig);

        SpeechSynthesisResult speechSynthesisResult = speechSynthesizer.SpeakSsmlAsync(inputText).get();

        if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
            System.out.println("Speech synthesized to speaker for text [" + inputText + "]");
        }
        else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
            SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
            System.out.println("CANCELED: Reason=" + cancellation.getReason());

            if (cancellation.getReason() == CancellationReason.Error) {
                System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                System.out.println("CANCELED: Did you set the speech resource key and region values?");
            }
        }

    }

    public static String translateWav(String speechKey,String speechRegion,String fileName,String recognitionLanguage,String toLanguage) throws InterruptedException, ExecutionException {
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
            return translationRecognitionResult.getTranslations().get(toLanguage);
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

    public static String pronunciationAssessmentWithContentAssessment(String speechKey,String speechRegion,String fileName,String language,String topic) {
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
