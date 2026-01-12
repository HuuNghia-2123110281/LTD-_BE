package com.nghiashop.ecome_backend.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nghiashop.ecome_backend.entity.Address;
import com.nghiashop.ecome_backend.repository.AddressRepository;
import com.nghiashop.ecome_backend.service.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<Address> getAll() {
        return addressRepository.findAll();
    }

    @Override
    public Address getById(Long id) {
        return addressRepository.findById(id).orElseThrow();
    }

    @Override
    public Address create(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public Address update(Long id, Address address) {
        Address existing = getById(id);
        existing.setReceiverName(address.getReceiverName());
        existing.setPhone(address.getPhone());
        existing.setAddress(address.getAddress());
        existing.setUser(address.getUser());
        return addressRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        addressRepository.deleteById(id);
    }
}
