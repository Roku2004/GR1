package com.example.itssprj_ver1.service;

import com.example.itssprj_ver1.model.customer;

import java.util.List;
import java.util.Map;

public interface customerServiceI {

    boolean addCustomer(String firstname, String lastname, String email, String phone, String gender, int age, int userid);

    List<Map<String, Object>> getAllCustomers();

    boolean updateCustomer(int customerid, String firstname, String lastname, String email, String phone, int age);

    customer infoCustomer(int customerid);
}
