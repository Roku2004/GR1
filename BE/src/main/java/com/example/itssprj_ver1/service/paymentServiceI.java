package com.example.itssprj_ver1.service;

import java.util.List;
import java.util.Map;

public interface paymentServiceI {
    boolean addPayment(String cufirstname, String culastname,String method, Float amount, Boolean paid);
    List<Map<String,Object>> getAllPayment(int customerId);
}
