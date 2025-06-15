package io.github.ricciow.format;

import java.util.List;

public class ChatFormat {
    String version;
    List<FormatRule> formats;

    public List<FormatRule> getFormats() {
        return formats;
    }

    public String getVersion() {
        return version;
    }
}