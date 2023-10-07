package server;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import com.google.gson.Gson;

@WebServlet(name = "server.AlbumServlet", value = "/albums/*")
@MultipartConfig
public class AlbumServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        if (albumID.equals("1")) {
            Profile profile = new Profile("Sex Pistols", "Never Mind The Bollocks!", "1977");
            sendJsonResponse(response, HttpServletResponse.SC_OK, profile);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Key not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Part imagePart = request.getPart("image");
        String artist = request.getParameter("artist");
        String title = request.getParameter("title");
        String year = request.getParameter("year");

        if (imagePart != null && artist != null && title != null && year != null) {
            String imageSize = String.valueOf(imagePart.getSize());

            ImageMetaData imageMetaData = new ImageMetaData("1", imageSize);
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, imageMetaData);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        String jsonData = this.gson.toJson(data);
        PrintWriter out = response.getWriter();
        out.print(jsonData);
        out.flush();
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        ErrorMsg errorMsg = new ErrorMsg(errorMessage);
        sendJsonResponse(response, statusCode, errorMsg);
    }

    private String generateAlbumID() {
        return UUID.randomUUID().toString();
    }
}
