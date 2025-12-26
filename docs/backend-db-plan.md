1. List of tables and their properties

- `users` - user profile and metadata.
    - `userId`: String (Unique, from KMPAuth)
    - `email`: String (Unique)
    - `displayName`: String
    - `createdAt`: Date
    - `lastLoginAt`: Date

- `user_lesson_progress` - individual user's progress:
    - `userId`: String (immutable, required on every query)
    - `lessonId`: String (associated, with lesson ID)
    - `status`: "not_started"|"started"|"completed"
    - `createdAt`: Date
    - `updatedAt`: Date
Combination of `userId` and `lessonId` is unique and serves as a key.

- `user_progress` - individual user's progress:
    - `userId`: String (immutable, required on every query)
    - `stepId`: String (associated, with step ID)
    - `status`: "repeating"|"completed"
    - `createdAt`: Date
    - `updatedAt`: Date
    - `reviewAt`: Date | NULL (next due; today for same-day retries)
    - `lastIntervalDays`: Int | NULL (most recent interval length)
Combination of `userId` and `stepId` is unique and serves as a key. 

2. YAML files schema

- `courses.yaml` - a list of courses with their lessons and steps. Fields:
    - `courses`: Array of objects with below properties: 
        - `courseId`: String (associated, with step ID)
        - `title`: String
        - `lessons`: Array of objects with below properties: 
            - `lessonId`: String (associated, with step ID)
            - `name`: String
            - `steps`: Array of objects with below properties: 
                - `stepId`: String (associated, with step ID)
                - `type`: "single-answer"|"multiple-answer"|"exact-text"
                - `question`: String
                - `explanation`: String
                - `repetable`: Boolean
                (for `type = "single-answer"`)
                - `answers`: Array of String
                - `correct`: String
                (for `type = "multiple-answer"`)
                - `answers`: Array of String
                - `correct`: Array of String
                (for `type = "exact-text"`)
                - `correct`: Array of String

3. Indexes

- `users` collection:
    - `{ "userId": 1 }` (Unique): Identification from KMPAuth.
    - `{ "email": 1 }` (Unique): For lookup by email.

- `user_progress` collection:
    - `{ "userId": 1 }` (Unique): Primary access pattern for fetching all user progress.
    - `{ "courses.lessons.steps.reviewAt": 1 }`: Multikey index used to identify items due for review across all users (e.g., for notifications).