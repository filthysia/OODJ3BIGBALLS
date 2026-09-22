package assignment.gui;

/**
 * Main window for a logged-in Admin Staff user: sidebar + swappable content
 * area (CardLayout). Built as a real NetBeans JFrame Form (GroupLayout, no
 * external library) - open this in NetBeans's Design view to drag
 * components around; never hand-edit inside initComponents().
 */
public class AdminDashboardFrame extends javax.swing.JFrame {

    private final assignment.model.AdminStaff current;
    private final assignment.service.UserService userService = new assignment.service.UserService();
    private final assignment.service.AccountService accountService = new assignment.service.AccountService();
    private final assignment.service.AssetService assetService = new assignment.service.AssetService();
    private final assignment.service.BillingConfigService billingConfigService = new assignment.service.BillingConfigService();
    private final assignment.service.LabRequestService labRequestService = new assignment.service.LabRequestService();

    /**
     * Creates new form AdminDashboardFrame
     */
    public AdminDashboardFrame(assignment.model.AdminStaff current) {
        this.current = current;
        initComponents();
        setSize(1440, 810);
        setResizable(false);
        setLocationRelativeTo(null);
        populateStaticCombos();
        refreshUsersTable();
        refreshAssignScreen();
        refreshAssetsTable();
        refreshNetworksTable();
        refreshLabTable();
        loadProfileFields();
    }

    private void populateStaticCombos() {
        for (assignment.model.AssetType t : assignment.model.AssetType.values()) cboAssetType.addItem(t);
        for (assignment.model.AssetStatus s : assignment.model.AssetStatus.values()) cboAssetStatus.addItem(s);
        cboRole.addItem("Admin Staff");
        cboRole.addItem("Medical Manager");
        cboRole.addItem("Doctor");
        cboRole.addItem("Patient");
        for (assignment.model.Gender g : assignment.model.Gender.values()) cboNewGender.addItem(g);
    }

