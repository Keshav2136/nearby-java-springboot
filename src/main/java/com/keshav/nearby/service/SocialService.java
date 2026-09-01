package com.keshav.nearby.service;

import com.keshav.nearby.entity.User;
import com.keshav.nearby.repository.UserRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.Map;

@Service
public class SocialService {

    private final UserRepository userRepository;
    private final Neo4jClient neo4jClient;

    public SocialService(UserRepository userRepository, Neo4jClient neo4jClient) {
        this.userRepository = userRepository;
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public User createUser(User user) {
        User saved = userRepository.save(user);
        neo4jClient.query("MERGE (u:User {userId: $userId}) SET u.username = $username")
            .bindAll(Map.of("userId", saved.getId(), "username", saved.getUsername()))
            .run();
        return saved;
    }

    @Transactional
    public void follow(Long followerId, Long followingId) {
        neo4jClient.query("MATCH (a:User {userId: $followerId}), (b:User {userId: $followingId}) CREATE (a)-[:FOLLOWS]->(b)")
            .bindAll(Map.of("followerId", followerId, "followingId", followingId))
            .run();
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        neo4jClient.query("MATCH (a:User {userId: $followerId})-[r:FOLLOWS]->(b:User {userId: $followingId}) DELETE r")
            .bindAll(Map.of("followerId", followerId, "followingId", followingId))
            .run();
    }

    public Collection<Map<String, Object>> getFollowing(Long userId) {
        return neo4jClient.query("MATCH (u:User {userId: $userId})-[:FOLLOWS]->(following) RETURN following.userId as userId, following.username as username")
            .bindAll(Map.of("userId", userId))
            .fetch().all();
    }

    public Collection<Map<String, Object>> getFollowers(Long userId) {
        return neo4jClient.query("MATCH (u:User {userId: $userId})<-[:FOLLOWS]-(follower) RETURN follower.userId as userId, follower.username as username")
            .bindAll(Map.of("userId", userId))
            .fetch().all();
    }

    public Collection<Map<String, Object>> getSuggestions(Long userId) {
        return neo4jClient.query("""
            MATCH (u:User {userId: $userId})-[:FOLLOWS]->(friend)-[:FOLLOWS]->(fof)
            WHERE fof.userId <> $userId AND NOT (u)-[:FOLLOWS]->(fof)
            RETURN DISTINCT fof.userId as userId, fof.username as username LIMIT 10
            """)
            .bindAll(Map.of("userId", userId))
            .fetch().all();
    }
}
