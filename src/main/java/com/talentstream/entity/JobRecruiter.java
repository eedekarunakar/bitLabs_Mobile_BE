package com.talentstream.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
//@Data
public class JobRecruiter {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recruiterId;

    @Column(nullable = false)
    private String companyname;

    @Column(nullable = false, unique = true)
    private String mobilenumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Job> jobs;

    @Column(nullable = false)
    private String roles = "ROLE_JOBRECRUITER";

    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    @JsonBackReference
    private List<TeamMember> teamMembers;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnore
    @JsonBackReference
    private List<PlacementDrive> placementDrive;
    

    public JobRecruiter() {

    }

    @Column(columnDefinition = "int default 0")
    private int alertCount;

    public int getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String role) {
        this.roles = role;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }
    public List<PlacementDrive> getPlacementDrive() {
		return placementDrive;
	}

	public void setPlacementDrive(List<PlacementDrive> placementDrive) {
		this.placementDrive = placementDrive;
	}

}
