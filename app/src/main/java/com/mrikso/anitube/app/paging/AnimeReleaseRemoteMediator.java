// package com.mrikso.anitube.app.paging;
//
// import androidx.paging.LoadType;
// import androidx.paging.PagingState;
// import androidx.paging.RemoteMediator.MediatorResult;
// import androidx.paging.rxjava3.RxRemoteMediator;
//
// import com.mrikso.anitube.app.data.AnimeDatabase;
// import com.mrikso.anitube.app.data.model.AnimeRemoteKeys;
// import com.mrikso.anitube.app.model.AnimeListReleases;
// import com.mrikso.anitube.app.model.AnimeReleaseModel;
// import com.mrikso.anitube.app.network.AnitubeApiService;
// import com.mrikso.anitube.app.parser.AnimeReleasesMapper;
//
// import io.reactivex.rxjava3.core.Single;
// import io.reactivex.rxjava3.functions.Function;
// import io.reactivex.rxjava3.schedulers.Schedulers;
//
// import java.util.ArrayList;
// import java.util.List;
//
// public class AnimeReleaseRemoteMediator extends RxRemoteMediator<Integer, AnimeReleaseModel> {
//    private static final int INVALID_PAGE = -1;
//    private AnitubeApiService service;
//    private AnimeDatabase database;
//    private AnimeReleasesMapper mapper;
//
//    public AnimeReleaseRemoteMediator(
//            AnitubeApiService service, AnimeDatabase database, AnimeReleasesMapper mapper) {
//        this.service = service;
//        this.database = database;
//        this.mapper = mapper;
//    }
//
//    @Override
//    public Single<MediatorResult> loadSingle(
//            LoadType loadType, PagingState<Integer, AnimeReleaseModel> state) {
//        try {
//
//            return Single.just(loadType)
//                    .subscribeOn(Schedulers.io())
//                    .map(it -> getKey(it, state))
//                    .flatMap(
//                            page -> {
//                                if (page == INVALID_PAGE) {
//                                    return Single.just(new MediatorResult.Success(true));
//                                }
//                                return service.getAnimeByPage(String.valueOf(page))
//                                        .subscribeOn(Schedulers.io())
//                                        .map(mapper::transform)
//                                        .map(
//                                                (Function<AnimeListReleases, MediatorResult>)
//                                                        data -> {
//                                                            insertToDb(page, loadType, data);
//
//                                                            return new MediatorResult.Success(
//                                                                    page >= data.getMaxPage());
//                                                        })
//                                        .onErrorReturn(
//                                                (Function<Throwable, MediatorResult>)
//                                                        e -> {
//                                                            e.printStackTrace();
//                                                            return new MediatorResult.Error(e);
//                                                        });
//                            })
//                    .onErrorReturn(MediatorResult.Error::new);
//
//        } catch (Exception io) {
//            io.printStackTrace();
//            return Single.just(new MediatorResult.Error(io));
//        }
//    }
//
//    private int getKey(LoadType loadType, PagingState<Integer, AnimeReleaseModel> state) {
//        switch (loadType) {
//            case REFRESH:
//                AnimeRemoteKeys remoteKeys = getRemoteKeyClosestToCurrentPosition(state);
//                return remoteKeys != null ? remoteKeys.getNextKey() - 1 : 1;
//
//            case PREPEND:
//                AnimeRemoteKeys remoteKeysPrepend = getRemoteKeyForFirstItem(state);
//                return remoteKeysPrepend.getPrevKey() != null
//                        ? remoteKeysPrepend.getPrevKey()
//                        : INVALID_PAGE;
//
//            case APPEND:
//                AnimeRemoteKeys remoteKeysAppend = getRemoteKeyForLastItem(state);
//                return remoteKeysAppend.getNextKey() != null
//                        ? remoteKeysAppend.getNextKey()
//                        : INVALID_PAGE;
//        }
//        return INVALID_PAGE;
//    }
//
//    private void insertToDb(int page, LoadType loadType, AnimeListReleases data) {
//        database.runInTransaction(
//                () -> {
//                    if (loadType == LoadType.REFRESH) {
//                        database.animeRemoteKeysDao().clearRemoteKeys();
//                        database.animeDao().clearAnimes();
//                    }
//
//                    Integer prevKey = page == 1 ? null : page - 1;
//                    Integer nextKey = page >= data.getMaxPage() ? null : page + 1;
//                    List<AnimeRemoteKeys> keys = new ArrayList<>();
//                    for (AnimeReleaseModel movie : data.getAnimeReleases()) {
//                        keys.add(new AnimeRemoteKeys(movie.getAnimeId(), prevKey, nextKey));
//                    }
//                    database.animeRemoteKeysDao().insertAll(keys);
//                    database.animeDao().insertAll(data.getAnimeReleases());
//                });
//    }
//
//    private AnimeRemoteKeys getRemoteKeyForLastItem(PagingState<Integer, AnimeReleaseModel> state)
// {
//        AnimeReleaseModel lastItem = state.lastItemOrNull();
//        if (lastItem != null) {
//            return database.animeRemoteKeysDao().remoteKeysByAnimeId(lastItem.getAnimeId());
//        }
//        return null;
//    }
//
//    private AnimeRemoteKeys getRemoteKeyForFirstItem(
//            PagingState<Integer, AnimeReleaseModel> state) {
//        AnimeReleaseModel firstItem = state.firstItemOrNull();
//        if (firstItem != null) {
//            return database.animeRemoteKeysDao().remoteKeysByAnimeId(firstItem.getAnimeId());
//        }
//        return null;
//    }
//
//    private AnimeRemoteKeys getRemoteKeyClosestToCurrentPosition(
//            PagingState<Integer, AnimeReleaseModel> state) {
//        Integer position = state.getAnchorPosition();
//        if (position != null) {
//            AnimeReleaseModel closesAnime = state.closestItemToPosition(position);
//            if (closesAnime != null) {
//                return
// database.animeRemoteKeysDao().remoteKeysByAnimeId(closesAnime.getAnimeId());
//            }
//        }
//        return null;
//    }
// }
//
