package com.damian.photogram.web.rest.feed;

import com.damian.photogram.web.rest.feed.dto.response.FeedDto;
import com.damian.photogram.service.feed.FeedService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
public class FeedController {
    private static final Logger log = LoggerFactory.getLogger(FeedController.class);
    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    // endpoint to fetch feed data from specific customer
    @GetMapping("customers/{username}/feed")
    public ResponseEntity<?> getCustomerFeed(
            @PathVariable @NotNull
            String username
    ) {
        log.debug("Received request for fetching feed for user: {}", username);
        final FeedDto feedDTO = feedService.getUserFeed(username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(feedDTO);
    }
}

