package com.mrikso.anitube.app.parser;

import com.mrikso.anitube.app.model.TorrentModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class TorrentsPageParser {

    public Single<List<TorrentModel>> parsePage(Document doc) {
        return Single.fromCallable(() -> parseTorrentPage(doc));
    }

    private List<TorrentModel> parseTorrentPage(Document doc) {
        Element rootContentElement = doc.selectFirst("div.content");
        Elements torrentsElement = rootContentElement.select("div.box > div.torrent_info_block");
        return parseTorrentsBlock(torrentsElement);
    }

    private List<TorrentModel> parseTorrentsBlock(Elements torrents) {
        List<TorrentModel> torrentsList = new ArrayList<>(torrents.size());
        for (Element element : torrents) {
            String name = StringUtils.removeLastChar(
                    element.selectFirst("div.torrent_title").text().trim());
            Element left_torrent_info_block = element.selectFirst("div.left_torrent_info_block");
            int seeds = Integer.parseInt(left_torrent_info_block
                    .selectFirst("div.col_download > span")
                    .text()
                    .trim());
            int leechers = Integer.parseInt(left_torrent_info_block
                    .selectFirst("div.col_distribution > span")
                    .text()
                    .trim());
            Elements torrentInfo = left_torrent_info_block.select("div.info_file_torrent");

            int downloadedCount = Integer.parseInt(
                    torrentInfo.get(0).text().replaceFirst("Завантажено:", "").trim());
            String size = torrentInfo.get(1).text().replaceFirst("Розмір:", "").trim();

            Element right_torrent_info_block = element.selectFirst("div.right_torrent_info_block");
            Elements torrentUrls = right_torrent_info_block.select("div.btn_block_right_torrent_info_block");

            String torrentUrl =
                    ApiClient.BASE_URL + torrentUrls.get(0).selectFirst("a").attr("href");
            String magrentUrl = torrentUrls.get(1).selectFirst("a").attr("href");
            TorrentModel torrentModel = new TorrentModel();
            torrentModel.setName(name);
            torrentModel.setSize(size);
            torrentModel.setDownloadedCount(downloadedCount);
            torrentModel.setSeeds(seeds);
            torrentModel.setLeechers(leechers);
            torrentModel.setTorrentUrl(torrentUrl);
            torrentModel.setMagnetUrl(magrentUrl);
            torrentsList.add(torrentModel);
        }
        return torrentsList;
        // #torrent_1522_info > div.right_torrent_info_block > div:nth-child(1) > span
    }
}
