package servlets;

import com.google.gson.Gson;
import database.AlbumDao;
import database.DatabaseConnection;
import rabbitmq.ReviewConsumer;
import rabbitmq.ReviewProducer;
import data_models.ErrorMsg;
import data_models.Profile;
import data_models.Review;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "server.servlets.ReviewServlet", value = "/review/*")
public class ReviewServlet extends HttpServlet {
    private final Gson gson = new Gson();
    protected AlbumDao albumDao = new AlbumDao(DatabaseConnection.getDataSource());
    private ReviewProducer reviewProducer;
    private ReviewConsumer reviewConsumer;

    @Override
    public void init() throws ServletException {
//        System.out.println("[review servlet] Initialized. Instance hash code: " + hashCode());
        reviewProducer = new ReviewProducer();
        reviewProducer.init();

        reviewConsumer = new ReviewConsumer(1);
        Thread consumerThread = new Thread(reviewConsumer);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String[] pathSegments = pathInfo.split("/");

        if (pathSegments.length == 3) {
            String likeOrNot = pathSegments[1];
            String albumID = pathSegments[2];
            Profile profile = albumDao.getAlbum(albumID);
//            System.out.println("doPost review: " + albumID + " " + likeOrNot);

            if (profile != null) {
                Review review = new Review(null, albumID, likeOrNot);
                String reviewJson = gson.toJson(review);
                reviewProducer.publishReview(reviewJson);
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, review);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Album not found");
            }
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid inputs");
        }
    }

    @Override
    public void destroy() {
        reviewProducer.close();
        reviewConsumer.shutdown();
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