package client;

import database.AlbumDao;
import org.apache.http.client.HttpClient;
import server.ImageMetaData;
import server.Profile;

import java.io.File;
import java.nio.file.Files;

public class AlbumClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    protected AlbumDao albumDao;

    public AlbumClient(HttpClient httpClient, AlbumDao albumDao, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.albumDao = albumDao;
    }

    public Profile getAlbum(String albumId) {
        try {
            return albumDao.getAlbum(albumId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ImageMetaData postAlbum(Profile profile, File imageFile) {
        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            return albumDao.createAlbum(profile, imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
