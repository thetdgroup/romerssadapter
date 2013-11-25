package com.thetdgroup;

import org.json.JSONException;
import org.json.JSONObject;

public class RSSCategories
{
 private String categoryName = "";
 private String categoryURL = "";
 private String categoryFileName = "";
 private String categoryDescription = "";
 
 //
	public String getCategoryName()
	{
		return categoryName;
	}
	
	public void setCategoryName(String categoryName)
	{
		this.categoryName = categoryName;
	}
	
	public String getCategoryURL()
	{
		return categoryURL;
	}

	public void setCategoryURL(String categoryURL)
	{
		this.categoryURL = categoryURL;
	}
	
	public String getCategoryFileName()
	{
		return categoryFileName;
	}

	public void setCategoryFileName(String categoryFileName)
	{
		this.categoryFileName = categoryFileName;
	}
	
	public String getCategoryDescription()
	{
		return categoryDescription;
	}

	public void setCategoryDescription(String categoryDescription)
	{
		this.categoryDescription = categoryDescription;
	}

	//
	public JSONObject toJSON() throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("category_name", this.categoryName);
		jsonObject.put("category_url", this.categoryURL);
		jsonObject.put("file_name", this.categoryFileName);
		jsonObject.put("category_description", this.categoryDescription);
		
		return jsonObject;
	}
}
