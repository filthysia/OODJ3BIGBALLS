# OODJ3BIGBALLS — Hospital Management System

## 1.0 Coursework Title

Hospital Management System (HMS)

## 2.0 The Coursework Overview

Effective communication, accurate data logging, and seamless coordination are
critical components of patient care and hospital administration. In many
healthcare facilities, managing patient records, tracking doctor assignments,
processing billing or medical grades/status, and collecting patient feedback
can be highly fragmented. Often, patients leave a hospital with little more
than a prescription, lacking a transparent channel to review their treatment
summaries or provide feedback on their care.

As such, you are commissioned to develop a Hospital Management System (HMS)
that supports healthcare professionals and administrative staff in managing
daily clinical operations at APU Medical Centre. This system will track
medical assessments, manage patient appointments, classify health
metrics/billing based on pre-defined criteria, and produce analytical
hospital reports.

There are 4 types of end users interacting with the HMS:

1. Administrative Staff
2. Medical Managers
3. Doctors
4. Patients

The application should have the following features:

- Login access
- User registration
- Medical grading & billing system
- Create new wards/clinics (Classes equivalent)
- Create new medical departments/specialties (Modules equivalent)
- Design medical assessment/check-up types
- Key in medical assessment & lab results
- Provide clinical feedback & prescriptions
- Analytical reports

In addition, a supporting document is needed to reflect the design of the
implementation codes and the implementation details that utilize
Object-Oriented Programming (OOP) concepts.

## 3.0 Objectives of This Coursework

Develop the practical ability to describe, justify, and implement an
object-oriented system.

## 4.0 Type

Group Assignment (minimum 3 and maximum 4 students)

## 5.0 Coursework Description

As an Object-Oriented Programming student, you are required to identify the
relationships among the healthcare entities (using OOP concepts like
inheritance, encapsulation, polymorphism, and abstraction) and develop a
user-friendly application for different types of users by fulfilling the
basic features listed in Section 2.0.

The application should be designed with four types of access rights.
Table 1.0 shows the functionalities that can be accessed by each user role
upon login.

**Table 1.0**

| Roles | Functionalities |
|---|---|
| Admin Staff | Create/Read/Update/Delete end users. |
| | Assign doctors to their respective Medical Managers. |
| | Manage and allocate physical hospital assets such as consultation rooms, inpatient wards, labs and X-rays or imaging rooms. |
| | Configure base consultation rates and accepted insurance networks. |
| Medical Managers | Edit personal / individual profile. |
| | Create or update specialized clinical departments (e.g., Cardiology). |
| | Design and modify operational shift rosters for doctors. |
| | View reports on hospital metrics and revenue summaries. |
| Doctors | Edit personal / individual profile. |
| | Log patient vital signs and write consultation notes. |
| | Issue digital medication prescriptions to patient's record. |
| | Issue requests to admin for lab test, X-rays or specialized imaging. |
| Patients | Edit personal / individual profile. |
| | Browse available slots of doctors' consultations and book/reschedule/cancel booking. |
| | View personal medical history and prescriptions. |
| | Submit ratings and comments to doctors and clinic visits. |

## 6.0 General Requirements

- The system submitted should compile and be executed without errors.
- User input validation should be done to avoid logical error.
- The system must run continuously.
- The implementation code must highlight the use of Object-oriented
  programming concepts.
- Students should use text files for storing and retrieving data required
  for the system.
- Database tools like Access, Oracle, SQL Server, etc. are not allowed.
- Graphical user interface must be included by using libraries in
  `java.awt` and `java.swing` packages.

**GenAI Declaration requirement**

- All use of GenAI tools must be clearly declared and properly acknowledged
  in the submission, and students are responsible for verifying the
  correctness and appropriateness of any AI-assisted output.

**Academic Integrity**

- All work submitted must be the student's own. Any undeclared use of
  third-party assistance, including generative artificial intelligence
  (GenAI), constitutes academic dishonesty and may be considered
  plagiarism. Such cases will be handled in accordance with APU regulations
  on academic integrity.
- Penalties will depend on the severity and nature of the offence. Minor
  breaches (e.g. poor academic practice) may result in mark deductions,
  while more serious breaches (e.g. failure to declare GenAI use) may lead
  to severe disciplinary action, including retaking the module with
  attendance, and expulsion for repeated offences.

## 7.0 Deliverables

- Report and source code
- Submission deadline: **27th September 2026**

### 7.1 Report

The report needs to be submitted in softcopy to Moodle with:

**A. Cover Page**
- Module
- Coursework Title
- Intake code
- Student name and ID
- Date Assigned (the date the report was handed out)
- Date Completed (the date the report is due to be handed in)

**B. Table of Contents**

**C. Contents**
- Design solution (Use Case Diagram and Class Diagram)
- Screenshots of output of the program with appropriate explanations
- Description and justification of Object-oriented concepts incorporated
  into the solution
- Additional feature

**D. Limitation and Conclusion**

**E. References**

**F. Appendix**
- Screenshots of all text files that are used to store the data
- AI prompt logs of all members

### 7.2 Source Code

Application source code needs to be compiled into a zip folder and
submitted to Moodle.

### 7.3 AI Prompt Log

Refer to the template attached in the Appendix.

## 8.0 Assignment Assessment Criteria

The assignment assessment consists of two main components: System
Implementation (40%) and System Documentation (15%).

| Assessment | Description | Weightage (%) |
|---|---|---|
| System Implementation | Performance result of all operations and design per requirements | 16 |
| | Appropriate design and the implementation of codes illustrating the object-oriented programming concept incorporated | 16 |
| | Presentation | 8 |
| System Documentation | Description and justification of the object-oriented concepts incorporated | 6 |
| | UML Diagrams | 4 |
| | Program output screenshots | 3 |
| | Report format and references | 2 |

