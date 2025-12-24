# Product Requirements Document (PRD) - Albert

## 1. Product Overview

Albert is a learning application designed to help self-learners and students master technology-related topics. The application combines bite-sized lessons with interactive questions and immediate feedback, utilizing a spaced repetition system (SRS) to ensure long-term knowledge retention.

## 2. User Problem

Self-learners often face several challenges when studying complex technical topics:
- Difficulty in retaining information over time without structured review.
- Overwhelm from large volumes of content without clear, manageable milestones.
- Lack of immediate feedback on their understanding of the material.
- Inconsistent learning experiences across different devices.

Albert addresses these issues by breaking down content into Course, Lesson, and LessonStep hierarchies, providing a clear path of progression and ensuring that key concepts are reviewed at optimal intervals.

## 3. Functional Requirements

### 3.1 Usage without logging in

- Users can browse courses and lessons without logging in.
- Course and lesson metadata is publicly available.
- Users can start lessons and track their progress without an account. 

### 3.2 Content Structure and Navigation

- The hierarchy of content is: Course -> Lesson -> Lesson Step.
- The main screen must display this hierarchy, allowing users to browse courses and lessons.
- Lessons must show their current state: Locked, Available, or Completed.
- Lessons must display review counts: To review (items seen, but scheduled for spaced-repetition review) and New (new items to learn).

### 3.3 Learning Experience

- Lesson elements are presented one at a time in a sequential flow.
- Supported element types:
  - Text: Informational content that requires a manual complete action.
  - Single-choice question: One correct answer from a list.
  - Multiple-choice question: One or more correct answers from a list.
  - Exact word: Free-text answer that must match a predefined set of correct answers.
- Each question must provide immediate feedback (correct/incorrect) and an explanation of the answer.

### 3.4 Progression and Unlocking

- Lessons unlock progressively: a lesson becomes Available only after the previous lesson in the course is marked as Completed.
- A lesson is marked as Completed when the learner has reached and completed the final element in its sequence for the first time.
- Note: A lesson can be Completed even if some repeatable elements within it are still due for review later.

### 3.5 Spaced Repetition System (SRS)

- Elements can be defined as one-time or repeatable.
- Repeatable elements follow a doubling interval progression for correct answers: 1 day, 2 days, 4 days, 8 days, and so on.
- Incorrect answers for repeatable elements trigger a same-day retry.
- Same-day retry constraint: An incorrectly answered element must not reappear until at least 5 other elements have been shown, unless fewer than 5 elements remain in the current session (in which case it appears at the end).
- After an element is eventually answered correctly on the same day, its SRS progression continues or restarts from the first interval (1 day).

### 3.6 Progress Storage

- The system must store the next due timestamp for every repeatable element.
- The system must maintain a full answer history for each element, including timestamps and outcomes (correct/incorrect).
- Content identifiers (Course ID, Lesson ID, Element ID) must be stable and immutable to preserve history integrity.

### 3.7 Authentication and User Management

- The application must support secure user authentication using KMPAuth.
- Logging in allows progress synchronization. 
- Users must remain logged in across sessions until they explicitly log out.

## 4. Product Boundaries

- No in-app content creation: Content is predefined on the backend (e.g., via YAML files).
- No administrative or content management UI in frontend.
- No statistics or analytics dashboards for users in the MVP.
- No support for video elements or numeric-specific input validation (beyond exact word matching).
- Limited screen set: Login, Course/Lesson List, and Lesson/Session Flow only.

## 5. User Stories

### US-001: First view

- ID: US-001
- Title: Opening application
- Description: When I open the application, I should see a list of courses and lessons.
- Acceptance Criteria:
  - The main screen displays courses as top-level items.
  - Expanding or selecting a course reveals its lessons.
  - Each lesson clearly indicates its status: Locked (with a lock icon or dimmed), Available, or Completed.
  - Pending review counts (Due now) and upcoming reviews (Scheduled) are visible for each lesson.

### US-002: View Course and Lesson Hierarchy

- ID: US-002
- Title: Navigation through Course Structure
- Description: As a learner, I want to see the list of courses and lessons so that I can understand the curriculum and my progress.
- Acceptance Criteria:
  - The main screen displays courses as top-level items.
  - Expanding or selecting a course reveals its lessons.
  - Each lesson clearly indicates its status: Locked (with a lock icon or dimmed), Available, or Completed.
  - Pending review counts (Due now) and upcoming reviews (Scheduled) are visible for each lesson.

### US-003: Lesson Progression and Unlocking
- ID: US-003
- Title: Automatic Unlocking of Sequential Lessons
- Description: As a learner, when I finish the last item on a lesson, I automatically start answering questions from the next lesson in the sequence. When I go back to main screen, I can see that the lesson I completed is in Completed state, and the new one is now Available.
- Acceptance Criteria:
  - A lesson is Locked if the previous lesson in the course sequence is not Completed.
  - Once the final element of a lesson is completed, the next lesson's status changes from Locked to Available.
  - The UI updates immediately to reflect the new state of the lessons.

