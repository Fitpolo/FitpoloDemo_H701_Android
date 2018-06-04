package com.fitpolo.support.entity;


import com.fitpolo.support.MokoSupport;

public enum FirmwareEnum {
    H701_CC_WHITOUT_HEARTRATE("CC", 32, "CC_00_32.bin"),
    H701_CC("CC", 32, "CC_01_32.bin"),
    H701_EE("EE", 32, "EE_01_32.bin"),
    ;


    private String header;
    private int lastestVersion;
    private String firmwareName;

    FirmwareEnum(String header, int lastestVersion, String firmwareName) {
        this.header = header;
        this.lastestVersion = lastestVersion;
        this.firmwareName = firmwareName;
    }

    public String getHeader() {
        return header;
    }

    public int getLastestVersion() {
        return lastestVersion;
    }

    public String getFirmwareName() {
        return firmwareName;
    }

    public static FirmwareEnum fromHeader(String header) {
        for (FirmwareEnum firwmareEnum : FirmwareEnum.values()) {
            if (firwmareEnum.getHeader().equals(header)) {
                if ("CC".equals(header)) {
                    if (MokoSupport.showHeartRate) {
                        return H701_CC;
                    } else {
                        return H701_CC_WHITOUT_HEARTRATE;
                    }
                }
                return firwmareEnum;
            }
        }
        return null;
    }

    public static FirmwareEnum fromLastestVersion(int lastestVersion) {
        for (FirmwareEnum firwmareEnum : FirmwareEnum.values()) {
            if (firwmareEnum.getLastestVersion() == lastestVersion) {
                return firwmareEnum;
            }
        }
        return null;
    }

    public static FirmwareEnum fromFirmwareName(String fromFirmwareName) {
        for (FirmwareEnum firwmareEnum : FirmwareEnum.values()) {
            if (firwmareEnum.getFirmwareName().equals(fromFirmwareName)) {
                return firwmareEnum;
            }
        }
        return null;
    }
}
