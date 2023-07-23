package com.jarad.postly.integration.controller;

import com.jarad.postly.controller.CommentController;
import com.jarad.postly.entity.Comment;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.CommentService;
import com.jarad.postly.util.dto.CommentDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    void testGetPostCommentsById() throws Exception {
        Long postId = 1L;
        Page<Comment> commentPage = new PageImpl<>(getElevenComments(), PageRequest.of(0, 10), 2);
        Post post = getPost();
        when(commentService.returnPostByPostId(anyLong())).thenReturn(post);
        when(commentService.returnAuthorsByUserId(anyLong())).thenReturn(new HashSet<>());
        when(commentService.returnPaginatedCommentsByCreationDateDescending(anyLong(), anyInt())).thenReturn(commentPage);
        when(commentService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/posts/{postId}/comments", postId)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/comments"))
                .andExpect(model().attributeExists("authorsByUserId"))
                .andExpect(model().attributeExists("pageNumbers"))
                .andExpect(model().attributeExists("commentPage"))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attributeExists("activeProfile"))
                .andExpect(model().attributeExists("postId"));

        verify(commentService, times(1)).returnAuthorsByUserId(anyLong());
        verify(commentService, times(1)).returnPaginatedCommentsByCreationDateDescending(anyLong(), anyInt());
        verify(commentService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    void testGetCommentById() throws Exception {
        Long postId = 1L;
        Comment comment = getComment();

        when(commentService.returnAuthorsByUserId(anyLong())).thenReturn(new HashSet<>());
        when(commentService.returnCommentById(anyLong(), anyLong())).thenReturn(comment);
        when(commentService.isCommentOwnedByUser(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(get("/posts/{postId}/comments/{commentId}", postId, comment.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/comment"))
                .andExpect(model().attributeExists("authorsByUserId"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attributeExists("activeProfile"))
                .andExpect(model().attributeExists("postId"))
                .andExpect(model().attributeExists("commentId"))
                .andExpect(model().attributeExists("personalComment"));

        verify(commentService, times(1)).returnAuthorsByUserId(anyLong());
        verify(commentService, times(1)).returnCommentById(postId, comment.getId());
        verify(commentService, times(1)).isCommentOwnedByUser(anyLong(), anyLong());
    }

    @Test
    void testGetPostCreateForm() throws Exception {
        Long postId = 1L;

        mockMvc.perform(get("/posts/{postId}/comments/create-form", postId))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/comment-create-form"))
                .andExpect(model().attributeExists("postId"))
                .andExpect(model().attribute("postId", postId))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("comment", Matchers.instanceOf(CommentDto.class)));
    }

    @Test
    void testGetPostUpdateForm() throws Exception {
        Long postId = 1L;
        Comment comment = getComment();

        when(commentService.returnCommentById(anyLong(), anyLong())).thenReturn(comment);

        mockMvc.perform(get("/posts/{postId}/comments/{commentId}/update-form", postId, comment.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/comment-update-form"))
                .andExpect(model().attributeExists("postId", "commentId", "comment"))
                .andExpect(model().attribute("postId", postId))
                .andExpect(model().attribute("commentId", comment.getId()))
                .andExpect(model().attribute("comment", comment));
    }

    @Test
    void testAddPostCommentById_Success() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        CommentDto commentDto = getCommentDto();

        when(commentService.createNewCommentAndReturnCommentId(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentId);

        mockMvc.perform(post("/posts/{postId}/comments/create-form", postId)
                        .with(csrf())
                        .with(user(userDetails))
                        .flashAttr("comment", commentDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId + "/comments/" + commentId));
    }

    @Test
    void testAddPostCommentById_ValidationErrors() throws Exception {
        Long postId = 1L;
        CommentDto commentDto = getCommentDto();
        commentDto.setDescription(null);

        mockMvc.perform(post("/posts/{postId}/comments/create-form", postId)
                        .with(csrf())
                        .with(user(userDetails))
                        .flashAttr("org.springframework.validation.BindingResult.comment", new BeanPropertyBindingResult(commentDto, "comment"))
                        .flashAttr("comment", commentDto))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/comment-create-form"))
                .andExpect(model().attributeExists("postId"))
                .andExpect(model().attribute("postId", postId));
    }

    @Test
    void testUpdateCommentById_Success() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        CommentDto commentDto = getCommentDto();

        mockMvc.perform(put("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .with(csrf())
                        .with(user(userDetails))
                        .flashAttr("comment", commentDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId + "/comments/" + commentId));
    }

    @Test
    void testUpdateCommentById_ValidationErrors() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        CommentDto commentDto = getCommentDto();
        commentDto.setDescription(null);

        mockMvc.perform(put("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .with(csrf())
                        .with(user(userDetails))
                        .flashAttr("org.springframework.validation.BindingResult.comment", new BeanPropertyBindingResult(commentDto, "comment"))
                        .flashAttr("comment", commentDto))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/comment-update-form"))
                .andExpect(model().attributeExists("postId"))
                .andExpect(model().attributeExists("commentId"))
                .andExpect(model().attribute("postId", postId))
                .andExpect(model().attribute("commentId", commentId));
    }

    @Test
    void testDeleteCommentById() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;

        mockMvc.perform(delete("/posts/{postId}/comments/{commentId}", postId, commentId)
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles/" + userDetails.getUserId() + "/comments"));
    }

    /**
     * Helper method that creates Dummy Test Doubles for the User
     *
     * @return User
     */
    private User getUser() {
        return User.builder()
                .id(1L)
                .email("1stUserEmail@Example.com")
                .password("1st User Password Example")
                .roles(new HashSet<>(Set.of(Role.builder().name("ROLE_PROFILE_ACTIVE").build())))
                .enabled(true)
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the User
     *
     * @return User
     */
    private Comment getComment() {
        return Comment.builder()
                .id(1L)
                .description("Description Example")
                .creationDate(Instant.now())
                .post(getPost())
                .profile(getProfile())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the Post
     *
     * @return Post
     */
    private Post getPost() {
        return Post.builder()
                .id(1L)
                .title("Post Title Example")
                .description("Post Description Example")
                .creationDate(Instant.now())
                .profile(getProfile())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the Profile
     *
     * @return Profile
     */
    private Profile getProfile() {
        return Profile.builder()
                .id(1L)
                .username("Profile Username Example")
                .creationDate(Instant.now())
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the CommentDto
     *
     * @return CommentDto
     */
    private CommentDto getCommentDto() {
        CommentDto commentDto = new CommentDto();
        commentDto.setDescription("CommentDto Description Example");

        return commentDto;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Comments
     *
     * @return List of Comments
     */
    private List<Comment> getElevenComments() {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            comments.add(Comment.builder()
                    .id((long) i)
                    .description(i + " Comment Description Example")
                    .creationDate(Instant.now())
                    .profile(Profile.builder().username(i + " Profile Username Example").build())
                    .post(Post.builder().profile(Profile.builder().id((long) i).build()).build())
                    .build());
        }

        return comments;
    }

}
