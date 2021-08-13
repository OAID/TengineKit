package com.tenginekit.engine.core;

public class SdkConfig {
    public SdkConfig setSdkFunction(SdkFunction sdkFunction) {
        this.sdkFunction = sdkFunction;
        return this;
    }

    public SdkConfig setAllowReport(boolean allowReport) {
        this.allowReport = allowReport;
        return this;
    }

    public SdkFunction sdkFunction = SdkFunction.FACE;
    public boolean allowReport = true;


    public enum SdkFunction {
        FACE(1);
        public final int value;

        SdkFunction(int value) {
            this.value = value;
        }
    }
}
