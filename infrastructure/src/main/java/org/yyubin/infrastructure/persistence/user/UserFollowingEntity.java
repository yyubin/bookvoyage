package org.yyubin.infrastructure.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.user.UserFollowing;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFollowingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false, insertable = false, updatable = false)
    private UserEntity follower;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false, insertable = false, updatable = false)
    private UserEntity followee;

    @Column(name = "followee_id", nullable = false)
    private Long followeeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserFollowingEntity fromDomain(UserFollowing userFollowing) {
        return UserFollowingEntity.builder()
                .id(userFollowing.getId())
                .followerId(userFollowing.getFollowerId().getValue())
                .followeeId(userFollowing.getFolloweeId().getValue())
                .createdAt(userFollowing.getCreatedAt())
                .build();
    }

    public UserFollowing toDomain() {
        return UserFollowing.of(
                this.id,
                UserId.of(this.followerId),
                UserId.of(this.followeeId),
                this.createdAt
        );
    }
}
