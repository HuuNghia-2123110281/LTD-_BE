package com.nghiashop.ecome_backend.service;

import java.util.List;

import com.nghiashop.ecome_backend.entity.Address;

public interface AddressService {
    List<Address> getAll();

    Address getById(Long id);

    Address create(Address address);

    Address update(Long id, Address address);

    void delete(Long id);
}
