package assignment.gui.patient;

import assignment.model.Appointment;
import assignment.model.AppointmentStatus;
import assignment.model.Doctor;
import assignment.model.Feedback;
import assignment.model.LabRequestStatus;
import assignment.model.Patient;
import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.model.PrescriptionStatus;
import assignment.model.Slot;
import assignment.service.AccountService;
import assignment.service.BookingService;
import assignment.service.Database;
import assignment.service.FeedbackService;
import assignment.service.LabRequestService;
import assignment.service.MedicalHistoryService;
import assignment.service.PrescriptionService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Main window for a logged-in Patient: sidebar + swappable content area
 * (CardLayout). Built as a real NetBeans JFrame Form (GroupLayout, no
 * external library) - open this in NetBeans's Design view to drag
 * components around; never hand-edit inside initComponents().
 */
public class PatientDashboardFrame extends javax.swing.JFrame {

    private final Patient current;
    private final AccountService accountService = new AccountService();
    private final BookingService bookingService = new BookingService();
    private final MedicalHistoryService historyService = new MedicalHistoryService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final LabRequestService labRequestService = new LabRequestService();

    /**
     * Creates new form PatientDashboardFrame
     */
    public PatientDashboardFrame(Patient current) {
        this.current = current;
        initComponents();
        setSize(1440, 810);
        setResizable(false);
        setLocationRelativeTo(null);

        javax.swing.ButtonGroup feedbackModeGroup = new javax.swing.ButtonGroup();
        feedbackModeGroup.add(radioFromVisit);
        feedbackModeGroup.add(radioDirect);

        loadProfileFields();
        refreshDashboard();
        refreshAppointmentsTable();
        refreshDoctorsTable();
        refreshHistory();
        refreshCompletedTable();
    }

    private void showCard(String key) {
        java.awt.CardLayout cl = (java.awt.CardLayout) pnlContent.getLayout();
        cl.show(pnlContent, key);
    }

