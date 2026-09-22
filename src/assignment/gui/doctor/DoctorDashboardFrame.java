package assignment.gui.doctor;

import assignment.model.AppointmentStatus;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.LabRequest;
import assignment.model.LabRequestStatus;
import assignment.model.LabTestType;
import assignment.model.Patient;
import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.service.AccountService;
import assignment.service.BookingService;
import assignment.service.ConsultationService;
import assignment.service.Database;
import assignment.service.LabRequestService;
import assignment.service.PrescriptionService;

/**
 * Main window for a logged-in Doctor: sidebar + swappable content area
 * (CardLayout). Built as a real NetBeans JFrame Form (GroupLayout, no
 * external library) - open this in NetBeans's Design view to drag
 * components around; never hand-edit inside initComponents().
 */
public class DoctorDashboardFrame extends javax.swing.JFrame {

    private final Doctor current;
    private final AccountService accountService = new AccountService();
    private final BookingService bookingService = new BookingService();
    private final ConsultationService consultationService = new ConsultationService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final LabRequestService labRequestService = new LabRequestService();

    private Patient lookedUpPatient;
    private Prescription currentRx;

    public DoctorDashboardFrame(Doctor current) {
        this.current = current;
        initComponents();
        java.awt.Image logoImg = new javax.swing.ImageIcon(getClass().getResource("/assignment/gui/logo.png")).getImage();
        lblBrand.setIcon(new javax.swing.ImageIcon(logoImg.getScaledInstance(44, 26, java.awt.Image.SCALE_SMOOTH)));
        lblBrand.setIconTextGap(8);
        setSize(1440, 810);
        setResizable(false);
        setLocationRelativeTo(null);
        cboLabType.setModel(new javax.swing.DefaultComboBoxModel<>(LabTestType.values()));
        cboProfileGender.setModel(new javax.swing.DefaultComboBoxModel<>(Gender.values()));
        txtProfileName.setText(current.getName());
        txtProfilePhone.setText(current.getPhone());
        txtProfileEmail.setText(current.getEmail());
        txtProfileAddress.setText(current.getAddress());
        cboProfileGender.setSelectedItem(current.getGender());
        txtSpecialization.setText(current.getSpecialization());
        chkAvailable.setSelected(current.isAvailable());
        txtNotes.setLineWrap(true);
        refreshDashboard();
        refreshAppointmentsTable();
        refreshLabHistoryTable();
    }

    private void showCard(String key) {
        java.awt.CardLayout cl = (java.awt.CardLayout) pnlContent.getLayout();
        cl.show(pnlContent, key);
    }

