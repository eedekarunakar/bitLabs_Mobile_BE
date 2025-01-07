package com.talentstream.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talentstream.entity.Job;
import com.talentstream.entity.JobRecruiter;
import com.talentstream.entity.PlacementDrive;
import com.talentstream.exception.JobNotFoundException;
import com.talentstream.exception.JobRecruiterNotFound;
import com.talentstream.repository.JobRecruiterRepository;
import com.talentstream.repository.JobRepository;
import com.talentstream.repository.PlacementRepository;


@Service
public class PlacementDriveService {
	
	@Autowired
	private PlacementRepository placementRepo;
	
	@Autowired
	private JobRepository jobRepo;
	
	@Autowired
	private JobRecruiterRepository jobRecruiterRepo;
	
	
	//Saving the Placement_Drive
	public PlacementDrive saveDrive(Long recruiterId, PlacementDrive drive) throws JobRecruiterNotFound {
		System.out.println("Request commint to service");
	    JobRecruiter recruiter = jobRecruiterRepo.findById(recruiterId)
	            .orElseThrow(() -> new JobRecruiterNotFound("Invalid recruiter Id: " + drive.getJobRecruiters().getRecruiterId()));
	  
		System.out.println("Getting jobs ");
	    List<Job> jobs = drive.getJobs().stream()
				.map(d -> jobRepo.findById(d.getId())
						.orElseThrow(() -> new IllegalArgumentException("Invalid job Id " + d.getId())))
				.collect(Collectors.toList());
		drive.setJobs(jobs);
		
		
		System.out.println("Set jobs to placement");
        //Jobs as Conducting as Drive
		for (Job job : jobs) {
			job.setPlacementDrive(drive);
		}
		
		//Assumed as a List
		List<PlacementDrive> alldrives= new ArrayList<PlacementDrive>();
		alldrives.add(drive);
		
		//Recruiter adding alldrives
		//recruiter.setPlacementDrive(alldrives);
		
		//Driver to add Recruiter
		System.out.println("Creating placement");
		 drive.setJobRecruiters(recruiter);
	     PlacementDrive save = placementRepo.save(drive);
	     System.out.println("Drive Created Successfully");
	     return save;
	}
	
	
	//Update the PlacementDrive
	public PlacementDrive updateDrive(long id, PlacementDrive updatedDrive) throws JobNotFoundException {
        return placementRepo.findById(id)
                .map(existingDrive -> {
                    existingDrive.setFromDate(updatedDrive.getFromDate());
                    existingDrive.setToDate(updatedDrive.getToDate());
                    existingDrive.setStartTime(updatedDrive.getStartTime());
                    existingDrive.setEndTime(updatedDrive.getEndTime());
                    existingDrive.setMode(updatedDrive.getMode());
                    existingDrive.setGudlines(updatedDrive.getGudlines());
                    existingDrive.setInterviewProccess(updatedDrive.getInterviewProccess());
                    return placementRepo.save(existingDrive);
                })
                .orElseThrow(() -> new JobNotFoundException("Drive with ID " + id + " not found."));
    }
	
	//getting all jobs withoutConductiong All Drives
	public List<Job> getAllJobsWithoutConductingDrive(long recruiterId){
		return jobRepo.findBygetAllJobswithoutConductingDrive(recruiterId);
	}
	
	public Optional<PlacementDrive> getPlacementDriveByDriveId(long id) {
	    return placementRepo.findById(id);
	 }
}
