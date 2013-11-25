package com.thetdgroup;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

import com.thetdgroup.RSSCategories;
import com.thetdgroup.AdapterConstants;

public final class RomeRSSAdapter extends BaseRSSAdapter
{
	private final List<SyndCategory> syndCategories = new ArrayList<SyndCategory>();
	private List<SyndEntry> syndEntries = new ArrayList<SyndEntry>();
	private HashMap<String, RSSCategories> rssCategories = new HashMap<String, RSSCategories>();
	
	private FuzeInCommunication fuzeInCommunication = new FuzeInCommunication();
	
	//
	public void initialize(final JSONObject configurationObject) throws Exception
	{
		if(configurationObject.has("adapter_configuration_file") == false)
		{
			throw new Exception("The adapter_configuration_file parameter was not found");
		}
		
		// Set FuzeIn connection
		if(configurationObject.has("fuzein_connection_info"))
		{
			JSONObject jsonCommParams = configurationObject.getJSONObject("fuzein_connection_info");
			
			fuzeInCommunication.setFuzeInConnection(jsonCommParams.getString("service_url"), 
																																											jsonCommParams.getInt("service_socket_timeout"), 
																																											jsonCommParams.getInt("service_connection_timeout"), 
																																											jsonCommParams.getInt("service_connection_retry"));
		}

		//
		parseAdapterConfiguration(configurationObject.getString("adapter_configuration_file"));
	}
	
	//
	public void destroy()
	{
	 if(fuzeInCommunication != null)
	 {
	 	fuzeInCommunication.releaseConnection();
	 }
	}
	
	//
	public JSONObject addFeedCategories(final String identificationKey, final JSONObject parameters) throws Exception
	{
		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.UNSUPPORTED);

