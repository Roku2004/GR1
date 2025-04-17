package com.example.itssprj_ver1.service;

import com.example.itssprj_ver1.model.customer;

public interface customerServiceI {
    boolean loginCustomer(String username, String password);

    boolean addCustomer(String firstname, String lastname, int age, String email, String gender, String username, String phone);
}
