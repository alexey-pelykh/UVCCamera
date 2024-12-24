-keep class com.serenegiant.usb.UVCCamera {
    native <methods>;
}

-keepclassmembers class com.serenegiant.usb.UVCCamera {
    protected long mNativePtr;

    protected int mScanningModeMin;
    protected int mScanningModeMax;
    protected int mScanningModeDef;

    protected int mExposureModeMin;
    protected int mExposureModeMax;
    protected int mExposureModeDef;

    protected int mExposurePriorityMin;
    protected int mExposurePriorityMax;
    protected int mExposurePriorityDef;

    protected int mExposureMin;
    protected int mExposureMax;
    protected int mExposureDef;

    protected int mAutoFocusMin;
    protected int mAutoFocusMax;
    protected int mAutoFocusDef;

    protected int mFocusMin;
    protected int mFocusMax;
    protected int mFocusDef;

    protected int mFocusRelMin;
    protected int mFocusRelMax;
    protected int mFocusRelDef;

    protected int mFocusSimpleMin;
    protected int mFocusSimpleMax;
    protected int mFocusSimpleDef;

    protected int mIrisMin;
    protected int mIrisMax;
    protected int mIrisDef;

    protected int mIrisRelMin;
    protected int mIrisRelMax;
    protected int mIrisRelDef;

    protected int mPanMin;
    protected int mPanMax;
    protected int mPanDef;

    protected int mTiltMin;
    protected int mTiltMax;
    protected int mTiltDef;

    protected int mRollMin;
    protected int mRollMax;
    protected int mRollDef;

    protected int mPanRelMin;
    protected int mPanRelMax;
    protected int mPanRelDef;

    protected int mTiltRelMin;
    protected int mTiltRelMax;
    protected int mTiltRelDef;

    protected int mRollRelMin;
    protected int mRollRelMax;
    protected int mRollRelDef;

    protected int mPrivacyMin;
    protected int mPrivacyMax;
    protected int mPrivacyDef;

    protected int mAutoWhiteBlanceMin;
    protected int mAutoWhiteBlanceMax;
    protected int mAutoWhiteBlanceDef;

    protected int mAutoWhiteBlanceCompoMin;
    protected int mAutoWhiteBlanceCompoMax;
    protected int mAutoWhiteBlanceCompoDef;

    protected int mWhiteBlanceMin;
    protected int mWhiteBlanceMax;
    protected int mWhiteBlanceDef;

    protected int mWhiteBlanceCompoMin;
    protected int mWhiteBlanceCompoMax;
    protected int mWhiteBlanceCompoDef;

    protected int mWhiteBlanceRelMin;
    protected int mWhiteBlanceRelMax;
    protected int mWhiteBlanceRelDef;

    protected int mBacklightCompMin;
    protected int mBacklightCompMax;
    protected int mBacklightCompDef;

    protected int mBrightnessMin;
    protected int mBrightnessMax;
    protected int mBrightnessDef;

    protected int mContrastMin;
    protected int mContrastMax;
    protected int mContrastDef;

    protected int mSharpnessMin;
    protected int mSharpnessMax;
    protected int mSharpnessDef;

    protected int mGainMin;
    protected int mGainMax;
    protected int mGainDef;

    protected int mGammaMin;
    protected int mGammaMax;
    protected int mGammaDef;

    protected int mSaturationMin;
    protected int mSaturationMax;
    protected int mSaturationDef;

    protected int mHueMin;
    protected int mHueMax;
    protected int mHueDef;

    protected int mZoomMin;
    protected int mZoomMax;
    protected int mZoomDef;

    protected int mZoomRelMin;
    protected int mZoomRelMax;
    protected int mZoomRelDef;

    protected int mPowerlineFrequencyMin;
    protected int mPowerlineFrequencyMax;
    protected int mPowerlineFrequencyDef;

    protected int mMultiplierMin;
    protected int mMultiplierMax;
    protected int mMultiplierDef;

    protected int mMultiplierLimitMin;
    protected int mMultiplierLimitMax;
    protected int mMultiplierLimitDef;

    protected int mAnalogVideoStandardMin;
    protected int mAnalogVideoStandardMax;
    protected int mAnalogVideoStandardDef;

    protected int mAnalogVideoLockStateMin;
    protected int mAnalogVideoLockStateMax;
    protected int mAnalogVideoLockStateDef;
}

-keep interface com.serenegiant.usb.IButtonCallback {
    <methods>;
}

-keep interface com.serenegiant.usb.IFrameCallback {
    <methods>;
}

-keep interface com.serenegiant.usb.IStatusCallback {
    <methods>;
}
