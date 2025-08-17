package com.chatbotcal.repository;

import com.chatbotcal.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, String> {


}

