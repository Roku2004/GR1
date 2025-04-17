package com.example.itssprj_ver1.service;

import com.example.itssprj_ver1.model.customer;
import com.example.itssprj_ver1.model.users;
import com.example.itssprj_ver1.repository.customerRepository;
import com.example.itssprj_ver1.repository.userRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class customerService implements customerServiceI {
    @Autowired
    private userRepository userRepository;
    @Autowired
    private customerRepository customerRepository;

    @Override
    public boolean loginCustomer(String username, String password) {
        users user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password) && user.getRole().getRoleid() == 4) {
            return true;
        }
        return false;
    }

    @Override
    public boolean addCustomer(String firstname, String lastname, int age, String email, String gender, String username, String phone) {
        users users = userRepository.findByUsername(username);
        customer customer = new customer();
        if(users == null){
            return false;
        }
        int userid = users.getId();
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setAge(age);
        customer.setEmail(email);
        customer.setGender(gender);
        customer.setPhone(phone);
        customer.getUserid().setId(userid);
        try{
            customerRepository.save(customer);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
