Okay, thank you for the excellent clarifications. This helps immensely in shaping a precise and effective API specification. Based on your feedback, let's proceed with designing the API.

We will adhere to OpenAPI guidelines, ensuring a RESTful architecture, JSON for payloads, JWT-based authentication for protected routes, and standard HTTP status codes.

Here is the proposed API Specification for KillrVideo 2025:

---

## KillrVideo 2025 API Specification (Version 1.0)

**Base URL:** `/api/v1`

**Authentication:**
All endpoints, unless otherwise specified, require authentication. Authentication will be handled via JWTs.
The JWT should be sent in the `Authorization` header: `Authorization: Bearer <token>`

**Common Response Objects:**

*   **Error Response:**
    ```json
    {
      "error": {
        "message": "A human-readable error message.",
        "code": "ERROR_CODE_IDENTIFIER", // Optional: A specific error code
        "details": [ /* Optional: Array of specific field errors */ ]
      }
    }
    ```
*   **Pagination:** Responses returning lists of items will be paginated.
    Query Parameters:
    *   `page` (integer, default: 1): The page number to retrieve.
    *   `pageSize` (integer, default: 10): The number of items per page.
    Response Structure for Paginated Lists:
    ```json
    {
      "data": [ /* array of items */ ],
      "pagination": {
        "currentPage": 1,
        "pageSize": 10,
        "totalItems": 100,
        "totalPages": 10
      }
    }
    ```

---

### 1. Account Management (FR-AM-001, FR-AM-002, FR-AM-003)

#### 1.1. User Registration
*   **Endpoint:** `POST /users/register`
*   **Description:** Allows a new user to register.
*   **Authentication:** None
*   **Request Body:**
    ```json
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "password": "securePassword123"
    }
    ```
*   **Successful Response (201 Created):**
    ```json
    {
      "userId": "uuid-user-1",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com"
    }
    ```
*   **Error Responses:** 400 Bad Request (e.g., validation errors, email already exists), 500 Internal Server Error.

#### 1.2. User Login
*   **Endpoint:** `POST /users/login`
*   **Description:** Authenticates a user and returns a JWT.
*   **Authentication:** None
*   **Request Body:**
    ```json
    {
      "email": "john.doe@example.com",
      "password": "securePassword123"
    }
    ```
*   **Successful Response (200 OK):**
    ```json
    {
      "token": "your.jwt.token",
      "user": {
        "userId": "uuid-user-1",
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "roles": ["creator"] // Example roles, could be "moderator"
      }
    }
    ```
*   **Error Responses:** 400 Bad Request (missing fields), 401 Unauthorized (invalid credentials), 500 Internal Server Error.

#### 1.3. Get User Profile
*   **Endpoint:** `GET /users/me`
*   **Description:** Retrieves the profile of the currently authenticated user.
*   **Authentication:** Required
*   **Successful Response (200 OK):**
    ```json
    {
      "userId": "uuid-user-1",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "roles": ["creator"]
    }
    ```
*   **Error Responses:** 401 Unauthorized, 500 Internal Server Error.

#### 1.4. Update User Profile
*   **Endpoint:** `PUT /users/me`
*   **Description:** Allows a logged-in user to edit their profile information.
*   **Authentication:** Required
*   **Request Body:** (Include only fields to be updated)
    ```json
    {
      "firstName": "Jonathan",
      "lastName": "Doer"
      // Email and password changes might need separate, more secure flows (e.g., email verification, current password confirmation) - TBD if needed for v1.
    }
    ```
*   **Successful Response (200 OK):**
    ```json
    {
      "userId": "uuid-user-1",
      "firstName": "Jonathan",
      "lastName": "Doer",
      "email": "john.doe@example.com",
      "roles": ["creator"]
    }
    ```
*   **Error Responses:** 400 Bad Request (validation errors), 401 Unauthorized, 500 Internal Server Error.

---

### 2. Video Catalog (FR-VC-001, FR-VC-002, FR-VC-003, FR-VC-004, FR-VC-005)