### US-004: Interactive Lesson Session
- ID: US-004
- Title: Single Element Learning Flow
- Description: As a learner, I want to interact with lesson elements one by one so that I can focus on small pieces of information.
- Acceptance Criteria:
  - When starting a lesson, the app presents the first pending or new element.
  - Only one element (text or question) is visible on the screen at a time.
  - User can move to the next element only after completing the current one (marking text as complete or answering a question correctly).

### US-005: Text Content Completion
- ID: US-005
- Title: Reading and Marking Text Elements
- Description: As a learner, I want to read text-based information and manually indicate when I have finished reading.
- Acceptance Criteria:
  - Text elements display the content clearly.
  - A Complete button is present at the end of the text content.
  - Tapping the Complete button records the completion and triggers the transition to the next element.

### US-006: Answering Single-Choice Questions
- ID: US-006
- Title: Single-Choice Question Interaction
- Description: As a learner, I want to answer questions with one correct option to test my knowledge.
- Acceptance Criteria:
  - A question is displayed with multiple selectable options.
  - User can select exactly one option and submit the answer.
  - Immediate feedback is shown: Green for correct, Red for incorrect.
  - An explanation for the correct answer is displayed after submission.
  - Correct answers allow the user to advance to the next element.

### US-007: Answering Multiple-Choice Questions
- ID: US-007
- Title: Multiple-Choice Question Interaction
- Description: As a learner, I want to answer questions that may have multiple correct answers.
- Acceptance Criteria:
  - A question is displayed with several options and checkboxes.
  - User can select one or more options and submit.
  - Feedback indicates if the entire selection matches the correct set of answers.
  - An explanation is provided regardless of the outcome.
  - Correct answers allow progression.

### US-008: Answering Exact Word Questions
- ID: US-008
- Title: Exact Word Matching Interaction
- Description: As a learner, I want to type in a specific word or phrase as an answer.
- Acceptance Criteria:
  - A text input field is provided for the answer.
  - The system compares the input against a list of acceptable correct answers.
  - Validation ignores leading and trailing whitespace.
  - Feedback and explanation are shown upon submission.

### US-009: Spaced Repetition Scheduling
- ID: US-009
- Title: Adaptive Spaced Repetition for Correct Answers
- Description: As a learner, I want the system to remember my correct answers and schedule reviews at increasing intervals.
- Acceptance Criteria:
  - When a repeatable element is answered correctly, the system calculates the next due date.
  - The progression follows the doubling pattern: 1, 2, 4, 8, ... days after the current date.
  - The new due date is saved to the user's progress history online.

### US-010: Same-Day Review for Mistakes
- ID: US-010
- Title: Retry Logic for Incorrect Answers
- Description: As a learner, I want to see questions I got wrong again later in the same session.
- Acceptance Criteria:
  - If a question is answered incorrectly, it is marked for same-day retry.
  - The element is placed back into the session queue.
  - The element must not appear until at least 5 other distinct elements have been shown.
  - If fewer than 5 elements are left in the session, the retry appears as the last item(s) of the session.
  - The element continues to reappear until answered correctly.

### US-011: Data Persistence and Synchronization
- ID: US-011
- Title: Reliable Progress Saving
- Description: As a learner, I want my progress to be saved automatically so that I don't lose my work if I switch devices or close the app.
- Acceptance Criteria:
  - Every interaction (lesson completion, question answer) is sent to the backend.
  - The system stores both the aggregate state (next due date) and the granular history (list of all attempts).
  - When logging in on a new device, the app fetches the latest progress state.

### US-012: Lesson Completion Definition
- ID: US-012
- Title: Reaching Lesson Completion Status
- Description: As a learner, I want to know when I have finished a lesson's primary content.
- Acceptance Criteria:
  - A lesson status changes to Completed as soon as the last element in the lesson's sequence is viewed/completed for the first time.
  - The completion status triggers the unlocking of the next lesson (if any).
  - Any remaining review items (SRS-based) do not prevent the lesson from being marked as Completed.

### US-013: User Authentication

- ID: US-013
- Title: Secure Login via KMPAuth
- Description: As a learner, I want to log in using a supported provider so that my progress is securely saved and synced.
- Acceptance Criteria:
  - User can see a login screen upon opening the app for the first time.
  - User can successfully authenticate using at least one provider supported by KMPAuth.
  - After login, the user is directed to the main course catalog.
  - User progress is automatically loaded from and saved to the online account.

## 6. Success Metrics

The success of the Albert MVP will be measured by user engagement and retention:

### 6.1 Primary Success Criterion: 5-Day Retention
- Target: More than 50% of new users should remain active after 5 days.
- Cohort Definition: New users who sign up and complete their first lesson.
- Retention Definition: A user is considered retained if they complete at least one learning session or answer at least 5 questions on the 5th day after their initial signup.

### 6.2 Key Performance Indicators (KPIs)
- Session Completion Rate: Percentage of started lessons that are successfully finished.
- Average Daily Active Users (DAU): Number of unique users engaging with learning content daily.
- Correct Answer Ratio: Percentage of first-attempt correct answers across all users.
- Review Adherence: Percentage of items reviewed on or before their due date.

### 6.3 Measurement Requirements
- The application must instrument the following events:
  - login_success
  - course_opened
  - lesson_started
  - element_presented
  - answer_submitted (with correct/incorrect flag)
  - element_completed
  - session_end
