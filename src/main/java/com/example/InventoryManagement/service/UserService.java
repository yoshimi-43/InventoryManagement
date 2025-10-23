package com.example.InventoryManagement.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.InventoryManagement.model.User;
import com.example.InventoryManagement.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ユーザー登録処理
    public void register(User user) {
        // パスワードを暗号化して保存
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    // Spring Security 認証用
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB からユーザー情報を取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

        // Spring Security の User オブジェクトに変換して返す
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // 権限が不要なら空リスト
                .build();
    }
}