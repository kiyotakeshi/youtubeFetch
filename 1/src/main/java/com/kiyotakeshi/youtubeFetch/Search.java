package com.kiyotakeshi.youtubeFetch;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            search.setOrder("date");

            // Restrict the search results to only include videos.
            // @see https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // @see https://developers.google.com/youtube/v3/docs/videos#resource
            search.setFields("items(id/kind,id/videoId,snippet/title)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED_PER_REQUEST);

            ArrayList<SearchResult> searchResults = new ArrayList<>();

            SearchListResponse firstSearchResponse = search.execute();

            List<SearchResult> firstSearchItems = firstSearchResponse.getItems();
            for (int i = 0; i < firstSearchItems.size(); i++) {
                searchResults.add(i, firstSearchItems.get(i));
            }

            int afterFirstSearchResultSize = searchResults.size();

            // @see https://developers.google.com/youtube/v3/guides/implementation/pagination
            search.setPageToken(firstSearchResponse.getNextPageToken());

            SearchListResponse secondSearchResponse = search.execute();
            List<SearchResult> secondSearchItems = secondSearchResponse.getItems();
            for (int i = 0; i < secondSearchItems.size(); i++) {
                searchResults.add(afterFirstSearchResultSize + i, secondSearchItems.get(i));
            }

            printResults(searchWord, searchResults);

        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void checkCommandLineArgument(String[] args) {
        if (args.length != 1) {
            System.out.println("You must set search word to first command line argument");
            System.exit(1);
        }
    }

    private static void printResults(String searchWord, ArrayList<SearchResult> searchResults) {
        System.out.println(searchResults.size() + " videos for search on \"" + searchWord + "\".");
        searchResults.forEach(result -> {
            System.out.println("Title: " + result.getSnippet().getTitle());
            System.out.println("Video Link: https://www.youtube.com/watch?v=" + result.getId().getVideoId());
            System.out.println("\n-------------------------------------------------------------\n");
        });
    }
}
