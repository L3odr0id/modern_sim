import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'modern_sim_method_channel.dart';

abstract class ModernSimPlatform extends PlatformInterface {
  /// Constructs a ModernSimPlatform.
  ModernSimPlatform() : super(token: _token);

  static final Object _token = Object();

  static ModernSimPlatform _instance = MethodChannelModernSim();

  /// The default instance of [ModernSimPlatform] to use.
  ///
  /// Defaults to [MethodChannelModernSim].
  static ModernSimPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ModernSimPlatform] when
  /// they register themselves.
  static set instance(ModernSimPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  void setMethodHandler(Future<void> Function(MethodCall call) handler) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getSimInfo() {
    throw UnimplementedError('getSimInfo() has not been implemented.');
  }

  Future<String> sendSMS({
    required String message,
    required String phone,
    int? subId,
    int? externalId,
    required int localId,
  }) {
    throw UnimplementedError('sendSMS() has not been implemented.');
  }
}
