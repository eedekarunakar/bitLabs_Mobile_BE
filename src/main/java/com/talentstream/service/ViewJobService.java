package com.talentstream.service;
 
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
 
import com.talentstream.dto.JobDTO;
import com.talentstream.dto.RecuriterSkillsDTO;
import com.talentstream.dto.SkillDTO;
import com.talentstream.entity.ApplicantProfile;
import com.talentstream.entity.ApplicantSkills;
import com.talentstream.entity.ApplyJob;
import com.talentstream.entity.Job;
import com.talentstream.entity.RecuriterSkills;
import com.talentstream.repository.ApplicantProfileRepository;
import com.talentstream.repository.JobRepository;
import com.talentstream.exception.CustomException;
@Service
public class ViewJobService {
	@Autowired
    private JobRepository jobRepository;
	@Autowired
	private CompanyLogoService companyLogoService;
	@Autowired
    private ApplyJobService applyJobService;
	@Autowired
	private final ApplicantProfileRepository applicantProfileRepository=null;
	
public ResponseEntity<JobDTO> getJobDetailsForApplicant(Long jobId) {
    final ModelMapper modelMapper = new ModelMapper();
	Job job = jobRepository.findById(jobId).orElse(null);
	if (job != null) {
        JobDTO jobDTO = new JobDTO();
        jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
        jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
        //jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
        jobDTO.setEmail(job.getJobRecruiter().getEmail());
        jobDTO.setJobStatus(job.getJobStatus());
        jobDTO.setJobTitle(job.getJobTitle());
        jobDTO.setMinimumExperience(job.getMinimumExperience());
        jobDTO.setMaximumExperience(job.getMaximumExperience());
        jobDTO.setMaxSalary(job.getMaxSalary());
        jobDTO.setMinSalary(job.getMinSalary());
        jobDTO.setLocation(job.getLocation());
        jobDTO.setEmployeeType(job.getEmployeeType());
        jobDTO.setIndustryType(job.getIndustryType());
        //jobDTO.setMinimumQualification(job.getMinimumQualification());
        jobDTO.setSpecialization(job.getSpecialization());
      //  jobDTO.setJobHighlights(job.getJobHighlights());
        jobDTO.setDescription(job.getDescription());
        jobDTO.setCreationDate(job.getCreationDate());
        jobDTO.setJobURL(job.getJobURL());
        long jobRecruiterId = job.getJobRecruiter().getRecruiterId();
	    byte[] imageBytes = null;
//	    try {
//	    	imageBytes = companyLogoService.getCompanyLogo(jobRecruiterId);
//	    }catch (CustomException ce) {
//        	System.out.println(ce.getMessage());
//        } 
	    System.out.println("Job Recruiter ID: " + jobRecruiterId);
	    System.out.println("Image Bytes: " + Arrays.toString(imageBytes));
 
	   
	        //jobDTO.setLogoFile(imageBytes);
        return ResponseEntity.ok(jobDTO);
    } else {
        throw new CustomException("Job with ID " + jobId + " not found.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
public ResponseEntity<?> getJobDetailsForApplicant(Long jobId, Long applicantId) {
    final ModelMapper modelMapper = new ModelMapper();
    Job job = jobRepository.findById(jobId).orElse(null);
    
 // Define the mapping between skills and suggested courses
    Map<String, String> skillToCourseMap = new HashMap<>();
    skillToCourseMap.put("HTML", "HTML&CSS");
    skillToCourseMap.put("CSS", "HTML&CSS");
    skillToCourseMap.put("JAVA", "JAVA");
    skillToCourseMap.put("PYTHON", "PYTHON");
    skillToCourseMap.put("MYSQL", "MYSQL");
    skillToCourseMap.put("SQL", "MYSQL");
    skillToCourseMap.put("SQL-SERVER", "MYSQL");
    skillToCourseMap.put("JAVASCRIPT", "JAVASCRIPT");
    skillToCourseMap.put("REACT", "REACT");
    skillToCourseMap.put("SPRING", "SPRING BOOT");
    skillToCourseMap.put("SPRING BOOT", "SPRING BOOT");
   
    
    
    
    List<String> suggestedCourses = List.of("HTML&CSS", "JAVA", "PYTHON", "MYSQL","SQL","JAVASCRIPT","REACT","SPRING","SPRING BOOT","SQL-SERVER");

    if (job == null) {
        throw new CustomException("Job with ID " + jobId + " not found.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    ApplicantProfile applicantProfile = applicantProfileRepository.findByApplicantId(applicantId);
    if (applicantProfile == null) {
        throw new CustomException("Applicant with ID " + applicantId + " not found.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Set<ApplicantSkills> applicantSkills = applicantProfile.getSkillsRequired();
    Set<RecuriterSkills> jobSkills = job.getSkillsRequired(); // Assuming Job has a method to get required skills

   
    Set<ApplicantSkills> matchedSkills = new HashSet<>();
   
    Set<ApplicantSkills> neitherMatchedNorNonMatchedSkills = new HashSet<>(applicantSkills);
    
    
    for (ApplicantSkills applicantSkill : applicantSkills) {
        boolean isMatched = jobSkills.stream()
            .anyMatch(jobSkill -> jobSkill.getSkillName().equalsIgnoreCase(applicantSkill.getSkillName()));
        
        if (isMatched) {
     
            matchedSkills.add(applicantSkill);
            neitherMatchedNorNonMatchedSkills.remove(applicantSkill);
        } 
        
    }
    
   
    double matchPercentage = ((double) matchedSkills.size() / jobSkills.size()) * 100;
    int roundedMatchPercentage = (int) Math.round(matchPercentage);
    String matchStatus;
    if (matchPercentage <= 45) {
        matchStatus = "Poor Match";
    } else if (matchPercentage <= 79) {
        matchStatus = "Fair Match";
    } else {
        matchStatus = "Good Match";
    }
    
 // Remove matched skills from jobSkills
    Set<RecuriterSkills> nonMatchedSkills = new HashSet<>(jobSkills);
    nonMatchedSkills.removeIf(jobSkill -> matchedSkills.stream()
        .anyMatch(matchedSkill -> matchedSkill.getSkillName().equalsIgnoreCase(jobSkill.getSkillName())));
    
   
    
    // Determine suggested courses based on non-matched skills
    List<String> nonMatchedSkillNames = nonMatchedSkills.stream()
        .map(RecuriterSkills::getSkillName)
        .map(String::toUpperCase) // Ensure case-insensitive comparison
        .collect(Collectors.toList());
    
    List<String> matchedCourses = nonMatchedSkillNames.stream()
            .map(skill -> skillToCourseMap.get(skill))
            .filter(course -> course != null && suggestedCourses.contains(course))
            .distinct()
            .collect(Collectors.toList());
    
//    List<String> matchedCourses = new ArrayList<String>();
    
 // Remove matched skills from jobSkills
    jobSkills.removeIf(jobSkill -> matchedSkills.stream()
        .anyMatch(matchedSkill -> matchedSkill.getSkillName().equalsIgnoreCase(jobSkill.getSkillName())));
    
    job.setSkillsRequired(jobSkills);
    
 
    
   
   

    JobDTO jobDTO = modelMapper.map(job, JobDTO.class);
    jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
    jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
    // jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
    jobDTO.setEmail(job.getJobRecruiter().getEmail());
    jobDTO.setMatchedSkills(matchedSkills);
    jobDTO.setMatchPercentage(roundedMatchPercentage);
    jobDTO.setMatchStatus(matchStatus);
    jobDTO.setSugesstedCourses(matchedCourses);
    jobDTO.setMatchPercentage(roundedMatchPercentage);
    jobDTO.setAdditionalSkills(neitherMatchedNorNonMatchedSkills);
    
    

    long jobRecruiterId = job.getJobRecruiter().getRecruiterId();
    byte[] imageBytes = null;
    // try {
    //     imageBytes = companyLogoService.getCompanyLogo(jobRecruiterId);
    // } catch (CustomException ce) {
    //     System.out.println(ce.getMessage());
    // }
    // jobDTO.setLogoFile(imageBytes);

    ApplyJob applyJob = applyJobService.getByJobAndApplicant(jobId, applicantId);
    if (applyJob != null) {
        jobDTO.setJobStatus("Already Applied");
    } else {
        jobDTO.setJobStatus("Apply now");
    }

    return ResponseEntity.ok(jobDTO);
}

public ResponseEntity<?> getJobDetailsForApplicantSkillMatch(Long jobId, Long applicantId) {

	final ModelMapper modelMapper = new ModelMapper();
			Job job = jobRepository.findById(jobId).orElse(null);
			
			 if (job == null) {
			        throw new CustomException("Job with ID " + jobId + " not found.", HttpStatus.INTERNAL_SERVER_ERROR);
			    }
			ApplicantProfile applicantProfile = applicantProfileRepository.findByApplicantId(applicantId);
			    if (applicantProfile == null) {
			        throw new CustomException("Applicant with ID " + applicantId + " not found.", HttpStatus.INTERNAL_SERVER_ERROR);
			    }
			  Set<ApplicantSkills> applicantSkills = applicantProfile.getSkillsRequired();
			    Set<RecuriterSkills> jobSkills = job.getSkillsRequired(); // Assuming Job has a method to get required skills
			    
			   
			    Set<ApplicantSkills> matchedSkills = new HashSet<>();
			   
			    Set<ApplicantSkills> neitherMatchedNorNonMatchedSkills = new HashSet<>(applicantSkills);
			    int originalJobSkillsSize = jobSkills.size();
			    
			    
			    for (ApplicantSkills applicantSkill : applicantSkills) {
			        boolean isMatched = jobSkills.stream()
			            .anyMatch(jobSkill -> jobSkill.getSkillName().equalsIgnoreCase(applicantSkill.getSkillName()));
			        
			        if (isMatched) {
			     
			            matchedSkills.add(applicantSkill);
			            neitherMatchedNorNonMatchedSkills.remove(applicantSkill);
			        } 
			        
			    }
			
			// Remove matched skills from jobSkills
			    jobSkills.removeIf(jobSkill -> matchedSkills.stream()
			        .anyMatch(matchedSkill -> matchedSkill.getSkillName().equalsIgnoreCase(jobSkill.getSkillName())));
			    
			    job.setSkillsRequired(jobSkills);
		
			
			 double matchPercentage = ((double) matchedSkills.size() / originalJobSkillsSize) * 100;
			 System.out.println(matchPercentage+" match ");
			    int roundedMatchPercentage = (int) Math.round(matchPercentage);
			    System.out.println(roundedMatchPercentage+" round ");
			    JobDTO jobDTO = modelMapper.map(job, JobDTO.class);
			jobDTO.setMatchPercentage(roundedMatchPercentage);
			jobDTO.setMatchedSkills(matchedSkills);
			    jobDTO.setAdditionalSkills(neitherMatchedNorNonMatchedSkills);
			    
			return ResponseEntity.ok(jobDTO);
}
 
}