package com.damian.photogram.feed;

import com.damian.photogram.feed.dto.FeedDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class FeedController {
    private final FeedService feedService;

    @Autowired
    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    // endpoint to fetch feed data from specific customers
    @GetMapping("/feed/{username}")
    public ResponseEntity<?> getFeed(
            @PathVariable @NotNull
            String username
    ) {
        FeedDTO feedDTO = feedService.getUserFeed(username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(feedDTO);
    }
}

