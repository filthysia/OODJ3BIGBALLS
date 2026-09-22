# Manual QA Checklist — APU Medical Centre

The service layer has automated coverage (`test/assignment/tests/ServiceTests.java` —
run it directly, it has its own `main()`, no external test library, uses an isolated
data folder so it never touches the real `data/` files).

The GUI is checked manually against this list instead of with an automated GUI
test tool, since that's well beyond what this course covers. Go through each
row after any change that could plausibly affect it; tick the box once verified
in a real run (Shift+F6 in NetBeans), not just by reading the code.

Demo logins: `AS001`/`admin123` · `MM001`/`admin123` · `DOC001`/`doc123` · `PT001`/`pass123`

## All roles

- [ ] Wrong password shows an inline error, does not open the dashboard
- [ ] Correct login opens the dashboard at the documented size (980×620 login / 1440×810
      dashboard), not needing a manual resize
- [ ] Every sidebar nav button switches the content area to the matching screen
- [ ] Logout returns to that role's login screen

## Admin Staff

- [ ] Dashboard KPI numbers match reality (compare against Manage Users' row count, etc.)
- [ ] Create a user of each of the 4 roles; each appears in the table afterwards
- [ ] Delete a user with nothing selected shows a warning, not a crash
- [ ] Delete a user with a row selected removes it from the table
- [ ] Assign a doctor to a manager; Assign Doctors table reflects the change
- [ ] Add a hospital asset; it appears with status AVAILABLE
- [ ] Allocate an asset to a department; status changes to ALLOCATED
- [ ] Release an allocated asset; status returns to AVAILABLE
- [ ] Change the base consultation rate; Billing screen shows the new value on reopen
- [ ] Add an insurance network with a duplicate code; rejected with a clear message
- [ ] Schedule a lab request onto a mismatched asset type; rejected with a clear message
- [ ] Edit profile fields and save; values persist after logout/login

## Doctor

- [ ] My Appointments table shows only this doctor's appointments
- [ ] Mark an appointment completed / no-show; status updates in the table
- [ ] Look up a patient by ID; name/blood type/allergies display correctly
- [ ] Look up a patient ID that doesn't exist; clear error, not a crash
- [ ] Log vital signs with valid numbers; BMI is shown and looks correct
- [ ] Log vital signs with non-numeric input; clear error, not a crash
- [ ] Write a consultation note; confirmation shown
- [ ] Issue a prescription with at least one medication line; appears in prescription history
- [ ] Finish a prescription with zero medication lines; it's auto-cancelled, not left dangling
- [ ] Raise a lab/imaging request; appears in this doctor's own request list

## Medical Manager

- [ ] Create a department; appears in the table
- [ ] Create a department with a duplicate code; rejected with a clear message
- [ ] Assign / remove a doctor on a department; doctor count updates
- [ ] Create a weekly roster; week start snaps to the Monday of that week
- [ ] Add a shift; clashing shift for the same doctor is rejected
- [ ] View roster detail; shift list matches what was added
- [ ] Hospital Metrics report renders with aligned columns (monospaced, not garbled)
- [ ] Revenue Summary report renders with aligned columns
- [ ] Edit profile (including office location) and save; persists after logout/login

## Patient

- [ ] Book Appointment: doctor list shows availability correctly
- [ ] Find Open Slots on a day with a published roster shows real slot times
- [ ] Find Open Slots on a day with no roster shows a clear "no slots" message, not an empty table
- [ ] Book a slot; it appears in My Appointments
- [ ] Reschedule an appointment to a bad date format; clear error, not a crash
- [ ] Cancel an appointment; status updates, cannot cancel a completed one
- [ ] Medical History shows appointments, vitals, notes, prescriptions, lab requests correctly
- [ ] Rate a Visit from a completed appointment vs. rate a doctor directly — both paths work
- [ ] Submit a rating outside 1-5; rejected with a clear message
- [ ] Edit profile (including blood type/allergies) and save; persists after logout/login

## Regression triggers

Re-run this whole list (or at least the relevant role's section) after:
- Any change to a service class's public method signatures or validation logic
- Any change to a `.form`/`.java` pair touched in the Designer
- Pulling changes made by someone else on the team
