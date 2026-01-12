package com.nghiashop.ecome_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nghiashop.ecome_backend.entity.Address;
import com.nghiashop.ecome_backend.entity.User;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}
