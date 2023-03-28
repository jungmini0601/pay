package com.jungmini.pay.repository;

import com.jungmini.pay.domain.FriendRequest;
import com.jungmini.pay.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsFriendRequestByRecipientAndRequester(Member recipient, Member requester);

    List<FriendRequest> findFriendRequestByRecipientOrderByCreatedAtDesc(Member recipient, Pageable pageable);
}
