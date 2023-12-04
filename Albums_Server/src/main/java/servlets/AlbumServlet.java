package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import database.AlbumDao;
import database.DatabaseConnection;
import database.DatabaseInitializer;
import data_models.ErrorMsg;
import data_models.ImageMetaData;
import data_models.Profile;

@WebServlet(name = "server.servlets.AlbumServlet", value = "/albums/*")
@MultipartConfig
public class AlbumServlet extends HttpServlet {
    private final Gson gson = new Gson();
    protected AlbumDao albumDao = new AlbumDao(DatabaseConnection.getDataSource());

    @Override
    public void init() throws ServletException {
        super.init();
        DatabaseInitializer.createTables();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (urlParts.length != 2) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }

        String albumID = urlParts[1];
        Profile profile = albumDao.getAlbum(albumID);

        if (profile != null) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, profile);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Key not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Part imagePart = request.getPart("image");
        String profileJson = request.getParameter("profile");

        if (imagePart != null && profileJson != null) {
            byte[] imageBytes = imagePart.getInputStream().readAllBytes();
            Profile profile = gson.fromJson(profileJson, Profile.class);

            ImageMetaData imageMetaData = albumDao.createAlbum(profile, imageBytes);
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, imageMetaData);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        String jsonData = this.gson.toJson(data);
        PrintWriter out = response.getWriter();
        out.write(jsonData);
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        ErrorMsg errorMsg = new ErrorMsg(errorMessage);
        sendJsonResponse(response, statusCode, errorMsg);
    }
}