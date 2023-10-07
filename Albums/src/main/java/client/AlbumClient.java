package client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class AlbumClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    public AlbumClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public int getAlbum(String albumId) {
        try {
            String url = baseUrl + "/" + albumId;
            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
//            System.out.println("GET Request Status Code: " + statusCode);
//            System.out.println("GET Response Body: " + responseBody);
            return statusCode;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int postAlbum(String artist, String title, String year, File imageFile) {
        try {
            HttpPost httpPost = new HttpPost(baseUrl);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("image", imageFile, ContentType.APPLICATION_OCTET_STREAM, imageFile.getName());
            builder.addTextBody("artist", artist, ContentType.TEXT_PLAIN);
            builder.addTextBody("title", title, ContentType.TEXT_PLAIN);
            builder.addTextBody("year", year, ContentType.TEXT_PLAIN);

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
//            System.out.println("POST Request Status Code: " + statusCode);
//            System.out.println("POST Response Body: " + responseBody);
            return statusCode;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
