package com.jarad.postly.unit.service;

import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Follower;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.embeddable.FollowerPK;
import com.jarad.postly.repository.CommentRepository;
import com.jarad.postly.repository.PostRepository;
import com.jarad.postly.repository.ProfileRepository;
import com.jarad.postly.service.CommentServiceImpl;
import com.jarad.postly.util.dto.CommentDto;
import com.jarad.postly.util.exception.CommentNotFoundException;
import com.jarad.postly.util.exception.PostNotFoundException;
import com.jarad.postly.util.exception.ProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private CommentServiceImpl commentService;

    private Profile profile;
    private Comment comment;
    private Post post;
    private CommentDto commentDto;
    private List<Comment> twoComments;

    @BeforeEach
    void setUp() {
        profile = getProfile();
        comment = getComment();
        post = getPost();
        commentDto = new CommentDto();
        twoComments = getTwoComments();
    }

    @Test
    void returnListOfPageNumbers_ValidTotalPages_ReturnsValidListOfPageNumbers() {
        int totalPages = 10;
        List<Integer> expectedResult = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();

        List<Integer> actualResult = commentService.returnListOfPageNumbers(totalPages);

        assertEquals(expectedResult, actualResult, "Lists should be equal");
    }

    @Test
    void returnPaginatedCommentsByCreationDateDescending_PagedResponseIsPresent_ReturnsValidPageOfComments() {
        Page<Comment> pagedResponse = new PageImpl<>(twoComments);
        when(commentRepository.findByPostId(anyLong(), any(Pageable.class))).thenReturn(pagedResponse);

        Page<Comment> actualResult = commentService.returnPaginatedCommentsByCreationDateDescending(1L, 1);

        assertThat(actualResult).isNotEmpty();
        assertEquals(twoComments, actualResult.getContent(), "Lists of Comments should be equal");
        verify(commentRepository, times(1)).findByPostId(anyLong(), any(Pageable.class));
    }

    @Test
    void returnAuthorsByUserId_ProfileSetOfAuthorsIdsIsPresent_ReturnsValidSetOfLongs() {
        Set<Long> profileSetOfAuthorIds = profile.getFollowers().stream()
                .map(follower -> follower.getId().getAuthorId())
                .collect(toSet());
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));

        Set<Long> actualResult = commentService.returnAuthorsByUserId(1L);

        assertThat(actualResult).isNotEmpty();
        assertEquals(profileSetOfAuthorIds, actualResult, "Sets of Author Ids should be equal");
        verify(profileRepository, times(1)).findByUserId(anyLong());

    }

    @Test
    void returnAuthorsByUserId_ProfileIsEmpty_ReturnsEmptySet() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        Set<Long> actualResult = commentService.returnAuthorsByUserId(1L);

        assertThat(actualResult).isEmpty();
        verify(profileRepository, times(1)).findByUserId(anyLong());
    }

    @Test
    void returnCommentById_CommentIsPresent_ReturnsValidComment() {
        when(commentRepository.findByPostIdAndId(anyLong(), anyLong())).thenReturn(Optional.of(comment));

        Comment actualResult = commentService.returnCommentById(1L, 2L);

        assertThat(actualResult).isNotNull();
        assertEquals(comment, actualResult, "Comments should be equal");
        verify(commentRepository, times(1)).findByPostIdAndId(anyLong(), anyLong());
    }

    @Test
    void returnCommentById_CommentIsEmpty_ThrowsCommentNotFoundException() {
        when(commentRepository.findByPostIdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.returnCommentById(1L, 2L));
        verify(commentRepository, times(1)).findByPostIdAndId(anyLong(), anyLong());
    }

    @Test
    void createNewCommentAndReturnCommentId_ProfileAndPostAndCommentArePresent_ReturnsValidCommentId() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Long actualResult = commentService.createNewCommentAndReturnCommentId(1L, 2L, commentDto);

        assertThat(actualResult).isNotNull();
        assertEquals(comment.getId(), actualResult, "Comment Ids should be equal");
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verify(postRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createNewCommentAndReturnCommentId_ProfileIsEmpty_ThrowsProfileNotFoundException() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> commentService.createNewCommentAndReturnCommentId(1L, 2L, commentDto));
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verifyNoMoreInteractions(postRepository, commentRepository);
    }

    @Test
    void createNewCommentAndReturnCommentId_ProfileIsPresentPostIsEmpty_ThrowsPostNotFoundException() {
        when(profileRepository.findByUserId(anyLong())).thenReturn(Optional.of(profile));
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PostNotFoundException.class, () -> commentService.createNewCommentAndReturnCommentId(1L, 2L, commentDto));
        verify(profileRepository, times(1)).findByUserId(anyLong());
        verify(postRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void updateExistingComment_CommentIsPresent_UpdatesExistingComment() {
        when(commentRepository.findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(comment));

        commentService.updateExistingComment(1L, 2L, 3L, commentDto);

        verify(commentRepository, times(1)).findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void updateExistingComment_CommentIsEmpty_ThrowsCommentNotFoundException() {
        when(commentRepository.findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.updateExistingComment(1L, 2L, 3L, commentDto));
        verify(commentRepository, times(1)).findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong());
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void deleteExistingComment_CommentIsPresent_DeletesExistingComment() {
        when(commentRepository.findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.of(comment));

        commentService.deleteExistingComment(1L, 2L, 3L);

        verify(commentRepository, times(1)).findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong());
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteExistingComment_CommentIsEmpty_ThrowsCommentNotFoundException() {
        when(commentRepository.findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.deleteExistingComment(1L, 2L, 3L));
        verify(commentRepository, times(1)).findByProfileUserIdAndPostIdAndId(anyLong(), anyLong(), anyLong());
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void isCommentOwnedByUser_WhenCommentIsOwnedByUser_ReturnsTrue() {
        when(commentRepository.existsByProfileUserIdAndId(anyLong(), anyLong())).thenReturn(true);

        boolean actualResult = commentService.isCommentOwnedByUser(1L, 2L);

        assertTrue(actualResult, "When Comment IS owned by User should return True");
        verify(commentRepository, times(1)).existsByProfileUserIdAndId(anyLong(), anyLong());
    }

    @Test
    void isCommentOwnedByUser_WhenCommentIsNotOwnedByUser_ReturnsFalse() {
        when(commentRepository.existsByProfileUserIdAndId(anyLong(), anyLong())).thenReturn(false);

        boolean actualResult = commentService.isCommentOwnedByUser(1L, 2L);

        assertFalse(actualResult, "When Comment IS NOT owned by User should return False");
        verify(commentRepository, times(1)).existsByProfileUserIdAndId(anyLong(), anyLong());
    }

    @Test
    void returnPostTitleByPostId_PostIsPresent_ReturnsValidTitle() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        String actualResult = commentService.returnPostTitleByPostId(1L);

        assertThat(actualResult).isNotEmpty();
        assertEquals(post.getTitle(), actualResult, "Titles of Profiles should be equal");
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void returnPostTitleByPostId_PostIsAbsent_ThrowsProfileNotFoundException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> commentService.returnPostTitleByPostId(1L));
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void returnPostByPostId_PostIsPresent_ReturnsValidPost() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        Post actualResult = commentService.returnPostByPostId(1L);

        assertThat(actualResult).isNotNull();
        assertEquals(post, actualResult, "Post should be the same");
        verify(postRepository, times(1)).findById(anyLong());
    }

    @Test
    void returnPostTitleByPostId_PostIsAbsent_ReturnsNull() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        Post actualResult = commentService.returnPostByPostId(1L);

        assertThat(actualResult).isNull();
        verify(postRepository, times(1)).findById(anyLong());
    }

    /**
     * Helper method that creates Dummy Test Double for the Profile Entity
     *
     * @return Profile object
     */
    private Profile getProfile() {
        return Profile.builder()
                .id(1L)
                .username("JohnDoe")
                .followers(Arrays.asList(getFollower(2L), getFollower(3L)))
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the Post Entity
     *
     * @return Post object
     */
    private Post getPost() {
        return Post.builder()
                .id(1L)
                .title("Title Example")
                .description("Description Example")
                .creationDate(Instant.now())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the Comment Entity
     *
     * @return Comment object
     */
    private Comment getComment() {
        return Comment.builder()
                .id(1L)
                .description("Description Example")
                .creationDate(Instant.now())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Double for the Follower Entity
     *
     * @return Follower object
     */
    private Follower getFollower(Long authorId) {
        return Follower.builder()
                .id(FollowerPK.builder().followerId(1L).authorId(authorId).build())
                .creationDate(Instant.now())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Comments
     *
     * @return List of Comments
     */
    private List<Comment> getTwoComments() {
        Comment comment1 = Comment.builder()
                .id(1L)
                .description("1st Comment Description Example")
                .creationDate(Instant.now())
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .description("2nd Comment Description Example")
                .creationDate(Instant.now())
                .build();

        return Arrays.asList(comment1, comment2);
    }
}