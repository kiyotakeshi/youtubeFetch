package com.kiyotakeshi.youtubeFetch;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Search {

    // result count
    private static final long NUMBER_OF_VIDEOS_RETURNED = 100;

    private static void checkCommandLineArgument(String[] args) {
        if (args.length != 1) {
            System.out.println("You must set search word to first command line argument");
            System.exit(1);
        }
    }

    /**
     * Search for videos on YouTube.
     * Display latest 100 videos URL.
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

            // Restrict the search results to only include videos.
            // @see https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // @see https://developers.google.com/youtube/v3/docs/videos#resource
            search.setFields("items(id/kind,id/videoId,snippet/title)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                prettyPrint(searchResultList.iterator(), searchWord);
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Video Link: https://www.youtube.com/watch?v=" + rId.getVideoId());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}
