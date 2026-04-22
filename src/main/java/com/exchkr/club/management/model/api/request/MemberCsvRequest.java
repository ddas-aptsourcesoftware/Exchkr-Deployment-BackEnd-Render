package com.exchkr.club.management.model.api.request;

import org.springframework.web.multipart.MultipartFile;

public class MemberCsvRequest {


    private MultipartFile membersCsvFile;


    public MultipartFile getMembersCsvFile() {
        return membersCsvFile;
    }

    public void setMembersCsvFile(MultipartFile membersCsvFile) {
        this.membersCsvFile = membersCsvFile;
    }
}
