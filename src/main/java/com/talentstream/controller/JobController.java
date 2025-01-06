package com.talentstream.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentstream.dto.JobDTO;
import com.talentstream.dto.RecuriterSkillsDTO;
import com.talentstream.dto.ScreeningAnswersWrapperDTO;
import com.talentstream.entity.Job;
import com.talentstream.entity.JobSearchCriteria;
import com.talentstream.entity.RecuriterSkills;
import com.talentstream.exception.CustomException;
import com.talentstream.service.CompanyLogoService;
import com.talentstream.service.JobService;

@RestController
@RequestMapping("/job")
public class JobController {

    final ModelMapper modelMapper = new ModelMapper();
    private final JobService jobService;
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private CompanyLogoService companyLogoService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/recruiters/saveJob/{jobRecruiterId}")
    public ResponseEntity<String> saveJob(@Valid @RequestBody JobDTO jobDTO, BindingResult bindingResult,
            @PathVariable Long jobRecruiterId) {
        if (bindingResult.hasErrors()) {
            // Handle validation errors
            Map<String, String> fieldErrors = new LinkedHashMap<>();

            bindingResult.getFieldErrors().forEach(fieldError -> {
                String fieldName = fieldError.getField();
                String errorMessage = fieldError.getDefaultMessage();

                // Append each field and its error message on a new line
                fieldErrors.merge(fieldName, errorMessage,
                        (existingMessage, newMessage) -> existingMessage + "\n" + newMessage);
            });

            // Construct the response body with each field and its error message on separate
            // lines
            StringBuilder responseBody = new StringBuilder();
            fieldErrors.forEach((fieldName, errorMessage) -> responseBody.append(fieldName).append(": ")
                    .append(errorMessage).append("\n"));
            logger.error("Validation errors occurred while saving job.");
            return ResponseEntity.badRequest().body(responseBody.toString());
        }
        try {
            return jobService.saveJob(jobDTO, jobRecruiterId);
        } catch (CustomException ce) {
            logger.error("CustomException occurred while saving job: {}", ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while saving job.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/recruiters/viewJobs/{jobRecruiterId}")
    public ResponseEntity<?> getJobsByRecruiter(@PathVariable Long jobRecruiterId) {
        try {
            List<Job> jobs = jobService.getJobsByRecruiter(jobRecruiterId);

            if (jobs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<JobDTO> jobDTOs = jobs.stream()
                    .map(job -> {
                        JobDTO jobDTO = modelMapper.map(job, JobDTO.class);

                        jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
                        jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
                        jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
                        jobDTO.setEmail(job.getJobRecruiter().getEmail());
                        jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
                        return jobDTO;
                    })
                    .collect(Collectors.toList());
            for (JobDTO job : jobDTOs) {
                long jobRecruiterId1 = job.getRecruiterId();
                byte[] imageBytes = null;
                try {
                    imageBytes = companyLogoService.getCompanyLogo(jobRecruiterId1);
                } catch (CustomException ce) {
                    logger.error("CustomException occurred while getting company logo for job with ID {}: {}",
                            job.getId(), ce.getMessage());
                    System.out.println(ce.getMessage());
                }

                job.setLogoFile(imageBytes);
            }

            return ResponseEntity.ok(jobDTOs);
        } catch (CustomException ce) {
            logger.error("CustomException occurred while getting jobs by recruiter ID {}: {}", jobRecruiterId,
                    ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while getting jobs by recruiter ID {}", jobRecruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(@ModelAttribute JobSearchCriteria searchCriteria) {
        try {
            List<Job> jobs = jobService.searchJobs(searchCriteria);

            if (jobs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<JobDTO> jobDTOs = jobs.stream()
                    .map(job -> {
                        JobDTO jobDTO = modelMapper.map(job, JobDTO.class);
                        jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
                        jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
                        jobDTO.setEmail(job.getJobRecruiter().getEmail());
                        return jobDTO;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(jobDTOs);
        } catch (CustomException ce) {
            logger.error("CustomException occurred while searching jobs: {}", ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while searching jobs.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/recruiters/viewJobs")
    public ResponseEntity<?> getAllJobs() {
        try {
            List<Job> jobs = jobService.getAllJobs();
            List<JobDTO> jobDTOs = jobs.stream()
                    .map(job -> convertEntityToDTO(job))
                    .collect(Collectors.toList());
            logger.info("Retrieved all jobs successfully.");
            return ResponseEntity.ok(jobDTOs);
        } catch (CustomException ce) {
            logger.error("CustomException occurred while retrieving all jobs: {}", ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while retrieving all jobs.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJobById(@PathVariable Long jobId) {
        try {
            Job job = jobService.getJobById(jobId);

            if (job != null) {
                JobDTO jobDTO = modelMapper.map(job, JobDTO.class);
                logger.info("Retrieved job with ID {} successfully.", jobId);
                return ResponseEntity.ok(jobDTO);
            } else {
                logger.warn("Job with ID {} not found.", jobId);
                return ResponseEntity.notFound().build();
            }
        } catch (CustomException ce) {
            logger.error("CustomException occurred while retrieving job with ID {}: {}", jobId, ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while retrieving job with ID {}.", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/recruiterscountjobs/{recruiterId}")
    public ResponseEntity<?> countJobsByRecruiter(@PathVariable Long recruiterId) {
        try {
            long jobCount = jobService.countJobsByRecruiterId(recruiterId);
            logger.info("Retrieved job count for recruiter with ID {} successfully.", recruiterId);
            return ResponseEntity.ok(jobCount);
        } catch (CustomException ce) {
            logger.error("CustomException occurred while retrieving job count for recruiter with ID {}: {}",
                    recruiterId, ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while retrieving job count for recruiter with ID {}.",
                    recruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }
    
    @GetMapping("/recruiterscountinactivejobs/{recruiterId}")
    public ResponseEntity<?>countInActiveJobsByRecruiter(@PathVariable Long recruiterId){
    	try {
    		long jobCount = jobService.countInActiveJobs(recruiterId);
    		logger.info("Retrieved inactive job counts for recruiter with ID {} successfully.", recruiterId);
    		return ResponseEntity.ok(jobCount);
    	}
    	catch(CustomException ce) {
    		logger.error("exception occurred while retrieving inactive job counts for recruiter with ID {}:{}",recruiterId, ce.getMessage());
    		 return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
    	}
    	catch(Exception e) {
    		logger.error("Internal server error occurred while retrieving inactive job count for recruiter with ID {}.",recruiterId, e);
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
    	}
    }

    private RecuriterSkillsDTO convertSkillsEntityToDTO(RecuriterSkills skill) {
        RecuriterSkillsDTO skillDTO = new RecuriterSkillsDTO();
        skillDTO.setSkillName(skill.getSkillName());
        return skillDTO;
    }

    private JobDTO convertEntityToDTO(Job job) {
        JobDTO jobDTO = new JobDTO();
        jobDTO.setId(job.getId());
        jobDTO.setJobTitle(job.getJobTitle());
        jobDTO.setMinimumExperience(job.getMinimumExperience());
        jobDTO.setMaximumExperience(job.getMaximumExperience());
        jobDTO.setMinSalary(job.getMinSalary());
        jobDTO.setMaxSalary(job.getMaxSalary());
        jobDTO.setLocation(job.getLocation());
        jobDTO.setEmployeeType(job.getEmployeeType());
        jobDTO.setIndustryType(job.getIndustryType());
        jobDTO.setMinimumQualification(job.getMinimumQualification());
        jobDTO.setSpecialization(job.getSpecialization());
        jobDTO.setRecruiterId(job.getJobRecruiter().getRecruiterId());
        jobDTO.setCompanyname(job.getJobRecruiter().getCompanyname());
        jobDTO.setEmail(job.getJobRecruiter().getEmail());
        jobDTO.setMobilenumber(job.getJobRecruiter().getMobilenumber());
        jobDTO.setScreeningQuestions(job.getScreeningQuestions());
        jobDTO.setCreationDate(job.getCreationDate());
        jobDTO.setIsSaved(job.getIsSaved());
        Set<RecuriterSkillsDTO> skillsDTOList = job.getSkillsRequired().stream()
                .map(this::convertSkillsEntityToDTO)
                .collect(Collectors.toSet());
        jobDTO.setSkillsRequired(skillsDTOList);
        return jobDTO;
    }

    @PostMapping("/changeStatus/{jobId}/{newStatus}")
    public ResponseEntity<String> changeJobStatus(@PathVariable Long jobId, @PathVariable String newStatus) {
        try {
            jobService.changeJobStatus(jobId, newStatus);
            logger.info("Job status changed to '{}' for jobId={}", newStatus, jobId);
            return ResponseEntity.ok("Job status changed successfully.");
        } catch (CustomException ce) {
            logger.error("Failed to change job status for jobId={}, status={}, error={}", jobId, newStatus,
                    ce.getMessage(), ce);
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error while changing job status for jobId={}, status={}", jobId, newStatus,
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/getStatus/{jobId}")
    public ResponseEntity<String> getJobStatus(@PathVariable Long jobId) {
        try {
            // Retrieve job status from the service
            String jobStatus = jobService.getJobStatus(jobId);
            logger.info("Successfully retrieved job status for jobId={}: {}", jobId, jobStatus);
            return ResponseEntity.ok(jobStatus);
        } catch (CustomException ce) {
            logger.error("Error retrieving job status for jobId={}, error={}", jobId, ce.getMessage(), ce);
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error retrieving job status for jobId={}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/{jobId}/{recruiterId}")
    public ResponseEntity<?> getJobById(@PathVariable Long jobId, @PathVariable Long recruiterId) {
        try {
            Job job = jobService.getJobById(jobId);

            if (job != null) {
                JobDTO jobDTO = modelMapper.map(job, JobDTO.class);
                logger.info("Job found for jobId={} and recruiterId={}", jobId, recruiterId);
                return ResponseEntity.ok(jobDTO);
            } else {
                logger.warn("No job found for jobId={} and recruiterId={}", jobId, recruiterId);
                return ResponseEntity.notFound().build();
            }
        } catch (CustomException ce) {
            logger.error("Error retrieving job for jobId={} and recruiterId={}, error={}", jobId, recruiterId,
                    ce.getMessage(), ce);
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error retrieving job for jobId={} and recruiterId={}", jobId, recruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @PutMapping("/editJob/{jobId}/{recruiterId}")
    public ResponseEntity<String> editJob(@RequestBody @Valid JobDTO jobDTO, @PathVariable Long jobId,
            @PathVariable Long recruiterId) {
        try {
            logger.info("Attempting to edit job for jobId={} by recruiterId={}", jobId, recruiterId);
            return jobService.editJob(jobDTO, jobId);
        } catch (CustomException ce) {
            logger.error("Error editing job for jobId={}, recruiterId={}, error={}", jobId, recruiterId,
                    ce.getMessage(), ce);
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error while editing job for jobId={}, recruiterId={}", jobId, recruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/promote/{applicantId}/{promote}")
    public List<Job> getJobsByPromoteState(@PathVariable long applicantId, @PathVariable String promote) {
        logger.info("Retrieving jobs by promote state={} for applicantId={}", promote, applicantId);
        return jobService.getJobsByPromoteState(applicantId, promote);
    }

    @GetMapping("/{recruiterId}/active")
    public ResponseEntity<?> getActiveJobsForRecruiter(@PathVariable Long recruiterId) {
        try {
            logger.info("Retrieving active jobs for recruiterId={}", recruiterId);
            return ResponseEntity.ok(jobService.getActiveJobsForRecruiter(recruiterId));
            
        } catch (Exception e) {
            logger.error("Internal server error while retrieving active jobs for recruiterId={}", recruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @GetMapping("/{recruiterId}/inactive")
    public ResponseEntity<?> getInactiveJobsForRecruiter(@PathVariable Long recruiterId) {
        try {
            logger.info("Retrieving inactive jobs for recruiterId={}", recruiterId);
            return ResponseEntity.ok(jobService.getInactiveJobsForRecruiter(recruiterId));
        } catch (Exception e) {
            logger.error("Internal server error while retrieving inactive jobs for recruiterId={}", recruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }

    @PostMapping("/recruiters/cloneJob/{jobId}/{jobRecruiterId}")
    public ResponseEntity<Map<String, String>> cloneJob(@PathVariable Long jobId, @PathVariable Long jobRecruiterId) {
        try {
            logger.info("Initiating job cloning for jobId={}, jobRecruiterId={}", jobId, jobRecruiterId);
            String result = jobService.cloneJob(jobId, jobRecruiterId);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            logger.info("Job cloning successful for jobId={}, jobRecruiterId={}", jobId, jobRecruiterId);
            return ResponseEntity.ok(response);
        } catch (CustomException ce) {
            logger.error("Custom exception occurred while cloning job for jobId={}, jobRecruiterId={}: {}", jobId,
                    jobRecruiterId, ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while cloning job for jobId={}, jobRecruiterId={}", jobId,
                    jobRecruiterId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/applicants/{applicantId}/saveAnswers/{jobId}")
    public ResponseEntity<String> saveScreeningAnswers(@PathVariable Long applicantId, @PathVariable Long jobId, @RequestBody ScreeningAnswersWrapperDTO answersWrapper) {
        try {
            jobService.saveScreeningAnswers(applicantId, jobId, answersWrapper.getAnswers());
            return ResponseEntity.ok("Answers saved successfully.");
        } catch (CustomException ce) {
            logger.error("CustomException occurred while saving answers for job ID {}: {}", jobId, ce.getMessage());
            return ResponseEntity.status(ce.getStatus()).body(ce.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error occurred while saving answers for job ID {}.", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred.");
        }
    }
    
    @PostMapping("/postjob/job")
	public ResponseEntity<?> saveJob(@RequestBody Job job){
		try {
			Job saveJob=jobService.saveJob(job);
			return new ResponseEntity<>(saveJob,HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>("Some External Errors Occurs.."+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
    
}
