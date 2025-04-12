package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.mapper.FollowMapper;
import com.example.redis.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result<String> follow(@PathVariable("id") Long followUserId,@PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result<Boolean> isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

}
