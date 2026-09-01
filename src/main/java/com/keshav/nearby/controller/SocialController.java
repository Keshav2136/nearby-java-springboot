package com.keshav.nearby.controller;

import com.keshav.nearby.entity.User;
import com.keshav.nearby.service.SocialService;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return socialService.createUser(user);
    }

    @PostMapping("/follow")
    public String follow(@RequestParam Long followerId, @RequestParam Long followingId) {
        socialService.follow(followerId, followingId);
        return "Followed successfully";
    }

    @PostMapping("/unfollow")
    public String unfollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        socialService.unfollow(followerId, followingId);
        return "Unfollowed successfully";
    }

    @GetMapping("/{userId}/following")
    public Collection<Map<String, Object>> following(@PathVariable Long userId) {
        return socialService.getFollowing(userId);
    }

    @GetMapping("/{userId}/followers")
    public Collection<Map<String, Object>> followers(@PathVariable Long userId) {
        return socialService.getFollowers(userId);
    }

    @GetMapping("/{userId}/suggestions")
    public Collection<Map<String, Object>> suggestions(@PathVariable Long userId) {
        return socialService.getSuggestions(userId);
    }
}