		return jsonObject;
	}
	
	public JSONObject removeFeedCategories(final String identificationKey, final JSONObject parameters) throws Exception
	{
		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.UNSUPPORTED);

		return jsonObject;
	}
	
	public JSONObject listFeedCategories(final String identificationKey) throws Exception
	{
		//
		JSONArray jsonArray = new JSONArray();
		
		for(Entry<String, RSSCategories> entry : rssCategories.entrySet()) 
		{
   String key = entry.getKey();
   RSSCategories rssCategory = entry.getValue();
   
   JSONObject jsonObject = new JSONObject();
   jsonObject.put("category_name", rssCategory.getCategoryName());
   jsonObject.put("category_description", rssCategory.getCategoryDescription());
   
   jsonArray.put(jsonObject);
  }

		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonArray);

		return jsonObject;
	}
	
	//
	// RSS 1.0 stands for "RDF Site Summary." This flavor of RSS incorporates RDF,
	// a Web standard for metadata. Because RSS 1.0 uses RDF, any RDF processor can
	// understand RSS without knowing anything about it in particular. This allows
	// syndicated feeds to easily become part of the Semantic Web.
	//
	public JSONObject submitRSS1_Feed(final String identificationKey, final JSONObject parameters) throws Exception
	{
		//
		// Validate that all parameters are present
		if(parameters.has("feed_title") == false)
		{
			throw new Exception("The 'feed_title' parameter is required.");
		}

		if(parameters.has("feed_content") == false)
		{
			throw new Exception("The 'feed_content' parameter is required.");
		}

		if(parameters.has("feed_category") == false)
		{
			throw new Exception("The 'feed_category' parameter is required.");
		}

		if(parameters.has("feed_author") == false)
		{
			throw new Exception("The 'feed_author' parameter is required.");
		}

		//
		String feedLink = "";

		if(parameters.has("feed_link") == true)
		{
			feedLink = parameters.getString("feed_link");
		}

		//
		// Create RSS Entry
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(parameters.getString("feed_author"));
		entry.setTitle(parameters.getString("feed_title"));
		entry.setLink(feedLink);
		entry.setPublishedDate(new Date());

		SyndContent description = new SyndContentImpl();
		description.setType("text/plain");
		description.setValue(parameters.getString("feed_content"));
		entry.setDescription(description);

		SyndCategory category = new SyndCategoryImpl();
		category.setName(parameters.getString("feed_category"));
		syndCategories.add(category);
		entry.setCategories(syndCategories);
		syndCategories.remove(category);

		syndEntries.add(entry);

		//
		// Submit Feed
		doSyndication("rss_1.0",
				            "FuzeIn Feeds",
																"Feed Link",
																"FuzeIn Feeds Alerts",
																"TDG Copyright",
																parameters.getString("feed_category"));

		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, "");

		return jsonObject;
	}

	//
 //	RSS 2.0 is championed by UserLand's Dave Winer.
	// In this version, RSS stands for "Really Simple Syndication"
	// and simplicity is its focus.
	//
	public JSONObject submitRSS2_Feed(final String identificationKey, final JSONObject parameters) throws Exception
	{
		//
		// Validate that all parameters are present
		if(parameters.has("feed_title") == false)
		{
			throw new Exception("The 'feed_title' parameter is required.");
		}

		if(parameters.has("feed_content") == false)
		{
			throw new Exception("The 'feed_content' parameter is required.");
		}

		if(parameters.has("feed_author") == false)
		{
			throw new Exception("The 'feed_author' parameter is required.");
		}

		//
		String feedLink = "";

		if(parameters.has("feed_link") == true)
		{
			feedLink = parameters.getString("feed_link");
		}

		//
		// Create RSS Entry
		SyndEntry entry = new SyndEntryImpl();
		entry.setAuthor(parameters.getString("feed_author"));
		entry.setTitle(parameters.getString("feed_title"));
		entry.setLink(feedLink);
		entry.setPublishedDate(new Date());

		SyndContent description = new SyndContentImpl();
		description.setType("text/plain");
		description.setValue(parameters.getString("feed_content"));
		entry.setDescription(description);

		syndEntries.add(entry);

		//
		// Submit Feed
		doSyndication("rss_2.0",
				            "FuzeIn Feeds",
				            "Feed Link",
																"FuzeIn Feeds Alerts",
																"TDG Copyright",
																parameters.getString("feed_category"));

		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, "");
		
		return jsonObject;
	}

	//
 //	ATOM Both RSS 1.0 and 2.0 are informal specifications; that is, they are not
	// published by a well-known standards body or industry consortium, but instead by
	// a small group of people.
	//
 //	Some people are concerned by this, because such specifications can be changed at
	// the whim of the people who control it. Standards bodies bring stability, by
	// limiting change and having well-established procedures for introducing it.
	// To introduce such stability to syndication, a group of people established an
	// IETF Working Group to standardise a format called Atom.
	//
	public JSONObject submitAtom_Feed(final String identificationKey, final JSONObject parameters) throws Exception
	{
		//
		// Validate that all parameters are present
		if(parameters.has("feed_title") == false)
		{
			throw new Exception("The 'feed_title' parameter is required.");
		}

		if(parameters.has("feed_content") == false)
		{
			throw new Exception("The 'feed_content' parameter is required.");
		}

		if(parameters.has("feed_category") == false)
		{
			throw new Exception("The 'feed_category' parameter is required.");
		}

		if(parameters.has("feed_author") == false)
		{
			throw new Exception("The 'feed_author' parameter is required.");
		}

		//
		String feedLink = "";

		if(parameters.has("feed_link") == true)
		{
			feedLink = parameters.getString("feed_link");
		}

		//
		// Create RSS Entry


		//
		// Submit Feed
		doSyndication("atom_1.0",
				            "FuzeIn Feeds",
																"Feed Link",
																"FuzeIn Feeds Alerts",
																"TDG Copyright",
																parameters.getString("feed_category"));

		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, "");

		return jsonObject;
	}

	//
	@SuppressWarnings("unchecked")
	public JSONObject parseRSSFeed(final String identificationKey, final JSONObject parameters) throws Exception
	{
		//
		// Validate that all parameters are present
		if(parameters.has("feed_category") == false)
		{
			throw new Exception("The 'feed_category' parameter is required.");
		}

		//
		XmlReader reader = null;
		JSONArray jsonFeedArray = new JSONArray();

		try
		{
			RSSCategories rssCategory = rssCategories.get(parameters.getString("feed_category"));
			
			if(rssCategory == null)
			{
				throw new Exception("The 'feed_category' " + parameters.getString("feed_category") + " has not been found");
			}
			
	 	URL url = new URL(rssCategory.getCategoryURL());
	 	reader = new XmlReader(url);

	 	//
	 	SyndFeed feed = new SyndFeedInput().build(reader);

	 	for(Iterator<SyndEntry> iterator = feed.getEntries().iterator(); iterator.hasNext();)
	 	{
	 	 SyndEntry entry = iterator.next();

	 	 //
	 	 //
	 	 JSONObject jsonFeedObject = new JSONObject();
	 	 jsonFeedObject.put("feed_title", entry.getTitle());
	 	 jsonFeedObject.put("published_date", entry.getPublishedDate());
	 	 jsonFeedObject.put("feed_uri", entry.getUri());

	 	 //
	 	 // Parse authors
	 	 JSONArray jsonAuthorArray = new JSONArray();

    List<SyndEntry> authorList = entry.getAuthors();

    for(SyndEntry author : authorList)
    {
     JSONObject jsonAuthorObject = new JSONObject();
     jsonAuthorObject.put("author", author.getAuthor());

     jsonAuthorArray.put(jsonAuthorObject);
    }

    //
    jsonFeedObject.put("feed_authors", jsonAuthorArray);
    jsonFeedArray.put(jsonFeedObject);
   }
		}
		finally
		{
			if(reader != null)
			{
		  reader.close();
			}
		}

		//
		//
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(AdapterConstants.ADAPTER_STATUS, AdapterConstants.status.SUCCESS);
		jsonObject.put(AdapterConstants.ADAPTER_DATA, jsonFeedArray);

		return jsonObject;
	}

	//
	private void doSyndication(final String feedType, 
																												final String title, 
																												final String link, 
																												final String description_loc, 
																												final String copyright, 
																												final String feedCategory) throws Exception
	{
		//
		//
		RSSCategories rssCategory = rssCategories.get(feedCategory);
		
		if(rssCategory == null)
		{
			throw new Exception("The 'feed_category' " + feedCategory + " is unsupported");
		}
		
		//
		// Create document if not already done so
		SyndFeed feed = null;
		File feedFile = new File(rssCategory.getCategoryFileName());

		if(feedFile.exists() == false)
		{
			feed = new SyndFeedImpl();
			feed.setLink(link);
			feed.setTitle(title);
			feed.setFeedType(feedType);
			feed.setDescription(description_loc);
			feed.setCopyright(copyright);
			feed.setEncoding("UTF-8");
		}
		else
		{
			//
	 	SyndFeedInput syndFeed = new SyndFeedInput();
	 	feed = syndFeed.build(new XmlReader(feedFile));
		}

		feed.setEntries(syndEntries);

		//
		// Write document
		final Writer writer = new FileWriter(rssCategory.getCategoryFileName());
		final SyndFeedOutput output = new SyndFeedOutput();

		output.output(feed, writer);
		writer.close();
	}

	@SuppressWarnings("unchecked")
	private void parseAdapterConfiguration(final String adapterConfigurationFile) throws Exception
	{
		//
		// Parse Configuration
		Document configurationDocument = saxBuilder.build(adapterConfigurationFile);

		//
		// Get Categories
		XPath xPath = XPath.newInstance("adapter_configuration/feed_category");
		List<Element> elements = xPath.selectNodes(configurationDocument);
		Iterator<Element> iterator = elements.iterator();
		
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			
			RSSCategories rssCategory = new RSSCategories();
			rssCategory.setCategoryName(element.getAttributeValue("category"));
			rssCategory.setCategoryURL(element.getAttributeValue("url"));
			rssCategory.setCategoryFileName(element.getAttributeValue("file_name"));
			rssCategory.setCategoryDescription(element.getAttributeValue("description"));
			
			rssCategories.put(element.getAttributeValue("category"), rssCategory);
		}
	}
}
