package DBProject;

import DBProject.DBHandler.MongoDBHandler;
import DBProject.DBHandler.MySQLHandler;
import DBProject.DBModels.Provider;
import DBProject.DBModels.Service;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class App {

    // Main method to launch the application
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Setting up the main JFrame
                JFrame mainFrame = new JFrame("DB Project Main Menu");
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.setSize(400, 300);
                mainFrame.setLayout(new GridLayout(5, 1));  // Using GridLayout for menu buttons

                // Creating buttons for different actions
                JButton addProviderButton = new JButton("Add New Provider");
                JButton addServiceButton = new JButton("Add Service to Existing Provider");
                JButton listServicesButton = new JButton("List and Manage Services");
                JButton editProviderButton = new JButton("Edit Provider");

                // Setting action listeners for each button
                addProviderButton.addActionListener(e -> new AddProviderWindow());
                addServiceButton.addActionListener(e -> new AddServiceWindow());
                listServicesButton.addActionListener(e -> new ListServicesWindow());
                editProviderButton.addActionListener(e -> new EditProviderWindow());

                // Adding buttons to the main frame
                mainFrame.add(addProviderButton);
                mainFrame.add(addServiceButton);
                mainFrame.add(listServicesButton);
                mainFrame.add(editProviderButton);

                mainFrame.setVisible(true);  // Display the main frame
            } catch (Exception e) {
                e.printStackTrace();  // Catch and log any exceptions
            }
        });
    }
}