    private void buildKpiTile(javax.swing.JPanel tile, javax.swing.JLabel label, javax.swing.JLabel value) {
        javax.swing.GroupLayout tileLayout = new javax.swing.GroupLayout(tile);
        tile.setLayout(tileLayout);
        tileLayout.setHorizontalGroup(
            tileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tileLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(tileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(value, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tileLayout.setVerticalGroup(
            tileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tileLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(label, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(value, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // ---------- dashboard ----------
    private void refreshDashboard() {
        lblWelcome.setText("Welcome Back, " + current.getName());

        int upcoming = 0;
        java.util.List<assignment.model.Appointment> appts = bookingService.forDoctor(current.getId());
        for (int i = 0; i < appts.size(); i++) {
            if (appts.get(i).getStatus() == AppointmentStatus.BOOKED) upcoming++;
        }
        lblKpiUpcomingValue.setText(String.valueOf(upcoming));

        int activeRx = 0;
        for (int i = 0; i < Database.prescriptions.size(); i++) {
            assignment.model.Prescription rx = Database.prescriptions.get(i);
            if (rx.getDoctorId() != null && rx.getDoctorId().equalsIgnoreCase(current.getId())
                    && rx.getStatus() == assignment.model.PrescriptionStatus.ACTIVE) {
                activeRx++;
            }
        }
        lblKpiActiveRxValue.setText(String.valueOf(activeRx));

        int pendingLab = 0;
        for (int i = 0; i < Database.labRequests.size(); i++) {
            LabRequest r = Database.labRequests.get(i);
            if (r.getDoctorId() != null && r.getDoctorId().equalsIgnoreCase(current.getId())
                    && r.getStatus() == LabRequestStatus.REQUESTED) {
                pendingLab++;
            }
        }
        lblKpiPendingLabValue.setText(String.valueOf(pendingLab));

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Appointment ID", "Date/Time", "Patient", "Department", "Status", "Fee"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        for (int i = 0; i < appts.size(); i++) {
            assignment.model.Appointment a = appts.get(i);
            model.addRow(new Object[]{a.getAppointmentId(), a.getDateTime(), a.getPatientId(),
                    a.getDepartmentCode(), a.getStatus(), a.getConsultationFee()});
        }
        tblDashAppts.setModel(model);
        tblDashAppts.setGridColor(new java.awt.Color(233, 234, 230));
        tblDashAppts.setShowVerticalLines(false);
        tblDashAppts.setRowHeight(30);
        tblDashAppts.setSelectionBackground(new java.awt.Color(233, 244, 238));
        tblDashAppts.setSelectionForeground(new java.awt.Color(22, 24, 26));
    }

    // ---------- appointments ----------
    private void refreshAppointmentsTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Appointment ID", "Date/Time", "Patient", "Department", "Status", "Fee"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        java.util.List<assignment.model.Appointment> appts = bookingService.forDoctor(current.getId());
        for (int i = 0; i < appts.size(); i++) {
            assignment.model.Appointment a = appts.get(i);
            model.addRow(new Object[]{a.getAppointmentId(), a.getDateTime(), a.getPatientId(),
                    a.getDepartmentCode(), a.getStatus(), a.getConsultationFee()});
        }
        tblAppointments.setModel(model);
    }

    private void btnMarkCompletedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkCompletedActionPerformed
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select an appointment first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            bookingService.markCompleted((String) tblAppointments.getValueAt(row, 0));
            refreshAppointmentsTable();
            refreshDashboard();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnMarkCompletedActionPerformed

    private void btnMarkNoShowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkNoShowActionPerformed
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select an appointment first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            bookingService.markNoShow((String) tblAppointments.getValueAt(row, 0));
            refreshAppointmentsTable();
            refreshDashboard();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnMarkNoShowActionPerformed

    // ---------- clinical ----------
    private void btnLookupPatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLookupPatientActionPerformed
        String key = txtPatientLookup.getText().trim();
        lookedUpPatient = null;
        for (Patient p : Database.patients) {
            if (p.getId().equalsIgnoreCase(key)) {
                lookedUpPatient = p;
                break;
            }
        }
        if (lookedUpPatient == null) {
            lblPatientInfo.setText("No such patient: " + key);
        } else {
            lblPatientInfo.setText(lookedUpPatient.getName() + "  blood=" + lookedUpPatient.getBloodType()
                    + "  allergies=" + lookedUpPatient.getAllergies());
        }
    }//GEN-LAST:event_btnLookupPatientActionPerformed

    private void btnLogVitalsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogVitalsActionPerformed
        if (lookedUpPatient == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Look up a valid patient first.",
                    "No patient", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double temp = Double.parseDouble(txtTemp.getText().trim());
            int sys = Integer.parseInt(txtSystolic.getText().trim());
            int dia = Integer.parseInt(txtDiastolic.getText().trim());
            int hr = Integer.parseInt(txtHeartRate.getText().trim());
            int rr = Integer.parseInt(txtRespRate.getText().trim());
            int spo2 = Integer.parseInt(txtSpo2.getText().trim());
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double height = Double.parseDouble(txtHeight.getText().trim());
            String apptId = txtClinicalApptId.getText().trim();
            assignment.model.VitalSigns v = consultationService.logVitals(lookedUpPatient.getId(),
                    current.getId(), apptId, temp, sys, dia, hr, rr, spo2, weight, height);
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Recorded " + v.getRecordId() + " (BMI " + String.format("%.1f", v.bmi()) + ")");
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please enter valid numbers for all vital sign fields.",
                    "Invalid input", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnLogVitalsActionPerformed

    private void btnSaveNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveNoteActionPerformed
        if (lookedUpPatient == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Look up a valid patient first.",
                    "No patient", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String apptId = txtClinicalApptId.getText().trim();
            assignment.model.ConsultationNote n = consultationService.writeNote(apptId, lookedUpPatient.getId(),
                    current.getId(), txtSymptoms.getText(), txtDiagnosis.getText(), txtNotes.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Saved note " + n.getNoteId());
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveNoteActionPerformed

    // ---------- prescriptions ----------
    private void btnStartRxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartRxActionPerformed
        try {
            String patientId = txtRxPatientId.getText().trim();
            String apptId = txtRxApptId.getText().trim();
            currentRx = prescriptionService.issue(patientId, current.getId(), apptId);
            lblRxStatus.setText("Started prescription " + currentRx.getPrescriptionId());
            refreshRxItemsTable();
            refreshRxHistoryTable(patientId);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnStartRxActionPerformed

    private void btnAddMedicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMedicationActionPerformed
        if (currentRx == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Start a prescription first.",
                    "No prescription", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int days = txtDuration.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtDuration.getText().trim());
            int qty = txtQuantity.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtQuantity.getText().trim());
            PrescriptionItem item = prescriptionService.addItem(currentRx.getPrescriptionId(),
                    txtDrugName.getText(), txtDosage.getText(), txtFrequency.getText(), days, qty, txtInstructions.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Added " + item.getItemId());
            refreshRxItemsTable();
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Duration and quantity must be whole numbers.",
                    "Invalid input", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddMedicationActionPerformed

    private void btnFinishRxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinishRxActionPerformed
        if (currentRx == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "No prescription in progress.",
                    "Nothing to finish", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentRx.getItems().isEmpty()) {
            prescriptionService.cancel(currentRx.getPrescriptionId());
            lblRxStatus.setText("No items added - prescription cancelled.");
        } else {
            lblRxStatus.setText("Prescription " + currentRx.getPrescriptionId()
                    + " has " + currentRx.getItems().size() + " item(s).");
        }
        refreshRxHistoryTable(currentRx.getPatientId());
        currentRx = null;
    }//GEN-LAST:event_btnFinishRxActionPerformed

    private void refreshRxItemsTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Drug", "Dosage", "Frequency", "Days", "Qty", "Instructions"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        if (currentRx != null) {
            java.util.List<PrescriptionItem> items = currentRx.getItems();
            for (int i = 0; i < items.size(); i++) {
                PrescriptionItem it = items.get(i);
                model.addRow(new Object[]{it.getDrugName(), it.getDosage(), it.getFrequency(),
                        it.getDurationDays(), it.getQuantity(), it.getInstructions()});
            }
        }
        tblRxItems.setModel(model);
    }

    private void refreshRxHistoryTable(String patientId) {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Prescription ID", "Issued", "Status", "Items"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        if (patientId != null && !patientId.isBlank()) {
            java.util.List<Prescription> list = prescriptionService.forPatient(patientId.trim());
            for (int i = 0; i < list.size(); i++) {
                Prescription rx = list.get(i);
                model.addRow(new Object[]{rx.getPrescriptionId(), rx.getIssuedDate(), rx.getStatus(), rx.getItems().size()});
            }
        }
        tblRxHistory.setModel(model);
    }

    // ---------- lab / imaging ----------
    private void refreshLabHistoryTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Request ID", "Patient", "Type", "Requested", "Status", "Asset"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        for (int i = 0; i < Database.labRequests.size(); i++) {
            LabRequest r = Database.labRequests.get(i);
            if (r.getDoctorId() != null && r.getDoctorId().equalsIgnoreCase(current.getId())) {
                model.addRow(new Object[]{r.getRequestId(), r.getPatientId(), r.getType(),
                        r.getRequestedDate(), r.getStatus(), r.getAssignedAssetId()});
            }
        }
        tblLabHistory.setModel(model);
    }

    private void btnRaiseRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRaiseRequestActionPerformed
        try {
            String patientId = txtLabPatientId.getText().trim();
            String apptId = txtLabApptId.getText().trim();
            LabTestType type = (LabTestType) cboLabType.getSelectedItem();
            LabRequest r = labRequestService.raise(patientId, current.getId(), apptId, type, txtLabReason.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Raised " + r.getRequestId() + " (" + r.getStatus() + ")");
            refreshLabHistoryTable();
            refreshDashboard();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRaiseRequestActionPerformed

    // ---------- profile ----------
    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfileActionPerformed
        try {
            accountService.editProfile(current, txtProfileName.getText(), txtProfilePhone.getText(),
                    txtProfileEmail.getText(), txtProfileAddress.getText(), (Gender) cboProfileGender.getSelectedItem());
            String spec = txtSpecialization.getText();
            if (spec != null && !spec.isBlank()) current.setSpecialization(spec.trim());
            current.setAvailable(chkAvailable.isSelected());
            accountService.persist(current);
            lblProfileStatus.setText("Profile saved.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveProfileActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        boolean ok = accountService.changePassword(current, new String(txtCurrentPassword.getPassword()),
                new String(txtNewPassword.getPassword()));
        if (ok) {
            javax.swing.JOptionPane.showMessageDialog(this, "Password changed.");
            txtCurrentPassword.setText("");
            txtNewPassword.setText("");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Current password is incorrect, or new password is invalid.");
        }
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    // ---------- nav ----------
    private void btnNavDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavDashboardActionPerformed
        refreshDashboard();
        showCard("dashboard");
    }//GEN-LAST:event_btnNavDashboardActionPerformed

    private void btnNavAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAppointmentsActionPerformed
        refreshAppointmentsTable();
        showCard("appointments");
    }//GEN-LAST:event_btnNavAppointmentsActionPerformed

    private void btnNavClinicalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavClinicalActionPerformed
        showCard("clinical");
    }//GEN-LAST:event_btnNavClinicalActionPerformed

    private void btnNavPrescriptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavPrescriptionsActionPerformed
        showCard("prescriptions");
    }//GEN-LAST:event_btnNavPrescriptionsActionPerformed

    private void btnNavLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavLabActionPerformed
        refreshLabHistoryTable();
        showCard("lab");
    }//GEN-LAST:event_btnNavLabActionPerformed