    // ===================================================================
    // Dashboard
    // ===================================================================
    private void refreshDashboard() {
        List<Appointment> mine = bookingService.forPatient(current.getId());

        int upcoming = 0;
        for (int i = 0; i < mine.size(); i++) {
            if (mine.get(i).getStatus() == AppointmentStatus.BOOKED) upcoming++;
        }
        lblUpcoming.setText("Upcoming appointments: " + upcoming);

        List<Prescription> rxs = prescriptionService.forPatient(current.getId());
        int activeRx = 0;
        for (int i = 0; i < rxs.size(); i++) {
            if (rxs.get(i).getStatus() == PrescriptionStatus.ACTIVE) activeRx++;
        }
        lblActiveRx.setText("Active prescriptions: " + activeRx);

        int pendingLab = 0;
        for (assignment.model.LabRequest r : labRequestService.forPatient(current.getId())) {
            if (r.getStatus() == LabRequestStatus.REQUESTED) pendingLab++;
        }
        lblPendingLab.setText("Pending lab requests: " + pendingLab);

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Appointment ID", "Date/Time", "Doctor", "Department", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (int i = 0; i < mine.size(); i++) {
            Appointment a = mine.get(i);
            model.addRow(new Object[]{a.getAppointmentId(), a.getDateTime(), a.getDoctorId(),
                    a.getDepartmentCode(), a.getStatus()});
        }
        tblUpcoming.setModel(model);
    }

    // ===================================================================
    // My Appointments
    // ===================================================================
    private void refreshAppointmentsTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Appointment ID", "Date/Time", "Doctor", "Department", "Status", "Fee"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        List<Appointment> mine = bookingService.forPatient(current.getId());
        for (int i = 0; i < mine.size(); i++) {
            Appointment a = mine.get(i);
            model.addRow(new Object[]{a.getAppointmentId(), a.getDateTime(), a.getDoctorId(),
                    a.getDepartmentCode(), a.getStatus(), a.getConsultationFee()});
        }
        tblAppointments.setModel(model);
    }

    private void btnRescheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRescheduleActionPerformed
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            lblApptMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblApptMessage.setText("Select an appointment in the table first.");
            return;
        }
        String id = (String) tblAppointments.getValueAt(row, 0);
        LocalDate date;
        LocalTime start;
        try {
            date = LocalDate.parse(txtRescheduleDate.getText().trim());
            start = LocalTime.parse(txtRescheduleTime.getText().trim());
        } catch (DateTimeParseException ex) {
            lblApptMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblApptMessage.setText("Use YYYY-MM-DD for date and HH:MM for time.");
            return;
        }
        try {
            Appointment a = bookingService.reschedule(id, date, start);
            refreshAppointmentsTable();
            lblApptMessage.setForeground(new java.awt.Color(21, 122, 82));
            lblApptMessage.setText("Moved " + id + " to " + a.getDateTime());
        } catch (RuntimeException ex) {
            lblApptMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblApptMessage.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnRescheduleActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        int row = tblAppointments.getSelectedRow();
        if (row < 0) {
            lblApptMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblApptMessage.setText("Select an appointment in the table first.");
            return;
        }
        String id = (String) tblAppointments.getValueAt(row, 0);
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Cancel appointment " + id + "?",
                "Confirm cancel", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                bookingService.cancel(id);
                refreshAppointmentsTable();
                lblApptMessage.setForeground(new java.awt.Color(21, 122, 82));
                lblApptMessage.setText("Cancelled " + id + ".");
            } catch (RuntimeException ex) {
                lblApptMessage.setForeground(new java.awt.Color(217, 79, 79));
                lblApptMessage.setText(ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnCancelActionPerformed

    // ===================================================================
    // Book Appointment
    // ===================================================================
    private void refreshDoctorsTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"ID", "Name", "Department", "Available"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Doctor d : Database.doctors) {
            model.addRow(new Object[]{d.getId(), d.getName(), d.getDepartmentCode(),
                    d.isAvailable() ? "Yes" : "No"});
        }
        tblDoctors.setModel(model);
    }

    private void btnFindSlotsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindSlotsActionPerformed
        int row = tblDoctors.getSelectedRow();
        if (row < 0) {
            lblBookingMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblBookingMessage.setText("Select a doctor in the table first.");
            return;
        }
        String doctorId = (String) tblDoctors.getValueAt(row, 0);
        LocalDate date;
        try {
            date = LocalDate.parse(txtBookDate.getText().trim());
        } catch (DateTimeParseException ex) {
            lblBookingMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblBookingMessage.setText("Use the format YYYY-MM-DD for the date.");
            return;
        }
        List<Slot> slots = bookingService.availableSlots(doctorId, date);
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Start", "End"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            model.addRow(new Object[]{s.getStart(), s.getEnd()});
        }
        tblSlots.setModel(model);
        if (slots.isEmpty()) {
            lblBookingMessage.setForeground(new java.awt.Color(139, 147, 140));
            lblBookingMessage.setText("No open slots that day (the doctor may not be rostered).");
        } else {
            lblBookingMessage.setForeground(new java.awt.Color(139, 147, 140));
            lblBookingMessage.setText(slots.size() + " open slot(s) found - select one and click Book.");
        }
    }//GEN-LAST:event_btnFindSlotsActionPerformed

    private void btnBookSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookSlotActionPerformed
        int doctorRow = tblDoctors.getSelectedRow();
        int slotRow = tblSlots.getSelectedRow();
        if (doctorRow < 0) {
            lblBookingMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblBookingMessage.setText("Select a doctor first.");
            return;
        }
        if (slotRow < 0) {
            lblBookingMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblBookingMessage.setText("Select an open slot first.");
            return;
        }
        String doctorId = (String) tblDoctors.getValueAt(doctorRow, 0);
        LocalTime start;
        LocalDate date;
        try {
            date = LocalDate.parse(txtBookDate.getText().trim());
            start = LocalTime.parse(String.valueOf(tblSlots.getValueAt(slotRow, 0)));
        } catch (DateTimeParseException ex) {
            lblBookingMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblBookingMessage.setText("Use the format YYYY-MM-DD for the date.");
            return;
        }
        try {
            Appointment appt = bookingService.book(current.getId(), doctorId, date, start);
            refreshAppointmentsTable();
            refreshDashboard();
            lblBookingMessage.setForeground(new java.awt.Color(21, 122, 82));
            lblBookingMessage.setText(String.format("Booked %s on %s, fee RM%.2f",
                    appt.getAppointmentId(), appt.getDateTime(), appt.getConsultationFee()));
        } catch (RuntimeException ex) {
            lblBookingMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblBookingMessage.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnBookSlotActionPerformed

    // ===================================================================
    // Medical History
    // ===================================================================
    private void refreshHistory() {
        txtHistory.setText(historyService.historyReport(current.getId()));
        txtHistory.setCaretPosition(0);

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Prescription ID", "Issued Date", "Status", "Doctor"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        List<Prescription> rxs = prescriptionService.forPatient(current.getId());
        for (int i = 0; i < rxs.size(); i++) {
            Prescription rx = rxs.get(i);
            model.addRow(new Object[]{rx.getPrescriptionId(), rx.getIssuedDate(), rx.getStatus(), rx.getDoctorId()});
        }
        tblPrescriptions.setModel(model);
    }

    private void btnViewItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewItemsActionPerformed
        int row = tblPrescriptions.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a prescription in the table first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String rxId = (String) tblPrescriptions.getValueAt(row, 0);
        List<Prescription> rxs = prescriptionService.forPatient(current.getId());
        Prescription found = null;
        for (int i = 0; i < rxs.size(); i++) {
            if (rxs.get(i).getPrescriptionId().equals(rxId)) {
                found = rxs.get(i);
                break;
            }
        }
        if (found == null) return;
        StringBuilder sb = new StringBuilder();
        List<PrescriptionItem> items = found.getItems();
        if (items.isEmpty()) {
            sb.append("(no items)");
        } else {
            for (int i = 0; i < items.size(); i++) {
                sb.append(items.get(i)).append('\n');
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this, sb.toString(), "Items in " + rxId,
                javax.swing.JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_btnViewItemsActionPerformed

    // ===================================================================
    // Rate a Visit
    // ===================================================================
    private void refreshCompletedTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Appointment ID", "Date/Time", "Doctor"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (Appointment a : bookingService.forPatient(current.getId())) {
            if (a.getStatus() == AppointmentStatus.COMPLETED) {
                model.addRow(new Object[]{a.getAppointmentId(), a.getDateTime(), a.getDoctorId()});
            }
        }
        tblCompleted.setModel(model);
    }

    private void btnSubmitFeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitFeedbackActionPerformed
        int docRating;
        int visitRating;
        try {
            docRating = Integer.parseInt(txtDoctorRating.getText().trim());
            visitRating = Integer.parseInt(txtVisitRating.getText().trim());
        } catch (NumberFormatException ex) {
            lblFeedbackMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblFeedbackMessage.setText("Ratings must be whole numbers from 1 to 5.");
            return;
        }
        if (docRating < 1 || docRating > 5 || visitRating < 1 || visitRating > 5) {
            lblFeedbackMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblFeedbackMessage.setText("Ratings must be between 1 and 5.");
            return;
        }

        String appointmentId = null;
        String doctorId = null;
        if (radioFromVisit.isSelected()) {
            int row = tblCompleted.getSelectedRow();
            if (row < 0) {
                lblFeedbackMessage.setForeground(new java.awt.Color(217, 79, 79));
                lblFeedbackMessage.setText("Select a completed visit in the table first.");
                return;
            }
            appointmentId = (String) tblCompleted.getValueAt(row, 0);
        } else {
            doctorId = txtDirectDoctorId.getText().trim();
            if (doctorId.isBlank()) {
                lblFeedbackMessage.setForeground(new java.awt.Color(217, 79, 79));
                lblFeedbackMessage.setText("Enter a Doctor ID for a direct rating.");
                return;
            }
        }

        try {
            Feedback fb = feedbackService.submit(current.getId(), doctorId, appointmentId,
                    docRating, visitRating, txtComment.getText());
            lblFeedbackMessage.setForeground(new java.awt.Color(21, 122, 82));
            lblFeedbackMessage.setText("Thank you - feedback " + fb.getFeedbackId() + " recorded.");
            txtDirectDoctorId.setText("");
            txtDoctorRating.setText("");
            txtVisitRating.setText("");
            txtComment.setText("");
        } catch (RuntimeException ex) {
            lblFeedbackMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblFeedbackMessage.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnSubmitFeedbackActionPerformed

    // ===================================================================
    // My Profile
    // ===================================================================
    private void loadProfileFields() {
        txtName.setText(current.getName());
        txtPhone.setText(current.getPhone());
        txtEmail.setText(current.getEmail());
        txtAddress.setText(current.getAddress());
        txtBloodType.setText(current.getBloodType());
        txtAllergies.setText(current.getAllergies());
    }

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfileActionPerformed
        try {
            accountService.editProfile(current, txtName.getText(), txtPhone.getText(),
                    txtEmail.getText(), txtAddress.getText(), null);
            String blood = txtBloodType.getText();
            if (blood != null && !blood.isBlank()) current.setBloodType(blood.trim());
            String allergies = txtAllergies.getText();
            if (allergies != null && !allergies.isBlank()) current.setAllergies(allergies.trim());
            accountService.persist(current);
            lblProfileMessage.setForeground(new java.awt.Color(21, 122, 82));
            lblProfileMessage.setText("Profile saved.");
        } catch (RuntimeException ex) {
            lblProfileMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblProfileMessage.setText(ex.getMessage());
        }
    }//GEN-LAST:event_btnSaveProfileActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        boolean ok = accountService.changePassword(current, new String(txtCurrentPassword.getPassword()),
                new String(txtNewPassword.getPassword()));
        if (ok) {
            lblProfileMessage.setForeground(new java.awt.Color(21, 122, 82));
            lblProfileMessage.setText("Password changed.");
            txtCurrentPassword.setText("");
            txtNewPassword.setText("");
        } else {
            lblProfileMessage.setForeground(new java.awt.Color(217, 79, 79));
            lblProfileMessage.setText("Current password is incorrect, or new password is invalid.");
        }
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    // ===================================================================
    // Sidebar nav
    // ===================================================================
    private void btnNavDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavDashboardActionPerformed
        refreshDashboard();
        showCard("dashboard");
    }//GEN-LAST:event_btnNavDashboardActionPerformed

    private void btnNavAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAppointmentsActionPerformed
        refreshAppointmentsTable();
        showCard("appointments");
    }//GEN-LAST:event_btnNavAppointmentsActionPerformed

    private void btnNavBookingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavBookingActionPerformed
        refreshDoctorsTable();
        showCard("booking");
    }//GEN-LAST:event_btnNavBookingActionPerformed

    private void btnNavHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavHistoryActionPerformed
        refreshHistory();
        showCard("history");
    }//GEN-LAST:event_btnNavHistoryActionPerformed

    private void btnNavFeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavFeedbackActionPerformed
        refreshCompletedTable();
        showCard("feedback");
    }//GEN-LAST:event_btnNavFeedbackActionPerformed

    private void btnNavProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavProfileActionPerformed
        showCard("profile");
    }//GEN-LAST:event_btnNavProfileActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        dispose();
        PatientLoginFrame login = new PatientLoginFrame();
        login.setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBookSlot;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnFindSlots;
    private javax.swing.JButton btnLogout;
    private javax.swing.JToggleButton btnNavAppointments;
    private javax.swing.JToggleButton btnNavBooking;
    private javax.swing.JToggleButton btnNavDashboard;
    private javax.swing.JToggleButton btnNavFeedback;
    private javax.swing.JToggleButton btnNavHistory;
    private javax.swing.JToggleButton btnNavProfile;
    private javax.swing.JButton btnReschedule;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JButton btnSubmitFeedback;
    private javax.swing.JButton btnViewItems;
    private javax.swing.JPanel cardAppointments;
    private javax.swing.JPanel cardBooking;
    private javax.swing.JPanel cardDashboard;
    private javax.swing.JPanel cardFeedback;
    private javax.swing.JPanel cardHistory;
    private javax.swing.JPanel cardProfile;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblAllergies;
    private javax.swing.JLabel lblApptMessage;
    private javax.swing.JLabel lblApptTitle;
    private javax.swing.JLabel lblActiveRx;
    private javax.swing.JLabel lblBloodType;
    private javax.swing.JLabel lblBookDate;
    private javax.swing.JLabel lblBookingMessage;
    private javax.swing.JLabel lblBookingTitle;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblComment;
    private javax.swing.JLabel lblCurrentPassword;
    private javax.swing.JLabel lblDashTitle;
    private javax.swing.JLabel lblDirectDoctorId;
    private javax.swing.JLabel lblDoctorRating;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFeedbackMessage;
    private javax.swing.JLabel lblFeedbackTitle;
    private javax.swing.JLabel lblHistoryTitle;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNewPassword;
    private javax.swing.JLabel lblPendingLab;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblProfileMessage;
    private javax.swing.JLabel lblProfileTitle;
    private javax.swing.JLabel lblRescheduleDate;
    private javax.swing.JLabel lblRescheduleTime;
    private javax.swing.JLabel lblUpcoming;
    private javax.swing.JLabel lblVisitRating;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JRadioButton radioDirect;
    private javax.swing.JRadioButton radioFromVisit;
    private javax.swing.JScrollPane scrollAppointments;
    private javax.swing.JScrollPane scrollCompleted;
    private javax.swing.JScrollPane scrollDoctors;
    private javax.swing.JScrollPane scrollHistoryText;
    private javax.swing.JScrollPane scrollPrescriptions;
    private javax.swing.JScrollPane scrollSlots;
    private javax.swing.JScrollPane scrollUpcoming;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTable tblCompleted;
    private javax.swing.JTable tblDoctors;
    private javax.swing.JTable tblPrescriptions;
    private javax.swing.JTable tblSlots;
    private javax.swing.JTable tblUpcoming;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAllergies;
    private javax.swing.JTextField txtBloodType;
    private javax.swing.JTextField txtBookDate;
    private javax.swing.JTextField txtComment;
    private javax.swing.JPasswordField txtCurrentPassword;
    private javax.swing.JTextField txtDirectDoctorId;
    private javax.swing.JTextField txtDoctorRating;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextArea txtHistory;
    private javax.swing.JTextField txtName;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtRescheduleDate;
    private javax.swing.JTextField txtRescheduleTime;
    private javax.swing.JTextField txtVisitRating;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        btnNavDashboard = new javax.swing.JToggleButton();
        btnNavAppointments = new javax.swing.JToggleButton();
        btnNavBooking = new javax.swing.JToggleButton();
        btnNavHistory = new javax.swing.JToggleButton();
        btnNavFeedback = new javax.swing.JToggleButton();
        btnNavProfile = new javax.swing.JToggleButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        cardDashboard = new javax.swing.JPanel();
        lblDashTitle = new javax.swing.JLabel();
        lblUpcoming = new javax.swing.JLabel();
        lblActiveRx = new javax.swing.JLabel();
        lblPendingLab = new javax.swing.JLabel();
        scrollUpcoming = new javax.swing.JScrollPane();
        tblUpcoming = new javax.swing.JTable();

        cardAppointments = new javax.swing.JPanel();
        lblApptTitle = new javax.swing.JLabel();
        scrollAppointments = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();
        lblRescheduleDate = new javax.swing.JLabel();
        txtRescheduleDate = new javax.swing.JTextField();
        lblRescheduleTime = new javax.swing.JLabel();
        txtRescheduleTime = new javax.swing.JTextField();
        btnReschedule = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblApptMessage = new javax.swing.JLabel();

        cardBooking = new javax.swing.JPanel();
        lblBookingTitle = new javax.swing.JLabel();
        scrollDoctors = new javax.swing.JScrollPane();
        tblDoctors = new javax.swing.JTable();
        lblBookDate = new javax.swing.JLabel();
        txtBookDate = new javax.swing.JTextField();
        btnFindSlots = new javax.swing.JButton();
        scrollSlots = new javax.swing.JScrollPane();
        tblSlots = new javax.swing.JTable();
        btnBookSlot = new javax.swing.JButton();
        lblBookingMessage = new javax.swing.JLabel();

        cardHistory = new javax.swing.JPanel();
        lblHistoryTitle = new javax.swing.JLabel();
        scrollHistoryText = new javax.swing.JScrollPane();
        txtHistory = new javax.swing.JTextArea();
        scrollPrescriptions = new javax.swing.JScrollPane();
        tblPrescriptions = new javax.swing.JTable();
        btnViewItems = new javax.swing.JButton();

        cardFeedback = new javax.swing.JPanel();
        lblFeedbackTitle = new javax.swing.JLabel();
        radioFromVisit = new javax.swing.JRadioButton();
        scrollCompleted = new javax.swing.JScrollPane();
        tblCompleted = new javax.swing.JTable();
        radioDirect = new javax.swing.JRadioButton();
        lblDirectDoctorId = new javax.swing.JLabel();
        txtDirectDoctorId = new javax.swing.JTextField();
        lblDoctorRating = new javax.swing.JLabel();
        txtDoctorRating = new javax.swing.JTextField();
        lblVisitRating = new javax.swing.JLabel();
        txtVisitRating = new javax.swing.JTextField();
        lblComment = new javax.swing.JLabel();
        txtComment = new javax.swing.JTextField();
        btnSubmitFeedback = new javax.swing.JButton();
        lblFeedbackMessage = new javax.swing.JLabel();

        cardProfile = new javax.swing.JPanel();
        lblProfileTitle = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblPhone = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        lblBloodType = new javax.swing.JLabel();
        txtBloodType = new javax.swing.JTextField();
        lblAllergies = new javax.swing.JLabel();
        txtAllergies = new javax.swing.JTextField();
        btnSaveProfile = new javax.swing.JButton();
        lblCurrentPassword = new javax.swing.JLabel();
        txtCurrentPassword = new javax.swing.JPasswordField();
        lblNewPassword = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        btnChangePassword = new javax.swing.JButton();
        lblProfileMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("APU Medical Centre · Patient Portal");

        pnlSidebar.setBackground(new java.awt.Color(255, 255, 255));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblBrand.setForeground(new java.awt.Color(11, 61, 42));
        lblBrand.setText("⊕  APU Medical");

        btnNavDashboard.setSelected(true);
        btnNavDashboard.setText("Dashboard");
        btnNavDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavDashboard.addActionListener(this::btnNavDashboardActionPerformed);

        btnNavAppointments.setText("My Appointments");
        btnNavAppointments.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavAppointments.addActionListener(this::btnNavAppointmentsActionPerformed);

        btnNavBooking.setText("Book Appointment");
        btnNavBooking.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavBooking.addActionListener(this::btnNavBookingActionPerformed);

        btnNavHistory.setText("Medical History");
        btnNavHistory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavHistory.addActionListener(this::btnNavHistoryActionPerformed);

        btnNavFeedback.setText("Rate a Visit");
        btnNavFeedback.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavFeedback.addActionListener(this::btnNavFeedbackActionPerformed);

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
                    .addComponent(btnNavBooking, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(btnNavBooking, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        javax.swing.ButtonGroup navGroup = new javax.swing.ButtonGroup();
        navGroup.add(btnNavDashboard);
        navGroup.add(btnNavAppointments);
        navGroup.add(btnNavBooking);
        navGroup.add(btnNavHistory);
        navGroup.add(btnNavFeedback);
        navGroup.add(btnNavProfile);

        pnlContent.setLayout(new java.awt.CardLayout());

        // ---- Dashboard card ----
        lblDashTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblDashTitle.setText("Dashboard");
        lblUpcoming.setText("Upcoming appointments: 0");
        lblActiveRx.setText("Active prescriptions: 0");
        lblPendingLab.setText("Pending lab requests: 0");
        scrollUpcoming.setViewportView(tblUpcoming);

        javax.swing.GroupLayout cardDashboardLayout = new javax.swing.GroupLayout(cardDashboard);
        cardDashboard.setLayout(cardDashboardLayout);
        cardDashboardLayout.setHorizontalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDashTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblActiveRx, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPendingLab, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollUpcoming, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardDashboardLayout.setVerticalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblDashTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblActiveRx, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblPendingLab, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(scrollUpcoming, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        pnlContent.add(cardDashboard, "dashboard");

        // ---- My Appointments card ----
        lblApptTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblApptTitle.setText("My Appointments");
        scrollAppointments.setViewportView(tblAppointments);
        lblRescheduleDate.setText("New Date (YYYY-MM-DD), for Reschedule:");
        lblRescheduleTime.setText("New Start Time (HH:MM):");
        btnReschedule.setText("Reschedule Selected");
        btnReschedule.addActionListener(this::btnRescheduleActionPerformed);
        btnCancel.setBackground(new java.awt.Color(194, 74, 79));
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Cancel Selected");
        btnCancel.addActionListener(this::btnCancelActionPerformed);
        lblApptMessage.setText(" ");

        javax.swing.GroupLayout cardAppointmentsLayout = new javax.swing.GroupLayout(cardAppointments);
        cardAppointments.setLayout(cardAppointmentsLayout);
        cardAppointmentsLayout.setHorizontalGroup(
            cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAppointmentsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblApptTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(lblRescheduleDate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRescheduleDate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRescheduleTime, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRescheduleTime, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReschedule, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblApptMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        cardAppointmentsLayout.setVerticalGroup(
            cardAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAppointmentsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblApptTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(lblRescheduleDate, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRescheduleDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblRescheduleTime, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRescheduleTime, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnReschedule, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblApptMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlContent.add(cardAppointments, "appointments");

        // ---- Book Appointment card ----
        lblBookingTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblBookingTitle.setText("Book Appointment");
        scrollDoctors.setViewportView(tblDoctors);
        lblBookDate.setText("Date to check (YYYY-MM-DD):");
        btnFindSlots.setText("Find Open Slots");
        btnFindSlots.addActionListener(this::btnFindSlotsActionPerformed);
        scrollSlots.setViewportView(tblSlots);
        btnBookSlot.setBackground(new java.awt.Color(21, 122, 82));
        btnBookSlot.setForeground(new java.awt.Color(255, 255, 255));
        btnBookSlot.setText("Book Selected Slot");
        btnBookSlot.addActionListener(this::btnBookSlotActionPerformed);
        lblBookingMessage.setText(" ");

        javax.swing.GroupLayout cardBookingLayout = new javax.swing.GroupLayout(cardBooking);
        cardBooking.setLayout(cardBookingLayout);
        cardBookingLayout.setHorizontalGroup(
            cardBookingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBookingLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardBookingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBookingTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollDoctors, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(lblBookDate, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBookDate, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFindSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollSlots, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(btnBookSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBookingMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        cardBookingLayout.setVerticalGroup(
            cardBookingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBookingLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblBookingTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(scrollDoctors, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(lblBookDate, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtBookDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnFindSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(scrollSlots, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnBookSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblBookingMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        pnlContent.add(cardBooking, "booking");

        // ---- Medical History card ----
        lblHistoryTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblHistoryTitle.setText("Medical History");
        txtHistory.setEditable(false);
        txtHistory.setFont(new java.awt.Font("Consolas", 0, 12));
        scrollHistoryText.setViewportView(txtHistory);
        scrollPrescriptions.setViewportView(tblPrescriptions);
        btnViewItems.setText("View Items");
        btnViewItems.addActionListener(this::btnViewItemsActionPerformed);

        javax.swing.GroupLayout cardHistoryLayout = new javax.swing.GroupLayout(cardHistory);
        cardHistory.setLayout(cardHistoryLayout);
        cardHistoryLayout.setHorizontalGroup(
            cardHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardHistoryLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHistoryTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollHistoryText, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(scrollPrescriptions, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(btnViewItems, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        cardHistoryLayout.setVerticalGroup(
            cardHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardHistoryLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblHistoryTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(scrollHistoryText, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(scrollPrescriptions, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnViewItems, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        pnlContent.add(cardHistory, "history");

        // ---- Rate a Visit card ----
        lblFeedbackTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblFeedbackTitle.setText("Rate a Visit");
        radioFromVisit.setSelected(true);
        radioFromVisit.setText("Rate from a completed visit (select a row below)");
        scrollCompleted.setViewportView(tblCompleted);
        radioDirect.setText("Rate a doctor directly (no appointment)");
        lblDirectDoctorId.setText("Doctor ID (only for direct rating):");
        lblDoctorRating.setText("Doctor Rating (1-5):");
        lblVisitRating.setText("Visit Rating (1-5):");
        lblComment.setText("Comment:");
        btnSubmitFeedback.setBackground(new java.awt.Color(21, 122, 82));
        btnSubmitFeedback.setForeground(new java.awt.Color(255, 255, 255));
        btnSubmitFeedback.setText("Submit Feedback");
        btnSubmitFeedback.addActionListener(this::btnSubmitFeedbackActionPerformed);
        lblFeedbackMessage.setText(" ");

        javax.swing.GroupLayout cardFeedbackLayout = new javax.swing.GroupLayout(cardFeedback);
        cardFeedback.setLayout(cardFeedbackLayout);
        cardFeedbackLayout.setHorizontalGroup(
            cardFeedbackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardFeedbackLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardFeedbackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFeedbackTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radioFromVisit, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollCompleted, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                    .addComponent(radioDirect, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDirectDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDirectDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDoctorRating, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDoctorRating, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVisitRating, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtVisitRating, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblComment, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtComment, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSubmitFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFeedbackMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        cardFeedbackLayout.setVerticalGroup(
            cardFeedbackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardFeedbackLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblFeedbackTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(radioFromVisit, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(scrollCompleted, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(radioDirect, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblDirectDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDirectDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblDoctorRating, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDoctorRating, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblVisitRating, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtVisitRating, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblComment, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtComment, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSubmitFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblFeedbackMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlContent.add(cardFeedback, "feedback");

        // ---- My Profile card ----
        lblProfileTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblProfileTitle.setText("My Profile");
        lblName.setText("Full Name");
        lblPhone.setText("Phone");
        lblEmail.setText("Email");
        lblAddress.setText("Address");
        lblBloodType.setText("Blood Type");
        lblAllergies.setText("Allergies");
        btnSaveProfile.setBackground(new java.awt.Color(21, 122, 82));
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save Changes");
        btnSaveProfile.addActionListener(this::btnSaveProfileActionPerformed);
        lblCurrentPassword.setText("Current Password");
        lblNewPassword.setText("New Password (min 6)");
        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(this::btnChangePasswordActionPerformed);
        lblProfileMessage.setText(" ");

        javax.swing.GroupLayout cardProfileLayout = new javax.swing.GroupLayout(cardProfile);
        cardProfile.setLayout(cardProfileLayout);
        cardProfileLayout.setHorizontalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardProfileLayout.setVerticalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblProfileMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                new PatientLoginFrame().setVisible(true);
            }
        });
    }
}
