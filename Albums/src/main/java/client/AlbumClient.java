package client;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import service_interface.Profile;

import java.io.File;
import java.io.IOException;

public class AlbumClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    public AlbumClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public int getAlbum(String albumId) throws IOException{
        long start = System.currentTimeMillis();
        String url = baseUrl + "/" + albumId;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);

//      System.out.println("client get: " + (System.currentTimeMillis() - start));

        return statusCode;
    }

    public int postAlbum(Profile profile, File imageFile) throws IOException{
        long start = System.currentTimeMillis();

        HttpPost httpPost = new HttpPost(baseUrl);
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
//        String responseBody = EntityUtils.toString(entity);
//        System.out.println(responseBody);
        EntityUtils.consume(entity);
//      System.out.println("client post: " + (System.currentTimeMillis() - start));

        return statusCode;
    }
}