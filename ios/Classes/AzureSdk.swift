import MicrosoftCognitiveServicesSpeech
import AVFoundation

public class AzureSdk {
    var key: String! = ""
    var region: String! = ""
    
    var synthesizer: SPXSpeechSynthesizer!;
    var translator: SPXTranslationRecognizer!;
    func initKeyRegion(key: String!,region: String!) {
        self.key = key
        self.region = region
    }
    func pronunciationAssessmentWithContentAssessment(fileName: String,language: String,topic: String,completion: @escaping (String?) -> Void) {
        // Creates an instance of a speech config with specified subscription key and service region.
        // Replace with your own subscription key and service region (e.g., "westus").
        let speechConfig = try! SPXSpeechConfiguration(subscription: key, region: region)

        print("key:",key!,"region:",region!)

        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0];
        let audioFilename = documentsPath.appendingPathComponent("\(fileName).wav");
        print("pronunciation assessment audio file path: ", audioFilename.path)

        let audioDataWithHeader = try! Data(contentsOf: audioFilename)
        let audioData = Array(audioDataWithHeader[46..<audioDataWithHeader.count])

        let startTime = Date()

        let audioFormat = SPXAudioStreamFormat(usingPCMWithSampleRate: 44100, bitsPerSample: 16, channels: 2)!
        guard let audioInputStream = SPXPushAudioInputStream(audioFormat: audioFormat) else {
            print("Error: Failed to create audio input stream.")
            return
        }

        guard let audioConfig = SPXAudioConfiguration(streamInput: audioInputStream) else {
            print("Error: audioConfig is Nil")
            return
        }

        let speechRecognizer = try! SPXSpeechRecognizer(speechConfiguration: speechConfig, language: language, audioConfiguration: audioConfig)

        let referenceText = ""
        let pronAssessmentConfig = try! SPXPronunciationAssessmentConfiguration("",
        gradingSystem: SPXPronunciationAssessmentGradingSystem.hundredMark,
    //     granularity: SPXPronunciationAssessmentGranularity.phoneme,
        granularity: SPXPronunciationAssessmentGranularity.word,
        enableMiscue: false)

        pronAssessmentConfig.enableProsodyAssessment()
        pronAssessmentConfig.enableContentAssessment(withTopic: topic)

        try! pronAssessmentConfig.apply(to: speechRecognizer)

        audioInputStream.write(Data(audioData))
        audioInputStream.write(Data())

