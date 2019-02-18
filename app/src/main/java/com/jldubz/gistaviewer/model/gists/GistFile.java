package com.jldubz.gistaviewer.model.gists;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.Keep;

@Keep
public class GistFile {
    /*
    "files": {
      "hello_world.rb": {
        "filename": "hello_world.rb",
        "type": "application/x-ruby",
        "language": "Ruby",
        "raw_url": "https://gist.githubusercontent.com/octocat/6cad326836d38bd3a7ae/raw/db9c55113504e46fa076e7df3a04ce592e2e86d8/hello_world.rb",
        "size": 167
      }
    }
    */

    private String filename;
    private String type;
    private String language;
    @SerializedName("raw_url")
    private String rawUrl;
    private int size;

    public GistFile() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
