package com.isaac.awesome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class Welcome extends ListActivity {
  //array of string from JSONObjects will go in here
  String[] storeNames; //array length will be declared all the way in the bottom when JSONArray length is known.
  String[] storeIDs; //array of store_ids saved in corresponding positions as usernames of usernames[] array.
  
   @Override
   protected void onCreate(Bundle saveInstanceState){
	   super.onCreate(saveInstanceState);
	  //getting api_token that is saved in the device
	  SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
	  String api_token = settings.getString("api_token", "");
	  
	  //this is the execution of AsyncTask, and it appears to be working but not sure if this is the right way. 
	  //Originally thought that this is all I needed, but maybe I need to have the try{} and call the methods separately as well?
	  //look into this.
	  gettingStuff getIt = new gettingStuff();
	  getIt.execute(api_token);
	  /*
	  try {
		//this process will put the array of string into values array.  
		  ProcessData(retrieveData(api_token));
		  new gettingStuff().execute(api_token);
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
	/*  ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.welcome, R.id.welcome_content, storeNames);
	  setListAdapter(adapter);
    */   
   }
   
   @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	   super.onListItemClick(l, v, position, id);
	    
		 String storeName = (String) getListAdapter().getItem(position);
		 //items are listed with storeNames, and using the position in the array of that particular storeName
		 //I pass that on because the array of storeIDs have exact same positions that correspond to storeNames
		 String storeID = storeIDs[position];
	     //Toast.makeText(this, storeName + " selected with ID = " + storeID, Toast.LENGTH_LONG).show();
	     
	     //implementing bundle to send necessary data to start a new activity
	     Intent i = new Intent(this, Products.class);
	     Bundle b = new Bundle();
	     //ID is what Products.java will use to make HttpGET request along with api_token
	     b.putString("ID", storeID);
	     i.putExtras(b);
		 startActivity(i);
		 finish(); 
		  
	}
  
   public String retrieveData(String api_token_from_mobile) throws IOException{
	  //Create an intermediate to connect with the web
	  HttpClient httpClient = new DefaultHttpClient();
	  
	  //Sending GET request, and thus parameter is sent through URL
	  HttpGet httpGet = new HttpGet("http://192.168.43.145:3000/stores.json?api_token_from_mobile=" + api_token_from_mobile);
	  String stringedresponse = "";
	  try{
		  //execute a request and assign the execution result to HttpResponse
		  HttpResponse httpResponse = httpClient.execute(httpGet);
		  System.out.println("httpResponse");
		  
		  //getEntity() obtains the message entity of this response
		  //getContent() creates a new InputStream object of the entity.
		  
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
	  
	 public void ProcessData(String bufferedandappended) throws IllegalStateException, 
	 IOException, JSONException, NoSuchAlgorithmException
	 {
		    
		    //putting all the buffered data into JSONArray because the data itself came as an array in a bracket [{...}{...}...]
		    JSONArray array2 = new JSONArray(bufferedandappended);
		    storeNames = new String[array2.length()]; //declaring storeNames array size here
		    storeIDs = new String[array2.length()]; //declaring storeIDs array size here
		    
		    //for loop to retrieve stringed JSONObjects into a string array to be displayed
			 for(int i=0; i<array2.length(); i++){
		       
		       
		    	 //setting values array to take in string of all store names
		    	  storeNames[i] = array2.getJSONObject(i).getJSONObject("store").getString("store_name");
		          storeIDs[i] = array2.getJSONObject(i).getJSONObject("store").getString("id");
		        }
			 
			 
			 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						R.layout.welcome, R.id.welcome_content, storeNames);
			  setListAdapter(adapter);
		   
	 }
		
	 //AsyncTask attempt
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
	 
	 
}



  

  
	