    private void refreshAssignScreen() {
        cboAssignDoctor.removeAllItems();
        for (assignment.model.Doctor d : assignment.service.Database.doctors) {
            cboAssignDoctor.addItem(d.getId() + " - " + d.getName());
        }
        cboAssignManager.removeAllItems();
        for (assignment.model.MedicalManager m : assignment.service.Database.managers) {
            cboAssignManager.addItem(m.getId() + " - " + m.getName());
        }
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Doctor ID", "Name", "Department", "Current Manager"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (assignment.model.Doctor d : assignment.service.Database.doctors) {
            String mgr = (d.getManagerId() == null) ? "(none)" : d.getManagerId();
            model.addRow(new Object[]{d.getId(), d.getName(), d.getDepartmentCode(), mgr});
        }
        tblAssign.setModel(model);
    }

    private void refreshAssetsTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Asset ID", "Name", "Type", "Location", "Capacity", "Status", "Department"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        java.util.List<assignment.model.HospitalAsset> assets = assetService.getAll();
        for (int i = 0; i < assets.size(); i++) {
            assignment.model.HospitalAsset a = assets.get(i);
            model.addRow(new Object[]{a.getAssetId(), a.getName(), a.getType(), a.getLocation(),
                    a.getCapacity(), a.getStatus(), a.getDepartmentCode()});
        }
        tblAssets.setModel(model);
    }

    private void refreshNetworksTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Code", "Name", "Coverage %", "Active"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        java.util.List<assignment.model.InsuranceNetwork> networks = billingConfigService.networks();
        for (int i = 0; i < networks.size(); i++) {
            assignment.model.InsuranceNetwork n = networks.get(i);
            model.addRow(new Object[]{n.getCode(), n.getName(), n.getCoveragePercent(), n.isActive()});
        }
        tblNetworks.setModel(model);
        assignment.model.ClinicConfig cfg = billingConfigService.getConfig();
        txtBaseRate.setText(String.valueOf(cfg.getBaseConsultationRate()));
        txtFollowUpRate.setText(String.valueOf(cfg.getFollowUpRate()));
        txtCurrency.setText(cfg.getCurrency());
    }

    private void refreshLabTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"Request ID", "Type", "Requested", "Status", "Asset", "Result"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        java.util.List<assignment.model.LabRequest> requests = assignment.service.Database.labRequests;
        for (int i = 0; i < requests.size(); i++) {
            assignment.model.LabRequest r = requests.get(i);
            model.addRow(new Object[]{r.getRequestId(), r.getType(), r.getRequestedDate(),
                    r.getStatus(), r.getAssignedAssetId(), r.getResultNotes()});
        }
        tblLab.setModel(model);
    }

    private void loadProfileFields() {
        txtProfileName.setText(current.getName());
        txtProfilePhone.setText(current.getPhone());
        txtProfileEmail.setText(current.getEmail());
        txtProfileAddress.setText(current.getAddress());
    }

    private void showCard(String key) {
        java.awt.CardLayout cl = (java.awt.CardLayout) pnlContent.getLayout();
        cl.show(pnlContent, key);
    }

    private void refreshUsersTable() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[]{"ID", "Name", "Role", "Phone", "Email"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        java.util.List<assignment.model.Person> users = userService.allUsers();
        for (int i = 0; i < users.size(); i++) {
            assignment.model.Person p = users.get(i);
            model.addRow(new Object[]{p.getId(), p.getName(), p.getRole(), p.getPhone(), p.getEmail()});
        }
        tblUsers.setModel(model);
    }

    private void btnDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteUserActionPerformed
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a user in the table first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tblUsers.getValueAt(row, 0);
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Delete user " + id + "? This cannot be undone.", "Confirm delete",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                userService.deleteUser(id);
                refreshUsersTable();
            } catch (RuntimeException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteUserActionPerformed

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        showCard("createUser");
    }//GEN-LAST:event_btnAddUserActionPerformed

    private String firstToken(String comboText) {
        if (comboText == null) return "";
        int dash = comboText.indexOf(" - ");
        return dash < 0 ? comboText.trim() : comboText.substring(0, dash).trim();
    }

    private void btnAssignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAssignActionPerformed
        Object doctorItem = cboAssignDoctor.getSelectedItem();
        Object managerItem = cboAssignManager.getSelectedItem();
        if (doctorItem == null || managerItem == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pick a doctor and a manager first.",
                    "Missing selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            userService.assignDoctorToManager(firstToken((String) doctorItem), firstToken((String) managerItem));
            refreshAssignScreen();
            javax.swing.JOptionPane.showMessageDialog(this, "Assigned.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAssignActionPerformed

    private String selectedAssetId() {
        int row = tblAssets.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select an asset in the table first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) tblAssets.getValueAt(row, 0);
    }

    private void btnAddAssetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAssetActionPerformed
        try {
            String capText = txtAssetCapacity.getText().trim();
            int capacity = capText.isBlank() ? 0 : Integer.parseInt(capText);
            assetService.create(txtAssetName.getText(), (assignment.model.AssetType) cboAssetType.getSelectedItem(),
                    txtAssetLocation.getText(), capacity);
            refreshAssetsTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Asset added.");
        } catch (NumberFormatException nfe) {
            javax.swing.JOptionPane.showMessageDialog(this, "Capacity must be a whole number.", "Invalid capacity", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddAssetActionPerformed

    private void btnUpdateAssetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateAssetActionPerformed
        String id = selectedAssetId();
        if (id == null) return;
        try {
            String name = txtAssetName.getText().isBlank() ? null : txtAssetName.getText();
            String location = txtAssetLocation.getText().isBlank() ? null : txtAssetLocation.getText();
            String capText = txtAssetCapacity.getText().trim();
            Integer capacity = capText.isBlank() ? null : Integer.valueOf(capText);
            assignment.model.AssetStatus status = (assignment.model.AssetStatus) cboAssetStatus.getSelectedItem();
            assetService.update(id, name, location, capacity, status);
            refreshAssetsTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Updated.");
        } catch (NumberFormatException nfe) {
            javax.swing.JOptionPane.showMessageDialog(this, "Capacity must be a whole number.", "Invalid capacity", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdateAssetActionPerformed

    private void btnAllocateAssetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAllocateAssetActionPerformed
        String id = selectedAssetId();
        if (id == null) return;
        try {
            assetService.allocate(id, txtAssetDept.getText());
            refreshAssetsTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Allocated.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAllocateAssetActionPerformed

    private void btnReleaseAssetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReleaseAssetActionPerformed
        String id = selectedAssetId();
        if (id == null) return;
        try {
            assetService.release(id);
            refreshAssetsTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Released.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnReleaseAssetActionPerformed

    private void btnDeleteAssetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteAssetActionPerformed
        String id = selectedAssetId();
        if (id == null) return;
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Delete asset " + id + "?",
                "Confirm delete", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                assetService.delete(id);
                refreshAssetsTable();
            } catch (RuntimeException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteAssetActionPerformed

    private void btnSaveRatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveRatesActionPerformed
        try {
            billingConfigService.setBaseConsultationRate(Double.parseDouble(txtBaseRate.getText().trim()));
            billingConfigService.setFollowUpRate(Double.parseDouble(txtFollowUpRate.getText().trim()));
            billingConfigService.setCurrency(txtCurrency.getText());
            refreshNetworksTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Rates saved.");
        } catch (NumberFormatException nfe) {
            javax.swing.JOptionPane.showMessageDialog(this, "Rates must be numbers.", "Invalid input", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveRatesActionPerformed

    private void btnAddNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNetworkActionPerformed
        try {
            double coverage = Double.parseDouble(txtNetworkCoverage.getText().trim());
            billingConfigService.addNetwork(txtNetworkCode.getText(), txtNetworkCode.getText(), coverage);
            txtNetworkCode.setText("");
            txtNetworkCoverage.setText("");
            refreshNetworksTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Network added.");
        } catch (NumberFormatException nfe) {
            javax.swing.JOptionPane.showMessageDialog(this, "Coverage % must be a number.", "Invalid input", javax.swing.JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddNetworkActionPerformed

    private String selectedLabRequestId() {
        int row = tblLab.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a request in the table first.",
                    "No selection", javax.swing.JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (String) tblLab.getValueAt(row, 0);
    }

    private void btnScheduleLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScheduleLabActionPerformed
        String id = selectedLabRequestId();
        if (id == null) return;
        try {
            labRequestService.schedule(id, txtLabAssetId.getText());
            refreshLabTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Scheduled.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnScheduleLabActionPerformed

    private void btnCompleteLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteLabActionPerformed
        String id = selectedLabRequestId();
        if (id == null) return;
        try {
            labRequestService.complete(id, txtLabResultNotes.getText());
            refreshLabTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Completed.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCompleteLabActionPerformed

    private void btnCancelLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelLabActionPerformed
        String id = selectedLabRequestId();
        if (id == null) return;
        try {
            labRequestService.cancel(id);
            refreshLabTable();
            javax.swing.JOptionPane.showMessageDialog(this, "Cancelled.");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCancelLabActionPerformed

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProfileActionPerformed
        try {
            accountService.editProfile(current, txtProfileName.getText(), txtProfilePhone.getText(),
                    txtProfileEmail.getText(), txtProfileAddress.getText(), null);
            javax.swing.JOptionPane.showMessageDialog(this, "Profile saved.");
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
            javax.swing.JOptionPane.showMessageDialog(this, "Current password is incorrect, or new password is invalid.",
                    "Rejected", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    private void btnCreateUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateUserActionPerformed
        try {
            String role = (String) cboRole.getSelectedItem();
            String name = txtNewName.getText();
            String ic = txtNewIc.getText();
            assignment.model.Gender gender = (assignment.model.Gender) cboNewGender.getSelectedItem();
            String phone = txtNewPhone.getText();
            String email = txtNewEmail.getText();
            String address = txtNewAddress.getText();
            String password = new String(txtCreateUserPassword.getPassword());
            assignment.model.Person created;
            if ("Admin Staff".equals(role)) {
                created = userService.createAdminStaff(name, ic, gender, phone, email, address, password, txtDeskLocation.getText());
            } else if ("Medical Manager".equals(role)) {
                created = userService.createMedicalManager(name, ic, gender, phone, email, address, password, txtOfficeLocation.getText());
            } else if ("Doctor".equals(role)) {
                created = userService.createDoctor(name, ic, gender, phone, email, address, password,
                        txtDeptCode.getText(), txtSpecialization.getText(), txtManagerId.getText());
            } else {
                created = userService.createPatient(name, ic, gender, phone, email, address, password,
                        txtBloodType.getText(), txtAllergies.getText());
            }
            javax.swing.JOptionPane.showMessageDialog(this, "Created " + created.getId());
            clearCreateUserForm();
            refreshUsersTable();
            showCard("users");
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not create user", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnCreateUserActionPerformed

    private void btnCancelCreateUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelCreateUserActionPerformed
        clearCreateUserForm();
        showCard("users");
    }//GEN-LAST:event_btnCancelCreateUserActionPerformed

    private void clearCreateUserForm() {
        txtNewName.setText("");
        txtNewIc.setText("");
        txtNewPhone.setText("");
        txtNewEmail.setText("");
        txtNewAddress.setText("");
        txtCreateUserPassword.setText("");
        txtDeskLocation.setText("");
        txtOfficeLocation.setText("");
        txtDeptCode.setText("");
        txtSpecialization.setText("");
        txtManagerId.setText("");
        txtBloodType.setText("");
        txtAllergies.setText("");
    }

    private void btnNavDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavDashboardActionPerformed
        showCard("dashboard");
    }//GEN-LAST:event_btnNavDashboardActionPerformed

    private void btnNavUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavUsersActionPerformed
        showCard("users");
    }//GEN-LAST:event_btnNavUsersActionPerformed

    private void btnNavAssignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAssignActionPerformed
        showCard("assign");
    }//GEN-LAST:event_btnNavAssignActionPerformed

    private void btnNavAssetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAssetsActionPerformed
        showCard("assets");
    }//GEN-LAST:event_btnNavAssetsActionPerformed

    private void btnNavBillingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavBillingActionPerformed
        showCard("billing");
    }//GEN-LAST:event_btnNavBillingActionPerformed

    private void btnNavLabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavLabActionPerformed
        showCard("lab");
    }//GEN-LAST:event_btnNavLabActionPerformed

    private void btnNavProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavProfileActionPerformed
        showCard("profile");
    }//GEN-LAST:event_btnNavProfileActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        dispose();
        AdminLoginFrame login = new AdminLoginFrame();
        login.setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAsset;
    private javax.swing.JButton btnAddNetwork;
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnAllocateAsset;
    private javax.swing.JButton btnAssign;
    private javax.swing.JButton btnCancelCreateUser;
    private javax.swing.JButton btnCancelLab;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnCompleteLab;
    private javax.swing.JButton btnCreateUser;
    private javax.swing.JButton btnDeleteAsset;
    private javax.swing.JButton btnDeleteUser;
    private javax.swing.JButton btnLogout;
    private javax.swing.JToggleButton btnNavAssets;
    private javax.swing.JToggleButton btnNavAssign;
    private javax.swing.JToggleButton btnNavBilling;
    private javax.swing.JToggleButton btnNavDashboard;
    private javax.swing.JToggleButton btnNavLab;
    private javax.swing.JToggleButton btnNavProfile;
    private javax.swing.JToggleButton btnNavUsers;
    private javax.swing.JButton btnReleaseAsset;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JButton btnSaveRates;
    private javax.swing.JButton btnScheduleLab;
    private javax.swing.JButton btnUpdateAsset;
    private javax.swing.JPanel cardAssets;
    private javax.swing.JPanel cardAssign;
    private javax.swing.JPanel cardBilling;
    private javax.swing.JPanel cardCreateUser;
    private javax.swing.JPanel cardDashboard;
    private javax.swing.JPanel cardLab;
    private javax.swing.JPanel cardProfile;
    private javax.swing.JPanel cardUsers;
    private javax.swing.JComboBox<assignment.model.AssetStatus> cboAssetStatus;
    private javax.swing.JComboBox<assignment.model.AssetType> cboAssetType;
    private javax.swing.JComboBox<String> cboAssignDoctor;
    private javax.swing.JComboBox<String> cboAssignManager;
    private javax.swing.JComboBox<assignment.model.Gender> cboNewGender;
    private javax.swing.JComboBox<String> cboRole;
    private javax.swing.JLabel lblAssetsTitle;
    private javax.swing.JLabel lblAssignTitle;
    private javax.swing.JLabel lblAllergies;
    private javax.swing.JLabel lblBaseRate;
    private javax.swing.JLabel lblBillingTitle;
    private javax.swing.JLabel lblBloodType;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblCreateUserPassword;
    private javax.swing.JLabel lblCreateUserTitle;
    private javax.swing.JLabel lblCurrency;
    private javax.swing.JLabel lblCurrentPw;
    private javax.swing.JLabel lblDashboardPlaceholder;
    private javax.swing.JLabel lblDeptCode;
    private javax.swing.JLabel lblDeskLocation;
    private javax.swing.JLabel lblFollowUpRate;
    private javax.swing.JLabel lblLabAssetId;
    private javax.swing.JLabel lblLabResultNotes;
    private javax.swing.JLabel lblLabTitle;
    private javax.swing.JLabel lblManagerId;
    private javax.swing.JLabel lblNetworkCode;
    private javax.swing.JLabel lblNetworkCoverage;
    private javax.swing.JLabel lblNewAddress;
    private javax.swing.JLabel lblNewEmail;
    private javax.swing.JLabel lblNewGender;
    private javax.swing.JLabel lblNewIc;
    private javax.swing.JLabel lblNewName;
    private javax.swing.JLabel lblNewPhone;
    private javax.swing.JLabel lblNewPw;
    private javax.swing.JLabel lblOfficeLocation;
    private javax.swing.JLabel lblProfileAddress;
    private javax.swing.JLabel lblProfileEmail;
    private javax.swing.JLabel lblProfileName;
    private javax.swing.JLabel lblProfilePhone;
    private javax.swing.JLabel lblProfileTitle;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblSpecialization;
    private javax.swing.JLabel lblUsersTitle;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlCreateUserForm;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JScrollPane scrollAssets;
    private javax.swing.JScrollPane scrollAssign;
    private javax.swing.JScrollPane scrollCreateUser;
    private javax.swing.JScrollPane scrollLab;
    private javax.swing.JScrollPane scrollNetworks;
    private javax.swing.JScrollPane scrollUsers;
    private javax.swing.JTable tblAssets;
    private javax.swing.JTable tblAssign;
    private javax.swing.JTable tblLab;
    private javax.swing.JTable tblNetworks;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtAllergies;
    private javax.swing.JTextField txtAssetCapacity;
    private javax.swing.JTextField txtAssetDept;
    private javax.swing.JTextField txtAssetLocation;
    private javax.swing.JTextField txtAssetName;
    private javax.swing.JTextField txtBaseRate;
    private javax.swing.JTextField txtBloodType;
    private javax.swing.JPasswordField txtCreateUserPassword;
    private javax.swing.JPasswordField txtCurrentPassword;
    private javax.swing.JTextField txtCurrency;
    private javax.swing.JTextField txtDeptCode;
    private javax.swing.JTextField txtDeskLocation;
    private javax.swing.JTextField txtFollowUpRate;
    private javax.swing.JTextField txtLabAssetId;
    private javax.swing.JTextField txtLabResultNotes;
    private javax.swing.JTextField txtManagerId;
    private javax.swing.JTextField txtNetworkCode;
    private javax.swing.JTextField txtNetworkCoverage;
    private javax.swing.JTextField txtNewAddress;
    private javax.swing.JTextField txtNewEmail;
    private javax.swing.JTextField txtNewIc;
    private javax.swing.JTextField txtNewName;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JTextField txtNewPhone;
    private javax.swing.JTextField txtOfficeLocation;
    private javax.swing.JTextField txtProfileAddress;
    private javax.swing.JTextField txtProfileEmail;
    private javax.swing.JTextField txtProfileName;
    private javax.swing.JTextField txtProfilePhone;
    private javax.swing.JTextField txtSpecialization;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        btnNavDashboard = new javax.swing.JToggleButton();
        btnNavUsers = new javax.swing.JToggleButton();
        btnNavAssign = new javax.swing.JToggleButton();
        btnNavAssets = new javax.swing.JToggleButton();
        btnNavBilling = new javax.swing.JToggleButton();
        btnNavLab = new javax.swing.JToggleButton();
        btnNavProfile = new javax.swing.JToggleButton();
        btnLogout = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();
        cardDashboard = new javax.swing.JPanel();
        lblDashboardPlaceholder = new javax.swing.JLabel();
        cardUsers = new javax.swing.JPanel();
        lblUsersTitle = new javax.swing.JLabel();
        btnAddUser = new javax.swing.JButton();
        btnDeleteUser = new javax.swing.JButton();
        scrollUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        cardAssign = new javax.swing.JPanel();
        lblAssignTitle = new javax.swing.JLabel();
        cboAssignDoctor = new javax.swing.JComboBox<>();
        cboAssignManager = new javax.swing.JComboBox<>();
        btnAssign = new javax.swing.JButton();
        scrollAssign = new javax.swing.JScrollPane();
        tblAssign = new javax.swing.JTable();
        cardAssets = new javax.swing.JPanel();
        lblAssetsTitle = new javax.swing.JLabel();
        txtAssetName = new javax.swing.JTextField();
        cboAssetType = new javax.swing.JComboBox<>();
        txtAssetLocation = new javax.swing.JTextField();
        txtAssetCapacity = new javax.swing.JTextField();
        cboAssetStatus = new javax.swing.JComboBox<>();
        txtAssetDept = new javax.swing.JTextField();
        btnAddAsset = new javax.swing.JButton();
        btnUpdateAsset = new javax.swing.JButton();
        btnAllocateAsset = new javax.swing.JButton();
        btnReleaseAsset = new javax.swing.JButton();
        btnDeleteAsset = new javax.swing.JButton();
        scrollAssets = new javax.swing.JScrollPane();
        tblAssets = new javax.swing.JTable();
        cardBilling = new javax.swing.JPanel();
        lblBillingTitle = new javax.swing.JLabel();
        lblBaseRate = new javax.swing.JLabel();
        txtBaseRate = new javax.swing.JTextField();
        lblFollowUpRate = new javax.swing.JLabel();
        txtFollowUpRate = new javax.swing.JTextField();
        lblCurrency = new javax.swing.JLabel();
        txtCurrency = new javax.swing.JTextField();
        btnSaveRates = new javax.swing.JButton();
        lblNetworkCode = new javax.swing.JLabel();
        txtNetworkCode = new javax.swing.JTextField();
        lblNetworkCoverage = new javax.swing.JLabel();
        txtNetworkCoverage = new javax.swing.JTextField();
        btnAddNetwork = new javax.swing.JButton();
        scrollNetworks = new javax.swing.JScrollPane();
        tblNetworks = new javax.swing.JTable();
        cardLab = new javax.swing.JPanel();
        lblLabTitle = new javax.swing.JLabel();
        lblLabAssetId = new javax.swing.JLabel();
        txtLabAssetId = new javax.swing.JTextField();
        btnScheduleLab = new javax.swing.JButton();
        lblLabResultNotes = new javax.swing.JLabel();
        txtLabResultNotes = new javax.swing.JTextField();
        btnCompleteLab = new javax.swing.JButton();
        btnCancelLab = new javax.swing.JButton();
        scrollLab = new javax.swing.JScrollPane();
        tblLab = new javax.swing.JTable();
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
        btnSaveProfile = new javax.swing.JButton();
        lblCurrentPw = new javax.swing.JLabel();
        txtCurrentPassword = new javax.swing.JPasswordField();
        lblNewPw = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        btnChangePassword = new javax.swing.JButton();
        cardCreateUser = new javax.swing.JPanel();
        lblCreateUserTitle = new javax.swing.JLabel();
        scrollCreateUser = new javax.swing.JScrollPane();
        pnlCreateUserForm = new javax.swing.JPanel();
        lblRole = new javax.swing.JLabel();
        cboRole = new javax.swing.JComboBox<>();
        lblNewName = new javax.swing.JLabel();
        txtNewName = new javax.swing.JTextField();
        lblNewIc = new javax.swing.JLabel();
        txtNewIc = new javax.swing.JTextField();
        lblNewGender = new javax.swing.JLabel();
        cboNewGender = new javax.swing.JComboBox<>();
        lblNewPhone = new javax.swing.JLabel();
        txtNewPhone = new javax.swing.JTextField();
        lblNewEmail = new javax.swing.JLabel();
        txtNewEmail = new javax.swing.JTextField();
        lblNewAddress = new javax.swing.JLabel();
        txtNewAddress = new javax.swing.JTextField();
        lblCreateUserPassword = new javax.swing.JLabel();
        txtCreateUserPassword = new javax.swing.JPasswordField();
        lblDeskLocation = new javax.swing.JLabel();
        txtDeskLocation = new javax.swing.JTextField();
        lblOfficeLocation = new javax.swing.JLabel();
        txtOfficeLocation = new javax.swing.JTextField();
        lblDeptCode = new javax.swing.JLabel();
        txtDeptCode = new javax.swing.JTextField();
        lblSpecialization = new javax.swing.JLabel();
        txtSpecialization = new javax.swing.JTextField();
        lblManagerId = new javax.swing.JLabel();
        txtManagerId = new javax.swing.JTextField();
        lblBloodType = new javax.swing.JLabel();
        txtBloodType = new javax.swing.JTextField();
        lblAllergies = new javax.swing.JLabel();
        txtAllergies = new javax.swing.JTextField();
        btnCreateUser = new javax.swing.JButton();
        btnCancelCreateUser = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("APU Medical Centre · Admin Dashboard");

        pnlSidebar.setBackground(new java.awt.Color(255, 255, 255));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblBrand.setForeground(new java.awt.Color(11, 61, 42));
        lblBrand.setText("⊕  APU Medical");

        btnNavDashboard.setSelected(true);
        btnNavDashboard.setText("Dashboard");
        btnNavDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavDashboard.addActionListener(this::btnNavDashboardActionPerformed);

        btnNavUsers.setText("Manage Users");
        btnNavUsers.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavUsers.addActionListener(this::btnNavUsersActionPerformed);

        btnNavAssign.setText("Assign Doctors");
        btnNavAssign.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavAssign.addActionListener(this::btnNavAssignActionPerformed);

        btnNavAssets.setText("Hospital Assets");
        btnNavAssets.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavAssets.addActionListener(this::btnNavAssetsActionPerformed);

        btnNavBilling.setText("Billing & Insurance");
        btnNavBilling.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNavBilling.addActionListener(this::btnNavBillingActionPerformed);

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
                    .addComponent(btnNavUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavAssign, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavAssets, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNavBilling, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(btnNavUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavAssign, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavAssets, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnNavBilling, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        navGroup.add(btnNavUsers);
        navGroup.add(btnNavAssign);
        navGroup.add(btnNavAssets);
        navGroup.add(btnNavBilling);
        navGroup.add(btnNavLab);
        navGroup.add(btnNavProfile);

        pnlContent.setLayout(new java.awt.CardLayout());

        lblDashboardPlaceholder.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblDashboardPlaceholder.setText("Dashboard");

        javax.swing.GroupLayout cardDashboardLayout = new javax.swing.GroupLayout(cardDashboard);
        cardDashboard.setLayout(cardDashboardLayout);
        cardDashboardLayout.setHorizontalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lblDashboardPlaceholder, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardDashboardLayout.setVerticalGroup(
            cardDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardDashboardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblDashboardPlaceholder, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlContent.add(cardDashboard, "dashboard");

        lblUsersTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblUsersTitle.setText("Manage End Users");

        btnAddUser.setBackground(new java.awt.Color(21, 122, 82));
        btnAddUser.setForeground(new java.awt.Color(255, 255, 255));
        btnAddUser.setText("+ Create User");
        btnAddUser.addActionListener(this::btnAddUserActionPerformed);

        btnDeleteUser.setBackground(new java.awt.Color(194, 74, 79));
        btnDeleteUser.setForeground(new java.awt.Color(255, 255, 255));
        btnDeleteUser.setText("Delete Selected");
        btnDeleteUser.addActionListener(this::btnDeleteUserActionPerformed);

        scrollUsers.setViewportView(tblUsers);

        javax.swing.GroupLayout cardUsersLayout = new javax.swing.GroupLayout(cardUsers);
        cardUsers.setLayout(cardUsersLayout);
        cardUsersLayout.setHorizontalGroup(
            cardUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardUsersLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUsersTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardUsersLayout.createSequentialGroup()
                        .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnDeleteUser, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardUsersLayout.setVerticalGroup(
            cardUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardUsersLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblUsersTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(cardUsersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(scrollUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardUsers, "users");

        lblAssignTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblAssignTitle.setText("Assign Doctors to Medical Managers");

        btnAssign.setBackground(new java.awt.Color(21, 122, 82));
        btnAssign.setForeground(new java.awt.Color(255, 255, 255));
        btnAssign.setText("Assign");
        btnAssign.addActionListener(this::btnAssignActionPerformed);

        scrollAssign.setViewportView(tblAssign);

        javax.swing.GroupLayout cardAssignLayout = new javax.swing.GroupLayout(cardAssign);
        cardAssign.setLayout(cardAssignLayout);
        cardAssignLayout.setHorizontalGroup(
            cardAssignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAssignLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardAssignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAssignTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardAssignLayout.createSequentialGroup()
                        .addComponent(cboAssignDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cboAssignManager, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnAssign, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollAssign, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardAssignLayout.setVerticalGroup(
            cardAssignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAssignLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblAssignTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(cardAssignLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboAssignDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboAssignManager, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAssign, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(scrollAssign, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardAssign, "assign");

        lblAssetsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblAssetsTitle.setText("Hospital Assets");

        txtAssetName.setToolTipText("Name");

        txtAssetLocation.setToolTipText("Location");

        txtAssetCapacity.setToolTipText("Capacity");

        txtAssetDept.setToolTipText("Dept code (for Allocate)");

        btnAddAsset.setText("+ Add");
        btnAddAsset.addActionListener(this::btnAddAssetActionPerformed);

        btnUpdateAsset.setText("Update");
        btnUpdateAsset.addActionListener(this::btnUpdateAssetActionPerformed);

        btnAllocateAsset.setText("Allocate to Dept");
        btnAllocateAsset.addActionListener(this::btnAllocateAssetActionPerformed);

        btnReleaseAsset.setText("Release");
        btnReleaseAsset.addActionListener(this::btnReleaseAssetActionPerformed);

        btnDeleteAsset.setBackground(new java.awt.Color(194, 74, 79));
        btnDeleteAsset.setForeground(new java.awt.Color(255, 255, 255));
        btnDeleteAsset.setText("Delete");
        btnDeleteAsset.addActionListener(this::btnDeleteAssetActionPerformed);

        scrollAssets.setViewportView(tblAssets);

        javax.swing.GroupLayout cardAssetsLayout = new javax.swing.GroupLayout(cardAssets);
        cardAssets.setLayout(cardAssetsLayout);
        cardAssetsLayout.setHorizontalGroup(
            cardAssetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAssetsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardAssetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAssetsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardAssetsLayout.createSequentialGroup()
                        .addComponent(txtAssetName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(cboAssetType, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtAssetLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtAssetCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(cboAssetStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtAssetDept, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(cardAssetsLayout.createSequentialGroup()
                        .addComponent(btnAddAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnUpdateAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnAllocateAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnReleaseAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(btnDeleteAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollAssets, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardAssetsLayout.setVerticalGroup(
            cardAssetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardAssetsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblAssetsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(cardAssetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtAssetName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboAssetType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAssetLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAssetCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboAssetStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAssetDept, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(cardAssetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdateAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAllocateAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReleaseAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteAsset, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(scrollAssets, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardAssets, "assets");

        lblBillingTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblBillingTitle.setText("Billing Configuration & Insurance Networks");

        lblBaseRate.setText("Base rate:");

        lblFollowUpRate.setText("Follow-up rate:");

        lblCurrency.setText("Currency:");

        btnSaveRates.setBackground(new java.awt.Color(21, 122, 82));
        btnSaveRates.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveRates.setText("Save Rates");
        btnSaveRates.addActionListener(this::btnSaveRatesActionPerformed);

        lblNetworkCode.setText("Network code:");

        lblNetworkCoverage.setText("Coverage %:");

        btnAddNetwork.setBackground(new java.awt.Color(21, 122, 82));
        btnAddNetwork.setForeground(new java.awt.Color(255, 255, 255));
        btnAddNetwork.setText("Add Network");
        btnAddNetwork.addActionListener(this::btnAddNetworkActionPerformed);

        scrollNetworks.setViewportView(tblNetworks);

        javax.swing.GroupLayout cardBillingLayout = new javax.swing.GroupLayout(cardBilling);
        cardBilling.setLayout(cardBillingLayout);
        cardBillingLayout.setHorizontalGroup(
            cardBillingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBillingLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardBillingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBillingTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardBillingLayout.createSequentialGroup()
                        .addComponent(lblBaseRate, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtBaseRate, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(lblFollowUpRate, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtFollowUpRate, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(lblCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnSaveRates, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(cardBillingLayout.createSequentialGroup()
                        .addComponent(lblNetworkCode, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtNetworkCode, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(lblNetworkCoverage, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtNetworkCoverage, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnAddNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollNetworks, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardBillingLayout.setVerticalGroup(
            cardBillingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardBillingLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblBillingTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(cardBillingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBaseRate, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBaseRate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFollowUpRate, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFollowUpRate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveRates, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(cardBillingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNetworkCode, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNetworkCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNetworkCoverage, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNetworkCoverage, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(scrollNetworks, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardBilling, "billing");

        lblLabTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblLabTitle.setText("Lab / Imaging Requests");

        lblLabAssetId.setText("Asset ID:");

        btnScheduleLab.setBackground(new java.awt.Color(21, 122, 82));
        btnScheduleLab.setForeground(new java.awt.Color(255, 255, 255));
        btnScheduleLab.setText("Schedule");
        btnScheduleLab.addActionListener(this::btnScheduleLabActionPerformed);

        lblLabResultNotes.setText("Result notes:");

        btnCompleteLab.setText("Complete / Record");
        btnCompleteLab.addActionListener(this::btnCompleteLabActionPerformed);

        btnCancelLab.setBackground(new java.awt.Color(194, 74, 79));
        btnCancelLab.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelLab.setText("Cancel");
        btnCancelLab.addActionListener(this::btnCancelLabActionPerformed);

        scrollLab.setViewportView(tblLab);

        javax.swing.GroupLayout cardLabLayout = new javax.swing.GroupLayout(cardLab);
        cardLab.setLayout(cardLabLayout);
        cardLabLayout.setHorizontalGroup(
            cardLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardLabLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLabTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cardLabLayout.createSequentialGroup()
                        .addComponent(lblLabAssetId, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtLabAssetId, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnScheduleLab, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(lblLabResultNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(txtLabResultNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnCompleteLab, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnCancelLab, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollLab, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE))
                .addGap(30, 30, 30))
        );
        cardLabLayout.setVerticalGroup(
            cardLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardLabLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblLabTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(cardLabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLabAssetId, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLabAssetId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnScheduleLab, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLabResultNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLabResultNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCompleteLab, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelLab, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(scrollLab, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardLab, "lab");

        lblProfileTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblProfileTitle.setText("My Profile");

        lblProfileName.setText("Full name");

        lblProfilePhone.setText("Phone");

        lblProfileEmail.setText("Email");

        lblProfileAddress.setText("Address");

        btnSaveProfile.setBackground(new java.awt.Color(21, 122, 82));
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save Changes");
        btnSaveProfile.addActionListener(this::btnSaveProfileActionPerformed);

        lblCurrentPw.setText("Current password");

        lblNewPw.setText("New password (min 6)");

        btnChangePassword.setBackground(new java.awt.Color(21, 122, 82));
        btnChangePassword.setForeground(new java.awt.Color(255, 255, 255));
        btnChangePassword.setText("Update Password");
        btnChangePassword.addActionListener(this::btnChangePasswordActionPerformed);

        javax.swing.GroupLayout cardProfileLayout = new javax.swing.GroupLayout(cardProfile);
        cardProfile.setLayout(cardProfileLayout);
        cardProfileLayout.setHorizontalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCurrentPw, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewPw, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardProfileLayout.setVerticalGroup(
            cardProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardProfileLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblProfileTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfilePhone, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtProfileAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblCurrentPw, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblNewPw, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(btnChangePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlContent.add(cardProfile, "profile");

        lblCreateUserTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblCreateUserTitle.setText("Create End User");

        lblRole.setText("Role");

        lblNewName.setText("Full name");

        lblNewIc.setText("IC / Passport");

        lblNewGender.setText("Gender");

        lblNewPhone.setText("Phone");

        lblNewEmail.setText("Email");

        lblNewAddress.setText("Address");

        lblCreateUserPassword.setText("Initial password");

        lblDeskLocation.setText("Desk location (Admin)");

        lblOfficeLocation.setText("Office location (Manager)");

        lblDeptCode.setText("Dept code (Doctor)");

        lblSpecialization.setText("Specialization (Doctor)");

        lblManagerId.setText("Manager ID (Doctor)");

        lblBloodType.setText("Blood type (Patient)");

        lblAllergies.setText("Allergies (Patient)");

        javax.swing.GroupLayout pnlCreateUserFormLayout = new javax.swing.GroupLayout(pnlCreateUserForm);
        pnlCreateUserForm.setLayout(pnlCreateUserFormLayout);
        pnlCreateUserFormLayout.setHorizontalGroup(
            pnlCreateUserFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCreateUserFormLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(pnlCreateUserFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboRole, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewName, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewName, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewIc, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewIc, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewGender, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboNewGender, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCreateUserPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCreateUserPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeskLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeskLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOfficeLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtOfficeLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlCreateUserFormLayout.setVerticalGroup(
            pnlCreateUserFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCreateUserFormLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(lblRole, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboRole, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewName, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewIc, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewIc, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewGender, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(cboNewGender, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblNewAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtNewAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblCreateUserPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtCreateUserPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblDeskLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeskLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblOfficeLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtOfficeLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDeptCode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(lblAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtAllergies, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, Short.MAX_VALUE))
        );

        scrollCreateUser.setViewportView(pnlCreateUserForm);

        btnCreateUser.setBackground(new java.awt.Color(21, 122, 82));
        btnCreateUser.setForeground(new java.awt.Color(255, 255, 255));
        btnCreateUser.setText("Create User");
        btnCreateUser.addActionListener(this::btnCreateUserActionPerformed);

        btnCancelCreateUser.setText("Cancel");
        btnCancelCreateUser.addActionListener(this::btnCancelCreateUserActionPerformed);

        javax.swing.GroupLayout cardCreateUserLayout = new javax.swing.GroupLayout(cardCreateUser);
        cardCreateUser.setLayout(cardCreateUserLayout);
        cardCreateUserLayout.setHorizontalGroup(
            cardCreateUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardCreateUserLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(cardCreateUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCreateUserTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollCreateUser, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
                    .addGroup(cardCreateUserLayout.createSequentialGroup()
                        .addComponent(btnCreateUser, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnCancelCreateUser, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        cardCreateUserLayout.setVerticalGroup(
            cardCreateUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardCreateUserLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblCreateUserTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(scrollCreateUser, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addGroup(cardCreateUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCreateUser, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelCreateUser, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        pnlContent.add(cardCreateUser, "createUser");

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
        final assignment.model.AdminStaff first = assignment.service.Database.adminStaff.get(0);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboardFrame(first).setVisible(true);
            }
        });
    }
}
