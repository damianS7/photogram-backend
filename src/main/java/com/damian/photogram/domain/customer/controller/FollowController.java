package com.damian.photogram.domain.customer.controller;

import com.damian.photogram.domain.customer.dto.response.FollowDto;
import com.damian.photogram.domain.customer.mapper.FollowDtoMapper;
import com.damian.photogram.domain.customer.model.Follow;
import com.damian.photogram.domain.customer.service.FollowService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class FollowController {
    private final FollowService followService;

    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    // endpoint to check if logged customer already follows
    @GetMapping("/customers/{customerId}/follow")
    public ResponseEntity<?> getFollow(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        Follow follow = followService.getFollow(customerId);
        FollowDto followDto = FollowDtoMapper.toFollowDto(follow);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followDto);
    }

    // endpoint to fetch all followers from logged customer
    @GetMapping("/customers/me/followers")
    public ResponseEntity<?> getFollowers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Follow> follows = followService.getFollowers(pageable);
        Page<FollowDto> friendsDTO = FollowDtoMapper.toFollowDtoPaged(follows);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(friendsDTO);
    }

    // endpoint to fetch all followers from specific customer
    @GetMapping("/customers/{customerId}/followers")
    public ResponseEntity<?> getCustomerFollowers(
            @PathVariable("customerId") @NotNull @Positive
            Long customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Follow> follows = followService.getFollowers(customerId, pageable);
        Page<FollowDto> followersDTO = FollowDtoMapper.toFollowDtoPaged(follows);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followersDTO);
    }

    // endpoint to fetch all following from logged customer
    @GetMapping("/customers/{customerId}/following")
    public ResponseEntity<?> getCustomerFollowing(
            @PathVariable @NotNull @Positive
            Long customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Follow> followed = followService.getFollowed(customerId, pageable);
        Page<FollowDto> followedDTO = FollowDtoMapper.toFollowDtoPaged(followed);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(followedDTO);
    }

    // endpoint to add a new follow for the logged customer
    @PostMapping("/customers/{customerId}/follow")
    public ResponseEntity<?> follow(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        Follow follow = followService.follow(customerId);
        FollowDto followDto = FollowDtoMapper.toFollowDto(follow);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(followDto);
    }

    // endpoint to delete a follow from the logged customer follow list.
    @DeleteMapping("/customers/{customerId}/unfollow")
    public ResponseEntity<?> unfollow(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        followService.unfollow(customerId);

        return ResponseEntity
                .noContent()
                .build();
    }
}

