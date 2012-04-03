package com.isaac.awesome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class Order extends Activity implements OnClickListener{
	private EditText userPasswordView;
	TextView tv;
	 
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
		 	super.onCreate(savedInstanceState);
		 	
		 	setContentView(R.layout.order);
		    tv = (TextView) this.findViewById(R.id.look);
	        
		    findViews();
		    setClickListeners();
		 	
	    }
	    
	    /* Get user interface elements*/
	    private void findViews (){
	    	userPasswordView = (EditText) findViewById(R.id.user_password);
	    	
	    }
	    
	    // For the purchase button
	    public void setClickListeners() {
	    	//BUTTON LISTENERS
	        View purchaseButton = findViewById(R.id.purchase_button);
	        purchaseButton.setOnClickListener(this);
	    }
	    
	    @Override
		public void onClick(View v) {		
			switch (v.getId()) {
			case R.id.purchase_button:   		
	    		v.performHapticFeedback(HapticFeedbackConstants.
	    				FLAG_IGNORE_VIEW_SETTING); //Haptic feedback is cool
	    		//Once the text boxes have been filled in we can post the data to the Rails server
	    		//purchaseNow();  
	    		//Toast.makeText(AddEventView.this, "Clicked A Button!", Toast.LENGTH_SHORT).show();
	    		
	    		purchaseNow();
	    		
	    		break;
			}
	    }
	    
	    public void purchaseNow()
	    {
	    	
	    	DefaultHttpClient client = new DefaultHttpClient();
	    	HttpPost post = new HttpPost("http://192.168.43.145:3000/orders");
	    	/* START p-xr.com*/
	    	InputStream is = null;
	    	 String result = ""; //result of everything i receive from rails. have to parse JSON from here
	    	 //JSONObject jArray = null;
	    	 String jsonReturnedStringed = ""; //this is where im storing api_token generated from rails
	    	
	    	//get cart_id that was sent in from Tab.java
	    	 Bundle d = getIntent().getExtras();
			 String cart_id = d.getString("cart_ID");
			 System.out.println(cart_id);
			//get api_token in SharedPref
			 SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
			 String api_token = settings.getString("api_token", "");
	    	 
	    	
	    	JSONObject holder = new JSONObject();
		    JSONObject orderHolder = new JSONObject();
		    JSONObject orderHolderTotal = new JSONObject();
		    
		    //send through JSONObjects: cart_id, password, api_token
		    try {	
		    	holder.put("api_token_from_mobile", api_token);
		    	holder.put("password", userPasswordView.getText().toString());
			    holder.put("cart_id", cart_id);
		    	//have to put in cart_id separately because it has to be part of params[:order] hash
		    	orderHolder.put("password", userPasswordView.getText().toString());
			    //orderHolder.put("cart_id", cart_id);
			    orderHolderTotal.put("order", orderHolder);
			    orderHolderTotal.put("api_token_from_mobile", api_token);
			    orderHolderTotal.put("cart_id", cart_id);
			    //orderHolderTotal.put("password", userPasswordView.getText().toString());
			   //NOTE THIS MIGHT BE THE RIGHT WAY TO SEND THEM IN because it yields
			   //params order hash = {"order":{"password":"4129340","cart_id":"265"},"api_token_from_mobile":"0118d022ce7815e6cb20ea33ea8429c07c77ae14"}


			   // Log.e("Ordering via JSON", "Ordering via JSON = "+ holder.toString());
			    Log.e("params order hash", "params order hash = "+ orderHolderTotal.toString());
		    	
			    StringEntity se = new StringEntity(orderHolderTotal.toString());
		    	
		    	post.setEntity(se);
		    	post.setHeader("Content-Type","application/json");
		 	    
		    	
		    }
		    catch (UnsupportedEncodingException e) {
		    	Log.e("Error",""+e);
		        e.printStackTrace();
		    } catch (JSONException js) {
		    	js.printStackTrace();
		    }

		    HttpResponse response = null;
		  
		    try {
		        response = client.execute(post);
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
		            /* entity.consumeContent();*/
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
		    	//System.out.println(result);
		    } catch(Exception e){
		        Log.e("log_tag", "Error converting result "+e.toString());
		    }
		   
		   System.out.println(result);
	        
		   try {
				JSONObject jsonObject = new JSONObject(result);
				JSONObject apiObject = jsonObject.getJSONObject("order"); 
				/*tokenplease = apiObject.toString();*/
				/*JSONObject jsonObject2 = jsonObject.getJSONObject("user"); */
				jsonReturnedStringed = apiObject.getString("id");
				Log.e("test at Order.java", result);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     tv.setText(jsonReturnedStringed);
		     
		    
		     //should get the order_id
		     //System.out.println(jsonReturnedStringed);
		     
		
			  
		   
		    
		} //end of signIn()
	}