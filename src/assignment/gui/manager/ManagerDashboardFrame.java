package assignment.gui.manager;

import assignment.model.Department;
import assignment.model.DepartmentStatus;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.MedicalManager;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.ShiftStatus;
import assignment.model.ShiftType;
import assignment.service.DepartmentService;
import assignment.service.ProfileService;
import assignment.service.ReportService;
import assignment.service.RosterService;
import assignment.service.UserService;

import java.time.LocalDate;
import java.util.List;

/**
 * Main window for a logged-in Medical Manager: sidebar + swappable content
 * area (CardLayout). Built as a real NetBeans JFrame Form (GroupLayout, no
 * external library) - open in NetBeans's Design view to drag components
 * around; never hand-edit inside initComponents().
 */
public class ManagerDashboardFrame extends javax.swing.JFrame {

    private final MedicalManager current;
    private final ProfileService profileService = new ProfileService();
    private final DepartmentService departmentService = new DepartmentService();
    private final RosterService rosterService = new RosterService();
    private final ReportService reportService = new ReportService();
    private final UserService userService = new UserService();

    public ManagerDashboardFrame(MedicalManager current) {
        this.current = current;
        initComponents();
        setSize(1440, 810);
        setResizable(false);
        setLocationRelativeTo(null);
        cboDeptStatus.setModel(new javax.swing.DefaultComboBoxModel<>(DepartmentStatus.values()));
        cboShiftType.setModel(new javax.swing.DefaultComboBoxModel<>(ShiftType.values()));
        cboUpdateShiftType.setModel(new javax.swing.DefaultComboBoxModel<>(ShiftType.values()));
        cboUpdateShiftStatus.setModel(new javax.swing.DefaultComboBoxModel<>(ShiftStatus.values()));
        cboProfileGender.setModel(new javax.swing.DefaultComboBoxModel<>(Gender.values()));
        refreshDashboard();
        refreshDepartmentsTable();
        refreshRostersTable();
        loadProfileFields();
    }

    private void showCard(String key) {
        java.awt.CardLayout cl = (java.awt.CardLayout) pnlContent.getLayout();
        cl.show(pnlContent, key);
    }

