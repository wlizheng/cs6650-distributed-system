package main

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type Profile struct {
	ALbumID string `json:"albumID,omitempty"`
	Artist  string `json:"artist"`
	Title   string `json:"title"`
	Year    string `json:"year"`
}

type ImageMetaData struct {
	AlbumID   string `json:"albumID"`
	ImageSize string `json:"imageSize"`
}

func main() {
	router := gin.Default()

	router.GET("/albums/:id", getAlbumByID())
	router.POST("/albums", postAlbums())

	router.Run(":8080")
}

func getAlbumByID() gin.HandlerFunc {
	return func(c *gin.Context) {
		albumID := c.Param("id")
		if albumID == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request"})
			return
		}

		if albumID != "1" {
			c.JSON(http.StatusNotFound, gin.H{"error": "Key not found"})
			return
		}

		profile := Profile{
			Artist: "Sex Pistols",
			Title:  "Never Mind The Bollocks!",
			Year:   "1977",
		}
		c.JSON(http.StatusOK, profile)
	}
}

func postAlbums() gin.HandlerFunc {
	return func(c *gin.Context) {
		// get the uploaded image file
		imageFile, _, err := c.Request.FormFile("image")
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request"})
			return
		}
		defer imageFile.Close()

		artist := c.Request.FormValue("artist")
		title := c.Request.FormValue("title")
		year := c.Request.FormValue("year")

		if artist == "" || title == "" || year == "" {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request"})
			return
		}

		albumID := "1"

		// get image size
		imageSize := int64(0)
		buf := make([]byte, 1024)
		for {
			n, err := imageFile.Read(buf)
			if n == 0 || err != nil {
				break
			}
			imageSize += int64(n)
		}
		imageSizeStr := strconv.FormatInt(imageSize, 10)

		imageMetaData := ImageMetaData{
			AlbumID:   albumID,
			ImageSize: imageSizeStr,
		}

		c.JSON(http.StatusOK, imageMetaData)
	}
}
