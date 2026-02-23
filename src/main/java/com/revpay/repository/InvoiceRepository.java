package com.revpay.repository;

import com.revpay.entity.Invoice;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByCustomer(User customer);
    List<Invoice> findByBusiness(User business);
    
    @Query("SELECT COALESCE(SUM(i.amount),0) FROM Invoice i WHERE i.business = :business AND i.status='PAID'")
    double totalRevenue(User business);
}
