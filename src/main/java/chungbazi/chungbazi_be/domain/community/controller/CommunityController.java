package chungbazi.chungbazi_be.domain.community.controller;

import chungbazi.chungbazi_be.domain.community.dto.CommunityRequestDTO;
import chungbazi.chungbazi_be.domain.community.dto.CommunityResponseDTO;
import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.community.service.CommentService;
import chungbazi.chungbazi_be.domain.community.service.CommunityService;
import chungbazi.chungbazi_be.domain.policy.dto.PopularSearchResponse;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import chungbazi.chungbazi_be.global.service.PopularSearchService;
import chungbazi.chungbazi_be.global.validation.annotation.ExistEntity;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
@Validated
public class CommunityController {

    private final CommunityService communityService;
    private final CommentService commentService;
    private final PopularSearchService popularSearchService;

    @PostMapping(value = "/posts/upload", consumes = "multipart/form-data")
    @Operation(summary = "게시글 업로드 API",
            description = """
                커뮤니티 게시글 업로드 API
                - 카테고리 목록:
                    * JOBS: 일자리
                    * HOUSING: 주거
                    * EDUCATION: 교육
                    * WELFARE_CULTURE: 복지·문화
                    * PARTICIPATION_RIGHTS: 참여·권리
                """)
    public ApiResponse<CommunityResponseDTO.UploadAndGetPostDto> uploadPost(
            @RequestPart("info") @Valid CommunityRequestDTO.UploadPostDto uploadPostDto ,
            @RequestPart(value = "imageList", required = false) List<MultipartFile> imageList) {
        return ApiResponse.onSuccess(communityService.uploadPost(uploadPostDto, imageList));
    }

    @GetMapping(value = "/posts")
    @Operation(summary = "커뮤니티 전체 게시글 또는 카테고리별 게시글 조회 API",
            description = """
                커뮤니티 전체 게시글 또는 카테고리별 게시글 조회 API
                - 카테고리 목록:
                    * JOBS: 일자리
                    * HOUSING: 주거
                    * EDUCATION: 교육
                    * WELFARE_CULTURE: 복지·문화
                    * PARTICIPATION_RIGHTS: 참여·권리
                """)
    public ApiResponse<CommunityResponseDTO.TotalPostListDto> getPosts(
            @RequestParam(required = false) Category category,
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "10") int size){
        return ApiResponse.onSuccess(communityService.getPosts(category, cursor, size));
    }

    @GetMapping(value = "/posts/{postId}")
    @Operation(summary = "개별 게시글 조회 API", description = "개별 게시글 조회 API")
    public ApiResponse<CommunityResponseDTO.UploadAndGetPostDto> getPost(
            @PathVariable @ExistEntity(entityType = Post.class) Long postId) {
        return ApiResponse.onSuccess(communityService.getPost(postId));
    }

    @PostMapping(value = "/comments/upload")
    @Operation(summary = "댓글 업로드 API", description = "댓글 업로드 API")
    public ApiResponse<CommunityResponseDTO.UploadAndGetCommentDto> uploadComment(
            @RequestBody @Valid CommunityRequestDTO.UploadCommentDto uploadCommentDto
    ) {
        return ApiResponse.onSuccess(commentService.uploadComment(uploadCommentDto));
    }

    @GetMapping(value = "/comments")
    @Operation(summary = "개별 게시글에 해당하는 댓글 조회 API", description = "개별 게시글에 해당하는 댓글 조회 API")
    public ApiResponse<CommunityResponseDTO.CommentListDto> getComments(
            @RequestParam @ExistEntity(entityType = Post.class) Long postId,
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.onSuccess(commentService.getComments(postId, cursor, size));
    }
    @PostMapping(value = "/likes")
    @Operation(summary = "개별 게시글 좋아요 API", description = "개별 게시글 좋아요 API")
    public ApiResponse<Void> likePost(@RequestParam @ExistEntity(entityType = Post.class) Long postId){
        communityService.likePost(postId);
        return ApiResponse.onSuccess(null);
    }
    @DeleteMapping(value = "/likes")
    @Operation(summary = "개별 게시글 좋아요 취소 API", description = "개별 게시글 좋아요 취소 API")
    public ApiResponse<Void> unlikePost(@RequestParam @ExistEntity(entityType = Post.class) Long postId){
        communityService.unlikePost(postId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("likes/{commentId}")
    @Operation(summary = "댓글 좋아요 API", description = "댓글에 좋아요를 누르는 API입니다")
    public ApiResponse<String> likeComment(@PathVariable @ExistEntity(entityType = Comment.class) Long commentId){
        communityService.likeComment(commentId);
        return ApiResponse.onSuccess("댓글에 좋아요를 누르셨습니다.");
    }

    @DeleteMapping("likes/{commentId}")
    @Operation(summary = "댓글 좋아요 취소 API", description = "댓글에 좋아요를 취소하는 API입니다")
    public ApiResponse<String> unlikeComment(@PathVariable @ExistEntity(entityType = Comment.class) Long commentId){
        communityService.unlikeComment(commentId);
        return ApiResponse.onSuccess("댓글 좋아요를 취소했습니다.");
    }

    @GetMapping("/search")
    @Operation(summary = "커뮤니티 검색 API", description = "커뮤니티 검색 API")
    public ApiResponse<CommunityResponseDTO.TotalPostListDto> getSearchPost(
            @RequestParam String query,
            @RequestParam(value = "filter", required = false, defaultValue = "title") String filter,
            @RequestParam(value = "period", required = false, defaultValue = "all") String period,
            @RequestParam Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CommunityResponseDTO.TotalPostListDto response = communityService.getSearchPost(query, filter, period, cursor, size);
        return ApiResponse.onSuccess(response);
    }
    @GetMapping("/search/popular")
    @Operation(summary = "커뮤니티 인기 검색어 조회 API", description = "커뮤니티 인기 검색어 조회 API")
    public ApiResponse<PopularSearchResponse> getSearchPopular(){
        PopularSearchResponse response = popularSearchService.getPopularSearch("community");
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "게시글 삭제 API", description = "해당하는 id의 게시글을 삭제하는 API입니다.")
    public ApiResponse<String> deletePost(@PathVariable @ExistEntity(entityType = Post.class) Long postId){
        communityService.deletePost(postId);
        return ApiResponse.onSuccess("해당 게시글이 삭제되었습니다.");
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제 API", description = "해당하는 id의 댓글을 삭제하는 API입니다.")
    public ApiResponse<String> deleteComment(@PathVariable @ExistEntity(entityType = Comment.class) Long commentId){
        communityService.deleteComment(commentId);
        return ApiResponse.onSuccess("해당 댓글이 삭제되었습니다.");
    }

}
