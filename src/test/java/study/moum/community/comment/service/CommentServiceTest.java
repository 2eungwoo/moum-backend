package study.moum.community.comment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;
import study.moum.auth.domain.entity.MemberEntity;
import study.moum.auth.domain.repository.MemberRepository;
import study.moum.community.article.domain.article.ArticleEntity;
import study.moum.community.article.domain.article.ArticleRepository;
import study.moum.community.article.domain.article_details.ArticleDetailsEntity;
import study.moum.community.article.domain.article_details.ArticleDetailsRepository;
import study.moum.community.article.dto.ArticleDto;
import study.moum.community.comment.domain.CommentEntity;
import study.moum.community.comment.domain.CommentRepository;
import study.moum.community.comment.dto.CommentDto;
import study.moum.custom.WithAuthUser;
import study.moum.global.error.ErrorCode;
import study.moum.global.error.exception.CustomException;
import study.moum.global.error.exception.NoAuthorityException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleDetailsRepository articleDetailsRepository;

    @Mock
    private MemberRepository memberRepository;

    private MemberEntity author;
    private ArticleDetailsEntity articleDetails;
    private ArticleEntity article;
    private CommentEntity comment;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // 테스트에 필요한 객체들 초기화
        author = MemberEntity.builder()
                .id(1)
                .email("test@user.com")
                .password("1234")
                .username("testuser")
                .role("ROLE_ADMIN")
                .build();

        article = ArticleEntity.builder()
                .id(1)
                .title("test title")
                .author(author)
                .category(ArticleEntity.ArticleCategories.RECRUIT_BOARD)
                .commentCount(0)
                .build();

        articleDetails = ArticleDetailsEntity.builder()
                .id(1)
                .comments(new ArrayList<>())
                .content("test content")
                .articleId(article.getId())
                .build();

        comment = CommentEntity.builder()
                .id(1)
                .articleDetails(articleDetails)
                .author(author)
                .content("test content")
                .build();
    }

    @Test
    @DisplayName("댓글 생성 성공")
    @WithAuthUser(email = "test@user.com", username = "testuser")
    void create_comment_success(){
        // given
        CommentDto.Request commentRequest = CommentDto.Request.builder()
                .articleDetails(articleDetails)
                .content("test content")
                .author(author)
                .build();

        // Mock 동작
        when(memberRepository.findByUsername(author.getUsername())).thenReturn(author);
        when(articleDetailsRepository.findById(articleDetails.getId())).thenReturn(Optional.ofNullable(articleDetails));
        when(articleRepository.findById(article.getId())).thenReturn(Optional.ofNullable(article));

        // when
        CommentDto.Response response = commentService.createComment(commentRequest,author.getUsername(),article.getId());

        // then
        verify(commentRepository).save(any(CommentEntity.class));
        assertEquals("test content", response.getContent());
        assertEquals(1, article.getCommentCount());

    }

    @Test
    @DisplayName("댓글 수정 성공")
    @WithAuthUser(email = "test@user.com", username = "testuser")
    void update_comment_success(){
        // given
        CommentDto.Request updateRequest = CommentDto.Request.builder()
                .articleDetails(articleDetails)
                .content("update content")
                .author(author)
                .build();

        // Mock 동작
        when(memberRepository.findByUsername(author.getUsername())).thenReturn(author);
        when(articleDetailsRepository.findById(articleDetails.getId())).thenReturn(Optional.ofNullable(articleDetails));
        when(articleRepository.findById(article.getId())).thenReturn(Optional.ofNullable(article));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));

        // when
        CommentDto.Response response = commentService.updateComment(updateRequest,author.getUsername(),article.getId());

        // then
        verify(commentRepository).save(any(CommentEntity.class));
        assertEquals("update content", response.getContent());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    @WithAuthUser(email = "test@user.com", username = "testuser")
    void delete_comment_success(){
        // given

        // Mock 동작
        when(memberRepository.findByUsername(author.getUsername())).thenReturn(author);
        when(articleDetailsRepository.findById(articleDetails.getId())).thenReturn(Optional.ofNullable(articleDetails));
        when(articleRepository.findById(article.getId())).thenReturn(Optional.ofNullable(article));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));

        // when
        CommentDto.Response response = commentService.deleteComment(author.getUsername(),article.getId());

        // then
        verify(commentRepository).deleteById(comment.getId());
        assertEquals(1, response.getCommentId());
        assertEquals(0,article.getCommentCount());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 없는 댓글")
    @WithMockUser(username = "testuser") // 로그인한 사용자 설정
    void updateComment_Fail_NoComment() {
        // Given
        CommentDto.Request requestDto = new CommentDto.Request("수정된 댓글 내용", author, articleDetails);

        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> commentService.updateComment(requestDto, "testuser", 1));
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void deleteComment_Fail_NoAuthorization() {
        // Given
        when(memberRepository.findByUsername("testuser")).thenReturn(author);
        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // 다른 사용자가 작성한 댓글로 설정
        MemberEntity anotherUser = MemberEntity.builder()
                .id(1)
                .email("another@user.com")
                .password("1234")
                .username("anotherUser")
                .role("ROLE_ADMIN")
                .build();

        comment.setAuthor(anotherUser);

        // When & Then
        NoAuthorityException exception = assertThrows(NoAuthorityException.class,
                () -> commentService.deleteComment("testuser", 1));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 이미 삭제된 댓글")
    @WithMockUser(username = "testuser") // 로그인한 사용자 설정
    void deleteComment_Fail_AlreadyDeleted() {
        // Given
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> commentService.deleteComment("testuser", 1));
        assertEquals(ErrorCode.COMMENT_ALREADY_DELETED, exception.getErrorCode());
    }
}