#### 2.1. Submit New Video
*   **Endpoint:** `POST /videos`
*   **Description:** Authenticated users (Creators) submit a new video via YouTube URL. Processing (thumbnail, embed, AI tag suggestions, embeddings) is asynchronous.
*   **Authentication:** Required
*   **Request Body:**
    ```json
    {
      "youTubeUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "title": "My Awesome Video", // Optional: User can provide, or backend can attempt to fetch from YouTube
      "description": "A description of my video.", // Optional
      "tags": ["tag1", "tag2"] // Optional: User-provided initial tags
    }
    ```
*   **Successful Response (202 Accepted):**
    ```json
    {
      "videoId": "temp-uuid-video-1", // Temporary or permanent ID, depending on backend strategy
      "status": "PROCESSING",
      "message": "Video submission accepted and is being processed.",
      "statusUrl": "/api/v1/videos/temp-uuid-video-1/status" // URL to check processing status
    }
    ```*   **Error Responses:** 400 Bad Request (invalid URL, missing fields), 401 Unauthorized, 403 Forbidden (user not allowed to create), 500 Internal Server Error.

#### 2.2. Get Video Processing Status
*   **Endpoint:** `GET /videos/{videoId}/status`
*   **Description:** Checks the processing status of a submitted video.
*   **Authentication:** Required (owner or moderator)
*   **Successful Response (200 OK):**
    ```json
    {
      "videoId": "uuid-video-1", // Now permanent ID if processing started
      "status": "COMPLETED", // or "PROCESSING", "FAILED"
      "details": { // Populated upon completion
        "title": "My Awesome Video",
        "description": "A description of my video.",
        "youTubeVideoId": "dQw4w9WgXcQ",
        "thumbnailUrl": "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        "embedHtml": "<iframe ...></iframe>",
        "suggestedTags": ["ai_tag1", "ai_tag2", "ai_tag3"],
        "durationSeconds": 212 // Example
      },
      "failureReason": null // or "Failed to fetch YouTube metadata." if status is FAILED
    }
    ```
*   **Error Responses:** 401 Unauthorized, 404 Not Found, 500 Internal Server Error.
*   **Note:** A webhook (internal or external to this API definition, as specified in your feedback) will be triggered by the backend processing service upon completion/failure.

#### 2.3. Update Video Details (e.g., after reviewing AI suggestions)
*   **Endpoint:** `PUT /videos/{videoId}`
*   **Description:** Allows the video uploader or a moderator to update video details (title, description, tags).
*   **Authentication:** Required (owner or moderator)
*   **Request Body:**
    ```json
    {
      "title": "My Updated Awesome Video",
      "description": "An updated description.",
      "tags": ["tag1", "updated_tag", "ai_tag1"] // User confirms/modifies tags
    }
    ```
*   **Successful Response (200 OK):** (Returns the full updated video object, similar to 2.4 Get Video Details)
*   **Error Responses:** 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error.

#### 2.4. Get Video Details
*   **Endpoint:** `GET /videos/{videoId}`
*   **Description:** Retrieves details for a specific video.
*   **Authentication:** Optional (some details might be restricted for non-auth users if needed, but not specified for v1)
*   **Successful Response (200 OK):**
    ```json
    {
      "videoId": "uuid-video-1",
      "title": "My Awesome Video",
      "description": "A description of my video.",
      "tags": ["tag1", "tag2"],
      "youTubeVideoId": "dQw4w9WgXcQ",
      "thumbnailUrl": "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
      "embedHtml": "<iframe ...></iframe>", // Or just the YouTube video ID for frontend embedding
      "uploader": {
        "userId": "uuid-user-1",
        "name": "John Doe" // Or just userId
      },
      "uploadedAt": "2025-05-10T10:00:00Z",
      "viewCount": 1234,
      "averageRating": 4.5,
      "userRating": 5 // Only if user is authenticated and has rated this video
    }
    ```