// Window to add a new provider along with a service
class AddProviderWindow {
    public AddProviderWindow() {
        JFrame frame = new JFrame("Add New Provider");
        frame.setBounds(100, 100, 400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(6, 2));  // Organizing components in grid layout

        // Creating input fields for provider and service details
        JTextField nameTextField = new JTextField();
        JTextField emailTextField = new JTextField();
        JTextField serviceNameTextField = new JTextField();
        JTextArea serviceDescriptionTextArea = new JTextArea();

        JButton submitButton = new JButton("Submit");

        // Adding labels and input fields to the frame
        frame.add(new JLabel("Provider Name:"));
        frame.add(nameTextField);
        frame.add(new JLabel("Email:"));
        frame.add(emailTextField);
        frame.add(new JLabel("Service Name:"));
        frame.add(serviceNameTextField);
        frame.add(new JLabel("Service Description:"));
        frame.add(new JScrollPane(serviceDescriptionTextArea));
        frame.add(submitButton);

        // Handling the submit button click event
        submitButton.addActionListener(e -> {
            String providerName = nameTextField.getText().trim();
            String providerEmail = emailTextField.getText().trim();
            String serviceName = serviceNameTextField.getText().trim();
            String serviceDescription = serviceDescriptionTextArea.getText().trim();

            // Validation to ensure required fields are filled
            if (providerName.isEmpty() || providerEmail.isEmpty()) {
                showErrorDialog(frame, "Name and Email are required!");
                return;
            }

            try {
                // Check if provider already exists
                if (MySQLHandler.providerExists(providerName, providerEmail)) {
                    showErrorDialog(frame, "Provider already exists!");
                } else {
                    // Create provider and service objects
                    Provider provider = new Provider(providerName, providerEmail);
                    Service service = new Service(serviceName, serviceDescription);

                    // Insert the provider and service into the MySQL database and MongoDB
                    int providerId = MySQLHandler.insertProvider(provider);
                    MySQLHandler.insertService(providerId, service);
                    MongoDBHandler.insertProviderWithService(provider, service);

                    // Show success dialog and close the frame
                    showSuccessDialog(frame, "Provider and service successfully added.");
                    frame.dispose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(frame, "Error: " + ex.getMessage());
            }
        });

        frame.setVisible(true);  // Show the frame
    }

    // Method to show error messages in a dialog
    private void showErrorDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Method to show success messages in a dialog
    private void showSuccessDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

// Window to edit an existing provider
class EditProviderWindow {
    public EditProviderWindow() {
        JFrame frame = new JFrame("Edit Provider");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(5, 2));

        // Input fields for the current and new provider details
        JTextField oldNameTextField = new JTextField();
        JTextField oldEmailTextField = new JTextField();
        JTextField newNameTextField = new JTextField();
        JTextField newEmailTextField = new JTextField();

        JButton submitButton = new JButton("Update");

        frame.add(new JLabel("Current Provider Name:"));
        frame.add(oldNameTextField);
        frame.add(new JLabel("Current Provider Email:"));
        frame.add(oldEmailTextField);
        frame.add(new JLabel("New Provider Name:"));
        frame.add(newNameTextField);
        frame.add(new JLabel("New Provider Email:"));
        frame.add(newEmailTextField);
        frame.add(submitButton);

        // Handle the submit action to update the provider details
        submitButton.addActionListener(e -> {
            String oldName = oldNameTextField.getText().trim();
            String oldEmail = oldEmailTextField.getText().trim();
            String newName = newNameTextField.getText().trim();
            String newEmail = newEmailTextField.getText().trim();

            // Ensure all fields are filled before proceeding
            if (oldName.isEmpty() || oldEmail.isEmpty() || newName.isEmpty() || newEmail.isEmpty()) {
                showErrorDialog(frame, "All fields are required!");
                return;
            }

            try {
                // Fetch provider ID for the old provider
                int providerId = MySQLHandler.getProviderId(oldName, oldEmail);
                if (providerId == -1) {
                    showErrorDialog(frame, "Provider not found!");
                    return;
                }

                // Update provider information in both MySQL and MongoDB
                MySQLHandler.updateProvider(providerId, newName, newEmail);
                MongoDBHandler.updateProvider(oldName, oldEmail, newName, newEmail);

                showSuccessDialog(frame, "Provider updated successfully.");
                frame.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(frame, "Error: " + ex.getMessage());
            }
        });

        frame.setVisible(true);  // Display the frame
    }

    // Method to show error messages in a dialog
    private void showErrorDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Method to show success messages in a dialog
    private void showSuccessDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

// Window to add a service to an existing provider
class AddServiceWindow {
    public AddServiceWindow() {
        JFrame frame = new JFrame("Add Service to Existing Provider");
        frame.setBounds(100, 100, 400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(7, 2));

        // Input fields for provider and service details
        JTextField nameTextField = new JTextField();
        JTextField emailTextField = new JTextField();
        JTextField serviceNameTextField = new JTextField();
        JTextArea serviceDescriptionTextArea = new JTextArea();

        JButton checkProviderButton = new JButton("Check Provider");
        JButton submitServiceButton = new JButton("Submit Service");

        submitServiceButton.setEnabled(false);  // Initially disabled until provider is checked

        frame.add(new JLabel("Provider Name:"));
        frame.add(nameTextField);
        frame.add(new JLabel("Email:"));
        frame.add(emailTextField);
        frame.add(new JLabel("Service Name:"));
        frame.add(serviceNameTextField);
        frame.add(new JLabel("Service Description:"));
        frame.add(new JScrollPane(serviceDescriptionTextArea));
        frame.add(checkProviderButton);
        frame.add(submitServiceButton);

        // Action listener for checking if the provider exists
        checkProviderButton.addActionListener(e -> {
            String providerName = nameTextField.getText().trim();
            String providerEmail = emailTextField.getText().trim();

            // Validate input
            if (providerName.isEmpty() || providerEmail.isEmpty()) {
                showErrorDialog(frame, "Name and Email are required!");
                return;
            }

            try {
                // Check if the provider exists in the database
                if (MySQLHandler.providerExists(providerName, providerEmail)) {
                    showSuccessDialog(frame, "Provider exists. You can now add services.");
                    submitServiceButton.setEnabled(true);  // Enable submit button
                } else {
                    showErrorDialog(frame, "Provider does not exist!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(frame, "Error: " + ex.getMessage());
            }
        });

        // Action listener for submitting the service
        submitServiceButton.addActionListener(e -> {
            String providerName = nameTextField.getText().trim();
            String providerEmail = emailTextField.getText().trim();
            String serviceName = serviceNameTextField.getText().trim();
            String serviceDescription = serviceDescriptionTextArea.getText().trim();

            // Ensure service name is provided
            if (serviceName.isEmpty()) {
                showErrorDialog(frame, "Service Name is required!");
                return;
            }

            try {
                // Create a service object and associate it with the provider
                Service service = new Service(serviceName, serviceDescription);
                int providerId = MySQLHandler.getProviderId(providerName, providerEmail);

                MySQLHandler.insertService(providerId, service);
                MongoDBHandler.addServiceToExistingProvider(providerName, providerEmail, service);

                showSuccessDialog(frame, "Service successfully added.");
                frame.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(frame, "Error: " + ex.getMessage());
            }
        });

        frame.setVisible(true);  // Show the window
    }

    private void showErrorDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

// Window to list and manage services for a provider
class ListServicesWindow {
    public ListServicesWindow() {
        JFrame frame = new JFrame("List and Manage Services");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel to take input for provider details
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        JTextField nameTextField = new JTextField();
        JTextField emailTextField = new JTextField();

        JButton fetchButton = new JButton("Fetch Services");
        inputPanel.add(new JLabel("Provider Name:"));
        inputPanel.add(nameTextField);
        inputPanel.add(new JLabel("Provider Email:"));
        inputPanel.add(emailTextField);
        inputPanel.add(new JLabel());
        inputPanel.add(fetchButton);

        // List model to display services
        DefaultListModel<Service> serviceListModel = new DefaultListModel<>();
        JList<Service> serviceList = new JList<>(serviceListModel);
        serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel to manage selected service
        JPanel managePanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update Service");
        JButton deleteButton = new JButton("Delete Service");
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        managePanel.add(updateButton);
        managePanel.add(deleteButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(serviceList), BorderLayout.CENTER);
        frame.add(managePanel, BorderLayout.SOUTH);

        // Fetch services based on provider details
        fetchButton.addActionListener(e -> {
            String providerName = nameTextField.getText().trim();
            String providerEmail = emailTextField.getText().trim();

            if (providerName.isEmpty() || providerEmail.isEmpty()) {
                showErrorDialog(frame, "Name and Email are required!");
                return;
            }

            try {
                // Fetch services from MySQL database
                List<Service> services = MySQLHandler.listServicesByProvider(providerName, providerEmail);
                serviceListModel.clear();
                for (Service service : services) {
                    serviceListModel.addElement(service);
                }
                if (services.isEmpty()) {
                    showErrorDialog(frame, "No services found for this provider.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(frame, "Error: " + ex.getMessage());
            }
        });

        // Enable buttons when a service is selected
        serviceList.addListSelectionListener(e -> {
            boolean selected = !serviceList.isSelectionEmpty();
            updateButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        });

        // Update selected service
        updateButton.addActionListener(e -> {
            Service selectedService = serviceList.getSelectedValue();
            if (selectedService == null) return;

            // Show input dialog for new service name and description
            String newName = JOptionPane.showInputDialog(frame, "Enter new service name:", selectedService.getName());
            String newDescription = JOptionPane.showInputDialog(frame, "Enter new service description:", selectedService.getDescription());

            if (newName != null && newDescription != null) {
                try {
                    String providerName = nameTextField.getText().trim();
                    String providerEmail = emailTextField.getText().trim();
                    Service updatedService = new Service(newName, newDescription);

                    // Update service in both MySQL and MongoDB
                    MySQLHandler.updateService(providerName, providerEmail, selectedService.getName(), updatedService);
                    MongoDBHandler.updateService(providerName, providerEmail, selectedService.getName(), updatedService);

                    showSuccessDialog(frame, "Service updated successfully.");
                    fetchButton.doClick();  // Refresh service list
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        // Delete selected service
        deleteButton.addActionListener(e -> {
            Service selectedService = serviceList.getSelectedValue();
            if (selectedService == null) return;

            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this service?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String providerName = nameTextField.getText().trim();
                    String providerEmail = emailTextField.getText().trim();

                    // Delete service from both MySQL and MongoDB
                    MySQLHandler.deleteService(providerName, providerEmail, selectedService.getName());
                    MongoDBHandler.deleteService(providerName, providerEmail, selectedService.getName());

                    showSuccessDialog(frame, "Service deleted successfully.");
                    fetchButton.doClick();  // Refresh service list
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorDialog(frame, "Error: " + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);  // Show the frame
    }

    private void showErrorDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}