    // ---------- dashboard ----------
    private void refreshDashboard() {
        int activeDept = 0;
        List<Department> depts = departmentService.getAll();
        for (int i = 0; i < depts.size(); i++) {
            if (depts.get(i).isActive()) activeDept++;
        }
        lblDashDeptCount.setText("Departments: " + depts.size() + " (" + activeDept + " active)");

        List<Doctor> myDoctors = userService.doctorsForManager(current.getId());
        lblDashDoctorCount.setText("Doctors managed: " + myDoctors.size());

        List<ShiftRoster> rosters = rosterService.getAll();
        lblDashRosterCount.setText("Rosters: " + rosters.size());

        int totalShifts = 0;
        for (int i = 0; i < rosters.size(); i++) {
            totalShifts += rosterService.shiftsFor(rosters.get(i).getRosterId()).size();
        }
        lblDashShiftCount.setText("Rostered shifts: " + totalShifts);

        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Roster ID", "Dept", "Week Start", "Week End", "Shifts"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (int i = 0; i < rosters.size(); i++) {
            ShiftRoster r = rosters.get(i);
            model.addRow(new Object[]{r.getRosterId(), r.getDepartmentCode(), r.getWeekStart(),
                    r.getWeekEnd(), rosterService.shiftsFor(r.getRosterId()).size()});
        }
        tblDashRosters.setModel(model);
    }

    // ---------- departments ----------
    private void refreshDepartmentsTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Code", "Name", "Description", "Fee", "Status", "Head Doctor", "Doctors"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        List<Department> depts = departmentService.getAll();
        for (int i = 0; i < depts.size(); i++) {
            Department d = depts.get(i);
            model.addRow(new Object[]{d.getCode(), d.getName(), d.getDescription(), d.getConsultationFee(),
                    d.getStatus(), d.getHeadDoctorId(), d.getDoctorIds().size()});
        }
        tblDepartments.setModel(model);
    }

    private String selectedDeptCode() {
        int row = tblDepartments.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a department in the table first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) tblDepartments.getValueAt(row, 0);
    }

    private void btnCreateDeptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateDeptActionPerformed
        try {
            double fee = txtDeptFee.getText().isBlank() ? 0 : Double.parseDouble(txtDeptFee.getText().trim());
            departmentService.create(txtDeptCode.getText(), txtDeptName.getText(), txtDeptDesc.getText(), fee);
            txtDeptCode.setText("");
            txtDeptName.setText("");
            txtDeptDesc.setText("");
            txtDeptFee.setText("");
            refreshDepartmentsTable();
            refreshDashboard();
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Fee must be a number.", "Invalid input",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCreateDeptActionPerformed

    private void btnAssignDoctorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAssignDoctorActionPerformed
        String code = selectedDeptCode();
        if (code == null) return;
        try {
            departmentService.assignDoctor(code, txtDeptDoctorId.getText());
            refreshDepartmentsTable();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAssignDoctorActionPerformed

    private void btnRemoveDoctorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveDoctorActionPerformed
        String code = selectedDeptCode();
        if (code == null) return;
        try {
            departmentService.removeDoctor(code, txtDeptDoctorId.getText());
            refreshDepartmentsTable();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRemoveDoctorActionPerformed

    private void btnSetHeadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetHeadActionPerformed
        String code = selectedDeptCode();
        if (code == null) return;
        try {
            departmentService.setHead(code, txtDeptDoctorId.getText());
            refreshDepartmentsTable();
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSetHeadActionPerformed

    private void btnUpdateDeptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateDeptActionPerformed
        String code = selectedDeptCode();
        if (code == null) return;
        try {
            Double fee = txtDeptNewFee.getText().isBlank() ? null : Double.parseDouble(txtDeptNewFee.getText().trim());
            DepartmentStatus status = (DepartmentStatus) cboDeptStatus.getSelectedItem();
            departmentService.update(code, null, null, fee, status);
            txtDeptNewFee.setText("");
            refreshDepartmentsTable();
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Fee must be a number.", "Invalid input",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdateDeptActionPerformed

    // ---------- rosters ----------
    private void refreshRostersTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Roster ID", "Dept", "Week Start", "Week End", "Shifts"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        List<ShiftRoster> rosters = rosterService.getAll();
        for (int i = 0; i < rosters.size(); i++) {
            ShiftRoster r = rosters.get(i);
            model.addRow(new Object[]{r.getRosterId(), r.getDepartmentCode(), r.getWeekStart(),
                    r.getWeekEnd(), rosterService.shiftsFor(r.getRosterId()).size()});
        }
        tblRosters.setModel(model);
    }

    private LocalDate parseDateOrWarn(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Use the format YYYY-MM-DD.", "Invalid date",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void btnCreateRosterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateRosterActionPerformed
        LocalDate date = parseDateOrWarn(txtRosterDate.getText());
        if (date == null) return;
        try {
            ShiftRoster r = rosterService.createRoster(txtRosterDept.getText(), date, current.getId());
            txtRosterDept.setText("");
            txtRosterDate.setText("");
            refreshRostersTable();
            refreshDashboard();
            javax.swing.JOptionPane.showMessageDialog(this, "Created " + r.getRosterId()
                    + " for week " + r.getWeekStart() + " to " + r.getWeekEnd());
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCreateRosterActionPerformed

    private void btnViewShiftsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewShiftsActionPerformed
        List<Shift> shifts = rosterService.shiftsFor(txtShiftRosterId.getText().trim());
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Shift ID", "Doctor", "Date", "Type", "Start", "End", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (int i = 0; i < shifts.size(); i++) {
            Shift s = shifts.get(i);
            model.addRow(new Object[]{s.getShiftId(), s.getDoctorId(), s.getDate(), s.getType(),
                    s.getStart(), s.getEnd(), s.getStatus()});
        }
        tblShiftDetail.setModel(model);
    }//GEN-LAST:event_btnViewShiftsActionPerformed

    private void btnAddShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddShiftActionPerformed
        LocalDate date = parseDateOrWarn(txtShiftDate.getText());
        if (date == null) return;
        try {
            ShiftType type = (ShiftType) cboShiftType.getSelectedItem();
            Shift s = rosterService.addShift(txtShiftRosterId.getText(), txtShiftDoctorId.getText(), date, type);
            txtShiftDoctorId.setText("");
            txtShiftDate.setText("");
            refreshRostersTable();
            refreshDashboard();
            javax.swing.JOptionPane.showMessageDialog(this, "Added " + s.getShiftId());
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddShiftActionPerformed

    private void btnUpdateShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateShiftActionPerformed
        String dateText = txtUpdateShiftDate.getText().trim();
        LocalDate date = null;
        if (!dateText.isBlank()) {
            date = parseDateOrWarn(dateText);
            if (date == null) return;
        }
        try {
            ShiftType type = (ShiftType) cboUpdateShiftType.getSelectedItem();
            ShiftStatus status = (ShiftStatus) cboUpdateShiftStatus.getSelectedItem();
            rosterService.updateShift(txtUpdateShiftId.getText(), date, type, null, null, status);
            refreshRostersTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Updated.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdateShiftActionPerformed

    private void btnRemoveShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveShiftActionPerformed
        try {
            rosterService.removeShift(txtUpdateShiftId.getText());
            refreshRostersTable();
            refreshDashboard();
            javax.swing.JOptionPane.showMessageDialog(this, "Removed.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRemoveShiftActionPerformed

    // ---------- reports ----------
    private void btnShowMetricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowMetricsActionPerformed
        txtReport.setText(reportService.hospitalMetricsReport());
    }//GEN-LAST:event_btnShowMetricsActionPerformed

    private void btnShowRevenueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowRevenueActionPerformed
        txtReport.setText(reportService.revenueSummaryReport());
    }//GEN-LAST:event_btnShowRevenueActionPerformed

    private void btnRefreshReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshReportActionPerformed
        if (txtReport.getText().contains("REVENUE SUMMARY")) {
            txtReport.setText(reportService.revenueSummaryReport());
        } else {
            txtReport.setText(reportService.hospitalMetricsReport());
        }
    }//GEN-LAST:event_btnRefreshReportActionPerformed

    // ---------- profile ----------
    private void loadProfileFields() {
        txtProfileName.setText(current.getName());
        txtProfilePhone.setText(current.getPhone());
        txtProfileEmail.setText(current.getEmail());
        txtProfileAddress.setText(current.getAddress());
        cboProfileGender.setSelectedItem(current.getGender());
        txtProfileOffice.setText(current.getOfficeLocation());
    }

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfileActionPerformed
        try {
            Gender gender = (Gender) cboProfileGender.getSelectedItem();
            profileService.updateProfile(current, txtProfileName.getText(), txtProfilePhone.getText(),
                    txtProfileEmail.getText(), txtProfileAddress.getText(), gender, txtProfileOffice.getText());
            lblBrand.setText(lblBrand.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Profile saved.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveProfileActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        boolean ok = profileService.changePassword(current, new String(txtProfileCurrentPw.getPassword()),
                new String(txtProfileNewPw.getPassword()));
        if (ok) {
            javax.swing.JOptionPane.showMessageDialog(this, "Password changed.");
            txtProfileCurrentPw.setText("");
            txtProfileNewPw.setText("");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Current password is incorrect, or new password is invalid.",
                    "Rejected", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    // ---------- sidebar nav ----------
    private void btnNavDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavDashboardActionPerformed
        refreshDashboard();
        showCard("dashboard");
    }//GEN-LAST:event_btnNavDashboardActionPerformed

    private void btnNavDepartmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavDepartmentsActionPerformed
        showCard("departments");
    }//GEN-LAST:event_btnNavDepartmentsActionPerformed

    private void btnNavRostersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavRostersActionPerformed
        showCard("rosters");
    }//GEN-LAST:event_btnNavRostersActionPerformed

    private void btnNavReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavReportsActionPerformed
        showCard("reports");
    }//GEN-LAST:event_btnNavReportsActionPerformed

    private void btnNavProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavProfileActionPerformed
        showCard("profile");
    }//GEN-LAST:event_btnNavProfileActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        dispose();
        ManagerLoginFrame login = new ManagerLoginFrame();
        login.setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddShift;
    private javax.swing.JButton btnAssignDoctor;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnCreateDept;
    private javax.swing.JButton btnCreateRoster;
    private javax.swing.JButton btnLogout;
    private javax.swing.JToggleButton btnNavDashboard;
    private javax.swing.JToggleButton btnNavDepartments;
    private javax.swing.JToggleButton btnNavProfile;
    private javax.swing.JToggleButton btnNavReports;
    private javax.swing.JToggleButton btnNavRosters;
    private javax.swing.JButton btnRefreshReport;
    private javax.swing.JButton btnRemoveDoctor;
    private javax.swing.JButton btnRemoveShift;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JButton btnSetHead;
    private javax.swing.JButton btnShowMetrics;
    private javax.swing.JButton btnShowRevenue;
    private javax.swing.JButton btnUpdateDept;
    private javax.swing.JButton btnUpdateShift;
    private javax.swing.JButton btnViewShifts;
    private javax.swing.JPanel cardDashboard;
    private javax.swing.JPanel cardDepartments;
    private javax.swing.JPanel cardProfile;
    private javax.swing.JPanel cardReports;
    private javax.swing.JPanel cardRosters;
    private javax.swing.JComboBox<DepartmentStatus> cboDeptStatus;
    private javax.swing.JComboBox<Gender> cboProfileGender;
    private javax.swing.JComboBox<ShiftType> cboShiftType;
    private javax.swing.JComboBox<ShiftType> cboUpdateShiftType;
    private javax.swing.JComboBox<ShiftStatus> cboUpdateShiftStatus;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblDashDeptCount;
    private javax.swing.JLabel lblDashDoctorCount;
    private javax.swing.JLabel lblDashRosterCount;
    private javax.swing.JLabel lblDashShiftCount;
    private javax.swing.JLabel lblDashTitle;
    private javax.swing.JLabel lblDeptCode;
    private javax.swing.JLabel lblDeptDesc;
    private javax.swing.JLabel lblDeptFee;
    private javax.swing.JLabel lblDeptName;
    private javax.swing.JLabel lblDeptNewFee;
    private javax.swing.JLabel lblDeptNewStatus;
    private javax.swing.JLabel lblDeptSelDoctor;
    private javax.swing.JLabel lblDeptTitle;
    private javax.swing.JLabel lblProfileAddress;
    private javax.swing.JLabel lblProfileCurrentPw;
    private javax.swing.JLabel lblProfileEmail;
    private javax.swing.JLabel lblProfileGender;
    private javax.swing.JLabel lblProfileName;
    private javax.swing.JLabel lblProfileNewPw;
    private javax.swing.JLabel lblProfileOffice;
    private javax.swing.JLabel lblProfilePhone;
    private javax.swing.JLabel lblProfileTitle;
    private javax.swing.JLabel lblReportsTitle;
    private javax.swing.JLabel lblRosterDate;
    private javax.swing.JLabel lblRosterDept;
    private javax.swing.JLabel lblRosterTitle;
    private javax.swing.JLabel lblShiftDate;
    private javax.swing.JLabel lblShiftDoctor;
    private javax.swing.JLabel lblShiftRosterId;
    private javax.swing.JLabel lblShiftType;
    private javax.swing.JLabel lblUpdateShiftDate;
    private javax.swing.JLabel lblUpdateShiftId;
    private javax.swing.JLabel lblUpdateShiftStatus;
    private javax.swing.JLabel lblUpdateShiftType;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlDeptForm;
    private javax.swing.JPanel pnlRosterForm;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrollDashRosters;
    private javax.swing.JScrollPane scrollDeptForm;
    private javax.swing.JScrollPane scrollDepartments;
    private javax.swing.JScrollPane scrollReport;
    private javax.swing.JScrollPane scrollRosterForm;
    private javax.swing.JScrollPane scrollRosters;
    private javax.swing.JScrollPane scrollShiftDetail;
    private javax.swing.JTable tblDashRosters;
    private javax.swing.JTable tblDepartments;
    private javax.swing.JTable tblRosters;
    private javax.swing.JTable tblShiftDetail;
    private javax.swing.JTextField txtDeptCode;
    private javax.swing.JTextField txtDeptDesc;
    private javax.swing.JTextField txtDeptDoctorId;
    private javax.swing.JTextField txtDeptFee;
    private javax.swing.JTextField txtDeptName;
    private javax.swing.JTextField txtDeptNewFee;
    private javax.swing.JPasswordField txtProfileCurrentPw;
    private javax.swing.JTextField txtProfileAddress;
    private javax.swing.JTextField txtProfileEmail;
    private javax.swing.JTextField txtProfileName;
    private javax.swing.JPasswordField txtProfileNewPw;
    private javax.swing.JTextField txtProfileOffice;
    private javax.swing.JTextField txtProfilePhone;
    private javax.swing.JTextArea txtReport;
    private javax.swing.JTextField txtRosterDate;
    private javax.swing.JTextField txtRosterDept;
    private javax.swing.JTextField txtShiftDate;
    private javax.swing.JTextField txtShiftDoctorId;
    private javax.swing.JTextField txtShiftRosterId;
    private javax.swing.JTextField txtUpdateShiftDate;
    private javax.swing.JTextField txtUpdateShiftId;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        btnNavDashboard = new javax.swing.JToggleButton();
        btnNavDepartments = new javax.swing.JToggleButton();
        btnNavRosters = new javax.swing.JToggleButton();
        btnNavReports = new javax.swing.JToggleButton();
        btnNavProfile = new javax.swing.JToggleButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        cardDashboard = new javax.swing.JPanel();
        lblDashTitle = new javax.swing.JLabel();
        lblDashDeptCount = new javax.swing.JLabel();
        lblDashDoctorCount = new javax.swing.JLabel();
        lblDashRosterCount = new javax.swing.JLabel();
        lblDashShiftCount = new javax.swing.JLabel();
        scrollDashRosters = new javax.swing.JScrollPane();
        tblDashRosters = new javax.swing.JTable();

        cardDepartments = new javax.swing.JPanel();
        scrollDeptForm = new javax.swing.JScrollPane();
        pnlDeptForm = new javax.swing.JPanel();
        lblDeptTitle = new javax.swing.JLabel();
        lblDeptCode = new javax.swing.JLabel();
        txtDeptCode = new javax.swing.JTextField();
        lblDeptName = new javax.swing.JLabel();
        txtDeptName = new javax.swing.JTextField();
        lblDeptDesc = new javax.swing.JLabel();
        txtDeptDesc = new javax.swing.JTextField();
        lblDeptFee = new javax.swing.JLabel();
        txtDeptFee = new javax.swing.JTextField();
        btnCreateDept = new javax.swing.JButton();
        scrollDepartments = new javax.swing.JScrollPane();
        tblDepartments = new javax.swing.JTable();
        lblDeptSelDoctor = new javax.swing.JLabel();
        txtDeptDoctorId = new javax.swing.JTextField();
        btnAssignDoctor = new javax.swing.JButton();
        btnRemoveDoctor = new javax.swing.JButton();
        btnSetHead = new javax.swing.JButton();
        lblDeptNewFee = new javax.swing.JLabel();
        txtDeptNewFee = new javax.swing.JTextField();
        lblDeptNewStatus = new javax.swing.JLabel();
        cboDeptStatus = new javax.swing.JComboBox<>();
        btnUpdateDept = new javax.swing.JButton();

        cardRosters = new javax.swing.JPanel();
        scrollRosterForm = new javax.swing.JScrollPane();
        pnlRosterForm = new javax.swing.JPanel();
        lblRosterTitle = new javax.swing.JLabel();
        lblRosterDept = new javax.swing.JLabel();
        txtRosterDept = new javax.swing.JTextField();
        lblRosterDate = new javax.swing.JLabel();
        txtRosterDate = new javax.swing.JTextField();
        btnCreateRoster = new javax.swing.JButton();
        scrollRosters = new javax.swing.JScrollPane();
        tblRosters = new javax.swing.JTable();
        lblShiftRosterId = new javax.swing.JLabel();
        txtShiftRosterId = new javax.swing.JTextField();
        btnViewShifts = new javax.swing.JButton();
        scrollShiftDetail = new javax.swing.JScrollPane();
        tblShiftDetail = new javax.swing.JTable();
        lblShiftDoctor = new javax.swing.JLabel();
        txtShiftDoctorId = new javax.swing.JTextField();
        lblShiftDate = new javax.swing.JLabel();
        txtShiftDate = new javax.swing.JTextField();
        lblShiftType = new javax.swing.JLabel();
        cboShiftType = new javax.swing.JComboBox<>();
        btnAddShift = new javax.swing.JButton();
        lblUpdateShiftId = new javax.swing.JLabel();
        txtUpdateShiftId = new javax.swing.JTextField();
        lblUpdateShiftDate = new javax.swing.JLabel();
        txtUpdateShiftDate = new javax.swing.JTextField();
        lblUpdateShiftType = new javax.swing.JLabel();
        cboUpdateShiftType = new javax.swing.JComboBox<>();
        lblUpdateShiftStatus = new javax.swing.JLabel();
        cboUpdateShiftStatus = new javax.swing.JComboBox<>();
        btnUpdateShift = new javax.swing.JButton();
        btnRemoveShift = new javax.swing.JButton();

        cardReports = new javax.swing.JPanel();
        lblReportsTitle = new javax.swing.JLabel();
        btnShowMetrics = new javax.swing.JButton();
        btnShowRevenue = new javax.swing.JButton();
        btnRefreshReport = new javax.swing.JButton();
        scrollReport = new javax.swing.JScrollPane();
        txtReport = new javax.swing.JTextArea();

        cardProfile = new javax.swing.JPanel();
        lblProfileTitle = new javax.swing.JLabel();
        lblProfileName = new javax.swing.JLabel();
        txtProfileName = new javax.swing.JTextField();
        lblProfilePhone = new javax.swing.JLabel();
        txtProfilePhone = new javax.swing.JTextField();
        lblProfileEmail = new javax.swing.JLabel();
        txtProfileEmail = new javax.swing.JTextField();
        lblProfileAddress = new javax.swing.JLabel();
        txtProfileAddress = new javax.swing.JTextField();
        lblProfileGender = new javax.swing.JLabel();
        cboProfileGender = new javax.swing.JComboBox<>();
        lblProfileOffice = new javax.swing.JLabel();
        txtProfileOffice = new javax.swing.JTextField();
        btnSaveProfile = new javax.swing.JButton();
        lblProfileCurrentPw = new javax.swing.JLabel();
        txtProfileCurrentPw = new javax.swing.JPasswordField();
        lblProfileNewPw = new javax.swing.JLabel();
        txtProfileNewPw = new javax.swing.JPasswordField();
        btnChangePassword = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("APU Medical Centre · Medical Manager Dashboard");

        // ---- sidebar ----
        pnlSidebar.setBackground(new java.awt.Color(255, 255, 255));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblBrand.setForeground(new java.awt.Color(11, 61, 42));
        lblBrand.setText("⊕  APU Medical");

        btnNavDashboard.setSelected(true);
        btnNavDashboard.setText("Dashboard");
        btnNavDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavDashboard.addActionListener(this::btnNavDashboardActionPerformed);

        btnNavDepartments.setText("Departments");
        btnNavDepartments.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavDepartments.addActionListener(this::btnNavDepartmentsActionPerformed);

        btnNavRosters.setText("Shift Rosters");
        btnNavRosters.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavRosters.addActionListener(this::btnNavRostersActionPerformed);

        btnNavReports.setText("Reports");
        btnNavReports.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavReports.addActionListener(this::btnNavReportsActionPerformed);

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
                    .addComponent(btnNavDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavRosters, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavReports, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(btnNavDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavRosters, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavReports, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        javax.swing.ButtonGroup navGroup = new javax.swing.ButtonGroup();
        navGroup.add(btnNavDashboard);
        navGroup.add(btnNavDepartments);
        navGroup.add(btnNavRosters);
        navGroup.add(btnNavReports);
        navGroup.add(btnNavProfile);

        pnlContent.setLayout(new java.awt.CardLayout());

        // ---- dashboard card ----
        lblDashTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblDashTitle.setText("Dashboard");
        lblDashDeptCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDashDeptCount.setText("Departments: -");
        lblDashDoctorCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDashDoctorCount.setText("Doctors managed: -");
        lblDashRosterCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDashRosterCount.setText("Rosters: -");
        lblDashShiftCount.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDashShiftCount.setText("Rostered shifts: -");
        scrollDashRosters.setViewportView(tblDashRosters);

        javax.swing.GroupLayout cardDashboardLayout = new javax.swing.GroupLayout(cardDashboard);
        cardDashboard.setLayout(cardDashboardLayout);
        cardDashboardLayout.setHorizontalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDashTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDashDeptCount, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDashDoctorCount, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDashRosterCount, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDashShiftCount, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollDashRosters, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE))
                .addContainerGap())
        );
        cardDashboardLayout.setVerticalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblDashTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblDashDeptCount, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblDashDoctorCount, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblDashRosterCount, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblDashShiftCount, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollDashRosters, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlContent.add(cardDashboard, "dashboard");

        // ---- departments card ----
        lblDeptTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblDeptTitle.setText("Manage Clinical Departments");
        lblDeptCode.setText("Code (e.g. CARD)");
        lblDeptName.setText("Name (e.g. Cardiology)");
        lblDeptDesc.setText("Description");
        lblDeptFee.setText("Consultation fee (RM)");
        btnCreateDept.setBackground(new java.awt.Color(21, 122, 82));
        btnCreateDept.setForeground(new java.awt.Color(255, 255, 255));
        btnCreateDept.setText("+ Create Department");
        btnCreateDept.addActionListener(this::btnCreateDeptActionPerformed);
        scrollDepartments.setViewportView(tblDepartments);
        lblDeptSelDoctor.setText("Doctor ID (for actions below, acting on the selected department row)");
        btnAssignDoctor.setText("Assign Doctor to Selected Dept");
        btnAssignDoctor.addActionListener(this::btnAssignDoctorActionPerformed);
        btnRemoveDoctor.setText("Remove Doctor from Selected Dept");
        btnRemoveDoctor.addActionListener(this::btnRemoveDoctorActionPerformed);
        btnSetHead.setText("Set Doctor as Head of Selected Dept");
        btnSetHead.addActionListener(this::btnSetHeadActionPerformed);
        lblDeptNewFee.setText("Update selected department - new fee (blank=keep)");
        lblDeptNewStatus.setText("New status");
        btnUpdateDept.setText("Update Selected Department");
        btnUpdateDept.addActionListener(this::btnUpdateDeptActionPerformed);

        javax.swing.GroupLayout pnlDeptFormLayout = new javax.swing.GroupLayout(pnlDeptForm);
        pnlDeptForm.setLayout(pnlDeptFormLayout);
        pnlDeptFormLayout.setHorizontalGroup(
            pnlDeptFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDeptFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlDeptFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDeptTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptName, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptName, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptFee, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptFee, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreateDept, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollDepartments, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
                    .addComponent(lblDeptSelDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAssignDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetHead, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptNewFee, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptNewFee, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptNewStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboDeptStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateDept, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDeptFormLayout.setVerticalGroup(
            pnlDeptFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDeptFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblDeptTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDeptName, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptName, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDeptDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDeptFee, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptFee, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCreateDept, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollDepartments, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblDeptSelDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnAssignDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnRemoveDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnSetHead, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblDeptNewFee, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptNewFee, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDeptNewStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboDeptStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnUpdateDept, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        scrollDeptForm.setViewportView(pnlDeptForm);

        javax.swing.GroupLayout cardDepartmentsLayout = new javax.swing.GroupLayout(cardDepartments);
        cardDepartments.setLayout(cardDepartmentsLayout);
        cardDepartmentsLayout.setHorizontalGroup(
            cardDepartmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDepartmentsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(scrollDeptForm, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
        cardDepartmentsLayout.setVerticalGroup(
            cardDepartmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDepartmentsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollDeptForm, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        pnlContent.add(cardDepartments, "departments");

        // ---- rosters card ----
        lblRosterTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblRosterTitle.setText("Shift Rosters");
        lblRosterDept.setText("Dept code");
        lblRosterDate.setText("Any date in target week (YYYY-MM-DD)");
        btnCreateRoster.setBackground(new java.awt.Color(21, 122, 82));
        btnCreateRoster.setForeground(new java.awt.Color(255, 255, 255));
        btnCreateRoster.setText("+ Create Weekly Roster");
        btnCreateRoster.addActionListener(this::btnCreateRosterActionPerformed);
        scrollRosters.setViewportView(tblRosters);
        lblShiftRosterId.setText("Roster ID (for all shift actions below)");
        btnViewShifts.setText("View Shifts of Roster");
        btnViewShifts.addActionListener(this::btnViewShiftsActionPerformed);
        scrollShiftDetail.setViewportView(tblShiftDetail);
        lblShiftDoctor.setText("Add shift - Doctor ID");
        lblShiftDate.setText("Shift date (YYYY-MM-DD)");
        lblShiftType.setText("Type");
        btnAddShift.setText("Add Shift");
        btnAddShift.addActionListener(this::btnAddShiftActionPerformed);
        lblUpdateShiftId.setText("Update/remove shift - Shift ID");
        lblUpdateShiftDate.setText("New date (blank=keep)");
        lblUpdateShiftType.setText("New type (optional)");
        lblUpdateShiftStatus.setText("New status (optional)");
        btnUpdateShift.setText("Update Shift");
        btnUpdateShift.addActionListener(this::btnUpdateShiftActionPerformed);
        btnRemoveShift.setBackground(new java.awt.Color(194, 74, 79));
        btnRemoveShift.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveShift.setText("Remove Shift");
        btnRemoveShift.addActionListener(this::btnRemoveShiftActionPerformed);

        javax.swing.GroupLayout pnlRosterFormLayout = new javax.swing.GroupLayout(pnlRosterForm);
        pnlRosterForm.setLayout(pnlRosterFormLayout);
        pnlRosterFormLayout.setHorizontalGroup(
            pnlRosterFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRosterFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlRosterFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRosterTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRosterDept, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRosterDept, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRosterDate, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRosterDate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCreateRoster, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollRosters, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
                    .addComponent(lblShiftRosterId, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtShiftRosterId, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewShifts, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollShiftDetail, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
                    .addComponent(lblShiftDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtShiftDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddShift, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpdateShiftId, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUpdateShiftId, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpdateShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUpdateShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpdateShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboUpdateShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpdateShiftStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboUpdateShiftStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateShift, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveShift, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlRosterFormLayout.setVerticalGroup(
            pnlRosterFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRosterFormLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblRosterTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblRosterDept, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRosterDept, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblRosterDate, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtRosterDate, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnCreateRoster, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollRosters, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblShiftRosterId, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtShiftRosterId, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnViewShifts, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollShiftDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblShiftDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtShiftDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnAddShift, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblUpdateShiftId, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtUpdateShiftId, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblUpdateShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtUpdateShiftDate, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblUpdateShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboUpdateShiftType, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblUpdateShiftStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboUpdateShiftStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(btnUpdateShift, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnRemoveShift, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        scrollRosterForm.setViewportView(pnlRosterForm);

        javax.swing.GroupLayout cardRostersLayout = new javax.swing.GroupLayout(cardRosters);
        cardRosters.setLayout(cardRostersLayout);
        cardRostersLayout.setHorizontalGroup(
            cardRostersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardRostersLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(scrollRosterForm, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
        cardRostersLayout.setVerticalGroup(
            cardRostersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardRostersLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scrollRosterForm, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
        pnlContent.add(cardRosters, "rosters");

        // ---- reports card ----
        lblReportsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblReportsTitle.setText("Reports");
        btnShowMetrics.setText("Hospital Metrics Report");
        btnShowMetrics.addActionListener(this::btnShowMetricsActionPerformed);
        btnShowRevenue.setText("Revenue Summary Report");
        btnShowRevenue.addActionListener(this::btnShowRevenueActionPerformed);
        btnRefreshReport.setText("Refresh");
        btnRefreshReport.addActionListener(this::btnRefreshReportActionPerformed);
        txtReport.setEditable(false);
        txtReport.setFont(new java.awt.Font("Consolas", 0, 12));
        scrollReport.setViewportView(txtReport);

        javax.swing.GroupLayout cardReportsLayout = new javax.swing.GroupLayout(cardReports);
        cardReports.setLayout(cardReportsLayout);
        cardReportsLayout.setHorizontalGroup(
            cardReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardReportsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblReportsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowRevenue, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefreshReport, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollReport, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE))
                .addContainerGap())
        );
        cardReportsLayout.setVerticalGroup(
            cardReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardReportsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblReportsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnShowMetrics, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnShowRevenue, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btnRefreshReport, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollReport, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlContent.add(cardReports, "reports");

        // ---- profile card ----
        lblProfileTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblProfileTitle.setText("My Profile");
        lblProfileName.setText("Full name");
        lblProfilePhone.setText("Phone");
        lblProfileEmail.setText("Email");
        lblProfileAddress.setText("Address");
        lblProfileGender.setText("Gender");
        lblProfileOffice.setText("Office location");
        btnSaveProfile.setBackground(new java.awt.Color(21, 122, 82));
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save Changes");
        btnSaveProfile.addActionListener(this::btnSaveProfileActionPerformed);
        lblProfileCurrentPw.setText("Current password");
        lblProfileNewPw.setText("New password (min 6)");
        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(this::btnChangePasswordActionPerformed);

        javax.swing.GroupLayout cardProfileLayout = new javax.swing.GroupLayout(cardProfile);
        cardProfile.setLayout(cardProfileLayout);
        cardProfileLayout.setHorizontalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileGender, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboProfileGender, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileCurrentPw, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileCurrentPw, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileNewPw, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileNewPw, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardProfileLayout.setVerticalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfileGender, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboProfileGender, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfileOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileOffice, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblProfileCurrentPw, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileCurrentPw, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblProfileNewPw, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileNewPw, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        assignment.service.Database.loadAll();
        assignment.service.SeedData.ensure();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ManagerLoginFrame().setVisible(true);
            }
        });
    }
}
