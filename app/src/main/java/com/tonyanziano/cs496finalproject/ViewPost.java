package com.tonyanziano.cs496finalproject;

import android.app.DownloadManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ViewPost extends AppCompatActivity {

    //entry point URL
    final public String entryPointURL = "http://www.tonyanziano.com:8080/";
    //declare username string that we will use throughout activity
    public String username;
    //declare string for title of post we are viewing
    public String title;
    //string for content of post we are viewing
    public String postContent;
    //volley queue to process requests
    public RequestQueue queue;
    //references for view comments page
    public TextView originalContent, originalTitle, originalAuthor;
    //references to Edit and Delete Post buttons so that we can hide them
    public Button editBtn, deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        //get username and post title from intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        title = intent.getStringExtra("title");

        //get views
        originalContent = (TextView) findViewById(R.id.view_post_content);
        originalAuthor = (TextView) findViewById(R.id.view_post_author);
        originalTitle = (TextView) findViewById(R.id.view_post_title);
        editBtn = (Button) findViewById(R.id.button_edit_post);
        deleteBtn = (Button) findViewById(R.id.button_delete_post);
        //hide buttons
        editBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.INVISIBLE);

        //initialize the queue
        queue = Volley.newRequestQueue(this);

        //fetch post data
        fetchPostData();
        //fetch comments
        fetchComments();
    }

    //function that will fetch comments and populate the screen view
    public void fetchComments(){
        //make url
        String url = "";
        try {
            url = entryPointURL + "comments/" + URLEncoder.encode(title, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            Log.e("Error", "Caught encoding exception: " + e);
        }

        //make request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new JSONArray(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //iterate through the response and create a view for each comment
                commentsToViews(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "Volley error with GET request for all comments: " + error);
            }
        });

        //send request
        queue.add(request);
    }

    //function that will take array of comment objects
    //and convert them to views to be added to screen
    public void commentsToViews(JSONArray array){
        //grab comments section view
        LinearLayout commentsSection = (LinearLayout) findViewById(R.id.comments_section);
        //iterate through each comment object and create a view
        for(int i = 0; i < array.length(); i++){
            try {
                String author = array.getJSONObject(i).getJSONObject("author").getString("username");
                String content = array.getJSONObject(i).getString("content");
                String date = array.getJSONObject(i).getString("date");
                Log.d("Debug", "Element: " + author + " " + content + " " + date);
                //now create a view to house each element
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                TextView authorView = new TextView(this);
                TextView contentView = new TextView(this);
                authorView.setText("Author: " + author);
                authorView.setTextSize(18);
                contentView.setText(content);
                contentView.setTextSize(15);
                //add to parents
                linearLayout.addView(authorView);
                linearLayout.addView(contentView);
                commentsSection.addView(linearLayout);

            } catch(JSONException e) {
                Log.e("Error", "Caught JSON object exception: " + e);
            }
        }
    }

    //function that will fetch post data from the API
    public void fetchPostData(){
        //make URL
        String url = "";
        try {
            url = entryPointURL + "posts/" + URLEncoder.encode(title, "UTF-8");
        } catch(UnsupportedEncodingException e){
            Log.e("Debug", "Caught encoding exception: " + e);
        }
        //process charac

        //construct JSON object
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", title);
        } catch (JSONException e) {
            Log.e("Error", "Caught JSON exception: " + e);
        }
        //create request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //get data from the post
                    String author = response.getJSONObject("author").getString("username");
                    String title = response.getString("title");
                    String content = response.getString("content");
                    //now populate our views with it
                    String authorFull = "Author: " + author;
                    originalAuthor.setText(authorFull);
                    originalTitle.setText(title);
                    originalContent.setText(content);
                    postContent = content;
                    Log.d("Debug", "Current user: " + username + "\nAuthor: " + author);
                    //check if current user is author
                    if(username.equals(author)){
                        Log.d("Debug", "Current user is author.");
                        //show edit / delete buttons
                        showButtons(editBtn, deleteBtn);
                    }
                } catch (JSONException e) {
                    Log.e("Error", "Caught JSON exception: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "Volley error: " + error);
            }
        });
        //submit request
        queue.add(request);
    }

    //function to show delete and edit buttons
    public void showButtons(Button btn1, Button btn2){
        btn1.setVisibility(View.VISIBLE);
        btn2.setVisibility(View.VISIBLE);
    }

    //prompts the user when pressing the delete button
    public void promptDeletePost(View view){
        RelativeLayout promptContainer = (RelativeLayout) findViewById(R.id.delete_post_prompt);
        promptContainer.setVisibility(View.VISIBLE);
    }

    //cancels the delete post prompt
    public void cancelDeletePost(View view){
        RelativeLayout promptContainer = (RelativeLayout) findViewById(R.id.delete_post_prompt);
        promptContainer.setVisibility(View.INVISIBLE);
    }

    //function to delete post if user is author
    public void deletePost(View view){
        //delete post (and all comments) and take back to home
        //make url
        String url = "";
        try {
            url = entryPointURL + "posts/" + URLEncoder.encode(title, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            Log.e("Error", "Caught encoding exception: " + e);
        }

        //make JSON object
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", title);
        } catch(JSONException e) {
            Log.e("Error", "Caught JSON object exception: " + e);
        }

        //make request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //get status
                try {
                    int responseStatus = response.getInt("status");
                    if(responseStatus == 1){
                        //success, post has been deleted
                        //create toast
                        Toast success = Toast.makeText(ViewPost.this, R.string.delete_post_success, Toast.LENGTH_SHORT);
                        success.setGravity(Gravity.BOTTOM, 0, 50);
                        success.show();
                        //take us back to home screen
                        //make dummy view so that we can use goBack()
                        View dummyView = new View(ViewPost.this);
                        goBack(dummyView);
                    }
                } catch(JSONException e) {
                    Log.e("Error", "Caught JSON object exception: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", "Volley error sending delete request: " + error);
            }
        });

        //submit request
        queue.add(request);
    }

    //function to edit post if user is author
    public void editPost(View view){
        //create intent with username and post title
        Intent intent = new Intent(this, EditPost.class);
        intent.putExtra("title", title);
        intent.putExtra("username", username);
        intent.putExtra("content", postContent);
        //call new activity
        startActivity(intent);
    }

    //function to leave comment
    public void createComment(View view){
        //create intent with username and post title
        Intent intent = new Intent(ViewPost.this, CreateComment.class);
        intent.putExtra("username", username);
        intent.putExtra("title", title);
        //call activity
        startActivity(intent);
    }

    //function to go back to home screen
    public void goBack(View view){
        //create intent with the user's name
        Intent intent = new Intent(this, Home.class);
        intent.putExtra("username", username);
        //call home activity
        startActivity(intent);
    }

    //TODO (optional)
    //function to edit comment if user is author
}
