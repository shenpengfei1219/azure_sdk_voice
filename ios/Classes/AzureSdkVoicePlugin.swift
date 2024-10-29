import Flutter
import UIKit

public class AzureSdkVoicePlugin: NSObject, FlutterPlugin {
    static var gChannel: FlutterMethodChannel?;
    
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "azure_sdk_voice", binaryMessenger: registrar.messenger())
      gChannel = channel
    let instance = AzureSdkVoicePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  var _audioRecorder: AudioRecorder = AudioRecorder();
  var _azureSdk: AzureSdk = AzureSdk();

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "init":
      guard let arguments = call.arguments as? [String: Any],
            let keyTmp = arguments["key"] as? String,
            let regionTmp = arguments["region"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
        _azureSdk.initKeyRegion(key: keyTmp, region: regionTmp)
      result("OK")
    case "startRecording":
      guard let arguments = call.arguments as? [String: Any],
            let nameTmp = arguments["name"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
      _audioRecorder.startRecording(fileName: nameTmp);
      result("OK")
    case "stopRecording":
      _audioRecorder.stopRecording();
      result("OK")
    case "playRecordedAudio":
      guard let arguments = call.arguments as? [String: Any],
            let nameTmp = arguments["name"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
      _audioRecorder.playRecordedAudio(recordedFileName: nameTmp)
      result("OK")
    case "pronunciationScore":
      guard let arguments = call.arguments as? [String: Any],
            let languageTmp = arguments["language"] as? String,
            let topicTmp = arguments["topic"] as? String,
            let nameTmp = arguments["name"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
        _azureSdk.pronunciationAssessmentWithContentAssessment(fileName: nameTmp,language: languageTmp,topic: topicTmp) {res in
        result(res)
      }
    case "translate":
      guard let arguments = call.arguments as? [String: Any],
            let recLanguageTmp = arguments["recLanguage"] as? String,
            let toLanguageTmp = arguments["toLanguage"] as? String,
            let nameTmp = arguments["name"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
        _azureSdk.translateWav(fileName: nameTmp,recLanguage: recLanguageTmp,toLanguage: toLanguageTmp) {res in
        result(res)
      }
    case "startTranslateContinuous":
        guard let arguments = call.arguments as? [String: Any],
              let recLanguageTmp = arguments["recLanguage"] as? String,
              let toLanguageTmp = arguments["toLanguage"] as? String,
              let code = arguments["code"] as? Int else {
            result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
            return
        }
        _azureSdk.startTranslateContinuous(recLanguage: recLanguageTmp,toLanguage: toLanguageTmp) {res,is_last in
            DispatchQueue.global(qos: .userInitiated).async {
                let arg2:[String:Any] = ["code":code,"res": res!,"is_last":is_last!]
                AzureSdkVoicePlugin.gChannel?.invokeMethod("callback", arguments: arg2)
            }
        }
        result("OK")
    case "stopTranslateContinuous":
        _azureSdk.stopTranslateContinuous()
        result("OK")
    case "speak":
      guard let arguments = call.arguments as? [String: Any],
            let code = arguments["code"] as? Int,
            let contentTmp = arguments["content"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
        _azureSdk.stopSynthesisToSpeaker()
        DispatchQueue.global(qos: .userInitiated).async {
            let arg2:[String:Any] = ["code":code,"res": "start","is_last":false]
            AzureSdkVoicePlugin.gChannel?.invokeMethod("callback", arguments: arg2)
        
        
            self._azureSdk.synthesisToSpeaker(inputText: contentTmp)
        

            let arg3:[String:Any] = ["code":code,"res": "stop","is_last":true]
            AzureSdkVoicePlugin.gChannel?.invokeMethod("callback", arguments: arg3)
        }
        
        
        result("OK")
    case "speak_stop":
        _azureSdk.stopSynthesisToSpeaker()
        result("OK")
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