        print("Analysising")
        // Handle the recognition result
        try! speechRecognizer.recognizeOnceAsync { result in
            guard let pronunciationResult = SPXPronunciationAssessmentResult(result) else {
                print("Error: pronunciationResult is Nil")
                return
            }
            print("generating result...")
            let pronunciationAssessmentResultJson = result.properties?.getPropertyBy(SPXPropertyId.speechServiceResponseJsonResult)
            let endTime = Date()
            let timeCost = endTime.timeIntervalSince(startTime) * 1000
            print("Time cost: \(timeCost)ms")
            completion(pronunciationAssessmentResultJson)
        }
    }

    struct TranslateResult: Codable {
        var rec: String
        var trans: String
    }

    func translateWav(fileName: String,recLanguage: String,toLanguage: String,completion: @escaping (String?) -> Void) {
        let speechTranslationConfiguration = try! SPXSpeechTranslationConfiguration(subscription: key, region: region)
        
        speechTranslationConfiguration.speechRecognitionLanguage = recLanguage;
        speechTranslationConfiguration.addTargetLanguage(toLanguage)

        print("key:",key!,"region:",region!)

        let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0];
        let audioFilename = documentsPath.appendingPathComponent("\(fileName).wav");
        print("translateWav audio file path: ", audioFilename.path)

        let audioConfig = SPXAudioConfiguration.init(wavFileInput: audioFilename.path)
        let translator = try! SPXTranslationRecognizer(speechTranslationConfiguration: speechTranslationConfiguration, audioConfiguration: audioConfig!)
        
        translator.addTargetLanguage(toLanguage)

        try! translator.recognizeOnceAsync { result in
            let translationDictionary = result.translations
            print("text: \(result.text ?? "no text") ")
            let dictKeys  = Array(translationDictionary.keys)
            print(dictKeys)
            let translationResult = translationDictionary[toLanguage] as? String
            print("translation result: \(translationResult ?? "(no result)")")
            
            let res = TranslateResult(rec: result.text ?? "", trans: translationResult ?? "")
            let encoder = JSONEncoder()
            
            do {
                let data = try encoder.encode(res)
                completion(String(data: data, encoding: .utf8)!) // {"name":"Alice","age":25}
            } catch {
                completion("error")
            }
        }
    }
    
    func startTranslateContinuous(recLanguage: String,toLanguage: String,completion: @escaping (String?,Bool?) -> Void) {
        let speechTranslationConfiguration = try! SPXSpeechTranslationConfiguration(subscription: key, region: region)
        
        speechTranslationConfiguration.speechRecognitionLanguage = recLanguage;
        speechTranslationConfiguration.addTargetLanguage(toLanguage)

        print("key:",key!,"region:",region!)

        let audioConfig = SPXAudioConfiguration()
        translator = try! SPXTranslationRecognizer(speechTranslationConfiguration: speechTranslationConfiguration, audioConfiguration: audioConfig)
        
        translator.addTargetLanguage(toLanguage)
        
        translator.addRecognizedEventHandler() {reco, evt in
            let translationDictionary = evt.result.translations
            print("text: \(evt.result.text ?? "no text") ")
            let dictKeys  = Array(translationDictionary.keys)
            print(dictKeys)
            let translationResult = translationDictionary[toLanguage] as? String
            print("translation result: \(translationResult ?? "(no result)")")
            
            let res = TranslateResult(rec: evt.result.text ?? "", trans: translationResult ?? "")
            let encoder = JSONEncoder()
            
            do {
                let data = try encoder.encode(res)
                completion(String(data: data, encoding: .utf8)!,false) // {"name":"Alice","age":25}
            } catch {
                completion("error",true)
            }
        }
        completion("start",false)
        try! translator.startContinuousRecognition()
    }
    
    func stopTranslateContinuous() {
        try! translator.stopContinuousRecognition()
    }

    func synthesisToSpeaker(inputText: String) {
        let audioSession = AVAudioSession.sharedInstance()
        print("inputText \(inputText)")
        var speechConfig: SPXSpeechConfiguration?
        do {
            try audioSession.setCategory(.playAndRecord, mode: .default, options: .defaultToSpeaker) // 设置为默认使用扬声器
            try audioSession.setActive(true)
            try speechConfig = SPXSpeechConfiguration(subscription: key, region: region)

        } catch {
            print("error \(error) happened")
            speechConfig = nil
        }

        synthesizer = try! SPXSpeechSynthesizer(speechConfig!)
        let result = try! synthesizer.speakSsml(inputText)
        if result.reason == SPXResultReason.canceled
        {
            let cancellationDetails = try! SPXSpeechSynthesisCancellationDetails(fromCanceledSynthesisResult: result)
            print("cancelled, error code: \(cancellationDetails.errorCode) detail: \(cancellationDetails.errorDetails!) ")
            print("Did you set the speech resource key and region values?");
            return
        }

        displayReason(reason: result.reason)
    }

    func displayReason(reason: SPXResultReason) {
        if reason == SPXResultReason.noMatch {        //Indicates speech could not be recognized. More details can be found in the SPXNoMatchDetails object.
            print("reason: noMatch")
        }
        if reason == SPXResultReason.canceled {    //Indicates that the recognition was canceled. More details can be found using the SPXCancellationDetails object.
            print("reason: canceled")
        }
        if reason == SPXResultReason.recognizingSpeech{    //Indicates the speech result contains hypothesis text as an intermediate result.
            print("reason: recognizingSpeech")
        }
        if reason == SPXResultReason.recognizedSpeech{    //Indicates the speech result contains final text that has been recognized. Speech recognition is now complete for this phrase.
            print("reason: recognizedSpeech")
        }
        if reason == SPXResultReason.recognizingIntent{    //Indicates the intent result contains hypothesis text as an intermediate result.
            print("reason: recognizingIntent")
        }
        if reason == SPXResultReason.recognizedIntent{    //Indicates the intent result contains final text and intent. Speech recognition and intent determination are now complete for this phrase.
            print("reason: recognizedIntent")
        }
        if reason == SPXResultReason.translatingSpeech{    //Indicates the translation result contains hypothesis text and translation(s) as an intermediate result.
            print("reason: translatingSpeech")
        }
        if reason == SPXResultReason.translatedSpeech{    //Indicates the translation result contains final text and corresponding translation(s). Speech recognition and translation are now complete for this phrase.
            print("reason: translatedSpeech")
        }
        if reason == SPXResultReason.synthesizingAudio{    //Indicates the synthesized audio result contains a non-zero amount of audio data
            print("reason: synthesizingAudio")
        }
        if reason == SPXResultReason.synthesizingAudioCompleted{    //Indicates the synthesized audio is now complete for this phrase.
            print("reason: synthesizingAudioCompleted")
        }
        if reason == SPXResultReason.recognizingKeyword{    //Indicates the speech result contains (unverified) keyword text.
            print("reason: recognizingKeyword")
        }
        if reason == SPXResultReason.recognizedKeyword{    //Indicates that keyword recognition completed recognizing the given keyword.
            print("reason: recognizedKeyword")
        }
        if reason == SPXResultReason.synthesizingAudioStarted{    //Indicates the speech synthesis is now started.
            print("reason: synthesizingAudioStarted")
        }
        if reason == SPXResultReason.voicesListRetrieved{    //Indicates the voices list has been retrieved successfully. Added in version 1.16.0
            print("reason: voicesListRetrieved")
        }
    //    return "unknown";
    }
    
    func stopSynthesisToSpeaker() {
        if synthesizer != nil {
            try! synthesizer.stopSpeaking()
        }

    }

}

