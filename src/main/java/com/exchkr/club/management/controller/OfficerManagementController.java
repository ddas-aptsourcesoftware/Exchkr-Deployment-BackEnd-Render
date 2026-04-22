package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.MemberCsvRequest;
import com.exchkr.club.management.model.api.request.MemberRequest;
import com.exchkr.club.management.model.api.response.MemberCountResponse;
import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.security.CustomUserDetails;
import com.exchkr.club.management.services.OfficerManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officer/members")
public class OfficerManagementController {

    private final OfficerManagementService officerManagementService;

    public OfficerManagementController(OfficerManagementService officerManagementService) {
        this.officerManagementService = officerManagementService;
    }

    private CustomUserDetails getPrincipal(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Officer')")
    public ResponseEntity<List<UserDTO>> getAllClubMembers(
            Authentication authentication,
            @RequestParam(defaultValue = "all") String filter) {

        CustomUserDetails user = getPrincipal(authentication);
        List<UserDTO> members = officerManagementService.getClubMembers(user.getClubId(), filter);
        return ResponseEntity.ok(members);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_Officer')")
    public ResponseEntity<Void> addMember(
            @RequestBody MemberRequest request,
            Authentication authentication) {

        CustomUserDetails user = getPrincipal(authentication);
        // Pass the Long actingUserId and clubId from JWT
        officerManagementService.addMember(request, user.getUserId(), user.getClubId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAuthority('ROLE_Officer')")
    public ResponseEntity<MemberCountResponse> getMemberCount(Authentication authentication) {
        CustomUserDetails user = getPrincipal(authentication);
        return ResponseEntity.ok(officerManagementService.getMemberCounts(user.getClubId()));
    }
    
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAuthority('ROLE_Officer')")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long memberId,
            Authentication authentication) {

        CustomUserDetails user = getPrincipal(authentication);
        officerManagementService.removeMember(memberId, user.getClubId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/csv", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_Officer')")
    public ResponseEntity<Void> addMembersCSV(
            @ModelAttribute MemberCsvRequest request,
            Authentication authentication) {

        CustomUserDetails user = getPrincipal(authentication);

        try {
            officerManagementService.addMembersCSV(
                    request.getMembersCsvFile(),
                    user.getUserId(),
                    user.getClubId()
            );
            return ResponseEntity.ok().build(); // 200

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }
}