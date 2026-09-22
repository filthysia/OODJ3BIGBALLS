package assignment.gui.manager;

import assignment.model.MedicalManager;
import assignment.service.Database;
import assignment.service.ProfileService;
import assignment.service.SeedData;

/**
 * Entry point for the Medical Manager GUI module. Built as a real NetBeans
 * JFrame Form (GroupLayout, no external library) - open in NetBeans's
 * Design view to drag components around; never hand-edit inside
 * initComponents().
 */
public class ManagerLoginFrame extends javax.swing.JFrame {

    private final ProfileService profileService = new ProfileService();

    /**
     * Creates new form ManagerLoginFrame
     */
    public ManagerLoginFrame() {
        initComponents();
        setSize(980, 620);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void btnSignInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSignInActionPerformed
        String id = txtManagerId.getText().trim();
        String pw = new String(txtPassword.getPassword());
        MedicalManager m = profileService.authenticate(id, pw);
        if (m == null) {
            lblError.setText("Invalid Manager ID or password.");
        } else {
            dispose();
            ManagerDashboardFrame dashboard = new ManagerDashboardFrame(m);
            dashboard.setVisible(true);
        }
    }//GEN-LAST:event_btnSignInActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSignIn;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblManagerId;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JPanel pnlCard;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtManagerId;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCard = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblManagerId = new javax.swing.JLabel();
        txtManagerId = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblError = new javax.swing.JLabel();
        btnSignIn = new javax.swing.JButton();
        lblHint = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("APU Medical Centre · Medical Manager Login");

        pnlCard.setBackground(new java.awt.Color(255, 255, 255));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblBrand.setForeground(new java.awt.Color(11, 61, 42));
        lblBrand.setText("APU Medical Centre");

        lblSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblSubtitle.setForeground(new java.awt.Color(139, 147, 140));
        lblSubtitle.setText("Medical Manager Module — sign in to continue");

        lblManagerId.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblManagerId.setText("Manager ID");

        txtManagerId.setText("MM001");

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
        lblHint.setText("Demo credentials: MM001 / admin123");

        javax.swing.GroupLayout pnlCardLayout = new javax.swing.GroupLayout(pnlCard);
        pnlCard.setLayout(pnlCardLayout);
        pnlCardLayout.setHorizontalGroup(
            pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSubtitle, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(lblManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(txtManagerId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                new ManagerLoginFrame().setVisible(true);
            }
        });
    }
}
