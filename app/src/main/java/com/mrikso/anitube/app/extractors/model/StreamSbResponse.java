package com.mrikso.anitube.app.extractors.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StreamSbResponse {
    @SerializedName("stream_data")
    private StreamData streamData;

    @SerializedName("status_code")
    private int statusCode;

    public StreamData getStreamData() {
        return streamData;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static class StreamData {

        @SerializedName("id")
        private int id;

        @SerializedName("file")
        private String file;

        @SerializedName("bitrate")
        private int bitrate;

        @SerializedName("title")
        private String title;

        @SerializedName("hash")
        private String hash;

        @SerializedName("backup")
        private String backup;

        @SerializedName("subs")
        private List<Subtitle> subs;

        public int getId() {
            return id;
        }

        public String getFile() {
            return file;
        }

        public int getBitrate() {
            return bitrate;
        }

        public String getHash() {
            return hash;
        }

        public String getBackup() {
            return backup;
        }

        public List<Subtitle> getSubs() {
            return subs;
        }
    }

    public static class Subtitle {

        @SerializedName("label")
        private String label;

        @SerializedName("file")
        private String file;

        public String getLabel() {
            return label;
        }

        public String getFile() {
            return file;
        }
    }
}
