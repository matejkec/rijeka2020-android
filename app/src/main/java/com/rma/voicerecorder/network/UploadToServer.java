package com.rma.voicerecorder.network;

import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

//import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadToServer extends AsyncTask<Void, Void, Void>{

    private String urlAddress;
    private File file;
    private NetworkOperationFinished myNetworkOperationListener;
    private String finalResponse="";

    public UploadToServer(String url, File file) {
        this.urlAddress = url;
        this.file = file;
    }

    // Kraj mrezne operacije
    public interface NetworkOperationFinished {
        void onNetworkOperationFinished(String response);
    }

    public void setNetworkOperationFinished(NetworkOperationFinished inputListener){
        this.myNetworkOperationListener = inputListener;
    }

    // PRIJE izvrsavanja zadatka
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            postData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // NAKON izvrsavanja zadatka
    @Override
    protected void onPostExecute(Void result) {
        // podizanje eventa; kazimo "nadredjenoj" aktivnosti da je mrezna operacija zavrsila:
        if (myNetworkOperationListener != null)
            myNetworkOperationListener.onNetworkOperationFinished(finalResponse);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void postData() {
        finalResponse = "";

        try {
            final MediaType MEDIA_TYPE = MediaType.parse(getMimeType(file.getAbsolutePath()));
            final OkHttpClient client = new OkHttpClient();

            // Generiranje tijela zahtjeva (payload):
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("audioFile", file.getName(),
                            RequestBody.create(file, MEDIA_TYPE))
                    .build();

/*            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            json.put("Label", "Label");
            json.put("AudioCaption", "Caption");
            json.put("AudioDescription", "Desc");
            json.put("AudioFile", "");*/

            // Generiranje zahtjeva:
            Request request = new Request.Builder()
                    .url(urlAddress)
                    .post(requestBody)
                    .build();

            // Primanje odgovora:
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                finalResponse = "";
                return;
            }

            String feedback = response.body().string();
            Thread.sleep(1000);
            if (feedback.equals("200")) {
                finalResponse = feedback;
            }

        } catch (Exception ex) {
            finalResponse = "500";
            ex.printStackTrace();
        }
    }
}
