package example.rab.business;

import org.springframework.beans.factory.annotation.Autowired;
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

}