*   **Error Responses:** 404 Not Found, 500 Internal Server Error.

#### 2.5. List Latest Videos
*   **Endpoint:** `GET /videos/latest`
*   **Description:** Get a paginated list of the latest videos added to the platform.
*   **Authentication:** Optional
*   **Query Parameters:** `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of video summary objects.
    ```json
    // Structure as per "Pagination" common response, with "data" containing:
    [
      {
        "videoId": "uuid-video-1",
        "title": "My Awesome Video",
        "thumbnailUrl": "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        "uploader": { "userId": "uuid-user-1", "name": "John Doe" },
        "uploadedAt": "2025-05-10T10:00:00Z",
        "viewCount": 1234,
        "averageRating": 4.5
      }
      // ... more video summaries
    ]
    ```
*   **Error Responses:** 500 Internal Server Error.

#### 2.6. List Videos by Tag
*   **Endpoint:** `GET /videos/by-tag/{tagName}`
*   **Description:** Get a paginated list of videos associated with a specific tag.
*   **Authentication:** Optional
*   **Query Parameters:** `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of video summary objects (same format as 2.5).
*   **Error Responses:** 404 Not Found (if tag doesn't exist or has no videos), 500 Internal Server Error.

#### 2.7. List Videos by User
*   **Endpoint:** `GET /users/{userId}/videos`
*   **Description:** Get a paginated list of videos uploaded by a specific user.
*   **Authentication:** Optional
*   **Query Parameters:** `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of video summary objects (same format as 2.5).
*   **Error Responses:** 404 Not Found (if user doesn't exist or has no videos), 500 Internal Server Error.

---

### 3. Search (FR-SE-001, FR-SE-002)

#### 3.1. Search Videos
*   **Endpoint:** `GET /search/videos`
*   **Description:** Performs keyword-based search for videos. This will likely involve searching titles, descriptions, and tags. It may also use vector embeddings for semantic search.
*   **Authentication:** Optional
*   **Query Parameters:**
    *   `query` (string, required): The search term.
    *   `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of video summary objects (same format as 2.5).
*   **Error Responses:** 400 Bad Request (missing query), 500 Internal Server Error.

#### 3.2. Tag Autocompletion Suggestions
*   **Endpoint:** `GET /tags/suggest`
*   **Description:** Provides tag autocompletion suggestions based on existing tags in the system as the user types.
*   **Authentication:** Optional
*   **Query Parameters:**
    *   `query` (string, required): The partial tag string.
    *   `limit` (integer, optional, default: 10): Max number of suggestions.
*   **Successful Response (200 OK):**
    ```json
    {
      "suggestions": [
        "gaming",
        "gameplay",
        "gadgets"
      ]
    }
    ```
*   **Error Responses:** 400 Bad Request (missing query), 500 Internal Server Error.

---

### 4. Comments (FR-CM-001, FR-CM-002, FR-CM-003, FR-CM-004)

#### 4.1. Post Comment on Video
*   **Endpoint:** `POST /videos/{videoId}/comments`
*   **Description:** Authenticated users post a comment on a video. Sentiment is determined server-side upon posting.
*   **Authentication:** Required
*   **Request Body:**
    ```json
    {
      "commentText": "This is a great video!"
    }
    ```
*   **Successful Response (201 Created):**
    ```json
    {
      "commentId": "uuid-comment-1",
      "videoId": "uuid-video-1",
      "commenter": {
        "userId": "uuid-user-2",
        "name": "Jane Smith"
      },
      "commentText": "This is a great video!",
      "postedAt": "2025-05-10T11:00:00Z",
      "sentiment": "POSITIVE" // e.g., POSITIVE, NEUTRAL, NEGATIVE
    }
    ```
*   **Error Responses:** 400 Bad Request, 401 Unauthorized, 404 Not Found (video), 500 Internal Server Error.

#### 4.2. List Comments for Video
*   **Endpoint:** `GET /videos/{videoId}/comments`
*   **Description:** Get paginated comments for a specific video.
*   **Authentication:** Optional
*   **Query Parameters:** `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of comment objects (format similar to 4.1 response).
*   **Error Responses:** 404 Not Found (video), 500 Internal Server Error.

#### 4.3. List Comments by User
*   **Endpoint:** `GET /users/{userId}/comments`
*   **Description:** Get paginated comments made by a specific user.
*   **Authentication:** Optional
*   **Query Parameters:** `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of comment objects (format similar to 4.1 response, potentially with added `videoId` or video title context).
*   **Error Responses:** 404 Not Found (user), 500 Internal Server Error.

---

### 5. Ratings (FR-RA-001, FR-RA-002, FR-RA-003)

#### 5.1. Rate Video
*   **Endpoint:** `POST /videos/{videoId}/ratings`
*   **Description:** Authenticated users rate a video on a 1-5 star scale. If the user has already rated, this updates their rating.
*   **Authentication:** Required
*   **Request Body:**
    ```json
    {
      "rating": 5 // Integer between 1 and 5
    }
    ```
*   **Successful Response (200 OK or 201 Created):**
    ```json
    {
      "videoId": "uuid-video-1",
      "userId": "uuid-user-2",
      "rating": 5,
      "newAverageRating": 4.6 // Updated average for the video
    }
    ```
*   **Error Responses:** 400 Bad Request (invalid rating value), 401 Unauthorized, 404 Not Found (video), 500 Internal Server Error.

#### 5.2. Get User's Rating for a Video (Implicitly handled)
*   The `userRating` field in the `GET /videos/{videoId}` response (Section 2.4) covers FR-RA-003. No separate endpoint is needed if this is sufficient.

---

### 6. AI-Powered Recommendations (FR-RC-001, FR-RC-002)

#### 6.1. Get Related Videos
*   **Endpoint:** `GET /videos/{videoId}/related`
*   **Description:** Displays a list of "Related videos" on a video's watch page based on content similarity (e.g., vector embeddings).
*   **Authentication:** Optional
*   **Query Parameters:** `limit` (integer, optional, default: 5)
*   **Successful Response (200 OK):** List of video summary objects (same format as 2.5, but not paginated for a typical "related" sidebar).
    ```json
    {
      "relatedVideos": [
        // array of video summary objects
      ]
    }
    ```
*   **Error Responses:** 404 Not Found (video), 500 Internal Server Error.

#### 6.2. Get Personalized "For You" Recommendations
*   **Endpoint:** `GET /recommendations/foryou`
*   **Description:** Provides logged-in users a personalized "For You" list of recommended videos.
*   **Authentication:** Required
*   **Query Parameters:** `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of video summary objects (same format as 2.5).
*   **Error Responses:** 401 Unauthorized, 500 Internal Server Error.

---

### 7. Playback Statistics (FR-PS-001)

#### 7.1. Record Video View
*   **Endpoint:** `POST /videos/{videoId}/views`
*   **Description:** Records a view for the video. Typically called on video page load.
*   **Authentication:** Optional (though decisions on uniqueness of views might involve auth status or IP/fingerprinting for anonymous users)
*   **Request Body:** Empty
*   **Successful Response (204 No Content):**
*   **Error Responses:** 404 Not Found (video), 500 Internal Server Error.
*   **Note:** The `viewCount` is returned as part of `GET /videos/{videoId}`.

---

### 8. Content Moderation (FR-MO-001, FR-MO-002, FR-MO-003, FR-MO-004, FR-MO-005)

#### 8.1. Flag Content (Video or Comment)
*   **Endpoint:** `POST /flags`
*   **Description:** Authenticated users flag content they deem inappropriate.
*   **Authentication:** Required
*   **Request Body:**
    ```json
    {
      "contentType": "video", // or "comment"
      "contentId": "uuid-video-1", // or "uuid-comment-1"
      "reasonCode": "SPAM", // Predefined: e.g., SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, HATE_SPEECH, OTHER
      "reasonText": "This is spammy." // Required if reasonCode is OTHER, optional otherwise
    }
    ```
*   **Successful Response (201 Created):**
    ```json
    {
      "flagId": "uuid-flag-1",
      "contentType": "video",
      "contentId": "uuid-video-1",
      "userId": "uuid-user-flagger",
      "reasonCode": "SPAM",
      "reasonText": "This is spammy.",
      "status": "PENDING",
      "flaggedAt": "2025-05-11T12:00:00Z"
    }
    ```
*   **Error Responses:** 400 Bad Request (invalid contentType, missing fields), 401 Unauthorized, 404 Not Found (content to be flagged), 500 Internal Server Error.

#### 8.2. List Flagged Content
*   **Endpoint:** `GET /moderation/flags`
*   **Description:** Moderators view a list of all flagged content. Reason is masked by default.
*   **Authentication:** Required (Moderator role)
*   **Query Parameters:**
    *   `status` (string, optional, default: "PENDING"): Filter by status (e.g., PENDING, RESOLVED_REMOVED, RESOLVED_UNFLAGGED).
    *   `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of flagged content objects.
    ```json
    // Structure as per "Pagination" common response, with "data" containing:
    [
      {
        "flagId": "uuid-flag-1",
        "contentType": "video",
        "contentId": "uuid-video-1",
        "contentPreview": "Title of the video...", // or snippet of comment
        "flaggerUserId": "uuid-user-flagger",
        "flaggedAt": "2025-05-11T12:00:00Z",
        "reasonCode": "SPAM", // Reason code is visible
        // "reasonText" is NOT included here by default (masked)
        "status": "PENDING"
      }
      // ... more flagged items
    ]
    ```
*   **Error Responses:** 401 Unauthorized, 403 Forbidden, 500 Internal Server Error.

#### 8.3. Get Flagged Content Details (with option to unmask reason - future/DB dependent)
*   **Endpoint:** `GET /moderation/flags/{flagId}`
*   **Description:** Moderator views details of a specific flag.
    *   **V1 Behavior:** The `reasonText` will be included if the `reasonCode` was 'OTHER', but other free-form text associated with predefined reasons (if any) might still be masked as per FR-MO-003 initially. Since the "unmasking via DB" is deferred, for v1, we might show `reasonText` always if it exists, or only if `reasonCode` is 'OTHER'. Let's assume for now it's shown if it exists, as the explicit "unmask" action API is deferred.
*   **Authentication:** Required (Moderator role)
*   **Successful Response (200 OK):**
    ```json
    {
      "flagId": "uuid-flag-1",
      "contentType": "video",
      "contentId": "uuid-video-1",
      // Potentially more details about the content itself
      "videoDetails": { /* ... video summary ... */ }, // if contentType is video
      "commentDetails": { /* ... comment object ... */ }, // if contentType is comment
      "flaggerUserId": "uuid-user-flagger",
      "flaggedAt": "2025-05-11T12:00:00Z",
      "reasonCode": "OTHER",
      "reasonText": "The actual reason text provided by the user.", // Visible
      "status": "PENDING",
      "moderatorActions": [
        // { "action": "COMMENT_ADDED", "moderatorId": "mod-uuid", "notes": "Investigating", "timestamp": "..." }
      ]
    }
    ```
*   **Error Responses:** 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error.

#### 8.4. Action Flagged Content
*   **Endpoint:** `POST /moderation/flags/{flagId}/action`
*   **Description:** Moderators act on flagged content (e.g., unflag, remove content).
*   **Authentication:** Required (Moderator role)
*   **Request Body:**
    ```json
    {
      "action": "REMOVE_CONTENT", // or "UNFLAG_CONTENT"
      "moderatorNotes": "Content violates policy X. Soft deleted." // Optional notes
    }
    ```
*   **Successful Response (200 OK):**
    ```json
    {
      "flagId": "uuid-flag-1",
      "status": "RESOLVED_REMOVED", // or "RESOLVED_UNFLAGGED"
      "actionTaken": "REMOVE_CONTENT",
      "moderatorId": "uuid-moderator-1",
      "actionTimestamp": "2025-05-11T13:00:00Z"
    }
    ```
*   **Error Responses:** 400 Bad Request (invalid action), 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error.
*   **Note:** "REMOVE_CONTENT" will trigger a soft delete of the associated video or comment.

#### 8.5. Restore Soft-Deleted Content (Video/Comment)
*   **Endpoint (Example for Video):** `POST /moderation/videos/{videoId}/restore`
*   **Endpoint (Example for Comment):** `POST /moderation/comments/{commentId}/restore`
*   **Description:** Moderators restore content that was previously soft-deleted.
*   **Authentication:** Required (Moderator role)
*   **Request Body:**
    ```json
    {
      "moderatorNotes": "Restored after appeal. Mistaken flag." // Optional
    }
    ```
*   **Successful Response (200 OK):**
    ```json
    {
      "contentId": "uuid-video-1", // or "uuid-comment-1"
      "status": "ACTIVE", // Or whatever the normal status is
      "message": "Content successfully restored."
    }
    ```
*   **Error Responses:** 401 Unauthorized, 403 Forbidden, 404 Not Found (content might be hard-deleted or never existed), 500 Internal Server Error.

---

### 9. Moderator Management (Hierarchical Roles)

#### 9.1. List Users (for potential Moderator assignment)
*   **Endpoint:** `GET /moderation/users`
*   **Description:** Allows Moderators to list users to find candidates for Moderator promotion.
*   **Authentication:** Required (Moderator role)
*   **Query Parameters:** `search` (string, optional, to search by email/name), `page`, `pageSize`
*   **Successful Response (200 OK):** Paginated list of user objects.
    ```json
    // Structure as per "Pagination" common response, with "data" containing:
    [
      {
        "userId": "uuid-user-candidate-1",
        "firstName": "Jane",
        "lastName": "Candidate",
        "email": "jane.candidate@example.com",
        "currentRoles": ["creator"]
      }
      // ... more users
    ]
    ```
*   **Error Responses:** 401 Unauthorized, 403 Forbidden, 500 Internal Server Error.

#### 9.2. Assign Moderator Role
*   **Endpoint:** `POST /moderation/users/{userId}/assign-moderator`
*   **Description:** Allows an existing Moderator to assign the Moderator role to another user.
*   **Authentication:** Required (Moderator role)
*   **Request Body:** Empty
*   **Successful Response (200 OK):**
    ```json
    {
      "userId": "uuid-user-promoted",
      "message": "User successfully assigned Moderator role.",
      "newRoles": ["creator", "moderator"]
    }
    ```
*   **Error Responses:** 400 Bad Request (user already a moderator), 401 Unauthorized, 403 Forbidden, 404 Not Found (user), 500 Internal Server Error.

#### 9.3. Revoke Moderator Role
*   **Endpoint:** `POST /moderation/users/{userId}/revoke-moderator`
*   **Description:** Allows an existing Moderator to revoke the Moderator role from another user (cannot revoke from the super-user or self, TBD on exact rules).
*   **Authentication:** Required (Moderator role)
*   **Request Body:** Empty
*   **Successful Response (200 OK):**
    ```json
    {
      "userId": "uuid-user-demoted",
      "message": "Moderator role successfully revoked from user.",
      "newRoles": ["creator"]
    }
    ```
*   **Error Responses:** 400 Bad Request (user not a moderator), 401 Unauthorized, 403 Forbidden (e.g., trying to demote self or superuser), 404 Not Found (user), 500 Internal Server Error.

---

This API specification covers the functional requirements outlined, incorporates your feedback, and adheres to general best practices for REST API design. It should provide a solid foundation for the backend implementations in Java, Python, and NodeJS.

Let me know your thoughts or if any further adjustments are needed!