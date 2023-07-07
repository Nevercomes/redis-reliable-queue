package example.rab.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = "application/json")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @PostMapping("/task")
    public String createTask() {
        return businessService.createTask();
    }

    @GetMapping("/task/{id}/status")
    public String getTaskStatus(@PathVariable String id) {
        return businessService.getTaskStatus(id);
    }
}
