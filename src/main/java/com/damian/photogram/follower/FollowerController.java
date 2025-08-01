package com.damian.photogram.follower;

import com.damian.photogram.follower.dto.FollowerDTO;
import com.damian.photogram.follower.http.FriendCreateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/api/v1")
@RestController
public class FollowerController {
    private final FollowerService followerService;

    @Autowired
    public FollowerController(FollowerService followerService) {
        this.followerService = followerService;
    }

    // endpoint to fetch all friends from logged customer
    @GetMapping("/friends")
    public ResponseEntity<?> getFriends() {
        Set<Follower> followers = followerService.getFriends();
        Set<FollowerDTO> friendsDTO = FollowerDTOMapper.toFriendDTOList(followers);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(friendsDTO);
    }

    // endpoint to add a new follower for the logged customer
    @PostMapping("/friends")
    public ResponseEntity<?> addFriend(
            @Validated @RequestBody
            FriendCreateRequest request
    ) {
        Follower follower = followerService.addFriend(request.customerId());
        FollowerDTO followerDTO = FollowerDTOMapper.toCustomerFriendDTO(follower);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(followerDTO);
    }

    // endpoint to delete a follower from the logged customer follower list.
    @DeleteMapping("/friends/{id}")
    public ResponseEntity<?> deleteFriend(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        followerService.deleteFriend(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}

