package io.cse482.pmtool.services;

import io.cse482.pmtool.domain.Backlog;
import io.cse482.pmtool.domain.ProjectTask;
import io.cse482.pmtool.exceptions.ProjectNotFoundException;
import io.cse482.pmtool.repositories.BacklogRepository;
import io.cse482.pmtool.repositories.ProjectRepository;
import io.cse482.pmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ProjectTaskService {


    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;


    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username){

        //Exception: project not found


            //all project tasks to be added to a specific project, project != null, backlog exists
            Backlog backlog = projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog();//backlogRepository.findByProjectIdentifier(projectIdentifier);


            //set backlog to project task
            projectTask.setBacklog(backlog);

            //want project sequence to be like this: IDPRO-1 IDPRO-2
            Integer BacklogSequence = backlog.getPTSequence();
            //update backlog sequence
            BacklogSequence++;

            backlog.setPTSequence(BacklogSequence);

            //add sequence to project task
            projectTask.setProjectSequence(projectIdentifier+"-"+BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);


            //initial status when status is null
            if(projectTask.getStatus()==""||projectTask.getStatus()==null){
                projectTask.setStatus("TO_DO");
            }

            //Initial priority when priority is null
            if(projectTask.getPriority()==null||projectTask.getPriority()==0){
                projectTask.setPriority(3);
            }

            return projectTaskRepository.save(projectTask);


    }

    public Iterable<ProjectTask>findBacklogById(String id, String username){

        projectService.findProjectByIdentifier(id, username);

        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username){

        //make sure we are searching on an existing backlog
        projectService.findProjectByIdentifier(backlog_id, username);

        //make sure that our task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);

        if(projectTask == null){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' not found");
        }

        //make sure that the backlog/project id in the path corresponds to the right project
        if(!projectTask.getProjectIdentifier().equals(backlog_id)){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' does not exist in project: '"+backlog_id);
        }


        return projectTask;
    }

    //Update project task
    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username){
        //find existing project task
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        //replace it with updated project
        projectTask = updatedTask;

        //save update
        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTaskRepository.delete(projectTask);
    }


}
