package com.example.itssprj_ver1.service;

import com.example.itssprj_ver1.model.*;
import com.example.itssprj_ver1.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class customerService implements customerServiceI {
    @Autowired
    private userRepository userRepository;
    @Autowired
    private customerRepository customerRepository;
    @Autowired
    private memRegRepository memRegRepository;
    @Autowired
    private exerSessionRepository exerSessionRepository;

    @Override
    public boolean addCustomer(String firstname, String lastname, String email, String phone, String gender, int age, int userid) {
        users users = userRepository.findById(userid);

        if (users == null) {
            return false;
        }
        customer customer = new customer();
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setAge(age);
        customer.setEmail(email);
        customer.setGender(gender);
        customer.setPhone(phone);
        customer.setUserid(users);
        try {
            customerRepository.save(customer);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getAllCustomers() {
        List<customer> customers = customerRepository.findAll();
        if (customers == null || customers.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        for (customer result : customers) {
            Map<String, Object> response = new HashMap<>();
            if (result.getUserid().isDeleted() == true) {
                continue;
            } else {
                response.put("customerId", result.getId());
                response.put("customerName", result.getFirstname() + " " + result.getLastname());
                response.put("customerAge", result.getAge());
                response.put("customerGender", result.getGender());
                response.put("customerPhone", result.getPhone());
                response.put("customerEmail", result.getEmail());
                response.put("infoUpdateAt", result.getUpdateAt());
                mappedResults.add(response);
            }
        }
        return mappedResults;
    }

    @Override
    public boolean updateCustomer(int customerid, String firstname, String lastname, String email, String phone, int age) {
        customer customer = customerRepository.findById(customerid);
        if (customer == null) {
            return false;
        }
        try {
            customer.setFirstname(firstname);
            customer.setLastname(lastname);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAge(age);
            customerRepository.save(customer);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public customer infoCustomer(int customerid) {
        customer customer = customerRepository.findByUserid_Id(customerid);
        return customer;
    }

    @Override
    public List<Map<String, Object>> getCustomerByPT(int ptid) {
        List<exerciseSession> sessions = exerSessionRepository.findByStaff_Userid_Id(ptid);
        List<memberRegister> memberRegisters = memRegRepository.findAll();

        if (sessions == null || sessions.isEmpty()) {
            if (memberRegisters == null || memberRegisters.isEmpty()) {
                return null;
            }
        }

        Set<Integer> customerIds = new HashSet<>();

        if (sessions != null) {
            for (exerciseSession session : sessions) {
                customerIds.add(session.getCustomer().getUserid().getId());
            }
        }

        if (memberRegisters != null) {
            for (memberRegister memberRegister : memberRegisters) {
                if (memberRegister.getStatus().equals("Gia hạn") || memberRegister.getStatus().equals("Đăng ký")) {
                    customerIds.add(memberRegister.getCustomer().getUserid().getId());
                }
            }
        }

        List<Map<String, Object>> customerList = new ArrayList<>();
        for (Integer customerId : customerIds) {
            customer customer = customerRepository.findByUserid_Id(customerId);
            if (customer != null && !customer.getUserid().isDeleted()) {
                Map<String, Object> customerMap = new HashMap<>();
                customerMap.put("customerId", customer.getId());
                customerMap.put("customerName", customer.getFirstname() + " " + customer.getLastname());
                customerMap.put("customerAge", customer.getAge());
                customerMap.put("customerGender", customer.getGender());
                customerMap.put("customerPhone", customer.getPhone());
                customerMap.put("customerEmail", customer.getEmail());
                customerList.add(customerMap);
            }
        }
        return customerList;
    }
}
