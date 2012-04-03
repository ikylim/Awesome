package com.isaac.awesome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Products extends ListActivity {
	   String[] productNames; //array of product names
	   String[] productIDs; //array of product ids
	 //gotta retrieve this once httpPOST to line_items controller is sent
	   String cartID = "null";
	   
	   @Override
	   protected void onCreate(Bundle saveInstanceState){
		   super.onCreate(saveInstanceState);
		   //get api_token that is saved on the device right now so that i can make connection to server
		   SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
		   String api_token = settings.getString("api_token", "");
	
		   //AsyncTask stuff
		   gettingStuff getIt = new gettingStuff();
		   getIt.execute(api_token); 
		   
		   /*
		   
		   try {
				//this process will put the array of string into values array.  
				  ProcessData(retrieveData(api_token));
			     } catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			     } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			     } catch (IOException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			     } catch (JSONException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			  }
			  */
			  
	       
		 //ArrayAdapter to put data (storeNames) and list it 
		/* ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						R.layout.products, R.id.products_content, productNames);
		 setListAdapter(adapter);
	*/
	
	   }
	   
	   @Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
		   super.onListItemClick(l, v, position, id);
		     
		     //use the position from ArrayAdapter to locate productID
			 String productName = (String) getListAdapter().getItem(position);
			 String productID = productIDs[position];
			 
			 //get api_token that is in SharedPreferences
			 SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
			 String api_token = settings.getString("api_token", "");
			 
			 //Calling AsyncTask to POST to line_items with the productID to purchase
			 gettingStuffPost gettingPost = new gettingStuffPost();
			 gettingPost.execute(api_token, productID);
	        
			  
		}
	   
	   //retrieve data from stores_controllers using the store_id from Bundle
	   public String retrieveData(String api_token_from_mobile) throws IOException{
			  //Create an intermediate to connect with the web
			  HttpClient httpClient = new DefaultHttpClient();
			  
			  //use bundle to locate the store id and go to the right link
			  Bundle b = getIntent().getExtras();
			  String store_id = b.getString("ID");
			  //Sending GET request, and thus parameter is sent through URL
			  HttpGet httpGet = new HttpGet("http://192.168.43.145:3000/stores/"+ store_id + ".json?api_token_from_mobile=" + api_token_from_mobile);
		      
			  String stringedresponse = "";
			  try{
				  //execute a request and assign the execution result to HttpResponse
				  HttpResponse httpResponse = httpClient.execute(httpGet);
				  System.out.println(httpResponse);
				  
				  //getEntity() obtains the message entity of this response
				  //getContent() creates a new §
				  InputStream inputStream = httpResponse.getEntity().getContent();
				  //we have a byte stream, now convert it to a character stream
				  InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				  
				  //we have to wrap the existing reader (InputStreamReader) and buffer the input
				  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				  
				  //append the separately coming chunks into one string element
				  StringBuilder stringBuilder = new StringBuilder();
				  String bufferedStrChunk = null;
				  
				  
				  //There could be many buffered chunks and we have to go through each of the characters
				  //and assign each chunk to bufferedStrChunk String variable and append that value one 
				  //one to the stringBuilder
				  while((bufferedStrChunk = bufferedReader.readLine()) != null){
					  stringBuilder.append(bufferedStrChunk);
				  }
				  bufferedReader.close();
				  stringedresponse = stringBuilder.toString();
			  }    
			       catch (ClientProtocolException e) {
			        e.printStackTrace();
			        Log.e("ClientProtocol",""+e);
			  }
			return stringedresponse;
			 
		    }
			 //Processing data that is sent from retrieveData to turn them into listable objects 
			 public void ProcessData(String bufferedandappended) throws IllegalStateException, 
			 IOException, JSONException, NoSuchAlgorithmException
			 {
				    
				    //putting all the buffered data into JSONArray because the data itself came as an array in a bracket [{...}{...}...]
				    JSONArray array2 = new JSONArray(bufferedandappended);
				    productNames = new String[array2.length()]; //declaring storeNames array size here
				    productIDs = new String[array2.length()]; //declaring storeIDs array size here
				    
				    //for loop to retrieve stringed JSONObjects into a string array to be displayed
					 for(int i=0; i<array2.length(); i++){
				        //array2.getJSONObject(i).getJSONObject("product").getString("title");
				       
				    	 //setting values array to take in string of all usernames
				    	 productNames[i] = array2.getJSONObject(i).getJSONObject("product").getString("title");
				         productIDs[i] = array2.getJSONObject(i).getJSONObject("product").getString("id");
				        }
					
					 //moved this adapter from onCreate so that AsyncTask works properly
					//ArrayAdapter to put data (storeNames) and list it 
					 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
									R.layout.products, R.id.products_content, productNames);
					 setListAdapter(adapter);
				   
			 }
			 
			//AsyncTask for getting data for productNames and productIDs
			 private class gettingStuff extends AsyncTask<String, Integer, String>
			 {
				 @Override
				 protected String doInBackground(String...api_token_from_mobile) 
				 {
					 String token = api_token_from_mobile[0];
					 try {
						 return retrieveData(token);
					 } catch(Exception e) {
						 Log.v("Exception retrieving data", 
							   "Exception:" +e.getMessage());
						 return "";
					 }
				 }
				 @Override
				 protected void onPostExecute(String result) 
				 {
					 try {
						 ProcessData(result);
					 } catch(Exception e) {
						 Log.v("Exception processing data",
							   "Exception:" +e.getMessage());
					 }
				 }
			 }
			 
		
			 
		
	
	//AsyncTask for getting data after posting to line_items_controller for purchase
	private class gettingStuffPost extends AsyncTask<String, Integer, String>
	{
			@Override
			protected String doInBackground(String...params) 
			{
				String token = params[0];
				String productID = params[1];
					 try {
						 return retrieveDataPost(token, productID);
					 } catch(Exception e) {
						 Log.v("Exception retrieving data", 
							   "Exception:" +e.getMessage());
						 return "";
					 }
				 }
			@Override
			protected void onPostExecute(String result) 
				 {
					 try {
						 ProcessDataPost(result);
					 } catch(Exception e) {
						 Log.v("Exception processing data",
							   "Exception:" +e.getMessage());
					 }
				 }
	}
			 
	//retrieving data from Posting to line_items_controller. Called from gettingStuffPost 
	public String retrieveDataPost(String token, String productID)
	{
				 DefaultHttpClient client = new DefaultHttpClient();
				 //Post request is difference than GET because values are not passed in the URL
				 ////NOTE THAT I HAD A WEIRD EXPERIENCE HERE
				 // if I get JSONObject error and line_items controller doesn't seem to send JSON
				 // then change to /line_items?product_id=" +product_id and see what happens
				 HttpPost post = new HttpPost("http://192.168.43.145:3000/line_items");
				 //?product_id="+ productID
				 InputStream is = null;
		    	 String result = "";
				//JSONObject holder to send in data as JSONObject
			     JSONObject holder = new JSONObject();
				 //putting the productID passed in to product_id for server
			  try{
			     holder.put("product_id", productID);
			     holder.put("api_token_from_mobile", token);
			     Log.e("Post product to line_items", "sending product id =" + holder.toString());
				 StringEntity se = new StringEntity(holder.toString());
			     post.setEntity(se);
			     post.setHeader("Content-Type","application/json");
			  } catch (UnsupportedEncodingException e) {
			    	Log.e("Error in PRODUCTS JAVA???",""+e);
			        e.printStackTrace();
			    } catch (JSONException js) {
			    	
			    	js.printStackTrace();
			      }
			  HttpResponse response = null;
			  
			    //execute POST!
			    try {
			        response = client.execute(post);
			        System.out.println(response);
			    } catch (ClientProtocolException e) {
			        e.printStackTrace();
			        Log.e("ClientProtocol",""+e);
			    } catch (IOException e) {
			        e.printStackTrace();
			        Log.e("IO",""+e);
			    }
				
			    
			    HttpEntity entity = response.getEntity();
			    
			    if (entity != null) {
			        try {
			            
			        	is = entity.getContent(); 
			        } catch (IOException e) {
			        	Log.e("IO E",""+e);
			            e.printStackTrace();
			        }
			    }
		        
			   try{
			    	BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			    	StringBuilder sb = new StringBuilder();
			    	String line = null;
			    	while ((line = reader.readLine()) != null) {
			    		sb.append(line + "\n");
			    	}
			    	is.close();
			    	result=sb.toString();
			    	Log.e("test at Product.java", result);
			    } catch(Exception e){
			        Log.e("log_tag", "Error converting result "+e.toString());
			    }
			   //System.out.println(result);
			   return result;
			   
			 }
	
	//Processing data from that was turned into string from posting to line_items_controller
	public void ProcessDataPost(String result)
	{
				 try{
						JSONObject jsonObject = new JSONObject(result);
						JSONObject tabObject = jsonObject.getJSONObject("cart"); 
						cartID = tabObject.getString("id"); 
						//cartID = result;
						Log.e("test", result);
						System.out.println("still not working?"); 
						System.out.println(cartID);
						
						 SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
					     SharedPreferences.Editor mEditor = settings.edit();
					     mEditor.putString("cart_ID", cartID);
					     mEditor.commit();
					     
					     Intent i = new Intent(this, Tab.class);
					       Bundle c = new Bundle();
					       //ID is what Tab.java will use to make HttpGET request along with api_token
					       //cartID = "128";
					       c.putString("cart_ID", cartID);
					       i.putExtras(c);
						   startActivity(i);
						   finish(); 
						
						
						} catch(Exception e){
							Log.e("JSON error in cart", ""+e);
							
						} 
			 }
				    
}
