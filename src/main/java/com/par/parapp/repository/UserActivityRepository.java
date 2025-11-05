package com.par.parapp.repository;

import com.par.parapp.model.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    @Query(value = "SELECT * FROM user_activity WHERE user_activity.user_login=:userLogin", nativeQuery = true)
    Optional<Page<UserActivity>> getAllByUserLogin(@Param("userLogin") String userLogin, Pageable pageable);
}
