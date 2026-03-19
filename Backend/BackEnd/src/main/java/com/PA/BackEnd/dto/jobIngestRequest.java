package com.PA.BackEnd.dto;

import java.util.ArrayList;
import java.util.List;

public class jobIngestRequest {
    private String targetRole;
    private List<jobPostingInput> postings = new ArrayList<>();

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public List<jobPostingInput> getPostings() {
        return postings;
    }

    public void setPostings(List<jobPostingInput> postings) {
        this.postings = postings;
    }
}
