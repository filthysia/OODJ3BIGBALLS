package assignment.gui.doctor;

import assignment.model.Doctor;
import assignment.model.Person;
import assignment.model.UserRole;
import assignment.service.AccountService;
import assignment.service.Database;
import assignment.service.SeedData;

/**
 * Entry point for the Doctor GUI module. Built as a real NetBeans JFrame
 * Form (GroupLayout, no external library) - open this in NetBeans's Design
 * view to drag components around; never hand-edit inside initComponents().
 */
public class DoctorLoginFrame extends javax.swing.JFrame {

    private final AccountService accountService = new AccountService();

    /**
     * Creates new form DoctorLoginFrame
     */
    public DoctorLoginFrame() {
        initComponents();
        setSize(980, 620);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void btnSignInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSignInActionPerformed
        String id = txtDoctorId.getText().trim();
        String pw = new String(txtPassword.getPassword());
        Person p = accountService.authenticate(UserRole.DOCTOR, id, pw);
        if (p == null || !(p instanceof Doctor)) {
            lblError.setText("Invalid Doctor ID or password.");
        } else {
            Doctor doctor = (Doctor) p;
            dispose();
            DoctorDashboardFrame dashboard = new DoctorDashboardFrame(doctor);
            dashboard.setVisible(true);
        }
    }//GEN-LAST:event_btnSignInActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSignIn;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblDoctorId;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JPanel pnlCard;
    private javax.swing.JTextField txtDoctorId;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCard = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblDoctorId = new javax.swing.JLabel();
        txtDoctorId = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblError = new javax.swing.JLabel();
        btnSignIn = new javax.swing.JButton();
        lblHint = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("APU Medical Centre · Doctor Login");

        pnlCard.setBackground(new java.awt.Color(255, 255, 255));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblBrand.setForeground(new java.awt.Color(11, 61, 42));
        lblBrand.setText("APU Medical Centre");

        lblSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblSubtitle.setForeground(new java.awt.Color(139, 147, 140));
        lblSubtitle.setText("Doctor Module — sign in to continue");

        lblDoctorId.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblDoctorId.setText("Doctor ID");

        txtDoctorId.setText("DOC001");

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblPassword.setText("Password");

        lblError.setForeground(new java.awt.Color(217, 79, 79));
        lblError.setText(" ");

        btnSignIn.setBackground(new java.awt.Color(21, 122, 82));
        btnSignIn.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnSignIn.setForeground(new java.awt.Color(255, 255, 255));
        btnSignIn.setText("Sign in");
        btnSignIn.addActionListener(this::btnSignInActionPerformed);

        lblHint.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lblHint.setForeground(new java.awt.Color(139, 147, 140));
        lblHint.setText("Demo credentials: DOC001 / doc123");

        javax.swing.GroupLayout pnlCardLayout = new javax.swing.GroupLayout(pnlCard);
        pnlCard.setLayout(pnlCardLayout);
        pnlCardLayout.setHorizontalGroup(
            pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitle, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSignIn, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHint, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(0, Short.MAX_VALUE))
        );
        pnlCardLayout.setVerticalGroup(
            pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblSubtitle, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(lblDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtDoctorId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSignIn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblHint, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(300, 300, 300)
                .addComponent(pnlCard, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(pnlCard, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Database.loadAll();
        SeedData.ensure();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DoctorLoginFrame().setVisible(true);
            }
        });
    }
}
