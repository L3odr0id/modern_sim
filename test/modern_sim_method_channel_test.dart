import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:modern_sim/modern_sim_method_channel.dart';

void main() {
  MethodChannelModernSim platform = MethodChannelModernSim();
  const MethodChannel channel = MethodChannel('modern_sim');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
