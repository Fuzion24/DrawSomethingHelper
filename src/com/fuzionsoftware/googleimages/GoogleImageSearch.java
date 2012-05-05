package com.fuzionsoftware.googleimages;


import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.net.Uri;

import com.fuzionsoftware.googleimages.GoogleImageResponse.GoogleImageResult;
import com.google.gson.Gson;


public class GoogleImageSearch {
	public static ArrayList<String> searchGoogleImages(String searchParameters, int count)
    {
		ArrayList<String> urls = new ArrayList<String>();
		int start = 0;
		try {
			while(start < count)
			{
				//https://developers.google.com/image-search/v1/jsondevguide
				Uri.Builder uri = new Uri.Builder()
					.scheme("https").authority("ajax.googleapis.com")
					.appendEncodedPath("ajax/services/search/images")
					.appendQueryParameter("v", "1.0")
					.appendQueryParameter("imgsz", "large")
					.appendQueryParameter("imgtype", "clipart")
					/*
					Specifies the search safety level, which may be one of the following:

					safe=active enables the highest level of safe search filtering.
					safe=moderate enables moderate safe search filtering (default).			
					safe=off disables safe search filtering.
					*/
					.appendQueryParameter("safe", "off")
					.appendQueryParameter("start", Integer.toString(start))
					.appendQueryParameter("q", searchParameters);
	
				URLConnection connection = new URL(uri.toString()).openConnection();
				
				//http://stackoverflow.com/questions/309424/in-java-how-do-i-read-convert-an-inputstream-to-a-string
				String json_results = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A").next();
				
		        GoogleImageResponse gImages = new Gson().fromJson(json_results, GoogleImageResponse.class);
		        
		        for(GoogleImageResult result : gImages.responseData.results)
		        	urls.add(result.url);
		        
		        start += gImages.responseData.results.length;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urls;
    }
}
