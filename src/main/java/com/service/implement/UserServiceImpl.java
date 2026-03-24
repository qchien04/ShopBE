package com.service.implement;

import com.DTO.UserAccountDTO;
import com.entity.OTPCode;
import com.entity.User;
import com.exception.UnauthorizedException;
import com.exception.UserAccountException;
import com.mapper.UserAccountMapper;
import com.repository.UserAccountRepo;
import com.repository.UserRoleRepo;
import com.request.ChangePasswordRequest;
import com.request.LoginRequest;
import com.request.UpdateUserInfoRequest;
import com.request.UserRegisterRequest;
import com.response.AuthRespone;
import com.security.TokenProvider;
import com.service.MailService;
import com.service.OTPCodeService;
import com.service.UserAccountService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserDetailsService, UserAccountService {

    private final UserAccountRepo userAccountRepo;
    private final UserRoleRepo userRoleRepo;

    private final TokenProvider tokenProvider;
    private final OTPCodeService otpCodeService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;

    public UserServiceImpl(UserAccountRepo userAccountRepo,
                           UserRoleRepo userRoleRepo, TokenProvider tokenProvider,
                           OTPCodeService otpCodeService,
                           MailService mailService, PasswordEncoder passwordEncoder,
                           UserAccountMapper userAccountMapper) {
        this.userAccountRepo = userAccountRepo;
        this.userRoleRepo = userRoleRepo;
        this.tokenProvider = tokenProvider;
        this.otpCodeService = otpCodeService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.userAccountMapper = userAccountMapper;
    }

    @Value("${domain}")
    private String domain;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userAccountRepo.findByUsername(username);

        if(user.isEmpty()){
            user=userAccountRepo.findByEmail(username);
        }
        if(user.isEmpty()) throw new UserAccountException("Not found");


        Set<GrantedAuthority> authorities = userRoleRepo.findByUser(user.get()).stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getName()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(
                user.get().getId(),
                user.get().getUsername(),
                user.get().getPassword(),
                user.get().getEmail(),
                authorities);
    }


    @Transactional
    public User save(User user) {
        return userAccountRepo.save(user);
    }

    public User findById(long id) throws UserAccountException {
        Optional<User> result= userAccountRepo.findByIdWithRole(id);
        if(result.isPresent()){
            return result.get();
        }
        throw new UserAccountException("User not found : "+id);
    }

    @Override
    public User register(UserRegisterRequest request) throws MessagingException {

        if (userAccountRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAccountException("Email exist!");
        }

        if (userAccountRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAccountException("Username exist!");
        }

        if (request.getUsername().contains("@")) {
            throw new UserAccountException("Username invalid!");
        }

        String enPass = passwordEncoder.encode(request.getPassword());

        User newUserAccount = User.builder()
                .fullName(request.getFullName())
                .dob(request.getDob())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(enPass)
                .isActive(false)
                .phoneNumber(request.getPhoneNumber())
                .build();

        // 1. Generate OTP
        String otp = String.valueOf(10000 + new Random().nextInt(90000));

        OTPCode otpCode = new OTPCode(otp, request.getEmail(), 5);
        otpCodeService.saveOTPCode(otpCode);

        // 2. Build verify link
        String verifyLink = String.format(
                domain+"/api/auth/auth-account?email=%s&key=%s",
                request.getEmail(),
                otp
        );

        // 3. Email content (HTML)
        String content = """
            <h3>Xác thực tài khoản</h3>
            <p>Vui lòng click vào link bên dưới để xác thực tài khoản:</p>
            <a href="%s">Xác thực tài khoản</a>
            <p>Link có hiệu lực trong 5 phút.</p>
            """.formatted(verifyLink);

        // 4. Send mail
        mailService.sendEmail(
                request.getEmail(),
                "Xác thực tài khoản",
                content,
                null
        );

        return userAccountRepo.save(newUserAccount);
    }

    @Override
    public AuthRespone authAccount(String email, String key) {
        OTPCode otp=otpCodeService.findOTPCode(email, key);

        if (otp == null) {
            return null;
        }

        userAccountRepo.activateUserByEmail(email);

        CustomUserDetails userDetails = (CustomUserDetails) loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt=tokenProvider.genarateToken(authentication);

        AuthRespone res=new AuthRespone(jwt,true);

        return res;
    }

    @Override
    public AuthRespone login(LoginRequest rq) {
        Authentication authentication=authenticate(rq.getUsername(),rq.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt=tokenProvider.genarateToken(authentication);
        AuthRespone res=new AuthRespone(jwt,true);
        return res;
    }

    @Override
    public AuthRespone loginWithGoogle(Map<String, String> request) {
        String accessToken = request.get("accessToken");
        String tokenInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo?" + accessToken.substring(1);
        RestTemplate restTemplate = new RestTemplate();
        try {
            Map<String, Object> tokenInfo = restTemplate.getForObject(tokenInfoUrl, Map.class);
            if (tokenInfo != null && tokenInfo.containsKey("email")) {
                String email = (String) tokenInfo.get("email");
                String name = (String) tokenInfo.get("name");
                String picture = (String) tokenInfo.get("picture");
                User find_user = findByEmail(email);
                System.out.println(find_user);

                if(find_user==null){
                    String randNumber = String.valueOf(100000000 + new Random().nextLong(899999999));
                    String enPass = passwordEncoder.encode(email+randNumber);

                    // 3. Email content (HTML)
                    String content = """
                    <h3>Mật khẩu tài khoản</h3>
                    <p>Bạn đã đăng nhập bằng Google của web chúng tôi</p>
                    <a href="%s">Shop Anbato</a>
                    <p>Hãy vào hồ sơ người dùng để đổi mật khẩu</p>
                    <p>Mật khẩu mặc định của bạn là "%s"</p>
                    """.formatted(domain,email+randNumber);

                    // 4. Send mail
                    mailService.sendEmail(
                            email,
                            "Mật khẩu tài khoản",
                            content,
                            null
                    );

                    User newUser=User.builder()
                            .username(email)
                            .email(email)
                            .fullName(name)
                            .avt(picture)
                            .password(enPass)
                            .isActive(true).build();
                    newUser = save(newUser);

                    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));

                    CustomUserDetails userDetails = new CustomUserDetails(
                            newUser.getId(),
                            newUser.getUsername(),
                            newUser.getPassword(),
                            email,
                            authorities);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String jwt=tokenProvider.genarateToken(authentication);
                    AuthRespone res=new AuthRespone(jwt,true);
                    return res;
                }
                else{
                    Authentication authentication=authenticate(email);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    String jwt=tokenProvider.genarateToken(authentication);
                    AuthRespone res=new AuthRespone(jwt,true);
                    return res;
                }

            } else {
                throw new UserAccountException("Something went wrong!");
            }
        } catch (Exception e) {
            throw new UserAccountException("Something went wrong!");
        }
    }

    @Override
    public boolean changePass(ChangePasswordRequest request) {
        Long accountId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        User user= findById(accountId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UserAccountException("Mật khẩu cũ không đúng");
        } else {
            String newHashPass=passwordEncoder.encode(request.getNewPassword());
            user.setPassword(newHashPass);
            userAccountRepo.save(user);
            return true;
        }
    }

    @Override
    public UserAccountDTO getProfile(long id) {
        System.out.println(id);
        Optional<User> userAccount=userAccountRepo.findByIdWithRole(id);
        if(userAccount.isPresent()){
            return userAccountMapper.toDto(userAccount.get());
        }
        else throw new UserAccountException("Not found!");
    }

    @Override
    public UserAccountDTO updateInfo(UpdateUserInfoRequest request) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        User userAccount=userAccountRepo.findByIdWithRole(myId).orElseThrow(()-> new UnauthorizedException("Invalid user!"));

        System.out.println(request.getFullName());
        System.out.println(request.getDob());
        userAccount.setFullName(request.getFullName());
        userAccount.setDob(request.getDob());
        userAccountRepo.save(userAccount);

        return userAccountMapper.toDto(userAccount);
    }

    @Override
    public UserAccountDTO updateAvt(String url) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        User userAccount=userAccountRepo.findByIdWithRole(myId).orElseThrow(()-> new UnauthorizedException("Invalid user!"));

        userAccount.setAvt(url);
        userAccountRepo.save(userAccount);

        return userAccountMapper.toDto(userAccount);
    }

    public Page<User> findAll(int page, int size) {
        return userAccountRepo.findAll(PageRequest.of(page,size, Sort.by("userId")));
    }

    @Transactional
    public void deleteById(int id) {
        userAccountRepo.deleteById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userAccountRepo.findByEmail(email).orElse(null);
    }


    public User findByUsername(String username) {
        Optional<User> result= userAccountRepo.findByUsername(username);
        return result.orElse(null);
    }

    @Override
    public List<User> searchUser(String query) {
        List<User> listUser=userAccountRepo.searchUser(query);
        return listUser;
    }

    public Authentication authenticate(String username,String password){
        try{
            CustomUserDetails userDetails=(CustomUserDetails) loadUserByUsername(username);

            if(userDetails==null){
                throw new UserAccountException("Invalid user");
            }
            if(!passwordEncoder.matches(password,userDetails.getPassword())){
                throw new UserAccountException("Invalid password or username");
            }
            return new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        }catch (UsernameNotFoundException e){
            throw new UserAccountException("Invalid user");
        }

    }

    public Authentication authenticate(String username){
        try {
            CustomUserDetails userDetails=(CustomUserDetails) loadUserByUsername(username);

            if(userDetails==null){
                throw new UserAccountException("Invalid user");
            }
            return new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        }catch (UsernameNotFoundException e){
            throw new UserAccountException("Invalid user");
        }

    }
}