## 9.0 Development Tools

The program must be written in Java language, and you can use any Java
development IDE as a tool, but back-end data must be stored in `.txt` files.

## 10.0 Academic Integrity

You are expected to maintain the utmost level of academic integrity during
the duration of the course. Plagiarism is a serious offence and will be
dealt with according to the regulations of Asia Pacific University on
plagiarism.

## 11.0 Performance Criteria

The final grade for this assessment may be adjusted based on the extent,
transparency, and appropriateness of AI usage. Adjustments will be
determined holistically in accordance with the AI Usage Rubric and are not
calculated through simple addition of individual penalties. The assessor's
judgment is final where supported by documented evidence.

### Appendix — Individual Learning Progress Report and Contribution

Submit a personal reflective log based on the following format:

| Student name | | Gen AI (version / provider) | |
|---|---|---|---|
| Purpose of using the tool | | | |

| ID | Prompt / Input | Output | Where do you use it / modification done? |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| n | | | |

- A short paragraph explaining how outputs were verified and integration
  with other student work.

---

# Implementation Notes

The section below documents the actual codebase as built — how to run it,
its structure, and what's been verified — as a companion to the coursework
brief above.

A Java coursework project (OODJ — Object-Oriented Design in Java)
implementing the Hospital Management System for the four roles specified in
Table 1.0: **Admin Staff**, **Medical Manager**, **Doctor**, and **Patient**.
Every role has both a text-menu console app and a full desktop GUI, both
backed by the same service layer.

## Tech stack

- **Java SE**, plain `javax.swing`/`java.awt` — no external UI library
- **Apache NetBeans** (Ant-based "Java Application" project) — the GUI screens
  are real `.form`/`.java` pairs, editable in NetBeans's drag-and-drop Designer,
  not hand-coded fakes
- **Plain text files** for persistence (`data/*.txt`, comma-separated) — no
  database, per Section 6.0's storage requirement
- No streams, lambdas, `Optional`, `var`, enhanced switch, or pattern-matching
  `instanceof` anywhere in the graded code — classic, explicit Java throughout,
  matching the level actually covered in lectures rather than modern
  functional-style syntax

## Running it

Open the project folder in NetBeans (`File → Open Project`). Two ways to run each role:

**Console** — right-click and run one of these `main()` classes under `assignment.app`:
`AdminStaffApp`, `MedicalManagerApp`, `DoctorApp`, `PatientApp` — or `HospitalApp` for a
single menu that routes to any of the four.

**GUI** — run the matching `*LoginFrame` class:
- `assignment.gui.AdminLoginFrame`
- `assignment.gui.doctor.DoctorLoginFrame`
- `assignment.gui.manager.ManagerLoginFrame`
- `assignment.gui.patient.PatientLoginFrame`

Both interfaces read and write the same `data/` files through the same service
classes — nothing is duplicated or mocked.

### Demo logins

| Role | ID | Password |
|---|---|---|
| Admin Staff | `AS001` | `admin123` |
| Medical Manager | `MM001` | `admin123` |
| Doctor | `DOC001` (also `DOC002`) | `doc123` |
| Patient | `PT001` | `pass123` |

First run with no `data/` files seeds this demo data automatically (`SeedData.ensure()`).

## Project structure

```
src/assignment/
  model/     33 files — Person hierarchy (Patient/Doctor/AdminStaff/MedicalManager),
             enums, value objects. Plain fields + getters/setters, toCsv()/fromCsv().
  service/   16 files — one class per use-case area (BookingService, RosterService,
             UserService, ...), all validation and business logic lives here.
  util/      CsvUtil, FileHandler, IdGenerator — the persistence plumbing.
  app/       6 files — console menus, one per role, thin wrappers around the
             service layer.
  gui/       Admin Staff GUI (root package) plus doctor/, manager/, patient/
             subpackages — each a Login + Dashboard (sidebar + CardLayout content)
             pair, built with GroupLayout so the NetBeans Design view and the
             running app always match.

data/        Plain-text "database" — one file per entity, comma-separated.
test/        Hand-written test harness (see Testing below).
```

## Architecture notes

- **Layered by responsibility, not by role**: `model` → `service` → (`app` or `gui`).
  Both interfaces call the exact same service methods, so a booking rule fixed once
  is fixed in both places.
- **`Database`** holds everything as static in-memory lists, loaded from and saved
  back to `data/*.txt` on every change — a deliberately simple stand-in for a real
  database, appropriate for the no-database requirement in Section 6.0.
- **GUI screens use `GroupLayout`**, NetBeans's own default layout manager, not the
  "Absolute Layout" option — that turned out to depend on an external library
  (`org.netbeans.lib.awtextra`), which was avoided in favor of what NetBeans
  generates automatically.

## Testing

- `test/assignment/tests/ServiceTests.java` — a hand-written harness (no JUnit;
  this course doesn't cover a testing framework, so it's plain Java: a `main()`
  method, manual `if` checks, PASS/FAIL output). Runs against an isolated data
  folder, so it never touches the real `data/` files. Run it the same way as any
  other class with a `main()` method (Shift+F6 in NetBeans).
- `QA_CHECKLIST.md` — a manual, per-role checklist for the GUI side, since
  automated GUI testing is out of scope for this course.

## Known gaps

- Visual polish (KPI tiles, styled tables) was applied to every Dashboard screen;
  some deeper screens use plainer default styling.
