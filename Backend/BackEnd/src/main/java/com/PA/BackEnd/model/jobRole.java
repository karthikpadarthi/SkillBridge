package com.PA.BackEnd.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Document(collection = "job_roles")
public class jobRole {
    @Id
    private String id;
    private String roleName;
    private List<String> requiredSkills;
    private Map<String, Integer> skillDemand = new LinkedHashMap<>();

    public jobRole() {}

    public jobRole(String roleName, List<String> requiredSkills) {
        this.roleName = roleName;
        this.requiredSkills = requiredSkills;
        this.id = roleName.toLowerCase().replace(" ", "-");
    }

    public jobRole(String roleName, List<String> requiredSkills, Map<String, Integer> skillDemand) {
        this(roleName, requiredSkills);
        this.skillDemand = skillDemand;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Map<String, Integer> getSkillDemand() {
        return skillDemand;
    }

    public void setSkillDemand(Map<String, Integer> skillDemand) {
        this.skillDemand = skillDemand;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
