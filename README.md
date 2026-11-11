# AI Questionnaire Engine

An AI-powered questionnaire engine developed as a proof of concept (PoC) for a legal-assistance system, using Spring Boot and Spring AI to interpret natural-language responses and manage configurable question flows.

The intended legal-assistance system aims to help people with limited financial resources access legal guidance. This PoC focuses on questionnaire intake and answer classification, producing structured intake data for the broader legal assistance workflow rather than generating legal advice. Sessions are stored in memory for the prototype.

## Problem and approach

People describe their circumstances in their own words rather than database labels. The engine accepts an option key, an option's displayed text, or a free-text reply. Exact matches avoid an AI request. Other replies are classified against only the current question's allowed options. The shared prompt asks the model to interpret English, Bengali, and Banglish (Bengali written in Latin letters).

The application loads [selp.json](src/main/resources/selp.json) by default. This file defines the questionnaire questions, allowed options, and next-question links. It contains 20 Bengali questions about sexual-harassment-related legal assistance. Its content is questionnaire data, not a validated legal decision procedure.

## Architecture

```mermaid
flowchart TD
    Client[Web UI or integrating application] --> Controller[REST controller]
    Controller --> Session[SessionService: in-memory state]
    Session --> Resolver[AIService]
    Resolver --> Exact{Exact key or context match?}
    Exact -->|Yes| Option[Resolved option]
    Exact -->|No| Provider[AIProviderFactory: Gemini or OpenRouter]
    Provider --> Parser[ClassificationResponseParser: parse JSON]
    Parser --> Check{AIService: valid result, confidence and allowed key?}
    Check -->|Yes| Option
    Check -->|No| Unresolved[HTTP 422: keep current question]
    Option --> Flow[QuestionService: follow nextQuestionId]
    Data[selp.json: questionnaire definition] --> Flow
    Flow --> Session
    Client --> Voice[POST /transcribe: Gemini audio transcription]
    Voice --> Transcript[Return transcript for review and submission]
```

- `QuestionnaireController` exposes the HTTP API and maps known failures to status codes.
- `QuestionnaireConfig` loads the resource selected by `app.questionnaire.resource`; `QuestionService` checks the start question and referenced next-question IDs at startup.
- `SessionService` stores a UUID session, current question, collected option keys, and completion flag. Answers are saved under each question's `field`.
- `AIService` first matches an option key or context, ignoring letter case and surrounding whitespace. Otherwise it sends the current question, allowed keys and contexts, collected answers, and latest reply to the selected provider. It sends no full conversation history.
- Both providers share a prompt and response parser. Markdown fences are tolerated; blank or malformed JSON becomes an unresolved classification. A result is accepted only if `valid` is true, an option key is supplied, confidence is at least `0.5`, and the key belongs to the current question. The confidence is model-reported, not a calibrated probability.
- Voice transcription always uses Gemini, independently of the classification provider. Transcription returns text; the caller submits that text through `/answer` separately.

### Conditional branching

The default questionnaire is [src/main/resources/selp.json](src/main/resources/selp.json), selected by `app.questionnaire.resource=classpath:selp.json` in the application configuration. It is loaded once at startup; restart the application after changing the file.

- `startQuestionId` identifies the first question.
- Each question has an `id`, question text, a `field` used in the collected answers, a `type`, and `options`. All bundled questions use `single_select`.
- Each option has a `key` stored as the answer, a `context` describing the option, and a `nextQuestionId` controlling the next step.

The backend follows the selected option's next ID; a null ID completes the session. An AI model selects an allowed option and never chooses the next question directly. API responses include option keys and contexts; next-question links remain on the backend.

The bundled questionnaire currently follows questions 1 through 20 regardless of which option is selected. The engine supports divergent paths by setting different next IDs in JSON; the bundled data does not demonstrate such divergence. Startup validation checks references, but does not detect cycles or unreachable questions.

## Run locally

