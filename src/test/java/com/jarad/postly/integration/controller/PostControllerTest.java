package com.jarad.postly.integration.controller;

import com.jarad.postly.controller.PostController;
import com.jarad.postly.entity.Post;
import com.jarad.postly.entity.Profile;
import com.jarad.postly.entity.Role;
import com.jarad.postly.entity.User;
import com.jarad.postly.security.UserDetailsImpl;
import com.jarad.postly.service.PostService;
import com.jarad.postly.util.dto.PostDto;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PostService postService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new UserDetailsImpl(getUser());
        // Set the user in the security context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Test
    public void testGetPaginatedPosts() throws Exception {
        Page<Post> postPage = new PageImpl<>(getElevenPosts(), PageRequest.of(0, 10), 2);
        when(postService.returnAuthorsByUserId(anyLong())).thenReturn(new HashSet<>());
        when(postService.returnPaginatedPostsByCreationDateDescending(anyInt())).thenReturn(postPage);
        when(postService.returnListOfPageNumbers(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/posts")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/posts"))
                .andExpect(model().attribute("authorsByUserId", new HashSet<>()))
                .andExpect(model().attribute("postPage", postPage))
                .andExpect(model().attribute("userId", userDetails.getUserId()))
                .andExpect(model().attribute("activeProfile", userDetails.isActiveProfile()));

        verify(postService, times(1)).returnAuthorsByUserId(anyLong());
        verify(postService, times(1)).returnPaginatedPostsByCreationDateDescending(anyInt());
        verify(postService, times(1)).returnListOfPageNumbers(anyInt());
    }

    @Test
    public void testGetPostById() throws Exception {
        Long postId = 1L;
        Post post = getPost();
        when(postService.returnPostById(postId)).thenReturn(post);
        when(postService.returnAuthorsByUserId(anyLong())).thenReturn(new HashSet<>());
        when(postService.isPostOwnedByUser(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(get("/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post"))
                .andExpect(model().attributeExists("authorsByUserId"))
                .andExpect(model().attributeExists("personalPost"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("userId"))
                .andExpect(model().attributeExists("activeProfile"));

        verify(postService, times(1)).returnPostById(postId);
        verify(postService, times(1)).returnAuthorsByUserId(anyLong());
        verify(postService, times(1)).isPostOwnedByUser(anyLong(), anyLong());
    }

    @Test
    public void testGetPostCreateForm() throws Exception {
        mockMvc.perform(get("/posts/create-form"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post-create-form"))
                .andExpect(model().attributeExists("post"));
    }

    @Test
    public void testGetPostUpdateForm() throws Exception {
        Long postId = 1L;
        Post post = getPost();
        post.setId(postId);
        when(postService.returnPostById(postId)).thenReturn(post);

        mockMvc.perform(get("/posts/" + postId + "/update-form"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post-update-form"))
                .andExpect(model().attributeExists("post", "postId"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("postId", postId));

        verify(postService, times(1)).returnPostById(postId);
    }

    @Test
    public void testAddPost_Success() throws Exception {
        PostDto postDto = getPostDto();
        Long postId = 1L;
        when(postService.createNewPostAndReturnPostId(anyLong(), any(PostDto.class))).thenReturn(postId);

        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .flashAttr("post", postDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(postService, times(1)).createNewPostAndReturnPostId(anyLong(), any());
    }

    @Test
    public void testAddPost_ValidationErrors() throws Exception {
        PostDto postDto = getPostDto();
        postDto.setTitle("");
        postDto.setDescription("");

        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .flashAttr("post", postDto))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post-create-form"));

        verifyNoInteractions(postService);
    }

    @Test
    public void testUpdatePostById_Success() throws Exception {
        Long postId = 1L;
        PostDto postDto = getPostDto();

        mockMvc.perform(put("/posts/" + postId)
                        .with(csrf())
                        .flashAttr("post", postDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        verify(postService, times(1)).updateExistingPost(anyLong(), eq(postId), any());
    }

    @Test
    public void testUpdatePostById_ValidationErrors() throws Exception {
        Long postId = 1L;

        PostDto postDto = getPostDto();
        postDto.setTitle("");
        postDto.setDescription("");

        mockMvc.perform(put("/posts/" + postId)
                        .with(csrf())
                        .flashAttr("post", postDto))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post-update-form"));

        verifyNoInteractions(postService);
    }

    @Test
    public void testDeletePostById() throws Exception {
        Long postId = 1L;

        mockMvc.perform(delete("/posts/" + postId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles/" + postId + "/posts"));

        verify(postService, times(1)).deleteExistingPost(anyLong(), eq(postId));
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
                .profile(Profile.builder()
                        .username("Profile Username Example")
                        .user(User.builder().id(1L).build())
                        .build()
                )
                .build();
    }

    /**
     * Helper method that creates Dummy Test Doubles for the List of Posts
     *
     * @return List of Posts
     */
    private List<Post> getElevenPosts() {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            posts.add(Post.builder()
                    .id((long) i)
                    .title(i + " Post Title Example")
                    .description(i + " Post Description Example")
                    .creationDate(Instant.now())
                    .profile(Profile.builder().username(i + " Profile Username Example").build())
                    .build());
        }

        return posts;
    }

    /**
     * Helper method that creates Dummy Test Doubles for the PostDto
     *
     * @return PostDto
     */
    private PostDto getPostDto() {
        int minDescriptionLength = 250;

        PostDto postDto = new PostDto();
        postDto.setTitle("CommentDto Title Example");
        postDto.setDescription(RandomStringUtils.random(minDescriptionLength, true, true));

        return postDto;
    }
}
