package com.ingr3.ingr3;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.*;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import java.applet.Applet;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author: Steven Byerly, Marshall Dickey
 * @desc: get nutrient information from wikipedia, pass to Google NLP API
 */
public class App
{
	//public static final String DEBUG_URL = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
	//public static final String DEBUG_URL = "http://kambafit.com/wp-content/uploads/2017/07/Mac.png";
	public static final String DEBUG_URL = "https://cdn.caffeineinformer.com/wp-content/uploads/dna-energy-drink-ingredients.jpg";
	
	
    public static void main( String[] args )
    {
//    	System.out.println(args[0]);
    	debug();
    }
    

    //various debug functionality for app
    public static void debug()
    {
//    	System.out.println(getNutrition("https://en.wikipedia.org/wiki/High-fructose_corn_syrup"));
//    	System.out.println(getNutrition("https://en.wikipedia.org/wiki/Rolled_oats"));
//    	System.out.println(lookupIngredient("potato"));
//    	System.out.println(getNutrition(lookupIngredient("orange (fruit)")));
//    	System.out.println(getNutrition(lookupIngredient("high fructose corn syrup")));
    	FoodItem macaroni = new FoodItem(DEBUG_URL);
//    	System.out.println("\n\n\n\n");
//    	System.out.println(formJsonForGraph(macaroni));
//    	getSentiment("HFCS is composed of 76% carbohydrates and 24% water, containing no fat, no protein, and no essential nutrients in significant amounts (table). In a 100 gram serving, it supplies 281 Calories, whereas in one tablespoon of 19 grams, it supplies 53 Calories (table link");
    	
//    	Ingredient milkfat = new Ingredient("milkfat");
//    	System.out.println(milkfat.desc);
//    	getIngredientsFromString(getIngredients(DEBUG_URL));
    	
    }
    
    //create the json to be passed to the graphing functionality
    public static String formJsonForGraph(FoodItem item)
    {
    	String[] names = new String[item.ingredients.size()];
    	String[] desc = new String[item.ingredients.size()];
    	double[][] healthValuesWrapper = new double[1][item.ingredients.size()];
    	double[] healthValues = new double[item.ingredients.size()];	
    	
    	int counter = 0;
    	for (Ingredient ingri : item.ingredients)
    	{
    		names[counter] = ingri.name;
    		desc[counter] = ingri.desc;
    		healthValues[counter] = ingri.healthValue;
    		counter++;
    	}
    	
    	healthValuesWrapper[0] = healthValues;
    	JSONObject obj = new JSONObject();
    	obj.put("names", names);
    	obj.put("descriptions", desc);
    	obj.put("healthValues", healthValuesWrapper);
    	return obj.toString();
    }
    
