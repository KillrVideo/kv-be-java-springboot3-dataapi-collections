package com.killrvideo.dao;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;

import static com.datastax.astra.client.model.Filters.eq;

import com.killrvideo.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao {

    private final Collection<Document> userCollection;

    @Autowired
    public UserDao(Database killrVideoDatabase) {
        this.userCollection = killrVideoDatabase.getCollection("users");
    }

    public User save(User user) {
        userCollection.insertOne(toDocument(user));
        return user;
    }

    public Optional<User> findById(String userId) {
        try {
            Optional<Document> doc = userCollection.findById(userId);
            if (doc.isPresent()) {
                User user = toUser(doc.get());
                return Optional.ofNullable(user);
            }
            return Optional.ofNullable(null);
        } catch (Exception e) {
            return Optional.ofNullable(null);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
        Optional<Document> doc = userCollection.findOne(eq("email", email));
        if (doc.isPresent()) {
            User user = toUser(doc.get());
            return Optional.ofNullable(user);
            }
            return Optional.ofNullable(null);
        } catch (Exception e) {
            return Optional.ofNullable(null);
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    private Document toDocument(User user) {
        return new Document()
            .append("user_id", user.getUserId())
            .append("first_name", user.getFirstName())
            .append("last_name", user.getLastName())
            .append("email", user.getEmail())
            .append("hashed_password", user.getHashedPassword());
    }

    private User toUser(Document document) {
        return new User(
            document.getString("user_id"),
            document.getString("first_name"),
            document.getString("last_name"),
            document.getString("email"),
            document.getString("hashed_password"),
            document.getInstant("created_at")
        );
    }
}