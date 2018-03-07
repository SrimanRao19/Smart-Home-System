import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CentralControlUI {
	
	private CentralControl centralControl;
	
	// UI Components
	private JLabel currentTemperatureLabel = new JLabel();
	private JLabel desiredTemperatureLabel = new JLabel();
	private JLabel systemModeLabel = new JLabel();
	private JLabel primaryHeatingUnitLabel = new JLabel("Off");
	private JLabel backupHeatingUnitLabel = new JLabel("Off");
	private JLabel coolingUnitLabel = new JLabel("Off");
	private JLabel fanLabel = new JLabel("Off");
	private JLabel temperatureControlStateLabel = new JLabel("Off");
	
	private JPanel primaryHeatingUnitBox = createColorBox();
	private JPanel backupHeatingUnitBox = createColorBox();
	private JPanel coolingUnitBox = createColorBox();
	private JPanel fanBox = createColorBox();
	
	private JSlider desiredTemperatureSlider = new JSlider(JSlider.HORIZONTAL, 40, 100, 75);;
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Smart Home System Central Control - Climate");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel myPanel = createMainPanel();
        refreshDesiredTemperatureText();
        refreshCurrentTemperatureText();
 
        frame.getContentPane().add(myPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
 
        centerWindow(frame);
        
        //Display the window.
        frame.setVisible(true);
    }
    
    private void centerWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }
    
    public void display() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    private void updateLabelText(final JLabel label, final String newText) {
    	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				label.setText(newText);
			}
		});
    }
    
    private void updateBoxColor(final JPanel box, final Color newColor) {
    	SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				box.setBackground(newColor);
			}
		});
    }
    
    public void refreshDesiredTemperatureText() {
    	String newText = String.valueOf(Temperature.getDesired());
    	updateLabelText(desiredTemperatureLabel, newText);
    }
    
    public void refreshCurrentTemperatureText() {
    	String newText = String.valueOf(Temperature.getCurrent());
    	updateLabelText(currentTemperatureLabel, newText);
    }
    
	private String getOnOffString(boolean isOn) {
		return (isOn ? "On" : "Off");
	}
	
	private Color getOnOffColor(boolean isOn) {
		return (isOn ? Color.GREEN : Color.RED);
	}
    
    public void updatePrimaryHeatingUnitDisplay(boolean newState) {
    	String text = getOnOffString(newState);
    	updateLabelText(primaryHeatingUnitLabel, text);
    	
    	Color color = getOnOffColor(newState);
    	updateBoxColor(primaryHeatingUnitBox, color);
    }
    
    public void updateBackupHeatingUnitDisplay(boolean newState) {
    	String text = getOnOffString(newState);
    	updateLabelText(backupHeatingUnitLabel, text);
    	
    	Color color = getOnOffColor(newState);
    	updateBoxColor(backupHeatingUnitBox, color);
    }
    
    public void updateCoolingUnitDisplay(boolean newState) {
    	String text = getOnOffString(newState);
    	updateLabelText(coolingUnitLabel, text);
    	
    	Color color = getOnOffColor(newState);
    	updateBoxColor(coolingUnitBox, color);
    }
    
    public void updateFanDisplay(boolean newState) {
    	String text = getOnOffString(newState);
    	updateLabelText(fanLabel, text);
    	
    	Color color = getOnOffColor(newState);
    	updateBoxColor(fanBox, color);
    }
    
    public void updateTemperatureControlStateText(String text) {
    	updateLabelText(temperatureControlStateLabel, text);
    }
    
    private ChangeListener getDesiredTemperatureChangedListener() {
    	ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				
				if(!slider.getValueIsAdjusting()) {
					Integer newDesiredTemperature = desiredTemperatureSlider.getValue();
					System.out.println("Updated desired temperature. New temperature = " + newDesiredTemperature);
					Temperature.setDesired(newDesiredTemperature);
					refreshDesiredTemperatureText();
				}
			}
		};
		
		return changeListener;
    }
    
    private ActionListener getSystemModeChangedListener() {
    	ActionListener actionListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateLabelText(systemModeLabel, e.getActionCommand());

				Mode newMode = getModeFromString(e.getActionCommand());
				
				if(newMode != null) {
					centralControl.addMessageToQueue(newMode);
				}
			}
		};
		
		return actionListener;
    }
    
    private JPanel createMainPanel() {
    	JPanel myPanel = new JPanel();
    	myPanel.setLayout(new GridBagLayout());
    	
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(10, 10, 0, 10);
    	c.fill = GridBagConstraints.BOTH;
    	c.gridheight = 1;
    	c.gridwidth = 1;
    	c.weightx = 1;
    	c.weighty = 1;
    	
    	JPanel systemStatusPanel = createSystemStatusPanel();
    	
    	// Mode Panel
    	JPanel modePanel = createSystemModePanel();
    	
    	// Desired Temperature Panel
    	JPanel desiredTemperaturePanel = createDesiredTemperaturePanel();
    	
    	// Main Panel
    	myPanel.add(systemStatusPanel, c);
    	c.gridy++;
    	
    	myPanel.add(desiredTemperaturePanel, c);
    	c.gridy++;
    	c.insets = new Insets(10, 10, 10, 10);
    	
    	myPanel.add(modePanel, c);
    	c.gridy++;
    	
    	return myPanel;
    }
    
    private JPanel createSystemStatusPanel() {
    	
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(5, 5, 0, 5);
    	c.fill = GridBagConstraints.NONE;
    	c.gridheight = 1;
    	c.gridwidth = 1;
    	c.weightx = 0;
    	c.weighty = 1;
    	c.anchor = GridBagConstraints.LINE_END;
    	
    	Border basicBorder = BorderFactory.createLineBorder(Color.BLACK);
    	TitledBorder systemStatusPanelBorder = BorderFactory.createTitledBorder(basicBorder, "System Status", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
    	
    	// System Status Panel
    	JPanel panel = new JPanel();
    	panel.setLayout(new GridBagLayout());
    	panel.setBorder(systemStatusPanelBorder);
    	
    	// First Column
    	
    	panel.add(new JLabel("Current Temperature:"), c);
    	c.gridy++;
    	
    	panel.add(new JLabel("Desired Temperature:"), c);
    	c.gridy++;
    	
    	panel.add(new JLabel("System Mode:"), c);
    	c.gridy++;
    	
    	panel.add(new JLabel("Primary Heating Unit:"), c);
    	c.gridy++;
    	
    	panel.add(new JLabel("Backup Heating Unit:"), c);
    	c.gridy++;
    	
    	panel.add(new JLabel("Cooling Unit:"), c);
    	c.gridy++;
    	
    	panel.add(new JLabel("Fan:"), c);
    	c.gridy++;
    	c.insets = new Insets(5, 5, 5, 5);
    	
    	panel.add(new JLabel("Temperature Control State:"), c);
    	
    	// Second Column
    	
    	c.insets = new Insets(5, 5, 0, 5);
    	c.weightx = 1;
    	c.anchor = GridBagConstraints.LINE_START;
    	c.gridx = 1;
    	c.gridy = 0;
    	
    	panel.add(currentTemperatureLabel, c);
    	c.gridy++;
    	
    	panel.add(desiredTemperatureLabel, c);
    	c.gridy++;
    	
    	panel.add(systemModeLabel, c);
    	c.gridy++;
    	
    	JPanel primaryHeatingUnitComboPanel = createColoredComboLabel(primaryHeatingUnitLabel, primaryHeatingUnitBox);
    	panel.add(primaryHeatingUnitComboPanel, c);
    	c.gridy++;
    	
    	JPanel backupHeatingUnitComboPanel = createColoredComboLabel(backupHeatingUnitLabel, backupHeatingUnitBox);
    	panel.add(backupHeatingUnitComboPanel, c);
    	c.gridy++;
    	
    	JPanel coolingUnitComboPanel = createColoredComboLabel(coolingUnitLabel, coolingUnitBox);
    	panel.add(coolingUnitComboPanel, c);
    	c.gridy++;
    	
    	JPanel fanComboPanel = createColoredComboLabel(fanLabel, fanBox);
    	panel.add(fanComboPanel, c);
    	c.gridy++;
    	c.insets = new Insets(5, 5, 5, 5);
    	
    	panel.add(temperatureControlStateLabel, c);
    	
    	return panel;
    }
    
    private JPanel createDesiredTemperaturePanel() {
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(5, 5, 5, 5);
    	c.fill = GridBagConstraints.BOTH;
    	c.gridheight = 1;
    	c.gridwidth = 1;
    	c.weightx = 1;
    	c.weighty = 1;
    	c.anchor = GridBagConstraints.LINE_START;
    	
    	Border basicBorder = BorderFactory.createLineBorder(Color.BLACK);
    	TitledBorder desiredTemperaturePanelBorder = BorderFactory.createTitledBorder(basicBorder, "Update Desired Temperature", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
    	
    	// System Status Panel
    	JPanel panel = new JPanel() {
			private static final long serialVersionUID = 546646L;

			@Override
    		public Dimension getMinimumSize() {
    			Dimension d = super.getMinimumSize();
    			Dimension min = new Dimension(Math.max(600, d.width), d.height);
    			return min;
    		}
    		
    		@Override
    		public Dimension getPreferredSize() {
    			Dimension d = super.getPreferredSize();
    			Dimension min = new Dimension(Math.max(600, d.width), d.height);
    			return min;
    		}
    	};
    	panel.setLayout(new GridBagLayout());
    	panel.setBorder(desiredTemperaturePanelBorder);
    	
		//Turn on labels at major tick marks.
    	desiredTemperatureSlider.setMajorTickSpacing(10);
    	desiredTemperatureSlider.setMinorTickSpacing(1);
    	desiredTemperatureSlider.setPaintTicks(true);
    	desiredTemperatureSlider.setPaintLabels(true);
    	desiredTemperatureSlider.setSnapToTicks(true);
    	desiredTemperatureSlider.addChangeListener(getDesiredTemperatureChangedListener());
    	    	
    	panel.add(desiredTemperatureSlider, c);
    	
    	return panel;
    }

    
    private JPanel createSystemModePanel() {
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(5, 5, 0, 5);
    	c.fill = GridBagConstraints.NONE;
    	c.gridheight = 1;
    	c.gridwidth = 1;
    	c.weightx = 1;
    	c.weighty = 1;
    	c.anchor = GridBagConstraints.LINE_START;
    	
    	Border basicBorder = BorderFactory.createLineBorder(Color.BLACK);
    	TitledBorder modePanelBorder = BorderFactory.createTitledBorder(basicBorder, "Update System Mode", TitledBorder.LEFT, TitledBorder.ABOVE_TOP);
    	
    	// System Status Panel
    	JPanel panel = new JPanel();
    	panel.setLayout(new GridBagLayout());
    	panel.setBorder(modePanelBorder);
    	
    	ButtonGroup group = new ButtonGroup();
    	
    	JRadioButton offOption = new JRadioButton(Mode.OFF.toString());
    	offOption.setActionCommand(Mode.OFF.toString());
    	systemModeLabel.setText(Mode.OFF.toString());
    	offOption.setSelected(true);
    	offOption.addActionListener(getSystemModeChangedListener());
    	group.add(offOption);

    	JRadioButton heatingOption = new JRadioButton(Mode.HEATING.toString());
    	heatingOption.setActionCommand(Mode.HEATING.toString());
    	heatingOption.addActionListener(getSystemModeChangedListener());
    	group.add(heatingOption);
    	
    	JRadioButton coolingOption = new JRadioButton(Mode.COOLING.toString());
    	coolingOption.setActionCommand(Mode.COOLING.toString());
    	coolingOption.addActionListener(getSystemModeChangedListener());
    	group.add(coolingOption);
    	
    	panel.add(offOption, c);
    	c.gridy++;
    	
    	
    	panel.add(heatingOption, c);
    	c.gridy++;
    	c.insets = new Insets(5, 5, 5, 5);
    	
    	panel.add(coolingOption, c);
    	
    	return panel;
    }
    
    private JPanel createColorBox() {
    	Dimension d = new Dimension(10, 10);
    	
    	JPanel myPanel = new JPanel();
    	myPanel.setBackground(Color.RED);
    	myPanel.setSize(d);
    	myPanel.setPreferredSize(d);
    	myPanel.setMinimumSize(d);
    	myPanel.setMaximumSize(d);
    	
    	return myPanel;
    }
    
    private JPanel createColoredComboLabel(JLabel label, JPanel box) {
    	JPanel panel = new JPanel();
    	panel.setLayout(new GridBagLayout());
    	
    	GridBagConstraints c = new GridBagConstraints();
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(0, 0, 0, 5);
    	c.fill = GridBagConstraints.NONE;
    	c.gridheight = 1;
    	c.gridwidth = 1;
    	c.weightx = 1;
    	c.weighty = 1;
    	c.anchor = GridBagConstraints.LINE_START;
    	
    	panel.add(box, c);
    	c.gridx++;
    	c.insets = new Insets(0, 0, 0, 0);
    	
    	panel.add(label, c);
    	
    	return panel;
    }

	public void setCentralControl(CentralControl centralControl) {
		this.centralControl = centralControl;
	}

	private Mode getModeFromString(String input) {
		for(Mode mode : Mode.values()) {
			if(mode.toString().equals(input)) {
				return mode;
			}
		}
		
		return null;
	}
}
