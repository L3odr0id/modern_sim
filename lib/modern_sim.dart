import 'modern_sim_platform_interface.dart';

class ModernSim {
  Future<String?> getPlatformVersion() {
    return ModernSimPlatform.instance.getPlatformVersion();
  }

  Future<String?> getSimInfo() {
    return ModernSimPlatform.instance.getSimInfo();
  }

  Future<String> sendSMS({
    required String message,
    required String phone,
    int? subId,
    int? externalId,
    int? localId,
  }) {
    return ModernSimPlatform.instance.sendSMS(
      message: message,
      phone: phone,
      subId: subId,
      externalId: externalId,
      localId: localId,
    );
  }
}
