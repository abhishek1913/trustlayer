package com.trustlayer.user.api;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserDirectory {

    Optional<UserSummary> findById(UUID id);

    Page<UserSummary> search(String emailContains, Boolean emailVerified, Pageable pageable);
}
