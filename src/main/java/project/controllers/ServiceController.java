package project.controllers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import project.models.ForumModel;
import project.models.ServiceModel;
import project.models.ThreadModel;
import project.models.UserModel;
import project.services.ForumService;
import project.services.ServiceService;
import project.services.ThreadService;
import project.services.UserService;

import java.sql.Timestamp;
import java.util.ArrayList;

@Controller
@RequestMapping("/service")

public class ServiceController {
    private ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping(path = "/status")
    public ResponseEntity getStatus(){
        return ResponseEntity.status(HttpStatus.OK).body(serviceService.getInfo());
    }

    @PostMapping(path = "/clear")
    public ResponseEntity clearDatabase(){
        serviceService.clearAll();
        return ResponseEntity.status(HttpStatus.OK).body("Очистка базы успешно завершена");
    }
}
