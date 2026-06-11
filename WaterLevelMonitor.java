package WaterLevelMonitoringSystem;

 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WaterLevelMonitor extends JFrame {

    // Thresholds
    private static final int MAX_LEVEL = 100;
    private static final int MIN_LEVEL = 0;
    private static final int PUMP_ON_THRESHOLD = 20;
    private static final int PUMP_OFF_THRESHOLD = 95;

    // State Variables
    private int currentWaterLevel = 50; 
    private boolean isPumpOn = false;
    private boolean isAutoMode = true; 
    private boolean isPumpDamaged = false; // Hardware State

    // UI Components
    private JProgressBar waterTankBar;
    private JLabel statusLabel;
    private JLabel pumpStatusLabel;
    private JLabel alertMessageLabel; // New Alert System
    private JToggleButton modeToggleButton;
    private JButton manualPumpButton;
    private JToggleButton damagePumpButton; // Hardware Failure Button
    private JSlider usageSlider;
    private Timer simulationTimer;

    public WaterLevelMonitor() {
        setTitle("Smart Water Level Monitor - Hardware Alerts");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 52, 54)); 

        // 1. Header Panel
        JLabel headerLabel = new JLabel("Smart Water Management", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        add(headerLabel, BorderLayout.NORTH);

        // 2. Tank Visualization (Progress Bar)
        waterTankBar = new JProgressBar(JProgressBar.VERTICAL, MIN_LEVEL, MAX_LEVEL);
        waterTankBar.setValue(currentWaterLevel);
        waterTankBar.setStringPainted(true);
        waterTankBar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        waterTankBar.setBackground(new Color(223, 230, 233));
        
        JPanel tankPanel = new JPanel();
        tankPanel.setOpaque(false);
        tankPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));
        tankPanel.setLayout(new BorderLayout());
        tankPanel.add(waterTankBar, BorderLayout.CENTER);
        add(tankPanel, BorderLayout.CENTER);

        // 3. Control & Alert Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Alert Message Label (Hidden by default)
        alertMessageLabel = new JLabel("NO ALERTS", SwingConstants.CENTER);
        alertMessageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        alertMessageLabel.setForeground(new Color(255, 107, 129)); // Bright Red
        alertMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        alertMessageLabel.setVisible(false);

        // Status Labels
        statusLabel = new JLabel("System Status: Normal", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        pumpStatusLabel = new JLabel("Pump Status: OFF", SwingConstants.CENTER);
        pumpStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pumpStatusLabel.setForeground(new Color(250, 177, 160)); 
        pumpStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hardware Controls Panel
        JPanel hardwarePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        hardwarePanel.setOpaque(false);
        
        damagePumpButton = new JToggleButton("Simulate Damaged Pump");
        damagePumpButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        damagePumpButton.setForeground(Color.RED);
        hardwarePanel.add(damagePumpButton);

        // Standard Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPanel.setOpaque(false);

        modeToggleButton = new JToggleButton("Mode: AUTO");
        modeToggleButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeToggleButton.setBackground(new Color(116, 185, 255));
        
        manualPumpButton = new JButton("Turn Pump ON");
        manualPumpButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        manualPumpButton.setEnabled(false); 

        buttonsPanel.add(modeToggleButton);
        buttonsPanel.add(manualPumpButton);

        // Slider for draining speed
        JLabel sliderLabel = new JLabel("Water Consumption Speed:");
        sliderLabel.setForeground(Color.WHITE);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        usageSlider = new JSlider(0, 10, 2); 
        usageSlider.setOpaque(false);
        usageSlider.setForeground(Color.WHITE);
        usageSlider.setMajorTickSpacing(2);
        usageSlider.setPaintTicks(true);
        usageSlider.setPaintLabels(true);

        // Add everything to control panel
        controlPanel.add(alertMessageLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(pumpStatusLabel);
        controlPanel.add(hardwarePanel);
        controlPanel.add(buttonsPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(sliderLabel);
        controlPanel.add(usageSlider);

        add(controlPanel, BorderLayout.SOUTH);

        // 4. Button Action Listeners
        modeToggleButton.addActionListener(e -> {
            isAutoMode = !modeToggleButton.isSelected();
            if (isAutoMode) {
                modeToggleButton.setText("Mode: AUTO");
                modeToggleButton.setBackground(new Color(116, 185, 255));
                manualPumpButton.setEnabled(false); 
            } else {
                modeToggleButton.setText("Mode: MANUAL");
                modeToggleButton.setBackground(new Color(253, 203, 110)); 
                manualPumpButton.setEnabled(true);  
            }
        });

        manualPumpButton.addActionListener(e -> {
            if (isPumpDamaged) {
                JOptionPane.showMessageDialog(this, "Cannot start pump: Hardware is Damaged!", "Hardware Fault", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (currentWaterLevel >= MAX_LEVEL) {
                JOptionPane.showMessageDialog(this, "Cannot start pump: Tank is already full!", "Safety Override", JOptionPane.WARNING_MESSAGE);
                return;
            }
            isPumpOn = !isPumpOn; 
            updatePumpUI();
        });

        damagePumpButton.addActionListener(e -> {
            isPumpDamaged = damagePumpButton.isSelected();
            if (isPumpDamaged) {
                isPumpOn = false; // Instantly kill the pump
                updatePumpUI();
            }
        });

        // 5. Start Simulation Engine
        startSimulation();
    }

    private void startSimulation() {
        simulationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulateWaterFlow();
            }
        });
        simulationTimer.start();
    }

    private void simulateWaterFlow() {
        int drainSpeed = usageSlider.getValue();

        // --- HARDWARE ALERTS & BUZZER LOGIC ---
        boolean isAlertActive = false;
        StringBuilder alertText = new StringBuilder();

        if (isPumpDamaged) {
            isAlertActive = true;
            alertText.append("ALERT: PUMP IS DAMAGED! ");
            isPumpOn = false; // Enforce hardware failure
        }

        if (currentWaterLevel <= MIN_LEVEL) {
            isAlertActive = true;
            alertText.append("ALERT: TANK IS EMPTY!");
        }

        if (isAlertActive) {
            Toolkit.getDefaultToolkit().beep(); // Triggers the physical system buzzer sound
            alertMessageLabel.setText(alertText.toString());
            alertMessageLabel.setVisible(true);
        } else {
            alertMessageLabel.setVisible(false);
        }

        // --- AUTOMATION LOGIC ---
        if (isAutoMode && !isPumpDamaged) {
            if (currentWaterLevel <= PUMP_ON_THRESHOLD && !isPumpOn) {
                isPumpOn = true;
                updatePumpUI();
            } else if (currentWaterLevel >= PUMP_OFF_THRESHOLD && isPumpOn) {
                isPumpOn = false;
                updatePumpUI();
            }
        } else if (!isAutoMode) {
            if (currentWaterLevel >= MAX_LEVEL && isPumpOn) {
                isPumpOn = false;
                updatePumpUI();
                statusLabel.setText("System Status: Auto-Shutoff (Tank Full)");
            }
        }

        // --- WATER PHYSICS ---
        if (isPumpOn) {
            currentWaterLevel += 15; 
            currentWaterLevel -= drainSpeed;
            statusLabel.setText("System Status: Filling Tank...");
        } else {
            currentWaterLevel -= drainSpeed; 
            if (drainSpeed == 0) {
                statusLabel.setText("System Status: Water Not in Use");
            } else {
                statusLabel.setText("System Status: Consuming Water...");
            }
        }

        // Keep bounds strictly within 0 to 100
        if (currentWaterLevel >= MAX_LEVEL) currentWaterLevel = MAX_LEVEL;
        if (currentWaterLevel <= MIN_LEVEL) currentWaterLevel = MIN_LEVEL;

        // Update UI
        waterTankBar.setValue(currentWaterLevel);
        waterTankBar.setString(currentWaterLevel + "%");
        updateTankColor();
    }

    private void updatePumpUI() {
        if (isPumpOn) {
            pumpStatusLabel.setText(isAutoMode ? "Pump Status: ON (Auto)" : "Pump Status: ON (Manual)");
            pumpStatusLabel.setForeground(new Color(85, 239, 196)); 
            manualPumpButton.setText("Turn Pump OFF");
        } else {
            pumpStatusLabel.setText("Pump Status: OFF");
            pumpStatusLabel.setForeground(new Color(250, 177, 160));
            manualPumpButton.setText("Turn Pump ON");
        }
    }

    private void updateTankColor() {
        if (currentWaterLevel < PUMP_ON_THRESHOLD) {
            waterTankBar.setForeground(new Color(214, 48, 49)); // Red (Danger)
        } else if (currentWaterLevel >= PUMP_OFF_THRESHOLD) {
            waterTankBar.setForeground(new Color(9, 132, 227)); // Deep Blue (Full)
        } else {
            waterTankBar.setForeground(new Color(116, 185, 255)); // Light Blue (Normal)
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            WaterLevelMonitor app = new WaterLevelMonitor();
            app.setLocationRelativeTo(null); 
            app.setVisible(true);
        });
    }
}