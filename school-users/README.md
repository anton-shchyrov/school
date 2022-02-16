# school-users
Common services

The service configuration is in the file `application.yaml`

All methods require HTTP Basic authentication

## Users management
Implements in `org.example.school.users.services.UserService`

Required role: `admin`

Endpoints:

+ /users/add - Provides for adding a new user
  + Method: POST
  + Params: none
  + Body: JSON object with fields
    + `login`: required
    + `password`: required
    + `name`: required. Human-readable username.
    + `role`: required. Role of user. Can take one of the values
      + admin
      + teacher
      + student
      + staff
  + Returns:
    + `{"status": "OK"}` if the addition was successful
    + `{"error": "Error message"}` and HTTP status not equal 200 if an error occurs

+ /users/update - Provides for user update
  + Method: POST
  + Params: none
  + Body: JSON object with fields
    + `login`: required
    + `password`
    + `name`

    At least one field `password` or `name` must be present

  + Returns:
    + `{"status": "OK"}` if the update was successful
    + `{"status": "Not found"}` if the user with the login `login` was not found
    + `{"error": "Error message"}` and HTTP status not equal 200 if an error occurs

+ /users/delete - Provides for user delete
  + Method: POST
  + Params: none
  + Body: JSON object with field
    + `login`: required

    At least one field `password` or `name` must be present

  + Returns:
    + `{"status": "OK"}` if the uses was successful deleted
    + `{"status": "Not found"}` if the user with the login `login` was not found
    + `{"error": "Error message"}` and HTTP status not equal 200 if an error occurs

## Teacher functions
Implements in `org.example.school.users.services.TeacherService`

Required role: `teacher`

Endpoints:

+ /teacher/track - Returns students statistics
  + Method: GET
  + Params
    + `class`: Required. The name of the class for which information is collected
    + `student`. Optional. Student login. If you are interested in statistics for a particular student
    + `dateFrom`. Optional. Starting date from which information is collected. Format `yyyy-mm-dd`
    + `dateTo`. Optional. End date until which information is collected. Format `yyyy-mm-dd`
  + Returns: if successful array of objects
    ```json
    {
      "date": "yyyy-mm-dd",
      "grades": {
        "student name": [
          {"grade": grade_value, "gradeType": "grade_type"}
        ]
      }
    }
    ```
    + `grade_value` can be grater then 0 or 0 if the student was absent
    + `grade_type` can be one of values `current`, `exam`

    if an error occurs returns object `{"error": "Error message"}` and HTTP status not equal 200
+ teacher/add-grades
  + Method: POST
  + Params: none
  + Body: JSON object with field
    + `class`. Required
    + `subject`. Required
    + `date`. Required
    + `gradeType`. Optional. If omitted, the value is taken `current`
    + `grades`. Required. Array of objects `{"student": "stundent login", "grade": grade_value}`
  + Returns: if successful array of objects
    ```json
    {
      "student": "student login",
      "status": "status string"
    }
    ```
    `status string` can be `OK` value if adding a grade for a student was successful and error message otherwise.

    if an error occurs returns object `{"error": "Error message"}` and HTTP status not equal 200

## Student functions
Implements in `org.example.school.users.services.StudentService`

Required role: `student`

Endpoints:

+ /student/track - Returns statistics for logged student
  + Method: GET
  + Params
    + `subject`: Optional. If you are interested in statistics for a particular subject
    + `dateFrom`. Optional. Starting date from which information is collected. Format `yyyy-mm-dd`
    + `dateTo`. Optional. End date until which information is collected. Format `yyyy-mm-dd`
  + Returns: if successful array of objects
    ```json
    {
      "subject": "subject name",
      "teacher": "teacher name",
      "average": average_value,
      "grades": [
        "student name": [
          {"grade": grade_value, "gradeType": "grade_type", "date": "yyyy-mm-dd"}
          {"grade": grade_value, "gradeType": "grade_type", "date": "yyyy-mm-dd"}
          {"grade": grade_value, "gradeType": "grade_type", "date": "yyyy-mm-dd"}
        ]
      ]
    }
    ```
    if an error occurs returns object `{"error": "Error message"}` and HTTP status not equal 200
