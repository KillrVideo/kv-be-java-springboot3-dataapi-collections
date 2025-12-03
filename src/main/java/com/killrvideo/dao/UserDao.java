package com.killrvideo.dao;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;

import com.killrvideo.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

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

    public Optional<User> findById(String id) {
        try {
            Optional<Document> doc = userCollection.findById(id);
            if (doc.isPresent()) {
                User user = toUser(doc.get());
                return Optional.ofNullable(user);
            }
            return Optional.ofNullable(null);
        } catch (Exception e) {
            return Optional.ofNullable(null);
        }
    }

    public Optional<User> findByUserId(String userId) {
        try {
            Optional<Document> doc = userCollection.findOne(Filters.eq("userid", userId));
            if (doc.isPresent()) {
                User user = toUser(doc.get());
                return Optional.ofNullable(user);
            }
            return Optional.ofNullable(null);
        } catch (Exception e) {
        	System.out.printf("Error locating user: [{}]\n" + e.toString(), userId);
            return Optional.ofNullable(null);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            Optional<Document> doc = userCollection.findOne(Filters.eq("email", email));
            if (doc.isPresent()) {
                User user = toUser(doc.get());
                System.out.println("User found: " + email);
                return Optional.ofNullable(user);
            } else {
                System.out.println("User not found: " + email);
            }
            return Optional.ofNullable(null);
        } catch (Exception e) {
            System.out.println("Error finding user by email: " + email);
            System.out.println("Error: " + e.getMessage());
            return Optional.ofNullable(null);
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public void update(User user) {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update");
        }
        userCollection.replaceOne(Filters.eq("userid", user.getUserId()), toDocument(user));
    }

    public List<User> searchUsers(String queryString, int limit) {
        try {

            Filter filter;

            if (queryString != null){
                filter = Filters.or(
                    Filters.eq("email", queryString),
                    Filters.eq("firstname", queryString),
                    Filters.eq("lastname", queryString)
                );
            } else {
                return Collections.emptyList();
            }

            CollectionFindOptions findOptions = new CollectionFindOptions().limit(limit);
            List<Document> docs = userCollection.find(filter, findOptions).toList();
            return docs.stream().map(this::toUser).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    private Document toDocument(User user) {
        return new Document()
            .append("userid", user.getUserId())
            .append("firstname", user.getFirstName())
            .append("lastname", user.getLastName())
            .append("email", user.getEmail())
            .append("hashed_password", user.getHashedPassword())
            .append("roles", user.getRoles())
        	.append("created_date", user.getCreatedAt());
    }

    private User toUser(Document document) {
        return new User(
            document.getString("userid"),
            document.getString("firstname"),
            document.getString("lastname"),
            document.getString("email"),
            document.getString("hashed_password"),
            document.getString("created_date"),
            document.getString("roles")
        );
    }
}