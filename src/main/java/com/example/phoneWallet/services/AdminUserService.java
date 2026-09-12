package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.dto.PageResponse;
import com.example.phoneWallet.dto.UserSummaryResponse;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public AdminUserService(UserRepository userRepository, CurrentUserService currentUserService, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    public PageResponse<UserSummaryResponse> listUsers(int page, int size) {
        Page<UserSummaryResponse> mapped = userRepository.findAll(
                PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"))
        ).map(UserSummaryResponse::from);
        return PageResponse.from(mapped);
    }

    @Transactional
    public UserSummaryResponse updateRole(Long userId, Role role) {
        if (role == Role.ADMIN) {
            throw new AccessDeniedException("ADMIN role cannot be granted through the HTTP API");
        }
        User actor = currentUserService.requireCurrentUser();
        User target = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (target.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Existing ADMIN accounts cannot be modified through the HTTP API");
        }
        if (role != Role.USER && role != Role.MERCHANT) {
            throw new AccessDeniedException("Only USER and MERCHANT roles can be assigned through the HTTP API");
        }
        target.setRole(role);
        User saved = userRepository.save(target);
        auditLogService.logSuccess(AuditAction.USER_ROLE_CHANGED, actor.getId(), actor.getUsername(), actor.getRole(), null, null,
                "Changed user " + target.getId() + " role to " + role);
        return UserSummaryResponse.from(saved);
    }
}
