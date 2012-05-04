package com.fuzionsoftware.googleimages;

public class GoogleImageResponse {
	public GoogleResponseData responseData;
	public String responseDetails;
	public int repsonseStatus;

	
	public class GoogleResponseData{
		public GoogleImageResult[] results;
		public GoogleImageCursor cursor;

	}
	public class GoogleImageCursor{
		public String estimatedResultCount;
		public String moreResultsUrl;
		public int currentPageIndex;
		public GoogleImagePage[] pages;
	}
	public class GoogleImagePage{
		public String start;
		public int label;
	}
	public class GoogleImageResult{
		public String GsearchResultClass;
		public int width;
		public int height;
		public String imageId;
		public int tbWidth;
		public int tbHeight;
		public String unescapedUrl;
		public String url;
		public String visibleUrl;
		public String title;
		public String titleNoFormatting;
		public String originalContextUrl;
		public String content;
		public String contentNoFormatting;
		public String tbUrl;
	}
}
