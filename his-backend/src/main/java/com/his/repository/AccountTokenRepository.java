package com.his.repository;

import com.his.entity.AccountToken;
import com.his.entity.User;
import com.his.enums.AccountTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {

    Optional<AccountToken> findByTokenHashAndTokenType(String tokenHash, AccountTokenType tokenType);

    void deleteByUserAndTokenType(User user, AccountTokenType tokenType);
}
