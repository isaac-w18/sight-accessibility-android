package com.example.finalproject;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class TagGetter {

    // Full disclosure:
    // ChatGPT helped me with this method, since I could not, for the life of me, get the threads to sync.
    // runOnUiThread wouldn't work and I tried everything. All the main logic was mine, it just helped me
    // with the thread syncing and using the Future<> class and Callable<> class

    // Returns labels that have over 85% probability.
    // If there is no label that has over 85% probability, returns only the first label.
    public static String getTopTags(Bitmap bitmap) {
//        final String[] topTags = {""};
        ExecutorService pool = Executors.newFixedThreadPool(3);
        Callable<String> task = () -> {
            try {
                return JSONtoTopTags(myVisionTester(bitmap));
            } catch (Exception ioe) {
                Log.v("Vision Test Failed", ioe.getMessage());
                return "";
            }
        };

        // Submit the task and get the Future
        Future<String> future = pool.submit(task);

        // Wait for the task to complete and get the result
        String topTags = "";
        try {
            topTags = future.get(); // This will block until the result is available
            Log.v("getTopTags", topTags);
        } catch (Exception e) {
            Log.v("Error", e.getMessage());
        } finally {
            pool.shutdown(); // Shut down the executor to release resources
        }

        Log.v("Finished: topTags", topTags);
        return topTags;
    }


//                    foodTitle[1] = new String(myVisionTester(R.drawable.salad));

//                    Log.v("Vision Test Real Result", topTags[0]);
//                    Log.v("Vision Test Result", topTags[0]);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            data.add(new FoodItem(R.drawable.boorsoki, foodTitle[0]));
////                            data.add(new FoodItem(R.drawable.salad, foodTitle[1]));
////                            adapter.notifyDataSetChanged();
//                        }
////                    });
//        pool.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    topTags[0] = JSONtoTopTags(myVisionTester(bitmap));
//                    Log.v("getTopTags", topTags[0]);
//
//                } catch (Exception ioe) {
//                    Log.v("Vision Test Failed", ioe.getMessage());
//                }
//            }
//        });
//
//        Log.v("Finished: topTags", topTags[0]);
//        return topTags[0];

    private static String JSONtoTopTags(JSONObject json) throws JSONException {
        JSONArray jsonArray = json.getJSONArray("responses");

        String tags = "";

        JSONObject responses = jsonArray.getJSONObject(0);
        JSONArray labelAnnotations = responses.getJSONArray("labelAnnotations");

        for (int j = 0; j < labelAnnotations.length(); j++) {
            JSONObject response = labelAnnotations.getJSONObject(j);
            double score = response.getDouble("score");

            String tag = response.getString("description");
            if (score >= 0.85) {
                if (j > 0) {
                    tags += ", ";
                }
                tags += tag;
            } else {
                if (j == 0) {
                    tags = tag;
                }
                break;
            }
        }
        return tags;
    }

    private static JSONObject myVisionTester(Bitmap bitmap) throws IOException, JSONException {
        //1. ENCODE image.
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bout);
        Image myimage = new Image();
        myimage.encodeContent(bout.toByteArray());

        //2. PREPARE AnnotateImageRequest
        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
        annotateImageRequest.setImage(myimage);
        Feature f = new Feature();
        f.setType("LABEL_DETECTION");
        f.setMaxResults(10);
        List<Feature> lf = new ArrayList<Feature>();
        lf.add(f);
        annotateImageRequest.setFeatures(lf);

        //3.BUILD the Vision
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyB36MBRgHGaDhimbD57P3Lyg3S_tfOl8z0"));
        Vision vision = builder.build();

        //4. CALL Vision.Images.Annotate
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        List<AnnotateImageRequest> list = new ArrayList<AnnotateImageRequest>();
        list.add(annotateImageRequest);
        batchAnnotateImagesRequest.setRequests(list);
        Vision.Images.Annotate task = vision.images().annotate(batchAnnotateImagesRequest);
        BatchAnnotateImagesResponse response = task.execute();

//        Log.v("Response", response.toPrettyString());
//        String description = jsonObject.getString("responses");

        return new JSONObject(response);
//        Log.v("MYTAG", response.toPrettyString());
    }

}
