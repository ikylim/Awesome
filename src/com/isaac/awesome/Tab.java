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


import android.app.Activity; 
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;

public class Tab extends ListActivity {
	String[] lineItems;
	String[] product_IDs;
	
	
	@Override
	   protected void onCreate(Bundle saveInstanceState){
		   super.onCreate(saveInstanceState);
		   //get api_token that is saved on the device right now so that i can make connection to server
		   SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
		   String api_token = settings.getString("api_token", "");
		   String cart_id_from_mobile = settings.getString("cart_ID", "");
		   
		 //use bundle to locate the cart_id and go to the right link
		  
		      Bundle c = getIntent().getExtras();
			  String cart_id = c.getString("cart_ID");
			  //String cart_id = "113";
			  System.out.println(cart_id);
		  
		   
	
		   //AsyncTask stuff
		   gettingStuff getIt = new gettingStuff();
		   getIt.execute(api_token, cart_id_from_mobile); 
		
	   }
	
	
  
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	   super.onListItemClick(l, v, position, id);
	   
	   SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
	   String api_token = settings.getString("api_token", "");
	   String cart_id_from_mobile = settings.getString("cart_ID", "");
	     
	     //use the position from ArrayAdapter to locate productID
		 String lineItem = (String) getListAdapter().getItem(position);
		 String productID = product_IDs[position];
		 
		   Intent i = new Intent(this, Order.class);
	       Bundle d = new Bundle();
	       //ID is what Tab.java will use to make HttpGET request along with api_token
	       //cartID = "128";
	       d.putString("cart_ID", cart_id_from_mobile);
	       i.putExtras(d);
		   startActivity(i);
		   finish(); 
		 
		/*//Don't do this asynctask because POST to order have to be done in Order.java
		  // since I have to type in the password after you click to purchase this
		 //Calling AsyncTask to POST to line_items with the productID to purchase
		 gettingStuffPost gettingPost = new gettingStuffPost();
		 gettingPost.execute(api_token, cart_id_from_mobile);
        */
		  
	}
	
	

	

  
      public String retrieveData(String api_token_from_mobile, String cart_id_from_mobile) throws IOException{
		  //Create an intermediate to connect with the web
		  HttpClient httpClient = new DefaultHttpClient();
		  
		  System.out.println(cart_id_from_mobile + "asdfas");

		  //Sending GET request, and thus parameter is sent through URL
		  HttpGet httpGet2 = new HttpGet("http://192.168.43.145:3000/carts/"+ cart_id_from_mobile+ ".json?api_token_from_mobile=" + api_token_from_mobile);
		   
	      
		  String stringedresponse = "";
		  try{
			  //execute a request and assign the execution result to HttpResponse
			  HttpResponse httpResponse2 = httpClient.execute(httpGet2);
			  //System.out.println("httpResponse");
			  
			  //getEntity() obtains the message entity of this response
			  //getContent() creates a new InputStream object of the entity.
			  
			  InputStream inputStream2 = httpResponse2.getEntity().getContent();
			  //we have a byte stream, now convert it to a character stream
			  InputStreamReader inputStreamReader = new InputStreamReader(inputStream2);
			  
			  //we have to wrap the existing reader (InputStreamReader) and buffer the input
			  BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader);
			  
			  //append the separately coming chunks into one string element
			  StringBuilder stringBuilder2 = new StringBuilder();
			  String bufferedStrChunk2 = null;
			  
			  
			  //There could be many buffered chunks and we have to go through each of the characters
			  //and assign each chunk to bufferedStrChunk String variable and append that value one 
			  //one to the stringBuilder
			  while((bufferedStrChunk2 = bufferedReader2.readLine()) != null){
				  stringBuilder2.append(bufferedStrChunk2);
			  }
			  bufferedReader2.close();
			  stringedresponse = stringBuilder2.toString();
		  }    
		       catch (ClientProtocolException e) {
		        e.printStackTrace();
		        Log.e("ClientProtocol",""+e);
		  }
		return stringedresponse;
		 
	    }
		  
		 public void ProcessData(String bufferedandappended) throws IllegalStateException, 
		 IOException, JSONException, NoSuchAlgorithmException
		 {
			    //changing productIDs[] to product_IDs[] allowed this to work somehow. Even if in different activity, I guess you shouldn't use the same variables
			    //putting all the buffered data into JSONArray because the data itself came as an array in a bracket [{...}{...}...]
			    JSONArray array3 = new JSONArray(bufferedandappended);
			    lineItems = new String[array3.length()]; //declaring storeNames array size here
			    product_IDs = new String[array3.length()]; //declaring storeIDs array size here
			    
			    //for loop to retrieve stringed JSONObjects into a string array to be displayed
				 for(int i=0; i<array3.length(); i++){
			        //array2.getJSONObject(i).getJSONObject("product").getString("title");
			       
			    	 //setting values array to take in string of all usernames
			    	 lineItems[i] = array3.getJSONObject(i).getJSONObject("line_item").getString("id");
			         product_IDs[i] = array3.getJSONObject(i).getJSONObject("line_item").getString("product_id");
			        }
				 
				 
				 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
							R.layout.tab, R.id.tab_content, lineItems);
			     setListAdapter(adapter);
		
			
		 }

		//AsyncTask 
		 private class gettingStuff extends AsyncTask<String, Integer, String>
		 {
			 //@Override
			 protected String doInBackground(String...api_token_from_mobile) 
			 {
				 String token = api_token_from_mobile[0];
				 String cart = api_token_from_mobile[1];
				 try {
					 return retrieveData(token, cart);
				 } catch(Exception e) {
					 Log.v("Exception retrieving data", 
						   "Exception:" +e.getMessage());
					 return "";
				 }
			 }
			 //@Override
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
}
/*
	
		//AsyncTask for getting data after posting to orders_controller for purchase
			private class gettingStuffPost extends AsyncTask<String, Integer, String>
			{
					@Override
					protected String doInBackground(String...params) 
					{
						String token = params[0];
						String cartID = params[1];
							 try {
								 return retrieveDataPost(token, cartID);
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
			public String retrieveDataPost(String token, String cartID)
			{
						 DefaultHttpClient client = new DefaultHttpClient();
						 //Post request is difference than GET because values are not passed in the URL
						 ////NOTE THAT I HAD A WEIRD EXPERIENCE HERE
						 // if I get JSONObject error and line_items controller doesn't seem to send JSON
						 // then change to /line_items?product_id=" +product_id and see what happens
						 HttpPost post = new HttpPost("http://10.0.0.5:3000/orders");
						 InputStream is = null;
				    	 String result = "";
						//JSONObject holder to send in data as JSONObject
					     JSONObject holder = new JSONObject();
						 //manually entering password
					     String password = "4129340";
					  try{
					     holder.put("cart_id", cartID);
					     holder.put("api_token_from_mobile", token);
					     holder.put("password", password);
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
					    	
					    	
					    	
					    	
					    } catch(Exception e){
					        Log.e("log_tag", "Error converting result "+e.toString());
					    }
					   System.out.println(result);
					   return result;
					   
					 }
			
			//Processing data from that was turned into string from posting to line_items_controller
			public void ProcessDataPost(String result)
			{
						 try{
								
								Log.e("test", result);
								System.out.println("did it work?"); 
								System.out.println(result);
								
								
								} catch(Exception e){
									Log.e("JSON error in cart", ""+e);
									System.out.println(result);
								} 
					 }
						    
		}
*/