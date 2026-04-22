package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.MemberRequest;
import com.exchkr.club.management.model.api.response.MemberCountResponse;
import com.exchkr.club.management.model.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OfficerManagementService {
    
    void addMember(MemberRequest request, Long actingUserId, Long clubId); 
    
    void removeMember(Long memberId, Long clubId);

    List<UserDTO> getClubMembers(Long clubId, String filter);

    MemberCountResponse getMemberCounts(Long clubId);
    
    void addMembersCSV(MultipartFile membersCsvFile, Long userId, Long clubId);
}