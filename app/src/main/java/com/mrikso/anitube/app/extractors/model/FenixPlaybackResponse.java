package com.mrikso.anitube.app.extractors.model;

public class FenixPlaybackResponse {
    private Episode episode;
    private String token;

    public Episode getEpisode() {
        return episode;
    }

    public String getToken() {
        return token;
    }

    public static class Episode {
        private String hls_master_url;
        private Track[] tracks;
        private Subtitle[] subtitles;
        private String thumbnail_url;

        public String getHls_master_url() {
            return hls_master_url;
        }

        public Track[] getTracks() {
            return tracks;
        }

        public Subtitle[] getSubtitles() {
            return subtitles;
        }

        public String getThumbnail_url() {
            return thumbnail_url;
        }
    }

    public static class Track {
        private String hls_url;
        private String label;
        private String language;
        private String vod_url;

        public String getHls_url() {
            return hls_url;
        }

        public String getLabel() {
            return label;
        }

        public String getLanguage() {
            return language;
        }

        public String getVod_url() {
            return vod_url;
        }
    }

    public static class Subtitle {
        private String language;
        private String kind;
        private String url;
        private String styled_url;
        private boolean is_default;
        private String label;

        public String getUrl() {
            return url != null && !url.isEmpty() ? url : styled_url;
        }

        public String getLanguage() {
            return language;
        }

        public String getKind() {
            return kind;
        }

        public boolean isIs_default() {
            return is_default;
        }
    }
}
