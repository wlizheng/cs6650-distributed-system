package client;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import service_interface.ImageMetaData;
import service_interface.Profile;

import java.io.File;

public class AlbumClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String albumUrl;
    private final String reviewUrl;
    public AlbumClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.albumUrl = baseUrl + "/albums";
        this.reviewUrl = baseUrl + "/review";
    }

    public int getAlbum(String albumID) {
        try {
            String url = albumUrl + "/" + albumID;
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String[] postAlbum(Profile profile, File imageFile) {
        try {
//            System.out.println(albumUrl);
            HttpPost httpPost = new HttpPost(albumUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("image", imageFile, ContentType.APPLICATION_OCTET_STREAM, imageFile.getName());

            Gson gson = new Gson();
            String profileJson = gson.toJson(profile);
            builder.addTextBody("profile", profileJson, ContentType.APPLICATION_JSON);

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            HttpEntity entity = response.getEntity();
            String responseJson = EntityUtils.toString(entity);
//            System.out.println("[postAlbum]" + responseJson);

            ImageMetaData imageMetaData = gson.fromJson(responseJson, ImageMetaData.class);
            String albumID = imageMetaData.getAlbumID();


            EntityUtils.consume(entity);
            return new String[]{String.valueOf(statusCode), albumID};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int postReview(String albumID, String likeOrNot) {
        try {
            String url = reviewUrl + "/" + likeOrNot + "/" + albumID;
//            System.out.println(url);
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            String json = "{\"albumID\": \"" + albumID + "\", \"likeOrNot\": \"" + likeOrNot + "\"}";
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
//            System.out.println(EntityUtils.toString(entity));
            EntityUtils.consume(entity);
            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}