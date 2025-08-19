package com.damian.photogram.domain.post.mapper;

import com.damian.photogram.domain.post.dto.response.CommentDto;
import com.damian.photogram.domain.post.model.Comment;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

public class CommentDtoMapper {
    public static CommentDto map(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getProfile().getUsername(),
                comment.getComment(),
                comment.getCreatedAt().toString()
        );
    }

    public static Set<CommentDto> map(Set<Comment> comments) {
        return comments
                .stream()
                .map(
                        CommentDtoMapper::map
                ).collect(Collectors.toSet());
    }

    public static Page<CommentDto> map(Page<Comment> comments) {
        return comments
                .map(
                        CommentDtoMapper::map
                );
    }

}
