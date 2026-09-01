package com.keshav.nearby.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Node("User")
public class UserNode {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String username;

    public UserNode() {}
    public UserNode(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
