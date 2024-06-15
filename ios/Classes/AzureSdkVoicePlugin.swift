import Flutter
import UIKit

public class AzureSdkVoicePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "azure_sdk_voice", binaryMessenger: registrar.messenger())
    let instance = AzureSdkVoicePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  var _audioRecorder: AudioRecorder = AudioRecorder();

  var key: String! = ""
  var region: String! = ""

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "init":
      guard let arguments = call.arguments as? [String: Any],
            let keyTmp = arguments["key"] as? String,
            let regionTmp = arguments["region"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
      key = keyTmp;
      region = regionTmp;
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
      pronunciationAssessmentWithContentAssessment(key: key,region: region,fileName: nameTmp,language: languageTmp,topic: topicTmp) {res in
        result(res)
      }
    case "translate":
      guard let arguments = call.arguments as? [String: Any],
            let languageTmp = arguments["language"] as? String,
            let nameTmp = arguments["name"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }
      translateWav(key: key,region: region,fileName: nameTmp,language: languageTmp) {res in
        result(res)
      }
    case "speak":
      guard let arguments = call.arguments as? [String: Any],
            let contentTmp = arguments["content"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments", details: nil))
          return
      }

      DispatchQueue.global(qos: .userInitiated).async {
                  synthesisToSpeaker(key: self.key,region: self.region,inputText: contentTmp)
              }
      result("OK")
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
