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


public class AwesomeActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	
	private EditText userEmailView;
	private EditText userPasswordView;
	TextView tv1;
	
	//SharedPreferences settings;
    //Editor mEditor = prefs.edit();
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*  
        SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
  	    String api_token = settings.getString("api_token", "");
        
  	  if (api_token != null) //if api_token is saved, then just log in automatically and go to Welcome actiity
        {
        	Intent i = new Intent(this, Welcome.class);
        	startActivity(i);
        	finish();
        }
        else //else log in 
        { */
  	    setContentView(R.layout.main);
     
        tv1 = (TextView) this.findViewById(R.id.display);
        
       
        findViews();
        setClickListeners();
        //}
    }
    
    /* Get all user interface elements here*/
    private void findViews (){
    	userEmailView = (EditText) findViewById(R.id.user_email);
    	userPasswordView = (EditText) findViewById(R.id.user_password);
    	
    }
    
    public void setClickListeners() {
    	//BUTTON LISTENERS
        View logInButton = findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(this);
    }
    
    @Override
	public void onClick(View v) {		
		switch (v.getId()) {
		case R.id.log_in_button:   		
    		v.performHapticFeedback(HapticFeedbackConstants.
    				FLAG_IGNORE_VIEW_SETTING); //Haptic feedback is cool
    		//Once the text boxes have been filled in we can post the data to the Rails server
    		signIn();  
    		//Toast.makeText(AddEventView.this, "Clicked A Button!", Toast.LENGTH_SHORT).show();
    		break;
		}
		
	}
    
    public void signIn(){
    	
   
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost("http://192.168.43.145:3000/api_tokens");
    	/* START p-xr.com*/
    	InputStream is = null;
    	 String result = ""; //result of everything i receive from rails. have to parse JSON from here
    	 //JSONObject jArray = null;
    	 String tokenplease = ""; //this is where im storing api_token generated from rails
    	
    	
    	 /* END */
    	
    	 /* hard part right here because rails app needs to take this data in */
    	JSONObject holder = new JSONObject();
	    JSONObject logInInfo = new JSONObject();
	    
	    String emailAddy = userEmailView.getText().toString();
	    String speedEmail = "lim.smile@gmail.com";
	    String speedPassword = "4129340";
	   
	    try {	
	    	//logInInfo.put("email", emailAddy);
		    //logInInfo.put("password", userPasswordView.getText().toString());
		    //Speed things up
	    	logInInfo.put("email", speedEmail);
		    logInInfo.put("password", speedPassword);
		    ///end
	    	
	    	
		    holder.put("log_in", logInInfo);
		    
		    Log.e("Login JSON", "Login JSON = "+ holder.toString());
		    
	    	StringEntity se = new StringEntity(holder.toString());
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
			JSONObject apiObject = jsonObject.getJSONObject("user"); 
			/*tokenplease = apiObject.toString();*/
			/*JSONObject jsonObject2 = jsonObject.getJSONObject("user"); */
			tokenplease = apiObject.getString("api_token");
			Log.e("test at Awesome.java", result);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     tv1.setText(tokenplease);
	     SharedPreferences settings = getSharedPreferences("myDataStorage", MODE_PRIVATE);
	     SharedPreferences.Editor mEditor = settings.edit();
	     mEditor.putString("api_token", tokenplease);
	     mEditor.commit();
	    
	     //did this system.out.println to see if the result is the user stuff im expecting and it worked
	     // System.out.println(result);
	     
	 if (tokenplease != null){
	     Intent i = new Intent(this, Welcome.class);
	     // bundle is passing data to Welcome activity
	     //Bundle b = new Bundle();
	     //b.putString("token", tokenplease);
	     //i.putExtras(b);
		 startActivity(i);
		 finish(); //having this here seems to make it faster
		  
	   } 
	    
	} //end of signIn()
}