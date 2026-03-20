# Palo Alto Submission

Candidate Name: `Padarthi Karthik`

Scenario Chosen: `Skill Bridge career transition platform`

Estimated Time Spent: `6 hours`

Video Submission: `https://youtu.be/oAz8rhgSVf4`

## Quick Start

### Prerequisites

- Node.js 18+
- npm 9+
- Java 17+ for the Spring Boot backend
- Backend available on `http://localhost:8081`
- Frontend available on `http://localhost:3000`

### Run Commands

Backend:

```bash
cd C:\Documents\PaloAlto_submission\Backend\BackEnd
./mvnw spring-boot:run
```

Frontend:

```bash
cd C:\Documents\PaloAlto_submission\Frontend\skill-bridge-frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

### Test Commands

Frontend:

```bash
cd C:\Documents\PaloAlto_submission\Frontend\skill-bridge-frontend
npm run lint
npm run build
```

Backend:

```bash
cd C:\Documents\PaloAlto_submission\Backend\BackEnd
./mvnw test
```

## Project Summary

This submission implements a full frontend for the Skill Bridge platform against the provided backend APIs. It includes:

- email/password registration and login
- JWT-protected routing and authenticated API calls
- dashboard with role list and metrics snapshot
- resume upload and analysis flow
- manual text-based gap analysis flow
- roadmap generation flow
- job intelligence page
- interview question generation and evaluation flows
- history and metrics pages

The frontend is configured to call the backend at `http://localhost:8081` through `.env.local`.

## Repository Structure

- `Backend/BackEnd` - Spring Boot backend
- `Frontend/skill-bridge-frontend` - Next.js frontend

## AI Disclosure

Did you use an AI assistant (Copilot, ChatGPT, etc.)? `Yes`

How did you verify the suggestions?

- compared frontend request/response shapes against the backend Spring controllers and DTOs
- ran `npm run lint`
- ran `npm run build`
- manually adjusted API field names and flow logic after checking backend contracts

Give one example of a suggestion you rejected or changed:

- an early version of the frontend assumed auth returned `jwt` and that roadmap/interview requests used flatter payloads
- after reviewing the backend controllers and DTOs, this was changed to match the actual contract: auth returns `token`, PDF endpoints use `resumeFile`, text fields use `githubLikeText`, and roadmap/question generation require a nested `profile` object                        
- I initially used resume as the multipart file field for PDF flows, but updated it to resumeFile after checking the controller method signatures.         
- I first sent githubText in analysis and roadmap requests, then changed it to githubLikeText to match the backend request model.                          
- The first version of roadmap and interview question generation used flatter payloads; I changed both to send a nested `profile` object after verifying the backend DTOs.                                                                                                                                            
- I originally sent interview evaluation data as separate questions and answers arrays, then changed it to the backend’s `expected answers[]` object structure with question metadata included.                                                                                                               
- The initial metrics mapping used fields like `avgCoverageScore`, `avgInterviewScore`, and `interviewCount` after checking the backend response class, I updated the frontend to use `averageCoverageScore`, `averageInterviewScore`, `interviewSetCount`, and `lastActivityAt`.

## Tradeoffs & Prioritization

What did you cut to stay within the 4–6 hour limit?

- no advanced charting library; metrics uses lightweight frontend bar rendering
- no polished file export format beyond JSON roadmap export
- limited mobile navigation polish compared to desktop
- no end-to-end frontend test suite
- no optimistic updates or complex caching layer
- Did not implement Asyncronous Handling

What would you build next if you had more time?

- stronger form abstractions and schema validation
- richer charting and analytics visualizations
- better roadmap export options such as PDF/print layouts
- improved history detail views with reuse across all flows
- frontend integration tests and API mocking
- backend CORS/security cleanup and deployment-ready environment setup
- Implement better security feautures
- Implement Ayncrnous handling and caching 

Known limitations:

- the backend must allow CORS from `http://localhost:3000`, especially for `/api/auth/**`
- the roadmap PDF flow requires re-uploading the PDF on the roadmap page because the frontend does not persist file binaries in storage
- some history response shapes are rendered generically because backend models are stored directly
- charts are intentionally simple and do not use a dedicated visualization library
- Metrics visualization uses simple frontend-rendered bars instead of a dedicated charting library.                                                        
- Error handling is implemented for common API failures, but there is no centralized toast/notification system yet.                                        
- Form validation is present, but it is lightweight and not backed by a shared schema validation library.                                                  
- There is no automated end-to-end test coverage for the full user flows yet.                                                                              
- The UI is responsive, but mobile navigation and smaller-screen polish could be improved further.                                                         
- Roadmap export is currently implemented as JSON download only, not as PDF or printable report output.                                                    
- The frontend assumes the current backend contract; if backend DTOs or field names change, the API layer will need to be updated accordingly.
