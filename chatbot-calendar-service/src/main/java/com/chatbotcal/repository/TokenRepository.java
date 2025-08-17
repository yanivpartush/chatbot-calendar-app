package com.chatbotcal.repository;

import com.chatbotcal.repository.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<UserToken, String> {

    public UserToken findByUserId(String userId);

}
