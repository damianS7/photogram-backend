package com.damian.photogram.follow;

import com.damian.photogram.follow.dto.FollowDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/api/v1")
@RestController
public class FollowController {
    private final FollowService followService;

    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    // endpoint to fetch all followers from logged customer
    @GetMapping("/followers")
    public ResponseEntity<?> getFollowers() {
        Set<Follow> follows = followService.getFollowers();
        Set<FollowDto> friendsDTO = FollowDtoMapper.toDtoSet(follows);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(friendsDTO);
    }

    // endpoint to fetch all followers from specific customer
    @GetMapping("/followers/{followedCustomerId}")
    public ResponseEntity<?> getCustomerFollowers(
            @PathVariable("followedCustomerId") @NotNull @Positive
            Long customerId
    ) {
        Set<Follow> follows = followService.getFollowers(customerId);
        Set<FollowDto> followersDTO = FollowDtoMapper.toDtoSet(follows);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followersDTO);
    }

    // endpoint to fetch all followed from specific customer
    @GetMapping("/followers/me/followed")
    public ResponseEntity<?> getFollowed() {
        Set<Follow> followed = followService.getFollowed();
        Set<FollowDto> followedDTO = FollowDtoMapper.toDtoSet(followed);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followedDTO);
    }

    // endpoint to fetch all followed from logged customer
    @GetMapping("/followers/{followedCustomerId}/followed")
    public ResponseEntity<?> getCustomerFollowed(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        Set<Follow> followed = followService.getFollowed(customerId);
        Set<FollowDto> followedDTO = FollowDtoMapper.toDtoSet(followed);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followedDTO);
    }

    // endpoint to add a new follow for the logged customer
    @PostMapping("/followers/{followedCustomerId}")
    public ResponseEntity<?> follow(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        Follow follow = followService.follow(customerId);
        FollowDto followDto = FollowDtoMapper.toDto(follow);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(followDto);
    }

    // endpoint to delete a follow from the logged customer follow list.
    @DeleteMapping("/followers/{followedCustomerId}")
    public ResponseEntity<?> unfollow(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        followService.unfollow(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}