    //get sentiment from description
    public static double getSentiment(String description)
    {
    	try (LanguageServiceClient language = LanguageServiceClient.create())
    	{
//    		GoogleCredential credential = GoogleCredential.getApplicationDefault();
    		
    		String text = description;
    		Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
    		Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
    		return sentiment.getScore();
    		
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return -999.0;
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
    
    //make ingredient string look prettier
    public static String trimIngredientString(String ingrString)
    {
    	ingrString = ingrString.replace(":", "");
    	ingrString = ingrString.replace("(", ",");
    	ingrString = ingrString.replace(")", ",");
    	ingrString = ingrString.replace("\\n", ",");
    	ingrString = ingrString.replace("contains", ",");
    	ingrString = ingrString.replace("\"", "");

    	return ingrString;
    	
    }
    
    //split and trim a String of ingredients into a hashset
    public static HashSet<String> getIngredientsFromString(String ingredientString)
    {
    	HashSet<String> ingris = new HashSet<String>();
    	String[] ingriArr = ingredientString.split(",");
    	for (int i=0;i<ingriArr.length;i++)
    	{
    		ingriArr[i] = ingriArr[i].replace(",","").trim();
    		
    		if (ingriArr[i] != "") ingris.add(ingriArr[i]);

    	}
    	return ingris;
    }
    
    //get ingredients from image URI using OCR API
    public static String getIngredients(String imgUri)
    {
    	String responseText;
    	URL url;
    	HttpURLConnection http;
    	String ingredientString;
    	try {
    		//create the http request
			url = new URL("https://vision.googleapis.com/v1/images:annotate?key=AIzaSyBGD1ycwUM0QpnOIn5UTrqTMrmjzIL4fxo");
			
			http = (HttpURLConnection)url.openConnection();;
			http.setRequestMethod("POST");
			String requestBody = getJson(imgUri);
			http.setRequestProperty("Content-Length", "" + Integer.toString(requestBody.getBytes().length));
			http.setRequestProperty("Content-Language", "en-US");
			http.setRequestProperty("Content-Type", "application/json");
			http.setDoInput(true);
			http.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream ( http.getOutputStream());
			wr.writeBytes(requestBody);
			wr.flush();
			wr.close();
			
			InputStream is = http.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line=rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();
			
			responseText = response.toString().toLowerCase();
			
    	} catch (Exception e) {
			System.out.println("exception caught for getIngredients call");
			e.printStackTrace();
			return "Not Found";
		}
        Pattern p = Pattern.compile("ngredients(.+)");

        try
        {
        	Matcher m = p.matcher(responseText);
        	if (m.find())
        	{
//        		System.out.println(m.group(0));
        		ingredientString = trimIngredientString(m.group(1));
        		return ingredientString;
        		
        	}
        } catch (IllegalStateException e)
        {
        	System.out.println("issue parsing json response from google vision");
        }
    	
    	return "Not Found";
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
        org.jsoup.nodes.Document wiki = Jsoup.parse(html);     

        //select title for later utility - should only be one
        Elements titleElements = wiki.select("h1");
        String title = titleElements.get(0).text();
        
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
        	//if all else fails, grab the first paragraph of the page (UNDEFINED BEHAVIOR)
        	if (nutritionText.equals("Not Found") && element.is("p") && (element.previousElementSibling() == null || !element.previousElementSibling().is("p")) &&
        			element.text().contains(title))
        	{
        		System.out.println("title matched: " + title);
        		nutritionText = element.text();
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
        org.jsoup.nodes.Document wiki = Jsoup.parse(html);        
        
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
			
			if (this.desc != null && !this.desc.equals("Not Found"))
			{
				
				this.healthValue = new Double(new DecimalFormat("#.##").format(App.getSentiment(this.desc))); //TODO: FIX THIS
			}
			} catch (Exception e) {
			System.out.println("Error initializing Ingredient object " + name);
		}
	}
	//TODO: add getters/setters
}

//class to map an ingredient to it's description
class FoodItem
{
	public HashSet<Ingredient> ingredients;
	
	//default constructor
	public FoodItem()
	{
		ingredients = new HashSet<Ingredient>();
	}
	
	//constructor to take in ingredient string, split on comma and populate ingredients
	public FoodItem(String ingredientURL)
	{
		HashSet<String> ingredientStrings = App.getIngredientsFromString(App.getIngredients(ingredientURL));
		ingredients = new HashSet<Ingredient>();
		for (String ingredientString : ingredientStrings)
		{
			Ingredient ingriObj = new Ingredient(ingredientString);
			if (ingriObj.desc != null && !ingriObj.desc.equals("Not Found")) 
				{
				ingredients.add(ingriObj); //check for problem ingredients without descs
				System.out.println("adding " + ingriObj.name + " with desc " + ingriObj.desc);
				}
			if (ingriObj.desc == null || (ingriObj.desc.equals("Not Found")))
			{
				System.out.println("Did not find description for " + ingriObj.name);
			}
		}
		
		//TODO: finish this
		
		
	}
	
	
}
