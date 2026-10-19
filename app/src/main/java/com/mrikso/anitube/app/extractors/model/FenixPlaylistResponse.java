package com.mrikso.anitube.app.extractors.model;

public class FenixPlaylistResponse {
    private Episode[] episodes;

    public Episode[] getEpisodes() {
        return episodes;
    }

    public String getFirstMasterUrl() {
        if (episodes != null && episodes.length > 0) {
            if (episodes[0].hls_master_url != null) {
                return episodes[0].hls_master_url;
            }
            if (episodes[0].tracks != null && episodes[0].tracks.length > 0) {
                return episodes[0].tracks[0].hls_url;
            }
        }
        return null;
    }

    public String getFirstSubtitleUrl() {
        if (episodes != null && episodes.length > 0 && episodes[0].subtitles != null) {
            for (Subtitle sub : episodes[0].subtitles) {
                if ("uk".equals(sub.language) && ("full".equals(sub.kind) || sub.is_default)) {
                    return sub.url;
                }
            }
            if (episodes[0].subtitles.length > 0) {
                return episodes[0].subtitles[0].url;
            }
        }
        return null;
    }


    public static class Episode {
        private String hls_master_url;
        private Track[] tracks;
        private Subtitle[] subtitles;

        public String getHls_master_url() {
            return hls_master_url;
        }

        public Track[] getTracks() {
            return tracks;
        }

        public Subtitle[] getSubtitles() {
            return subtitles;
        }
    }

    public static class Track {
        private String hls_url;

        public String getHls_url() {
            return hls_url;
        }
    }

    public static class Subtitle {
        private String language;
        private String kind;
        private String url;
        private boolean is_default;

        public String getUrl() {
            return url;
        }
    }

    public static class FenixMediaAccess {
        private String origin;
        private String token;

        public String getOrigin() {
            return origin;
        }

        public String getToken() {
            return token;
        }
    }

}