package com.kiyotakeshi.youtubeFetch;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Search {

    // @see https://developers.google.com/youtube/v3/docs/search/list#maxResults
    private static final long NUMBER_OF_VIDEOS_RETURNED_PER_REQUEST = 50;

    /**
     * Search for videos on YouTube.
     *
     * @param args first argument: search word
     */
    public static void main(String[] args) {

        checkCommandLineArgument(args);
        String searchWord = args[0];

        // @see # https://console.developers.google.com/project/_/apiui/credential
        String apiKey = System.getenv("YOUTUBE_APIKEY");
        if (apiKey == null) {
            throw new RuntimeException("Not set environment variable `YOUTUBE_APIKEY`. Following example\n" +
                    "$ export YOUTUBE_APIKEY='hogehoge'");
        }

        try {
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequest -> {
            }).setApplicationName("youtube-cmdline-search").build();

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(apiKey);
            search.setQ(searchWord);

            // @see https://developers.google.com/youtube/v3/docs/search/list#order
            search.setOrder("viewCount");

            // Restrict the search results to only include videos.
            // @see https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // @see https://developers.google.com/youtube/v3/docs/search/list#relevanceLanguage
            search.setRelevanceLanguage("ja");

            // @see https://developers.google.com/youtube/v3/docs/search/list#regionCode
            search.setRegionCode("JP");

            // @see https://developers.google.com/youtube/v3/docs/search/list#publishedAfter
            search.setPublishedAfter(new DateTime(String.valueOf(LocalDateTime.now().minusDays(3))));

            // @see https://developers.google.com/youtube/v3/docs/videos#resource
            search.setFields("items(id/kind,id/videoId,snippet/title)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED_PER_REQUEST);

            ArrayList<SearchResult> searchResults = new ArrayList<>();

            SearchListResponse firstSearchResponse = search.execute();

            List<SearchResult> firstSearchItems = firstSearchResponse.getItems();
            for (int i = 0; i < firstSearchItems.size(); i++) {
                searchResults.add(i, firstSearchItems.get(i));
            }

            List<SearchResult> japaneseContentsResult = searchResults
                    .stream()
                    .filter(result -> extracted(result.getSnippet().getTitle()))
                    .collect(Collectors.toList());

            printResults(searchWord, japaneseContentsResult, 10);

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean extracted(String title) {
        boolean containsJapanese = false;
        for (String s : title.split("")) {
            if (s.matches("^[ぁ-んァ-ヶｱ-ﾝﾞﾟ一-龠]*$")) {
                containsJapanese = true;
                break;
            }
        }
        return containsJapanese;
    }

    private static void checkCommandLineArgument(String[] args) {
        if (args.length != 1) {
            System.out.println("You must set search word to first command line argument");
            System.exit(1);
        }
    }

    private static void printResults(String searchWord, List<SearchResult> searchResults, int displayCount) {
        System.out.println(displayCount + " videos for search on \"" + searchWord + "\".\n");
        IntStream.range(0, displayCount).forEach(i -> {
            System.out.println("Title: " + searchResults.get(i).getSnippet().getTitle());
            System.out.println("Video Link: https://www.youtube.com/watch?v=" + searchResults.get(i).getId().getVideoId());
            System.out.println("\n-------------------------------------------------------------\n");
        });
    }
}
