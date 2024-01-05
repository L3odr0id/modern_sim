import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'modern_sim_platform_interface.dart';

/// An implementation of [ModernSimPlatform] that uses method channels.
class MethodChannelModernSim extends ModernSimPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('modern_sim');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> getSimInfo() async {
    final simInfo = await methodChannel.invokeMethod<String>('getSimInfo');
    return simInfo;
  }

  @override
  Future<String> sendSMS({
    required String message,
    required String phone,
    int? subId,
    int? externalId,
    int? localId,
  }) {
    final mapData = <dynamic, dynamic>{};
    mapData['message'] = message;
    mapData['phone'] = phone;
    mapData['subId'] = subId;
    mapData['externalId'] = externalId;
    mapData['localId'] = localId;

    return methodChannel
        .invokeMethod<String>('sendSMS', mapData)
        .then((value) => value ?? 'Error sending sms');
  }

  @override
  void setMethodHandler(Future<void> Function(MethodCall call) handler) {
    methodChannel.setMethodCallHandler(handler);
    // Use call.arguments and call.method to get info
  }
}
