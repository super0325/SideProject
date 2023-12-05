package com.example.demo.myEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public enum MyMediaType {

	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),  // XLSX
    CSV("text/csv"),                                                            // CSV
    PDF("application/pdf");                                                     // PDF
	

	@Autowired
    private final MediaType mediaType;

	// enum 的建構子默認是 private
    MyMediaType(String mediaType) {
        this.mediaType = MediaType.valueOf(mediaType);
    }

    public MediaType getMediaType() {
        return mediaType;
    }
    
}
