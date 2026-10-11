package com.example.shopping_system.Service类;

import com.example.shopping_system.实体类.Seller;
import com.example.shopping_system.Repository接口.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerService {
    
    @Autowired
    private SellerRepository sellerRepository;
    
    public Optional<Seller> login(String username, String password) {
        Optional<Seller> sellerOpt = sellerRepository.findByUsername(username);
        if (sellerOpt.isPresent() && sellerOpt.get().getPassword().equals(password)) {
            return sellerOpt;
        }
        return Optional.empty();
    }
    
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<Seller> sellerOpt = sellerRepository.findByUsername(username);
        if (sellerOpt.isPresent() && sellerOpt.get().getPassword().equals(oldPassword)) {
            Seller seller = sellerOpt.get();
            seller.setPassword(newPassword);
            sellerRepository.save(seller);
            return true;
        }
        return false;
    }
    
    public void initializeSeller() {
        // 初始化默认卖家账号
        if (sellerRepository.count() == 0) {
            Seller seller = new Seller();
            seller.setUsername("admin");
            seller.setPassword("admin123");
            sellerRepository.save(seller);
        }
    }
    
    public Seller getDefaultSeller() {
        return sellerRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("尚未配置卖家账号，请先初始化卖家"));
    }
}
