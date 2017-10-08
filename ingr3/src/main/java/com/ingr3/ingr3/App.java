package com.ingr3.ingr3;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.applet.Applet;
import java.io.*;
import org.json.JSONObject;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

/**
 * @author: Steven Byerly, Marshal Dickey
 * @desc: get nutrient information from wikipedia, pass to Google NLP API, return to user
 */
public class App extends Applet
{
	//public static final String DEBUG_URL = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";
	//public static final String DEBUG_URL = "http://kambafit.com/wp-content/uploads/2017/07/Mac.png";
	public static final String DEBUG_URL = "https://cdn.caffeineinformer.com/wp-content/uploads/dna-energy-drink-ingredients.jpg"; //monster energy drink url
	
	public static void main (String[] args)
	{
		while (true)
		{
			String url = getUrlFromFS();
			FoodItem fi = new FoodItem(url);
			String json = formJsonForGraph(fi);
			printJsonToFS(json);
		}
	}
	
	public String returnData()
	{
		String url = getParameter("test");
		FoodItem item = new FoodItem(url);
		return formJsonForGraph(item);

	}

	public static void printJsonToFS(String jsonData)
	{
		try {
			PrintWriter writer = new PrintWriter("/home/steve/workspace/ingr3/src/main/java/com/ingr3/ingr3/jsonData.txt", "UTF-8");
			writer.print(jsonData);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String getUrlFromFS()
	{
		File file = new File("/home/steve/workspace/ingr3/src/main/java/com/ingr3/ingr3/url.txt");
		while (!file.isFile())
		{
			file = new File("/home/steve/workspace/ingr3/src/main/java/com/ingr3/ingr3/url.txt");
			System.out.println("looking for url.txt");
		}
		System.out.println("url.txt found!");
		BufferedReader reader;
		StringBuilder sb = new StringBuilder();
		try 
		{
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			
			while ((text=reader.readLine()) != null)
			{
				sb.append(text);
			}
			reader.close();
			file.delete();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		return sb.toString();
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
    
    //get sentiment/healthValue from description String using google NLP API
    public static double getSentiment(String description)
    {
    	try (LanguageServiceClient language = LanguageServiceClient.create())
    	{
    		String text = description;
    		Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
    		Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
    		return sentiment.getScore();
    		
    	} catch (IOException e) {
    		System.out.println("IOexception caught while getting sentiment from description");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("exception caught while getting sentiment from description");
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
    
    //get a string description of the nutritional information of an item linked to by param:url
    public static String getNutrition(String url) throws Exception
    {
    	//get wiki HTML for parsing
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

        //select title for later utility - should only be one of these on all standard formatted wikipedia pages
        Elements titleElements = wiki.select("h1");
        String title = titleElements.get(0).text();
        
        //parse out the Nutrition/Nutrients section
        String nutritionText = "Not Found";
        Elements newEles = wiki.select("h3, h2, p");
        for (Element element : newEles)
        {
        	//look for a description of the ingredient in specific places, i.e. after 'nutrients' or 'nutritional value' headers
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
    
    //get wikipedia article of ingredient based on name, using wikipedia search API and article UIDs
    //TODO: actually catch exceptions more specifically
    public static String lookupIngredient(String ingredient) throws Exception
    {
    	//craft and connect to URL using the ingredient string
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
        
        //use regex to find the page ID
        Pattern p = Pattern.compile("pageid.: ([0-9]+)");

        try
        {
        	Matcher m = p.matcher(searchText);
        	if (m.find())
        	{
        		//format and return page url correctly using the id we found
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

//class to represent an Ingredient, an object with a healthValue field, a name field and a description field
class Ingredient
{
	public double healthValue; 		//used to show the 'sentiment'/health value of the description of the ingredient
	public String name;				//name of the ingredient
	public String desc;				//description of the ingredient, obtained from wikipedia
	
	public Ingredient(String name)
	{
		this.name = name;
		try {
			//attempt to look up a description for this ingredient
			this.desc = App.getNutrition(App.lookupIngredient(name));
			
			//if description found, continue and use the description to find the healthValue/sentiment
			if (this.desc != null && !this.desc.equals("Not Found"))
			{
				
				this.healthValue = new Double(new DecimalFormat("#.##").format(App.getSentiment(this.desc))); 
			}
			} catch (Exception e) {
			System.out.println("Error initializing Ingredient object " + name);
		}
	}
	
}

//class to store a HashSet of ingredient objects, and have a constructor that can initialize it correctly with all fields
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
		//get a set of ingredients as strings
		HashSet<String> ingredientStrings = App.getIngredientsFromString(App.getIngredients(ingredientURL));
		ingredients = new HashSet<Ingredient>();
		
		//for each valid ingredient string, create an Ingredient object and initialize fields
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
		
	}
	
}
