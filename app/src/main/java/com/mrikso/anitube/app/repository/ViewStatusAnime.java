package com.mrikso.anitube.app.repository;

public enum ViewStatusAnime {
    STATUS_NONE_WATCH(0), // не дивлюсь
    STATUS_WILL(2), // заплановано
    STATUS_WATCH(3), // переглядаю
    STATUS_SEEN(4), // переглянуто
    STATUS_PONED(5), // відкладено
    STATUS_ADAND(6); // покинуто

    final int statusCode;

    ViewStatusAnime(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public static ViewStatusAnime fromId(int id) {
        for (ViewStatusAnime type : values()) {
            if (type.getStatusCode() == id) {
                return type;
            }
        }
        return null;
    }
}
