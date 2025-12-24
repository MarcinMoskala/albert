GET /course
response: CourseApi
endpoint for all users to get a list of all courses, with their lessons and steps. 

PUT /synchronize 
body: UserCourseProgressApi
response: UserCourseProgressApi
endpoint for logged-in users to synchronize their progress with the server. Merges progress from users and server, resolving conflicts by taking the most recent update for each step.

POST /login
body: LoginRequest
response: LoginResponse