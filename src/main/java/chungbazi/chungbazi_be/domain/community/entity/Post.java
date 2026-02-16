package chungbazi.chungbazi_be.domain.community.entity;

import chungbazi.chungbazi_be.domain.report.entity.enums.ReportReason;
import chungbazi.chungbazi_be.global.utils.TimeFormatter;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    private boolean anonymous; //익명 여부

    @Builder.Default
    @Column(columnDefinition = "integer default 0")
    private Integer views = 0;

    @Builder.Default
    @Column(columnDefinition = "integer default 0")
    private Integer postLikes = 0;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContentStatus status = ContentStatus.VISIBLE;

    @Builder.Default
    @Column(columnDefinition = "integer default 0")
    private Integer reportCount = 0;

    @Column(name = "report_reason")
    @Enumerated(EnumType.STRING)
    private ReportReason reportReason;

    public void increaseReportCount() {
        this.reportCount++;
    }

    public void decreaseReportCount() {
        if (this.reportCount > 0) {
            this.reportCount--;
        }
    }

    public void deleteByAdmin(ReportReason reason) {
        this.status = ContentStatus.DELETED;
        this.reportReason = reason;
    }

    public void autoHide() {
        this.status = ContentStatus.HIDDEN;
    }

    public boolean isHiddenOrDeleted() {
        return status != ContentStatus.VISIBLE;
    }

    public String getThumbnailUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }
    public String getFormattedCreatedAt() {
        return TimeFormatter.formatCreatedAt(this.getCreatedAt());
    }
    public void incrementViews() {
        this.views = this.views + 1;
    }

    public void incrementLike(){this.postLikes = this.postLikes + 1;}
    public void decrementLike(){
        if(this.postLikes > 0) {
            this.postLikes -= 1;
        }
    }
}
