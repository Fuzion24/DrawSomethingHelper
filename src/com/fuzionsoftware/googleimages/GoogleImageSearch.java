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
					.appendQueryParameter("imgtype", "clipart")
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
