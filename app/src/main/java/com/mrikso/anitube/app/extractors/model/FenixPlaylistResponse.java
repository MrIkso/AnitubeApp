package com.mrikso.anitube.app.extractors.model;

public class FenixPlaylistResponse {
    private String slug;
    private String default_edition;
    private Episode[] episodes;
    private Playback playback;

    public String getSlug() {
        return slug;
    }

    public String getDefault_edition() {
        return default_edition;
    }

    public Episode[] getEpisodes() {
        return episodes;
    }

    public Playback getPlayback() {
        return playback;
    }

    public String getFirstMasterUrl() {
        if (episodes != null && episodes.length > 0) {
            Episode ep = episodes[0];
            if (ep.hls_master_url != null && !ep.hls_master_url.isEmpty()) {
                return ep.hls_master_url;
            }
            if (ep.tracks != null && ep.tracks.length > 0 && ep.tracks[0].hls_url != null && !ep.tracks[0].hls_url.isEmpty()) {
                return ep.tracks[0].hls_url;
            }
            if (ep.thumbnail_url != null && !ep.thumbnail_url.isEmpty()) {
                String thumb = ep.thumbnail_url;
                int qIndex = thumb.indexOf('?');
                if (qIndex != -1) {
                    thumb = thumb.substring(0, qIndex);
                }
                if (thumb.endsWith("thumb.jpg")) {
                    return thumb.substring(0, thumb.length() - "thumb.jpg".length()) + "master.m3u8";
                }
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
        private int number;
        private String edition;
        private String hls_master_url;
        private Track[] tracks;
        private Subtitle[] subtitles;
        private String thumbnail_url;

        public int getNumber() {
            return number;
        }

        public String getEdition() {
            return edition;
        }

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

    public static class Playback {
        private int version;
        private String token;

        public int getVersion() {
            return version;
        }

        public String getToken() {
            return token;
        }
    }

    public static class FenixMediaAccess {
        private String origin;
        private String token;
        private String url_prefix;

        public String getOrigin() {
            return origin;
        }

        public String getToken() {
            return token;
        }

        public String getUrl_prefix() {
            return url_prefix;
        }
    }

}