Prerequisites: JDK **25**, network access for Gradle dependencies and provider calls, and a key for the chosen classification provider. The repository pins Spring Boot 4.1.0, Spring AI 2.0.0, and Gradle 9.5.1. Use the included wrapper; no separate Gradle installation is needed.

From PowerShell in this directory:

```powershell
$env:OPENROUTER_API_KEY = '<your-key>'
$env:APP_AI_PROVIDER = 'openrouter'
.\gradlew.bat bootRun
```

Open <http://localhost:8080> for the bundled UI. The default classifier is OpenRouter with `openai/gpt-4o-mini`. To use Gemini instead:

```powershell
$env:GEMINI_API_KEY = '<your-key>'
$env:APP_AI_PROVIDER = 'gemini'
.\gradlew.bat bootRun
```

On macOS/Linux, use `./gradlew bootRun` and export the equivalent environment variables. Exact option matches need no model call. Free-text classification needs the active provider's real key; audio always needs a real Gemini key. The `abc` and `xyz` fallback values in configuration are placeholders.

Configuration lives in [application.properties](src/main/resources/application.properties). Useful environment overrides:

| Variable | Purpose / repository default |
| --- | --- |
| `APP_AI_PROVIDER` | `openrouter` or `gemini`; default `openrouter` |
| `OPENROUTER_API_KEY` | OpenRouter credentials |
| `GEMINI_API_KEY` | Gemini classification and/or transcription credentials |
| `SPRING_AI_OPENAI_CHAT_MODEL` | OpenRouter model; `openai/gpt-4o-mini` |
| `SPRING_AI_GOOGLE_GENAI_CHAT_MODEL` | Gemini classifier; `gemini-2.5-flash` |
| `APP_VOICE_TRANSCRIPTION_MODEL` | Gemini audio model; `gemini-3.1-pro-preview` |
| `APP_QUESTIONNAIRE_RESOURCE` | `classpath:selp.json`; can use `file:/absolute/path/custom.json` |

Both provider clients are configured at startup; the factory selects which one classifies answers. Model names above are repository defaults, not guarantees of current provider availability. Classification temperature is configured as zero, which does not guarantee identical repeated outputs.

## API

| Method | Path | Request | Response |
| --- | --- | --- | --- |
| POST | `/session` | No body | `sessionId`, initial `question` |
| POST | `/answer` | JSON `sessionId`, `answer` | `completed`, `answers`, `nextQuestion` |
| GET | `/session/{sessionId}` | Session ID in path | `sessionId`, `completed`, `answers`, `currentQuestion` |
| POST | `/transcribe` | Multipart `audio`, optional `sessionId` for question context | `transcript` |

Example using PowerShell:

```powershell
$session = Invoke-RestMethod -Method Post -Uri http://localhost:8080/session
$session.question.options | Format-Table context, key
# Example: select the first option returned for the current question.
$body = @{ sessionId = $session.sessionId; answer = $session.question.options[0].key } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/answer -ContentType 'application/json' -Body $body
Invoke-RestMethod -Uri "http://localhost:8080/session/$($session.sessionId)"
```

With the default `selp.json`, the example selects the first question's sexual-harassment option without an AI call. For each subsequent answer, use the latest `nextQuestion.options` to select an option key, or submit a natural-language reply for AI classification. The `answers` object stores option keys under questionnaire field names, such as `legalProblem`. Completion returns `completed: true` and `nextQuestion: null`; retrieving a completed session returns `currentQuestion: null`.

Known controller error responses have a `message` field: blank required answer/session ID → **400**; unknown session → **404**; answer after completion → **409**; unresolved/low-confidence/disallowed classification → **422**; provider call failure → **502**. Rejected classifications and provider failures do not advance the session. Framework-level errors may have a different response shape.

`/transcribe` requires a non-empty upload whose content type starts with `audio/`. Multipart limits are 15 MB. Content-type checking alone does not validate audio bytes or model format support. Transcription does not save an answer or advance the session. In the bundled UI, the transcript is placed in the text box so the user can review it before submitting.
