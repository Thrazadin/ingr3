package com.ingr3.ingr3;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

/**
 * @author: Steven Byerly, Marshall Dickey
 * @desc: get nutrient information from wikipedia, pass to Google NLP API
 */
public class App 
{
	public static final String DEBUG_URL = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
	
    public static void main( String[] args ) throws Exception
    {
//    	System.out.println(getNutrition("https://en.wikipedia.org/wiki/High-fructose_corn_syrup"));
//    	System.out.println(getNutrition("https://en.wikipedia.org/wiki/Rolled_oats"));
//    	System.out.println(lookupIngredient("potato"));
//    	System.out.println(getNutrition(lookupIngredient("orange (fruit)")));
//    	System.out.println(getNutrition(lookupIngredient("high fructose corn syrup")));
//    	FoodItem soup = new FoodItem("corn, potatoes, orange (fruit), high fructose corn syrup");
//    	for (Ingredient ingri : soup.ingredients)
//    	{
//    		System.out.println(ingri.desc);
//    	}
//    	getSentimentHtml("FAKEBASE64IMG");
    }
    
    public static String getJson(String URL)
    {
    	String requestBody =
"    		{"
+ "    				  \"requests\":["
+ "    				    {"
+ "    				      \"image\":{"
+ "    				        \"source\":{"
+ "    				          \"imageUri\":"
+ "    				            \""+ URL + "\""
+ "    				        }"
+ "    				      },"
+ "    				      \"features\":["
+ "    				        {"
+ "    				          \"type\":\"TEXT_DETECTION\","
+ "    				          \"maxResults\":1"
+ "    				        }"
+ "    				      ]"
+ "    				    }"
+ "    				  ]"
+ "    				}";
    	
    	JSONObject obj = new JSONObject(requestBody);
    	return obj.toString();
    	
    }
    
    //get the 'sentiment' for the description, aka how good or bad it is using google's nlp api
    public static double getSentimentHtml(String imgUri)
    {
    	
    	try {
    		//create the http request
			URL url = new URL("https://vision.googleapis.com/v1/images:annotate?key=AIzaSyBGD1ycwUM0QpnOIn5UTrqTMrmjzIL4fxo");
			URLConnection con = url.openConnection();
			HttpURLConnection http = (HttpURLConnection)con;
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			
			String base64img = "TESTBASE64IMG";
			String base64imgURI = "https://www.ambarinutrition.com/v/vspfiles/assets/images/macaroni%20&%20cheese%20label.jpg";
			//String requestBody = "{\"requests\": [{\"image\": {\"content\": " + base64img + ": [{\"type\": \"TEXT_DETECTION\"}]}]}";
			String requestBody = "{" + 
  "requests\":[" +
    "{" +
      "image\":{" +
        "source\":{" +
          "imageUri\": " +
            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png" +
        "}" +
      "}," +
      "features\":[ " +
        "{" +
          "type\":\"TEXT_DETECTION\","+
          "maxResults\":1"+
        "}"+
      "]" +
    "}"+
  "]" +
"}";
			http.setRequestProperty("Content-Type", "application/json");
			http.setRequestProperty("charset", "utf-8");
			byte[] out = requestBody.getBytes(StandardCharsets.UTF_8);
			int length = out.length;
			http.setFixedLengthStreamingMode(length);
			http.connect();
			try(OutputStream os = http.getOutputStream())
			{
				os.write(out);
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				System.out.println(inputLine);
			}
			in.close();
			
    	} catch (Exception e) {
			System.out.println("exception caught for getSentiment call");
			e.printStackTrace();
		}
    	
    	
    	return 0.0;
    }
    
    public static String getNutrition(String url) throws Exception
    {
    	URL wikiTest = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(wikiTest.openStream()));
        
        StringBuilder sb = new StringBuilder(); //create stringbuilder to read in html from page
        String inputLine;
        while ((inputLine = in.readLine()) != null)
        {
        	sb.append(inputLine);
        }
        in.close();
        
        String html = sb.toString(); // create html string from stringbuilder
        Document wiki = Jsoup.parse(html);     

        //parse out the Nutrition/Nutrients section
        String nutritionText = "Not Found";
        Elements newEles = wiki.select("h3, h2, p");
        for (Element element : newEles)
        {
        	if ((element.is("h3")) && element.children().size() > 0 && element.child(0).attr("id").toLowerCase().contains("nutri"))
        	{
        		nutritionText = ((Element)element.nextSibling()).text();
        	}
        	if (element.is("h2") && element.children().size() > 0 && (element.child(0).attr("id").toLowerCase().contains("nutri")))
			{
        		nutritionText = ((Element)element.nextSibling()).text();
			}
        	
         }
        
        
        return nutritionText;
    }
    
    //get wikipedia article of ingredient based on name
    //TODO: actually catch exceptions more specifically? 
    public static String lookupIngredient(String ingredient) throws Exception
    {
    	//TODO: handle spaces correctly
    	String ingredientString = ingredient.replace(" ", "%20");
    	String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + ingredientString +"&srwhat=text&limit=1";
    	URL ingredientPage = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(ingredientPage.openStream()));
        
        StringBuilder sb = new StringBuilder(); //create stringbuilder to read in html from page
        String inputLine;
        while ((inputLine = in.readLine()) != null)
        {
        	sb.append(inputLine);
        }
        in.close();
        String html = sb.toString(); // create html string from stringbuilder
        Document wiki = Jsoup.parse(html);        
        
        String searchText = wiki.text();
        
        //use regex to find the page url
        Pattern p = Pattern.compile("pageid.: ([0-9]+)");

        try
        {
        	Matcher m = p.matcher(searchText);
        	if (m.find())
        	{
        		//format and return page url correctly
        		return "https://en.wikipedia.org/?curid=" + m.group(1);
        	}
        	
        } catch (IllegalStateException e)
        {
        	System.out.println("wikipedia article not found for " + ingredient);
        	e.printStackTrace();
        }
        
        
        return null;
    }
    
}

class Ingredient
{
	public double healthValue;
	public String name;
	public String desc;
	
	public Ingredient(String name)
	{
		this.name = name;
		try {
			this.desc = App.getNutrition(App.lookupIngredient(name));
//			this.healthValue = App.getSentiment(this.desc); //TODO: FIX THIS
		} catch (Exception e) {
			System.out.println("Error initializing Ingredient object " + name);
		}
	}
	//TODO: add getters/setters
}

//class to map an ingredient to it's description
class FoodItem
{
	public Set<Ingredient> ingredients;
	
	//default constructor
	public FoodItem()
	{
		ingredients = new HashSet<Ingredient>();
	}
	
	//constructor to take in ingredient string, split on comma and populate ingredients
	public FoodItem(String ingredientString)
	{
		ingredients = new HashSet<Ingredient>();
		//TODO: finish this
		String[] ingriStrings = ingredientString.split(",");
		for (int i=0;i<ingriStrings.length;i++)
		{
			Ingredient currIngr = new Ingredient(ingriStrings[i].trim());
			ingredients.add(currIngr);
		}
	}
	
}
