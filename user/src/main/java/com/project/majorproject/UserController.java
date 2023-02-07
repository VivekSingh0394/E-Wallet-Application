package com.project.majorproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/add")
    public String addUser(@RequestBody() UserRequest userRequest)
    {
        return userService.addUser(userRequest);
    }
    @GetMapping("/findByUser/{userName}")
    public User findUserByUserName(@PathVariable("userName") String userName)
    {
     return userService.findByUserName(userName);
    }
    @GetMapping("/findEmail/{userName}")
    public UserResponseDto findEmailAndName(@PathVariable("userName")String userName)
    {
        return userService.findEmailAndName(userName);
    }

}

