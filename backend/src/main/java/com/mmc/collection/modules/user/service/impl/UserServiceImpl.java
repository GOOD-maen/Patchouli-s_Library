package com.mmc.collection.modules.user.service.impl;

import com.mmc.collection.modules.user.dao.UserDAO;
import com.mmc.collection.modules.user.dto.RegisterDTO;
import com.mmc.collection.modules.user.dto.LoginDTO;
import com.mmc.collection.modules.user.dto.UserDTO;
import com.mmc.collection.modules.user.dto.UserUpdateDTO;
import com.mmc.collection.modules.user.entity.UserDO;
import com.mmc.collection.modules.user.service.UserService;
import com.mmc.collection.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mmc.collection.common.email.service.EmailService;
import com.mmc.collection.common.cache.VerificationCodeCache;

import java.util.Optional;
import java.util.Random;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeCache codeCache;

    @Autowired
    private JwtUtil jwtUtil;



    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 生成100000-999999之间的6位数
        return String.valueOf(code);
    }

    @Override
    public UserDTO register(RegisterDTO registerDTO) {
        // 1. 验证验证码
        String email = registerDTO.getEmail();
        String inputCode = registerDTO.getVerificationCode();

        // 检查验证码是否为空
        if (inputCode == null || inputCode.trim().isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }

        // 从缓存获取正确的验证码
        String storedCode = codeCache.get(email);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(inputCode)) {
            throw new RuntimeException("验证码错误");
        }

        // 验证码正确，从缓存中删除（防止重复使用）
        codeCache.remove(email);

        // 2. 检查用户名是否已存在
        if (userDAO.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 3. 检查邮箱是否已存在
        if (userDAO.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 4. 创建新用户
        UserDO user = new UserDO();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());

        // 5. 密码加密
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        user.setPassword(encodedPassword);

        //创建公开的ID
        char[] alphabet = "0123456789".toCharArray();
        String nanoId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, alphabet, 16);
        user.setUuid(nanoId);

        // 6. 保存到数据库
        UserDO savedUser = userDAO.save(user);

        // 7. 转换为DTO返回
        return convertToDTO(savedUser);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 1. 参数验证
        if (loginDTO.getAccount() == null || loginDTO.getAccount().trim().isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 2. 根据账号查询用户
        Optional<UserDO> userOpt = userDAO.findByUsername(loginDTO.getAccount());
        if (userOpt.isEmpty()) {
            userOpt = userDAO.findByEmail(loginDTO.getAccount());
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }

        UserDO user = userOpt.get();

        // 3. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 登录成功，生成 JWT token（使用用户名作为 subject）
        String token = jwtUtil.generateToken(user.getUsername());
        return token;
    }

    @Override
    public UserDTO getUserById(Long id) {
        UserDO user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        UserDO user = userDAO.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    /*发送测试邮件（固定验证码）*/

    /*发送正式验证码（随机生成并存储）*/
    @Override
    public void sendVerificationCode(String email) {
        // 1. 检查邮箱是否已被注册（可选）
        if (userDAO.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 2. 生成6位随机验证码
        String code = generateCode();

        // 3. 存储到缓存，有效期5分钟（300秒）
        codeCache.put(email, code, 300);

        // 4. 发送邮件
        emailService.sendVerificationCode(email, code ,"Patchouli's Library的注册验证码","亲爱的用户，您好。\n很高兴与您的相遇，希望我们能够陪您一起度过一段愉快的时光。");

        System.out.println("已向 " + email + " 发送验证码: " + code);
    }

    /**
     * 将 UserDO 转换为 UserDTO
     */
    private UserDTO convertToDTO(UserDO user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUuid(user.getUuid());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setAvatar(user.getAvatar());
        dto.setBackgroundImage(user.getBackgroundImage());
        dto.setSignature(user.getSignature());
        return dto;
    }


    @Override
    public UserDTO getUserByAccount(String account) {
        Optional<UserDO> userOpt = userDAO.findByUsername(account);
        if (userOpt.isEmpty()) {
            userOpt = userDAO.findByEmail(account);
        }
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        return convertToDTO(userOpt.get());
    }

    @Override
    public void sendResetCode(String email) {
        // 1. 检查邮箱是否存在
        if (!userDAO.existsByEmail(email)) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 2. 生成6位随机验证码
        String code = generateCode();

        // 3. 存储到缓存，有效期5分钟（300秒）
        codeCache.put(email, code, 300);

        // 4. 发送邮件
        emailService.sendVerificationCode(email, code,"Patchouli's Library的重置密码验证码","亲爱的用户，您好。\n很高兴与您的再次相遇，我们将继续竭诚为您服务。");

        System.out.println("已向 " + email + " 发送找回密码验证码: " + code);
    }

    @Override
    public boolean verifyResetCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        String storedCode = codeCache.get(email);
        if (storedCode == null) {
            return false; // 已过期或不存在
        }
        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后从缓存中删除，防止重复使用
            codeCache.remove(email);
        }
        return isValid;
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        // 1. 查找用户
        UserDO user = userDAO.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // 3. 保存
        userDAO.save(user);
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(String currentUsername, UserUpdateDTO updateDTO) {
        UserDO user = userDAO.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
            if (userDAO.existsByUsername(updateDTO.getUsername())) {
                throw new RuntimeException("昵称已被使用");
            }
            user.setUsername(updateDTO.getUsername());
        }

        if (updateDTO.getSignature() != null) {
            user.setSignature(updateDTO.getSignature());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getBackgroundImage() != null) {
            user.setBackgroundImage(updateDTO.getBackgroundImage());
        }

        userDAO.save(user);
        return convertToDTO(user);
    }

}