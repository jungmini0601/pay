package com.jungmini.pay.repository;

import com.jungmini.pay.domain.Friend;
import com.jungmini.pay.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsFriendByRecipientAndRequester(Member recipient, Member requester);
}
