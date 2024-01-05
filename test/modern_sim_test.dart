import 'package:flutter/src/services/message_codec.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:modern_sim/modern_sim.dart';
import 'package:modern_sim/modern_sim_platform_interface.dart';
import 'package:modern_sim/modern_sim_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockModernSimPlatform
    with MockPlatformInterfaceMixin
    implements ModernSimPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String?> getSimInfo() {
    // TODO: implement getSimInfo
    throw UnimplementedError();
  }

  @override
  void setMethodHandler(Future<void> Function(MethodCall call) handler) {
    // TODO: implement setMethodHandler
  }

  @override
  Future<String> sendSMS({
    required String message,
    required String phone,
    int? localId,
    int? subId,
    int? externalId,
  }) {
    // TODO: implement sendSMS
    throw UnimplementedError();
  }
}

void main() {
  final ModernSimPlatform initialPlatform = ModernSimPlatform.instance;

  test('$MethodChannelModernSim is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelModernSim>());
  });

  test('getPlatformVersion', () async {
    ModernSim modernSimPlugin = ModernSim();
    MockModernSimPlatform fakePlatform = MockModernSimPlatform();
    ModernSimPlatform.instance = fakePlatform;

    expect(await modernSimPlugin.getPlatformVersion(), '42');
  });
}
