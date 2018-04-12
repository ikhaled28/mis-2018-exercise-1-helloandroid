// Assigment 1
// Mohammad Izabul Khaled - 119013
// Tanveer Al Jami - 119118

package com.example.mis.helloandroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {

    public EditText url_text_box;
    public TextView planeView;
    public Button connectBtn;
    public Button toggleButton;
    public ImageView myImageView;
    private ProgressDialog dataLoadingProgress;
    public  boolean isImageAvailable;
    public  boolean isClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataLoadingProgress = new ProgressDialog(this);
        isImageAvailable = false;
        url_text_box = findViewById(R.id.enter_url);
        planeView = findViewById(R.id.displayText);
        connectBtn = findViewById(R.id.connect);
        toggleButton = findViewById(R.id.toggleButton);
        myImageView = (ImageView) findViewById(R.id.show_image);

        planeView.setVisibility(View.VISIBLE);
        myImageView.setVisibility(View.INVISIBLE);

        planeView.setMovementMethod(new ScrollingMovementMethod());

        if(!isImageAvailable){
            toggleButton.setEnabled(false);
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if(!isClicked){
                    toggleButton.setText("PLANE TEXT");
                    myImageView.setVisibility(View.VISIBLE);
                    planeView.setVisibility(View.INVISIBLE);
                    isClicked = true;
                } else {
                    toggleButton.setText("IMAGE");
                    myImageView.setVisibility(View.INVISIBLE);
                    planeView.setVisibility(View.VISIBLE);
                    isClicked = false;
                }
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
            if(url_text_box.getText().length() != 0){
                if (isOnline(getApplicationContext())) {
                    if (planeView.getText().length() != 0) {
                        planeView.setText("");
                        if (myImageView != null){
                            myImageView.setImageResource(android.R.color.transparent);
                        }
                        isImageAvailable = false;
                        isClicked = false;
                        toggleButton.setEnabled(false);
                        toggleButton.setText("IMAGE");
                    }
                    String page = url_text_box.getText().toString();
                    if (URLUtil.isValidUrl(page)) {
                        new urlCaller().execute(page);
                    } else {
                        postToastMessage("URL is not valid");
                    }
                } else {
                    postToastMessage("No Internet Connection");
                }
            }else {
                postToastMessage("Please Enter URL First.");
            }
             hideKeyboard();
            }
        });
    }

    public void postToastMessage(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    //https://stackoverflow.com/questions/43061216/dismiss-keyboard-on-button-click-that-close-fragment
    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    class urlCaller extends AsyncTask<String, Void, String> {
        boolean failure = false;
        int errorCode = 0;
        String str;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataLoadingProgress.setMessage("Loading...");
            dataLoadingProgress.setIndeterminate(false);
            dataLoadingProgress.setCancelable(true);
            dataLoadingProgress.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URLConnection connection = (new URL(args[0])).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                // Read and store the result line by line then return the entire string.
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder html = new StringBuilder();
                String type = connection.getHeaderField("Content-Type");
                if (type.startsWith("image")) {
                    // Download image
                    isImageAvailable = true;
                    new imageCaller().execute(args[0]);
                }
                for (String line; (line = reader.readLine()) != null; ) {
                    html.append(line);
                }
                in.close();
                str = html.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                postToastMessage(e.toString());
            } catch (UnknownHostException e){
                e.printStackTrace();
                postToastMessage(e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                postToastMessage(e.toString());
            }catch (Exception e) {
                e.printStackTrace();
                postToastMessage(e.toString());
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            try {
                if ((dataLoadingProgress != null) && dataLoadingProgress.isShowing()) {
                    if(!isImageAvailable) {
                        dataLoadingProgress.dismiss();
                    }
                    planeView.setVisibility(View.VISIBLE);
                    myImageView.setVisibility(View.INVISIBLE);
                    planeView.setText(str.toString());
                }
            } catch (final IllegalArgumentException e) {
            } catch (final Exception e) {
            } finally {
            }
        }
    }

    private class imageCaller extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                //Log.d(TAG,e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            if ((dataLoadingProgress != null) && dataLoadingProgress.isShowing()) {
                dataLoadingProgress.dismiss();
                myImageView.setVisibility(View.INVISIBLE);
                toggleButton.setEnabled(true);
                ImageView imageView = (ImageView) findViewById(R.id.show_image);
                imageView.setImageBitmap(result);
            }
        }
    }
}