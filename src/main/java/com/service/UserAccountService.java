package com.service;

import com.DTO.UserAccountDTO;
import com.entity.User;
import com.request.ChangePasswordRequest;
import com.request.LoginRequest;
import com.request.UpdateUserInfoRequest;
import com.request.UserRegisterRequest;
import com.response.AuthRespone;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;

public interface UserAccountService{
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    public User findById(long id) ;

    public User register(UserRegisterRequest rq) throws MessagingException;
    public AuthRespone authAccount(String email, String key);
    public AuthRespone login(LoginRequest request);
    public AuthRespone loginWithGoogle(Map<String, String> request);
    public boolean changePass(ChangePasswordRequest request);

    public UserAccountDTO getProfile(long id);

    public UserAccountDTO updateInfo(UpdateUserInfoRequest request);
    public UserAccountDTO updateAvt(String url);

    public void deleteById(int id);
    public User findByEmail(String email);
    public List<User> searchUser(String query);
    public User save(User user);

    public List<UserAccountDTO> getNewUsers();
}