    private void btnNavProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavProfileActionPerformed
        showCard("profile");
    }//GEN-LAST:event_btnNavProfileActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        dispose();
        DoctorLoginFrame login = new DoctorLoginFrame();
        login.setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddMedication;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnFinishRx;
    private javax.swing.JButton btnLogVitals;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnLookupPatient;
    private javax.swing.JButton btnMarkCompleted;
    private javax.swing.JButton btnMarkNoShow;
    private javax.swing.JToggleButton btnNavAppointments;
    private javax.swing.JToggleButton btnNavClinical;
    private javax.swing.JToggleButton btnNavDashboard;
    private javax.swing.JToggleButton btnNavLab;
    private javax.swing.JToggleButton btnNavPrescriptions;
    private javax.swing.JToggleButton btnNavProfile;
    private javax.swing.JButton btnRaiseRequest;
    private javax.swing.JButton btnSaveNote;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JButton btnStartRx;
    private javax.swing.JPanel cardAppointments;
    private javax.swing.JPanel cardClinical;
    private javax.swing.JPanel cardDashboard;
    private javax.swing.JPanel cardLab;
    private javax.swing.JPanel cardPrescriptions;
    private javax.swing.JPanel cardProfile;
    private javax.swing.JCheckBox chkAvailable;
    private javax.swing.JComboBox<LabTestType> cboLabType;
    private javax.swing.JComboBox<Gender> cboProfileGender;
    private javax.swing.JLabel lblAddressLbl;
    private javax.swing.JLabel lblAppointmentsTitle;
    private javax.swing.JLabel lblApptIdLbl;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblClinicalTitle;
    private javax.swing.JLabel lblCurrentPwLbl;
    private javax.swing.JLabel lblDiagnosisLbl;
    private javax.swing.JLabel lblDiastolicLbl;
    private javax.swing.JLabel lblDosageLbl;
    private javax.swing.JLabel lblDrugLbl;
    private javax.swing.JLabel lblDurationLbl;
    private javax.swing.JLabel lblEmailLbl;
    private javax.swing.JLabel lblFrequencyLbl;
    private javax.swing.JLabel lblGenderLbl;
    private javax.swing.JLabel lblHeartRateLbl;
    private javax.swing.JLabel lblHeightLbl;
    private javax.swing.JLabel lblHistoryHeader;
    private javax.swing.JLabel lblInstructionsLbl;
    private javax.swing.JLabel lblLabApptLbl;
    private javax.swing.JLabel lblLabHistoryHeader;
    private javax.swing.JLabel lblLabPatientLbl;
    private javax.swing.JLabel lblLabReasonLbl;
    private javax.swing.JLabel lblLabTitle;
    private javax.swing.JLabel lblLabTypeLbl;
    private javax.swing.JLabel lblMedHeader;
    private javax.swing.JLabel lblNameLbl;
    private javax.swing.JLabel lblNewPwLbl;
    private javax.swing.JLabel lblNotesHeader;
    private javax.swing.JLabel lblNotesLbl;
    private javax.swing.JLabel lblPasswordHeader;
    private javax.swing.JLabel lblPatientIdLbl;
    private javax.swing.JLabel lblPatientInfo;
    private javax.swing.JLabel lblPhoneLbl;
    private javax.swing.JLabel lblProfileStatus;
    private javax.swing.JLabel lblProfileTitle;
    private javax.swing.JLabel lblQuantityLbl;
    private javax.swing.JLabel lblRespRateLbl;
    private javax.swing.JLabel lblRxApptLbl;
    private javax.swing.JLabel lblRxPatientLbl;
    private javax.swing.JLabel lblRxStatus;
    private javax.swing.JLabel lblRxTitle;
    private javax.swing.JLabel lblSpecLbl;
    private javax.swing.JLabel lblSpo2Lbl;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JLabel lblWelcomeSub;
    private javax.swing.JPanel pnlKpiUpcoming;
    private javax.swing.JLabel lblKpiUpcomingLabel;
    private javax.swing.JLabel lblKpiUpcomingValue;
    private javax.swing.JPanel pnlKpiActiveRx;
    private javax.swing.JLabel lblKpiActiveRxLabel;
    private javax.swing.JLabel lblKpiActiveRxValue;
    private javax.swing.JPanel pnlKpiPendingLab;
    private javax.swing.JLabel lblKpiPendingLabLabel;
    private javax.swing.JLabel lblKpiPendingLabValue;
    private javax.swing.JLabel lblDashApptsTitle;
    private javax.swing.JLabel lblSymptomsLbl;
    private javax.swing.JLabel lblSystolicLbl;
    private javax.swing.JLabel lblTempLbl;
    private javax.swing.JLabel lblVitalsHeader;
    private javax.swing.JLabel lblWeightLbl;
    private javax.swing.JPanel pnlClinicalForm;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlLabForm;
    private javax.swing.JPanel pnlPrescriptionsForm;
    private javax.swing.JPanel pnlProfileForm;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrollAppointments;
    private javax.swing.JScrollPane scrollClinical;
    private javax.swing.JScrollPane scrollDashAppts;
    private javax.swing.JScrollPane scrollLab;
    private javax.swing.JScrollPane scrollLabHistory;
    private javax.swing.JScrollPane scrollPrescriptions;
    private javax.swing.JScrollPane scrollProfile;
    private javax.swing.JScrollPane scrollRxHistory;
    private javax.swing.JScrollPane scrollRxItems;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTable tblDashAppts;
    private javax.swing.JTable tblLabHistory;
    private javax.swing.JTable tblRxHistory;
    private javax.swing.JTable tblRxItems;
    private javax.swing.JTextField txtClinicalApptId;
    private javax.swing.JPasswordField txtCurrentPassword;
    private javax.swing.JTextField txtDiagnosis;
    private javax.swing.JTextField txtDiastolic;
    private javax.swing.JTextField txtDosage;
    private javax.swing.JTextField txtDrugName;
    private javax.swing.JTextField txtDuration;
    private javax.swing.JTextField txtFrequency;
    private javax.swing.JTextField txtHeartRate;
    private javax.swing.JTextField txtHeight;
    private javax.swing.JTextField txtInstructions;
    private javax.swing.JTextField txtLabApptId;
    private javax.swing.JTextField txtLabPatientId;
    private javax.swing.JTextField txtLabReason;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JTextArea txtNotes;
    private javax.swing.JTextField txtPatientLookup;
    private javax.swing.JTextField txtProfileAddress;
    private javax.swing.JTextField txtProfileEmail;
    private javax.swing.JTextField txtProfileName;
    private javax.swing.JTextField txtProfilePhone;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtRespRate;
    private javax.swing.JTextField txtRxApptId;
    private javax.swing.JTextField txtRxPatientId;
    private javax.swing.JTextField txtSpecialization;
    private javax.swing.JTextField txtSpo2;
    private javax.swing.JTextField txtSymptoms;
    private javax.swing.JTextField txtSystolic;
    private javax.swing.JTextField txtTemp;
    private javax.swing.JTextField txtWeight;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        btnNavDashboard = new javax.swing.JToggleButton();
        btnNavAppointments = new javax.swing.JToggleButton();
        btnNavClinical = new javax.swing.JToggleButton();
        btnNavPrescriptions = new javax.swing.JToggleButton();
        btnNavLab = new javax.swing.JToggleButton();
        btnNavProfile = new javax.swing.JToggleButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();
        cardDashboard = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        lblWelcomeSub = new javax.swing.JLabel();
        pnlKpiUpcoming = new javax.swing.JPanel();
        lblKpiUpcomingLabel = new javax.swing.JLabel();
        lblKpiUpcomingValue = new javax.swing.JLabel();
        pnlKpiActiveRx = new javax.swing.JPanel();
        lblKpiActiveRxLabel = new javax.swing.JLabel();
        lblKpiActiveRxValue = new javax.swing.JLabel();
        pnlKpiPendingLab = new javax.swing.JPanel();
        lblKpiPendingLabLabel = new javax.swing.JLabel();
        lblKpiPendingLabValue = new javax.swing.JLabel();
        lblDashApptsTitle = new javax.swing.JLabel();
        scrollDashAppts = new javax.swing.JScrollPane();
        tblDashAppts = new javax.swing.JTable();
        cardAppointments = new javax.swing.JPanel();
        lblAppointmentsTitle = new javax.swing.JLabel();
        btnMarkCompleted = new javax.swing.JButton();
        btnMarkNoShow = new javax.swing.JButton();
        scrollAppointments = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();
        cardClinical = new javax.swing.JPanel();
        scrollClinical = new javax.swing.JScrollPane();
        pnlClinicalForm = new javax.swing.JPanel();
        lblClinicalTitle = new javax.swing.JLabel();
        lblPatientIdLbl = new javax.swing.JLabel();
        txtPatientLookup = new javax.swing.JTextField();
        btnLookupPatient = new javax.swing.JButton();
        lblPatientInfo = new javax.swing.JLabel();
        lblApptIdLbl = new javax.swing.JLabel();
        txtClinicalApptId = new javax.swing.JTextField();
        lblVitalsHeader = new javax.swing.JLabel();
        lblTempLbl = new javax.swing.JLabel();
        txtTemp = new javax.swing.JTextField();
        lblSystolicLbl = new javax.swing.JLabel();
        txtSystolic = new javax.swing.JTextField();
        lblDiastolicLbl = new javax.swing.JLabel();
        txtDiastolic = new javax.swing.JTextField();
        lblHeartRateLbl = new javax.swing.JLabel();
        txtHeartRate = new javax.swing.JTextField();
        lblRespRateLbl = new javax.swing.JLabel();
        txtRespRate = new javax.swing.JTextField();
        lblSpo2Lbl = new javax.swing.JLabel();
        txtSpo2 = new javax.swing.JTextField();
        lblWeightLbl = new javax.swing.JLabel();
        txtWeight = new javax.swing.JTextField();
        lblHeightLbl = new javax.swing.JLabel();
        txtHeight = new javax.swing.JTextField();
        btnLogVitals = new javax.swing.JButton();
        lblNotesHeader = new javax.swing.JLabel();
        lblSymptomsLbl = new javax.swing.JLabel();
        txtSymptoms = new javax.swing.JTextField();
        lblDiagnosisLbl = new javax.swing.JLabel();
        txtDiagnosis = new javax.swing.JTextField();
        lblNotesLbl = new javax.swing.JLabel();
        txtNotes = new javax.swing.JTextArea();
        btnSaveNote = new javax.swing.JButton();
        cardPrescriptions = new javax.swing.JPanel();
        scrollPrescriptions = new javax.swing.JScrollPane();
        pnlPrescriptionsForm = new javax.swing.JPanel();
        lblRxTitle = new javax.swing.JLabel();
        lblRxPatientLbl = new javax.swing.JLabel();
        txtRxPatientId = new javax.swing.JTextField();
        lblRxApptLbl = new javax.swing.JLabel();
        txtRxApptId = new javax.swing.JTextField();
        btnStartRx = new javax.swing.JButton();
        lblRxStatus = new javax.swing.JLabel();
        lblMedHeader = new javax.swing.JLabel();
        lblDrugLbl = new javax.swing.JLabel();
        txtDrugName = new javax.swing.JTextField();
        lblDosageLbl = new javax.swing.JLabel();
        txtDosage = new javax.swing.JTextField();
        lblFrequencyLbl = new javax.swing.JLabel();
        txtFrequency = new javax.swing.JTextField();
        lblDurationLbl = new javax.swing.JLabel();
        txtDuration = new javax.swing.JTextField();
        lblQuantityLbl = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        lblInstructionsLbl = new javax.swing.JLabel();
        txtInstructions = new javax.swing.JTextField();
        btnAddMedication = new javax.swing.JButton();
        scrollRxItems = new javax.swing.JScrollPane();
        tblRxItems = new javax.swing.JTable();
        btnFinishRx = new javax.swing.JButton();
        lblHistoryHeader = new javax.swing.JLabel();
        scrollRxHistory = new javax.swing.JScrollPane();
        tblRxHistory = new javax.swing.JTable();
        cardLab = new javax.swing.JPanel();
        scrollLab = new javax.swing.JScrollPane();
        pnlLabForm = new javax.swing.JPanel();
        lblLabTitle = new javax.swing.JLabel();
        lblLabPatientLbl = new javax.swing.JLabel();
        txtLabPatientId = new javax.swing.JTextField();
        lblLabApptLbl = new javax.swing.JLabel();
        txtLabApptId = new javax.swing.JTextField();
        lblLabTypeLbl = new javax.swing.JLabel();
        cboLabType = new javax.swing.JComboBox<>();
        lblLabReasonLbl = new javax.swing.JLabel();
        txtLabReason = new javax.swing.JTextField();
        btnRaiseRequest = new javax.swing.JButton();
        lblLabHistoryHeader = new javax.swing.JLabel();
        scrollLabHistory = new javax.swing.JScrollPane();
        tblLabHistory = new javax.swing.JTable();
        cardProfile = new javax.swing.JPanel();
        scrollProfile = new javax.swing.JScrollPane();
        pnlProfileForm = new javax.swing.JPanel();
        lblProfileTitle = new javax.swing.JLabel();
        lblNameLbl = new javax.swing.JLabel();
        txtProfileName = new javax.swing.JTextField();
        lblPhoneLbl = new javax.swing.JLabel();
        txtProfilePhone = new javax.swing.JTextField();
        lblEmailLbl = new javax.swing.JLabel();
        txtProfileEmail = new javax.swing.JTextField();
        lblAddressLbl = new javax.swing.JLabel();
        txtProfileAddress = new javax.swing.JTextField();
        lblGenderLbl = new javax.swing.JLabel();
        cboProfileGender = new javax.swing.JComboBox<>();
        lblSpecLbl = new javax.swing.JLabel();
        txtSpecialization = new javax.swing.JTextField();
        chkAvailable = new javax.swing.JCheckBox();
        btnSaveProfile = new javax.swing.JButton();
        lblProfileStatus = new javax.swing.JLabel();
        lblPasswordHeader = new javax.swing.JLabel();
        lblCurrentPwLbl = new javax.swing.JLabel();
        txtCurrentPassword = new javax.swing.JPasswordField();
        lblNewPwLbl = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        btnChangePassword = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("APU Medical Centre · Doctor Dashboard");

        pnlSidebar.setBackground(new java.awt.Color(255, 255, 255));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblBrand.setForeground(new java.awt.Color(11, 61, 42));
        lblBrand.setText("APU Medical");

        btnNavDashboard.setSelected(true);
        btnNavDashboard.setText("Dashboard");
        btnNavDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavDashboard.addActionListener(this::btnNavDashboardActionPerformed);

        btnNavAppointments.setText("My Appointments");
        btnNavAppointments.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavAppointments.addActionListener(this::btnNavAppointmentsActionPerformed);

        btnNavClinical.setText("Log Vitals & Notes");
        btnNavClinical.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavClinical.addActionListener(this::btnNavClinicalActionPerformed);

        btnNavPrescriptions.setText("Prescriptions");
        btnNavPrescriptions.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavPrescriptions.addActionListener(this::btnNavPrescriptionsActionPerformed);

        btnNavLab.setText("Lab / Imaging Requests");
        btnNavLab.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavLab.addActionListener(this::btnNavLabActionPerformed);

        btnNavProfile.setText("My Profile");
        btnNavProfile.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavProfile.addActionListener(this::btnNavProfileActionPerformed);

        btnLogout.setText("← Log out");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavClinical, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavPrescriptions, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavLab, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(btnNavDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavClinical, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavPrescriptions, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavLab, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        javax.swing.ButtonGroup navGroup = new javax.swing.ButtonGroup();
        navGroup.add(btnNavDashboard);
        navGroup.add(btnNavAppointments);
        navGroup.add(btnNavClinical);
        navGroup.add(btnNavPrescriptions);
        navGroup.add(btnNavLab);
        navGroup.add(btnNavProfile);

        pnlContent.setLayout(new java.awt.CardLayout());
        pnlContent.setBackground(new java.awt.Color(250, 250, 248));

        // ---- dashboard ----
        cardDashboard.setBackground(new java.awt.Color(250, 250, 248));

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblWelcome.setForeground(new java.awt.Color(22, 24, 26));
        lblWelcome.setText("Welcome Back");

        lblWelcomeSub.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblWelcomeSub.setForeground(new java.awt.Color(139, 147, 140));
        lblWelcomeSub.setText("Here's what's happening at APU Medical Centre today.");

        pnlKpiUpcoming.setBackground(new java.awt.Color(255, 255, 255));
        lblKpiUpcomingLabel.setForeground(new java.awt.Color(139, 147, 140));
        lblKpiUpcomingLabel.setText("Upcoming Appointments");
        lblKpiUpcomingValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblKpiUpcomingValue.setText("0");
        buildKpiTile(pnlKpiUpcoming, lblKpiUpcomingLabel, lblKpiUpcomingValue);

        pnlKpiActiveRx.setBackground(new java.awt.Color(255, 255, 255));
        lblKpiActiveRxLabel.setForeground(new java.awt.Color(139, 147, 140));
        lblKpiActiveRxLabel.setText("Active Prescriptions Issued");
        lblKpiActiveRxValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblKpiActiveRxValue.setText("0");
        buildKpiTile(pnlKpiActiveRx, lblKpiActiveRxLabel, lblKpiActiveRxValue);

        pnlKpiPendingLab.setBackground(new java.awt.Color(255, 255, 255));
        lblKpiPendingLabLabel.setForeground(new java.awt.Color(139, 147, 140));
        lblKpiPendingLabLabel.setText("Pending Lab Requests Raised");
        lblKpiPendingLabValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblKpiPendingLabValue.setText("0");
        buildKpiTile(pnlKpiPendingLab, lblKpiPendingLabLabel, lblKpiPendingLabValue);

        lblDashApptsTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblDashApptsTitle.setText("Upcoming Appointments");

        scrollDashAppts.setViewportView(tblDashAppts);

        javax.swing.GroupLayout cardDashboardLayout = new javax.swing.GroupLayout(cardDashboard);
        cardDashboard.setLayout(cardDashboardLayout);
        cardDashboardLayout.setHorizontalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWelcomeSub, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardDashboardLayout.createSequentialGroup()
                        .addComponent(pnlKpiUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlKpiActiveRx, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlKpiPendingLab, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblDashApptsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollDashAppts, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardDashboardLayout.setVerticalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblWelcomeSub, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlKpiUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlKpiActiveRx, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlKpiPendingLab, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(lblDashApptsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrollDashAppts, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardDashboard, "dashboard");

        // ---- appointments ----
        lblAppointmentsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblAppointmentsTitle.setText("My Appointments");
        btnMarkCompleted.setBackground(new java.awt.Color(21, 122, 82));
        btnMarkCompleted.setForeground(new java.awt.Color(255, 255, 255));
        btnMarkCompleted.setText("Mark Completed");
        btnMarkCompleted.addActionListener(this::btnMarkCompletedActionPerformed);
        btnMarkNoShow.setBackground(new java.awt.Color(194, 74, 79));
        btnMarkNoShow.setForeground(new java.awt.Color(255, 255, 255));
        btnMarkNoShow.setText("Mark No-Show");
        btnMarkNoShow.addActionListener(this::btnMarkNoShowActionPerformed);
        scrollAppointments.setViewportView(tblAppointments);

        javax.swing.GroupLayout cardAppointmentsLayout = new javax.swing.GroupLayout(cardAppointments);
        cardAppointments.setLayout(cardAppointmentsLayout);
        cardAppointmentsLayout.setHorizontalGroup(
            cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAppointmentsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAppointmentsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardAppointmentsLayout.createSequentialGroup()
                        .addComponent(btnMarkCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnMarkNoShow, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardAppointmentsLayout.setVerticalGroup(
            cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAppointmentsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblAppointmentsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMarkCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMarkNoShow, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(scrollAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardAppointments, "appointments");

        // ---- clinical ----
        lblClinicalTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblClinicalTitle.setText("Log Vitals & Notes");
        lblPatientIdLbl.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblPatientIdLbl.setText("Patient ID");
        btnLookupPatient.setText("Look Up");
        btnLookupPatient.addActionListener(this::btnLookupPatientActionPerformed);
        lblPatientInfo.setForeground(new java.awt.Color(21, 122, 82));
        lblPatientInfo.setText(" ");
        lblApptIdLbl.setText("Related Appointment ID (optional)");
        lblVitalsHeader.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblVitalsHeader.setText("Vital Signs");
        lblTempLbl.setText("Temperature (C)");
        lblSystolicLbl.setText("BP Systolic");
        lblDiastolicLbl.setText("BP Diastolic");
        lblHeartRateLbl.setText("Heart Rate");
        lblRespRateLbl.setText("Respiratory Rate");
        lblSpo2Lbl.setText("SpO2 %");
        lblWeightLbl.setText("Weight (kg)");
        lblHeightLbl.setText("Height (cm)");
        btnLogVitals.setBackground(new java.awt.Color(21, 122, 82));
        btnLogVitals.setForeground(new java.awt.Color(255, 255, 255));
        btnLogVitals.setText("Log Vitals");
        btnLogVitals.addActionListener(this::btnLogVitalsActionPerformed);
        lblNotesHeader.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblNotesHeader.setText("Consultation Note");
        lblSymptomsLbl.setText("Symptoms");
        lblDiagnosisLbl.setText("Diagnosis");
        lblNotesLbl.setText("Notes");
        txtNotes.setRows(3);
        btnSaveNote.setBackground(new java.awt.Color(21, 122, 82));
        btnSaveNote.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveNote.setText("Save Note");
        btnSaveNote.addActionListener(this::btnSaveNoteActionPerformed);

        javax.swing.GroupLayout pnlClinicalFormLayout = new javax.swing.GroupLayout(pnlClinicalForm);
        pnlClinicalForm.setLayout(pnlClinicalFormLayout);
        pnlClinicalFormLayout.setHorizontalGroup(
            pnlClinicalFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClinicalFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlClinicalFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblClinicalTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPatientIdLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlClinicalFormLayout.createSequentialGroup()
                        .addComponent(txtPatientLookup, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnLookupPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblPatientInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblApptIdLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtClinicalApptId, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVitalsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTempLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSystolicLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSystolic, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDiastolicLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDiastolic, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHeartRateLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHeartRate, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRespRateLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRespRate, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSpo2Lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSpo2, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWeightLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHeightLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLogVitals, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNotesHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSymptomsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSymptoms, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDiagnosisLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDiagnosis, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNotesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveNote, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlClinicalFormLayout.setVerticalGroup(
            pnlClinicalFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClinicalFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblClinicalTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblPatientIdLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(pnlClinicalFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPatientLookup, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLookupPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(lblPatientInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblApptIdLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtClinicalApptId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblVitalsHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblTempLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblSystolicLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtSystolic, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDiastolicLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDiastolic, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblHeartRateLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtHeartRate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblRespRateLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRespRate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblSpo2Lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtSpo2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblWeightLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblHeightLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnLogVitals, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblNotesHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblSymptomsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtSymptoms, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDiagnosisLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDiagnosis, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNotesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSaveNote, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        scrollClinical.setViewportView(pnlClinicalForm);

        javax.swing.GroupLayout cardClinicalLayout = new javax.swing.GroupLayout(cardClinical);
        cardClinical.setLayout(cardClinicalLayout);
        cardClinicalLayout.setHorizontalGroup(
            cardClinicalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardClinicalLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollClinical, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        cardClinicalLayout.setVerticalGroup(
            cardClinicalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardClinicalLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollClinical, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardClinical, "clinical");

        // ---- prescriptions ----
        lblRxTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblRxTitle.setText("Prescriptions");
        lblRxPatientLbl.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblRxPatientLbl.setText("Patient ID");
        lblRxApptLbl.setText("Related Appointment ID (optional)");
        btnStartRx.setBackground(new java.awt.Color(21, 122, 82));
        btnStartRx.setForeground(new java.awt.Color(255, 255, 255));
        btnStartRx.setText("Start Prescription");
        btnStartRx.addActionListener(this::btnStartRxActionPerformed);
        lblRxStatus.setForeground(new java.awt.Color(21, 122, 82));
        lblRxStatus.setText(" ");
        lblMedHeader.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblMedHeader.setText("Add Medication");
        lblDrugLbl.setText("Drug Name");
        lblDosageLbl.setText("Dosage");
        lblFrequencyLbl.setText("Frequency");
        lblDurationLbl.setText("Duration Days");
        lblQuantityLbl.setText("Quantity");
        lblInstructionsLbl.setText("Instructions");
        btnAddMedication.setText("Add Medication");
        btnAddMedication.addActionListener(this::btnAddMedicationActionPerformed);
        scrollRxItems.setViewportView(tblRxItems);
        btnFinishRx.setText("Finish Prescription");
        btnFinishRx.addActionListener(this::btnFinishRxActionPerformed);
        lblHistoryHeader.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblHistoryHeader.setText("Prescription History (for entered Patient ID)");
        scrollRxHistory.setViewportView(tblRxHistory);

        javax.swing.GroupLayout pnlPrescriptionsFormLayout = new javax.swing.GroupLayout(pnlPrescriptionsForm);
        pnlPrescriptionsForm.setLayout(pnlPrescriptionsFormLayout);
        pnlPrescriptionsFormLayout.setHorizontalGroup(
            pnlPrescriptionsFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrescriptionsFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlPrescriptionsFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRxTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRxPatientLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRxPatientId, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRxApptLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRxApptId, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStartRx, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMedHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDrugLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDrugName, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDosageLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDosage, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFrequencyLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDurationLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblQuantityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblInstructionsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInstructions, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddMedication, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollRxItems, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(btnFinishRx, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHistoryHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollRxHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlPrescriptionsFormLayout.setVerticalGroup(
            pnlPrescriptionsFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrescriptionsFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblRxTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblRxPatientLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRxPatientId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblRxApptLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRxApptId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnStartRx, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblRxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblMedHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDrugLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDrugName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDosageLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDosage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblFrequencyLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDurationLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblQuantityLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblInstructionsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtInstructions, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnAddMedication, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(scrollRxItems, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnFinishRx, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblHistoryHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrollRxHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        scrollPrescriptions.setViewportView(pnlPrescriptionsForm);

        javax.swing.GroupLayout cardPrescriptionsLayout = new javax.swing.GroupLayout(cardPrescriptions);
        cardPrescriptions.setLayout(cardPrescriptionsLayout);
        cardPrescriptionsLayout.setHorizontalGroup(
            cardPrescriptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPrescriptionsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollPrescriptions, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        cardPrescriptionsLayout.setVerticalGroup(
            cardPrescriptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPrescriptionsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollPrescriptions, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardPrescriptions, "prescriptions");

        // ---- lab / imaging ----
        lblLabTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblLabTitle.setText("Lab / Imaging Requests");
        lblLabPatientLbl.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblLabPatientLbl.setText("Patient ID");
        lblLabApptLbl.setText("Related Appointment ID (optional)");
        lblLabTypeLbl.setText("Test Type");
        lblLabReasonLbl.setText("Clinical Reason");
        btnRaiseRequest.setBackground(new java.awt.Color(21, 122, 82));
        btnRaiseRequest.setForeground(new java.awt.Color(255, 255, 255));
        btnRaiseRequest.setText("Raise Request");
        btnRaiseRequest.addActionListener(this::btnRaiseRequestActionPerformed);
        lblLabHistoryHeader.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblLabHistoryHeader.setText("Requests I've Raised");
        scrollLabHistory.setViewportView(tblLabHistory);

        javax.swing.GroupLayout pnlLabFormLayout = new javax.swing.GroupLayout(pnlLabForm);
        pnlLabForm.setLayout(pnlLabFormLayout);
        pnlLabFormLayout.setHorizontalGroup(
            pnlLabFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLabFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlLabFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLabTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLabPatientLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLabPatientId, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLabApptLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLabApptId, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLabTypeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboLabType, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLabReasonLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLabReason, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRaiseRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLabHistoryHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollLabHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlLabFormLayout.setVerticalGroup(
            pnlLabFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLabFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblLabTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblLabPatientLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtLabPatientId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblLabApptLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtLabApptId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblLabTypeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboLabType, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblLabReasonLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtLabReason, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnRaiseRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblLabHistoryHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(scrollLabHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        scrollLab.setViewportView(pnlLabForm);

        javax.swing.GroupLayout cardLabLayout = new javax.swing.GroupLayout(cardLab);
        cardLab.setLayout(cardLabLayout);
        cardLabLayout.setHorizontalGroup(
            cardLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardLabLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollLab, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        cardLabLayout.setVerticalGroup(
            cardLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardLabLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollLab, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardLab, "lab");

        // ---- profile ----
        lblProfileTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblProfileTitle.setText("My Profile");
        lblNameLbl.setText("Full Name");
        lblPhoneLbl.setText("Phone");
        lblEmailLbl.setText("Email");
        lblAddressLbl.setText("Address");
        lblGenderLbl.setText("Gender");
        lblSpecLbl.setText("Specialization");
        chkAvailable.setText("Available for new bookings");
        btnSaveProfile.setBackground(new java.awt.Color(21, 122, 82));
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save Changes");
        btnSaveProfile.addActionListener(this::btnSaveProfileActionPerformed);
        lblProfileStatus.setForeground(new java.awt.Color(21, 122, 82));
        lblProfileStatus.setText(" ");
        lblPasswordHeader.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblPasswordHeader.setText("Change Password");
        lblCurrentPwLbl.setText("Current Password");
        lblNewPwLbl.setText("New Password (min 6)");
        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(this::btnChangePasswordActionPerformed);

        javax.swing.GroupLayout pnlProfileFormLayout = new javax.swing.GroupLayout(pnlProfileForm);
        pnlProfileForm.setLayout(pnlProfileFormLayout);
        pnlProfileFormLayout.setHorizontalGroup(
            pnlProfileFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfileFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlProfileFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNameLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPhoneLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmailLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAddressLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGenderLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboProfileGender, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSpecLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkAvailable, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPasswordHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCurrentPwLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewPwLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlProfileFormLayout.setVerticalGroup(
            pnlProfileFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfileFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblNameLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblPhoneLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblEmailLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblAddressLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblGenderLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboProfileGender, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblSpecLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(chkAvailable, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfileStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblPasswordHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblCurrentPwLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewPwLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        scrollProfile.setViewportView(pnlProfileForm);

        javax.swing.GroupLayout cardProfileLayout = new javax.swing.GroupLayout(cardProfile);
        cardProfile.setLayout(cardProfileLayout);
        cardProfileLayout.setHorizontalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollProfile, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        cardProfileLayout.setVerticalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollProfile, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardProfile, "profile");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
            .addComponent(pnlContent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Database.loadAll();
        assignment.service.SeedData.ensure();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Doctor demo = null;
                for (Doctor d : Database.doctors) {
                    if (d.getId().equalsIgnoreCase("DOC001")) demo = d;
                }
                new DoctorDashboardFrame(demo).setVisible(true);
            }
        });
    }
}
