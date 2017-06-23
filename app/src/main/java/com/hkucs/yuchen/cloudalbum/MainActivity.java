package com.hkucs.yuchen.cloudalbum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button btn_record;
    private Button btn_upload;
    private ListView list;
    private TextView tv_path;
    private TextView tv_response;

    private static final int SELECT_VIDEO = 3;
    private static final int FINISH_RECORD = 4;
    private String selectedPath;
    private List<HashMap<String, Object>> mylist;
    private SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //btn_record
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent launchactivity= new Intent(MainActivity.this,CameraActivity.class);
                //startActivity(launchactivity);

                launchactivity.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(launchactivity, "Select a Video "),FINISH_RECORD );
            }
        });

        //btn_upload
        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                chooseVideo();
            }
        });

        //btn_refresh
        btn_upload = (Button) findViewById(R.id.btn_refresh);
        btn_upload.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                refreshVideoList();
            }
        });

//        //textView
//        tv_path = (TextView) findViewById(R.id.tv_path);
//        tv_response = (TextView) findViewById(R.id.tv_response);

        refreshVideoList();

        //list
        list = (ListView) findViewById(R.id.videoList);
        mylist = new ArrayList<HashMap<String, Object>>();

        //configure adapter
        adapter = new SimpleAdapter(this,
                mylist,//data source
                R.layout.list_item,//layout
                new String[] {"itemTitle","itemImg"}, //attribute
                new int[] {R.id.itemTitle,R.id.itemImg}); //id
        adapter.setViewBinder(new SimpleAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                    ImageView iv = (ImageView) view;
                    Bitmap bm = (Bitmap) data;
                    iv.setImageBitmap(bm);
                    return true;
                }
                return false;

            }
        });

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @SuppressWarnings("unchecked")
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ListView listView = (ListView)parent;
                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                String itemTitle = (String)map.get("itemTitle");
                Intent launchactivity_3= new Intent(MainActivity.this,VideoActivity.class);
                launchactivity_3.putExtra("videoName", itemTitle);
                ArrayList<String> arrayList = new ArrayList<String>();
                for (int i=0;i<mylist.size();i++){
                    arrayList.add((String)mylist.get(i).get("itemTitle"));
                }
                launchactivity_3.putStringArrayListExtra("arrayList",arrayList);
                startActivity(launchactivity_3);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            }
        });


    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a Video "), SELECT_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==FINISH_RECORD){
            refreshVideoList();
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                System.out.println("SELECT_VIDEO");
                Uri selectedImageUri = data.getData();
                selectedPath = getPath(selectedImageUri);
                //tv_path.setText(selectedPath);
            }
            uploadVideo();
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();

        return path;
    }

    private void uploadVideo() {
        class UploadVideo extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(MainActivity.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                uploading.dismiss();
                //tv_response.setText(Html.fromHtml("<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>"));
                //tv_response.setMovementMethod(LinkMovementMethod.getInstance());
            }

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                System.out.println("******upload**"+selectedPath);
                String msg = u.uploadVideo(selectedPath);
                return msg;
            }
        }

        UploadVideo uv = new UploadVideo();
        uv.execute();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.setMessage(
                "Uploaded suceesfully").create();
        final AutoCloseDialog d = new AutoCloseDialog(dialog);
        d.show(1000);

        refreshVideoList();
    }

    private void refreshVideoList(){
        String stringUrl = "http://i.cs.hku.hk/~htlin/php/show.php";
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebPageTask().execute(stringUrl);
        } else {
            System.out.println("No network connection available.");
        }
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e){
                return "Unable to retrieve web page.URL maybe invalid.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            String[] linesOfFiles = result.split("<br>");
            mylist.clear();
            Bitmap[] Image = new Bitmap[linesOfFiles.length-1];
            for(int i=0;i<linesOfFiles.length-1;i++){
                System.out.println("The file name is: "+linesOfFiles[i]);
                String S = "http://i.cs.hku.hk/~htlin/php/videos/"+linesOfFiles[i];
                Bitmap picture = createVideoThumbnail(S,10,10);
                Image[i] = getResizedBitmap(picture,100,100);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("itemTitle",linesOfFiles[i]);
                map.put("itemImg",Image[i]);
                mylist.add(map);
            }
            adapter.notifyDataSetChanged();

        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            int len = 5000;
            try{
                System.out.println("URL="+myurl);
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000/*milliseconds*/);
                conn.setConnectTimeout(15000/*milliseconds*/);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                String contentAsString = readIt(is,len);
                return contentAsString;
            }finally {
                if(is!=null){
                    is.close();
                }
            }
        }
    }

    private String readIt(InputStream stream, int len) throws IOException,UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream,"UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


}


