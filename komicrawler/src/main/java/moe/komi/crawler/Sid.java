package moe.komi.crawler;

import com.google.gson.annotations.SerializedName;

public enum Sid {
    @SerializedName("komica")
    KOMICA("komica");

    private final String value;
    private Sid(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};