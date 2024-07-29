<h1> Score Evaluation Application </h1>
This Spring Boot application is designed to handle the evaluation of test scores. It includes two main features: accepting score sheets and retrieving evaluated scores with optional filters.

## Technologies Used
- **Spring Boot**: 2.7.18
- **Java**: 11
- **MySQL**: Database for storing scores and testee information
- **Liquibase**: For database migrations
- **Swagger UI**: For API documentation
- **CI/CD**: Auto builds with guthub actions and deployed to render server (free tier)
## Feature 1: Accept Score Sheets
<h3><b>Endpoint: POST /evaluation/sheets</b></h3>

**Description:** Accepts subjects' score sheets as input and persists the data.

**Scoring Rule:** 
- 1 point for each correct answer
- Deduct 0.25 points for each incorrect answer

**Sample Request Body:**
```json
[
  {
    "testeeId": "1",
    "subjects": [
      {
        "subject": "maths",
        "totalQuestions": 100,
        "correct": 90,
        "incorrect": 10
      },
      {
        "subject": "science",
        "totalQuestions": 100,
        "correct": 60,
        "incorrect": 40
      },
      {
        "subject": "general",
        "totalQuestions": 100,
        "correct": 75,
        "incorrect": 35
      }
    ]
  }
]
```
**For Valid Requests:**
- **Response: "Score Sheets Created"**
- **HTTP 202 status code**

**For InValid Requests:**
- **HTTP 400 status Bad Request with message**

**My Assumptions:**
- If empty or no request body sent, throws 404 bad request with **Score Sheets cannot be Null or Empty**
- A testee can be inserted without subjects, later for the same testeeId, subjects can be added and updated. For the same testeeId, allows to add the subjects. If exists, it updates the scores, if not adds the subjects with scores to that testeeId.
- If no testeeId passed, throws bad request with **TesteeId cannot be Null or Empty**
- Allowed subjects are maths, general, science. If other subjects are passed, throws bad request with **Invalid Subject with subject name**
- sum of incorrect and correct answers should not exceed total number of questions. If yes, throws **Total of correct and incorrect answers exceeds total questions for subject**

## Feature #2: Retrieve Evaluated Scores
<h3><b>Endpoint: GET /evaluation/scores</b></h3>
<b>Description:</b> Retrieves evaluated scores based on optional filters like List of testeeIds, list of allowed subjects, totalRange, averageRange, scoreRange. Based on the optional filters passed, fetched the data and return the response body with 200 OK</b>

**Sample Request:**
```text
http://localhost:8080/evaluation/scores?testeeIds=1, 2, 3, 4, 5&subjects =general, science&totalRange=1-50&averageRange=&scoreRange=
```
**Response:**
```json
[
    {
        "testeeId": "5",
        "scores": {
            "general": 25.0,
            "average": 7.0,
            "total": 21.0,
            "science": -4.0
        }
    },
    {
        "testeeId": "2",
        "scores": {
            "general": 2.5,
            "average": 0.33,
            "total": 1.0,
            "science": -1.5
        }
    }
]
```
Response is ordered by totalScore and then testeeId.

**My Assumptions:**
- If not allowed subjects are passed, then throws bad request with **Invalid Subject with subject name**
- If wrong format of range is provided, then throws bad request with **Invalid range format. Expected format: min-max**

To pull the docker image: 
```text
  docker pull surarohith/score_evaluation
```

  
