package info.ginj.ui;

import info.ginj.Ginj;
import info.ginj.export.Exporter;
import info.ginj.export.clipboard.ClipboardExporter;
import info.ginj.export.disk.DiskExporter;
import info.ginj.export.online.AbstractOAuth2Exporter;
import info.ginj.export.online.OAuthAccount;
import info.ginj.export.online.dropbox.DropboxExporter;
import info.ginj.export.online.exception.AuthorizationException;
import info.ginj.export.online.exception.CommunicationException;
import info.ginj.export.online.google.GooglePhotosExporter;
import info.ginj.model.Account;
import info.ginj.model.ExportSettings;
import info.ginj.model.Target;
import info.ginj.model.TargetPrefs;
import info.ginj.ui.layout.ButtonLayout;
import info.ginj.util.UI;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This window displays and manages the export targets
 */
public class TargetManagementFrame extends JFrame implements TargetListChangeListener {
    public static final Dimension CONFIG_PREFERRED_SIZE = new Dimension(400, 300);
    public static final Dimension DETAIL_PANEL_PREFERRED_SIZE = new Dimension(500, 400);

    public static final String SELECT_TARGET_TYPE_STEP = "select_target_type";
    public static final String GOOGLE_AUTHORIZE_STEP = "google_authorize";
    public static final String GOOGLE_PHOTOS_CONFIGURE_STEP = "google_photos_configure";
    public static final String DROPBOX_AUTHORIZE_STEP = "dropbox_authorize";
    public static final String DROPBOX_CONFIGURE_STEP = "dropbox_configure";
    public static final String CLIPBOARD_CONFIGURE_STEP = "clipboard_configure";
    public static final String DISK_CONFIGURE_STEP = "disk_configure";

    private static StarWindow starWindow = null;
    private final DefaultListModel<Target> targetListModel;

    public TargetManagementFrame(StarWindow starWindow) {
        super();
        TargetManagementFrame.starWindow = starWindow;
        starWindow.addTargetChangeListener(this);

        // For Alt+Tab behaviour
        this.setTitle(Ginj.getAppName() + " target configuration");
        this.setIconImage(StarWindow.getAppIcon());


        // No window title bar or border.
        // Note: setDefaultLookAndFeelDecorated(true); must not have been called anywhere for this to work
        setUndecorated(true);

        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Prepare title bar
        JPanel titleBar = UI.getTitleBar("Target management", e -> onClose());
        contentPane.add(titleBar, BorderLayout.NORTH);


        // Prepare main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(new JLabel("Defined targets:"), BorderLayout.NORTH);


        // Prepare list panel
        JPanel listPanel = new JPanel(new BorderLayout());
        targetListModel = new DefaultListModel<>();
        loadTargets();
        final JList<Target> targetList = new JList<>(targetListModel);
        listPanel.add(new JScrollPane(targetList), BorderLayout.CENTER);

        // Prepare button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new ButtonLayout(SwingConstants.TOP, 10));

        final JButton addTargetButton = new JButton("Add");
        addTargetButton.addActionListener(e -> onNewTarget());
        buttonPanel.add(addTargetButton);

        final JButton editTargetButton = new JButton("Edit");
        editTargetButton.setEnabled(false);
        editTargetButton.addActionListener(e -> onEditTarget());
        buttonPanel.add(editTargetButton);

        final JButton deleteTargetButton = new JButton("Delete");
        deleteTargetButton.addActionListener(e -> onDeleteTarget(targetList.getSelectedIndex()));
        deleteTargetButton.setEnabled(false);
        buttonPanel.add(deleteTargetButton);

        final JButton moveTargetUpButton = new JButton("Move up");
        moveTargetUpButton.addActionListener(e -> onMoveTarget(targetList, -1));
        moveTargetUpButton.setEnabled(false);
        buttonPanel.add(moveTargetUpButton);

        final JButton moveTargetDownButton = new JButton("Move down");
        moveTargetDownButton.addActionListener(e -> onMoveTarget(targetList, +1));
        moveTargetDownButton.setEnabled(false);
        buttonPanel.add(moveTargetDownButton);

        mainPanel.add(listPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        contentPane.add(mainPanel, BorderLayout.CENTER);

        targetList.addListSelectionListener(e -> {
            if (targetList.getSelectedIndex() == -1) {
                editTargetButton.setEnabled(false);
                deleteTargetButton.setEnabled(false);
                moveTargetUpButton.setEnabled(false);
                moveTargetDownButton.setEnabled(false);
            }
            else {
//                editTargetButton.setEnabled(true);
                deleteTargetButton.setEnabled(true);
                moveTargetUpButton.setEnabled(targetList.getSelectedIndex() > 0);
                moveTargetDownButton.setEnabled(targetList.getSelectedIndex() < targetListModel.size() - 1);
            }
        });

        // Prepare lower panel
        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new FlowLayout());
        final JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> onClose());
        lowerPanel.add(closeButton);

        contentPane.add(lowerPanel, BorderLayout.SOUTH);

        // Add default "draggable window" behaviour
        UI.addDraggableWindowMouseBehaviour(this, titleBar);

        setPreferredSize(CONFIG_PREFERRED_SIZE);
        pack();

        // Center window
        setLocationRelativeTo(null);
    }

    private void loadTargets() {
        for (Target target : Ginj.getTargetPrefs().getTargetList()) {
            targetListModel.addElement(target);
        }
    }

    private void onNewTarget() {
        // TODO Add an Image to UIManager under the key wizard.sidebar.image.

        TargetTypeBrancher brancher = new TargetTypeBrancher();
        Wizard wizard = brancher.createWizard();

        // Map map = (Map) WizardDisplayer.showWizard(wizard);
        WizardDisplayer.showWizard(wizard);
    }

    private void onEditTarget() {
        // TODO.
    }

    private void onDeleteTarget(int selectedIndex) {
        if (selectedIndex != -1) {
            final Target target = targetListModel.getElementAt(selectedIndex);
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(TargetManagementFrame.this, "Are you sure you want to delete the target\n'" + target.toString() + "'?", "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                targetListModel.removeElementAt(selectedIndex);
                saveTargetListModelToPrefs();
            }
        }
    }

    private void onMoveTarget(JList<Target> list, int direction) {
        final int selectedIndex = list.getSelectedIndex();
        if (selectedIndex != -1) {
            final int newIndex = selectedIndex + direction;
            if (newIndex >= 0 && newIndex < targetListModel.size()) {
                final Target element = targetListModel.getElementAt(selectedIndex);
                targetListModel.removeElementAt(selectedIndex);
                targetListModel.add(newIndex, element);
                saveTargetListModelToPrefs();
                list.setSelectedIndex(newIndex);
            }
        }
    }

    private void saveTargetListModelToPrefs() {
        final TargetPrefs targetPrefs = Ginj.getTargetPrefs();
        final List<Target> targetList = targetPrefs.getTargetList();
        targetList.clear();
        targetList.addAll(Collections.list(targetListModel.elements()));
        targetPrefs.save();
        starWindow.notifyTargetListChange();
    }

    private void onClose() {
        // Close window
        dispose();
    }

    @Override
    public void onTargetListChange() {
        targetListModel.clear();
        loadTargets();
    }

    @Override
    public void dispose() {
        // Unregister this window
        starWindow.setTargetManagementFrame(null);
        starWindow.removeTargetChangeListener(this);
        super.dispose();
    }

    /////////////////////////////
    // Wizard flow inner classes


    public static class TargetTypeBrancher extends WizardBranchController {

        private final Map<String, Wizard> exporterWizardMap = new HashMap<>();

        public TargetTypeBrancher() {
            // Create the base pages - these are also WizardPage subclasses. The last (and only) one is the branch point
            super(new WizardPage[]{new ServiceSelectionPage()});

            // Prepare a map for the next exporter-dependant maps.
            exporterWizardMap.put(DiskExporter.NAME, WizardPage.createWizard(new WizardPage[]{new DiskConfigurationPage()}, new TextResultProducer()));
            exporterWizardMap.put(ClipboardExporter.NAME, WizardPage.createWizard(new WizardPage[]{new ClipboardConfigurationPage()}, new TextResultProducer()));
            exporterWizardMap.put(DropboxExporter.NAME, WizardPage.createWizard(new WizardPage[]{new DropboxAuthorizePage(), new DropboxConfigurationPage()}, new OAuthAccountResultProducer()));
            exporterWizardMap.put(GooglePhotosExporter.NAME, WizardPage.createWizard(new WizardPage[]{new GoogleAuthorizePage(), new GooglePhotosConfigurationPage()}, new GooglePhotosAccountResultProducer()));
        }

        @SuppressWarnings("rawtypes")
        public Wizard getWizardForStep(String step, Map data) {
//            System.err.println("Get Wizard For Step " + step + " with " + data);
            if (SELECT_TARGET_TYPE_STEP.equals(step)) {
                //check data in the map to decide which wizard will follow
                final Exporter exporter = (Exporter) data.get(TargetPrefs.EXPORTER_KEY);
                if (exporter != null) {
                    return exporterWizardMap.get(exporter.getExporterName());
                }
            }
            //Not known yet
            return null;
        }
    }

    private static class ServiceSelectionPage extends WizardPage {

        public ServiceSelectionPage() {
            super(SELECT_TARGET_TYPE_STEP, "Select target type");
        }

        @Override
        protected void renderingPage() {
            super.renderingPage();

            //noinspection rawtypes
            JList exporterJList = UI.getWizardList(TargetPrefs.EXPORTER_KEY, Exporter.getList().toArray(), -1, true, true);

            JPanel intermediatePanel = new JPanel();
            intermediatePanel.setPreferredSize(DETAIL_PANEL_PREFERRED_SIZE);
            intermediatePanel.add(exporterJList);

            add(intermediatePanel);
        }

        @Override
        protected String validateContents(Component component, Object event) {
            return null;
        }
    }


    public static abstract class OAuthAuthorizePage extends WizardPage {
        public OAuthAuthorizePage(String stepId, String stepDescription) {
            super(stepId, stepDescription);
        }

        @Override
        protected void renderingPage() {
            JTextArea helpTextArea = new JTextArea();
            helpTextArea.setLineWrap(true);
            helpTextArea.setWrapStyleWord(true);
            helpTextArea.setText(getHelpText());

            JLabel connectLabel = new JLabel(getConnectIcon());
//            connectLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//            connectLabel.addMouseListener(new MouseAdapter() {
//                public void mouseClicked(MouseEvent e) {
//                    should cause a "NEXT" event (like the 'Next' button)
//                }
//            });


            JPanel intermediatePanel = new JPanel();
            intermediatePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(5, 5, 0, 5);
            intermediatePanel.add(connectLabel, c);

            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(0, 5, 5, 5);
            c.fill = GridBagConstraints.HORIZONTAL;
            intermediatePanel.add(helpTextArea, c);

            add(intermediatePanel);

            intermediatePanel.setPreferredSize(DETAIL_PANEL_PREFERRED_SIZE);
        }

        protected abstract String getHelpText();

        protected abstract ImageIcon getConnectIcon();


        @SuppressWarnings("rawtypes")
        @Override
        public WizardPanelNavResult allowNext(String stepName, Map settings, Wizard wizard) {
            return new WizardPanelNavResult() {

                @Override
                public boolean isDeferredComputation() {
                    return true;
                }

                @Override
                public void start(Map settings, ResultProgressHandle progress) {
                    boolean isOK = false;
                    try {
                        progress.setBusy("Authorizing " + Ginj.getAppName() + "...");
                        //setBusy(true);
                        final AbstractOAuth2Exporter exporter = (AbstractOAuth2Exporter) settings.get(TargetPrefs.EXPORTER_KEY);
                        OAuthAccount account = exporter.authorize();
                        if (account != null) {
                            account.setId(UUID.randomUUID().toString());
                            //noinspection unchecked
                            settings.put(TargetPrefs.ACCOUNT_KEY, account);
                            isOK = true;
                        }
                    }
                    catch (AuthorizationException | CommunicationException ex) {
                        UI.alertException(OAuthAuthorizePage.this, "", "", ex);
                    }
                    finally {
                        setBusy(false);
                    }
                    if (isOK) {
                        //WizardPanelNavResult.REMAIN_ON_PAGE;
                        progress.finished(WizardPanelNavResult.PROCEED);
                    }
                    else {
                        //progress.finished(result);
                        progress.failed("Could not get authorization", false);
                    }
                }
            };
        }
    }

    public static class GoogleAuthorizePage extends OAuthAuthorizePage {

        public GoogleAuthorizePage() {
            super(GOOGLE_AUTHORIZE_STEP, "Authorize");
        }

        @Override
        protected String getHelpText() {
            return "To be able to upload captures to Google Photos, you must authorize " + Ginj.getAppName() + " to access your account.\n" + Ginj.getAppName() + " requests access to:\n" +
                    "- read your profile to retrieve your name and e-mail\n" +
                    "- create and share an album\n" +
                    "- upload captures to that album.\n" +
                    "Clicking the 'Next' button will open a browser to ask for your authorization (login may be required), and you will be taken to the next step once " + Ginj.getAppName() + " has been authorized.";
        }

        @Override
        protected ImageIcon getConnectIcon() {
            return UI.createIcon(getClass().getResource("/img/logo/googlephotos_connect.png"), 300);
        }
    }


    private static class DropboxAuthorizePage extends OAuthAuthorizePage {

        public DropboxAuthorizePage() {
            super(DROPBOX_AUTHORIZE_STEP, "Authorize");
        }

        @Override
        protected ImageIcon getConnectIcon() {
            return UI.createIcon(getClass().getResource("/img/logo/dropbox_connect.png"), 300);
        }

        @Override
        protected String getHelpText() {
            return "To be able to upload captures to Dropbox, you must authorize " + Ginj.getAppName() + " to access your account.\n" + Ginj.getAppName() + " requests access to:\n" +
                    "- read your profile to retrieve your name and e-mail\n" +
                    "- upload captures to your Dropbox (under Applications > " + Ginj.getAppName() + ").\n" +
                    "- create shared links to those captures\n" +
                    "Clicking the 'Next' button will open a browser to ask for your authorization (login may be required), and you will be taken to the next step once " + Ginj.getAppName() + " has been authorized.";
        }
    }

    public static class GooglePhotosConfigurationPage extends WizardPage {
        public GooglePhotosConfigurationPage() {
            super(GOOGLE_PHOTOS_CONFIGURE_STEP, "Configure");
        }

        @SuppressWarnings("rawtypes")
        @Override
        public WizardPanelNavResult allowBack(String stepName, Map settings, Wizard wizard) {
            // Prevent back button because re-authenticating throws an exception. TODO see why...
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }

        @Override
        protected void renderingPage() {
            final Exporter exporter = (Exporter) getWizardData(TargetPrefs.EXPORTER_KEY);
            final OAuthAccount account = (OAuthAccount) getWizardData(TargetPrefs.ACCOUNT_KEY);

            JPanel intermediatePanel = new JPanel();
            intermediatePanel.setPreferredSize(DETAIL_PANEL_PREFERRED_SIZE);

            if (exporter != null && account != null) {
                JPanel fieldsPanel = UI.getFieldPanel(
                        "Username:", UI.getWizardTextField(TargetPrefs.ACCOUNT_USERNAME_KEY, account.getName(), false, true),
                        "Email:", UI.getWizardTextField(TargetPrefs.ACCOUNT_EMAIL_KEY, account.getEmail(), false, true),
                        "Display as:", UI.getWizardTextField(TargetPrefs.DISPLAY_NAME_KEY, exporter.getDefaultShareText() + " (" + account.getEmail() + ")", true, true),
                        "Create one album:", UI.getWizardList(ExportSettings.ALBUM_GRANULARITY_KEY, GooglePhotosExporter.Granularity.values(), 0, true, true),
                        "Share album:", UI.getWizardCheckBox(ExportSettings.MUST_SHARE_KEY, true, true, true),
                        "Copy link to clipboard:", UI.getWizardCheckBox(ExportSettings.MUST_COPY_PATH_KEY, true, true, true)
                );

                intermediatePanel.add(fieldsPanel);
            }
            add(intermediatePanel);
        }
    }

    private static class DropboxConfigurationPage extends WizardPage {
        public DropboxConfigurationPage() {
            super(DROPBOX_CONFIGURE_STEP, "Configure");
        }

        @Override
        protected void renderingPage() {
            final Exporter exporter = (Exporter) getWizardData(TargetPrefs.EXPORTER_KEY);
            final OAuthAccount account = (OAuthAccount) getWizardData(TargetPrefs.ACCOUNT_KEY);

            JPanel intermediatePanel = new JPanel();
            intermediatePanel.setPreferredSize(DETAIL_PANEL_PREFERRED_SIZE);

            if (exporter != null && account != null) {
                JPanel fieldsPanel = UI.getFieldPanel(
                        "Username:", UI.getWizardTextField(TargetPrefs.ACCOUNT_USERNAME_KEY, account.getName(), false, true),
                        "Email:", UI.getWizardTextField(TargetPrefs.ACCOUNT_EMAIL_KEY, account.getEmail(), false, true),
                        "Display as:", UI.getWizardTextField(TargetPrefs.DISPLAY_NAME_KEY, exporter.getDefaultShareText() + " (" + account.getEmail() + ")", true, true),
                        "Share capture:", UI.getWizardCheckBox(ExportSettings.MUST_SHARE_KEY, true, true, true),
                        "Copy link to clipboard:", UI.getWizardCheckBox(ExportSettings.MUST_COPY_PATH_KEY, true, true, true)
                );

                intermediatePanel.add(fieldsPanel);
            }
            add(intermediatePanel);
        }
    }

    public static class ClipboardConfigurationPage extends WizardPage {
        public ClipboardConfigurationPage() {
            super(CLIPBOARD_CONFIGURE_STEP, "Configure");
        }

        @Override
        protected void renderingPage() {
            String defaultName = "";

            final Exporter exporter = (Exporter) getWizardData(TargetPrefs.EXPORTER_KEY);
            if (exporter != null) {
                defaultName = exporter.getDefaultShareText();
            }

            JPanel fieldsPanel = UI.getFieldPanel(
                    "Display as:", UI.getWizardTextField(TargetPrefs.DISPLAY_NAME_KEY, defaultName, true, true)
            );


            JPanel intermediatePanel = new JPanel();
            intermediatePanel.setPreferredSize(DETAIL_PANEL_PREFERRED_SIZE);

            intermediatePanel.add(fieldsPanel);
            add(intermediatePanel);
        }
    }

    public static class DiskConfigurationPage extends WizardPage {

        public DiskConfigurationPage() {
            super(DISK_CONFIGURE_STEP, "Configure");
        }

        @Override
        protected void renderingPage() {
            String defaultName = "";

            final Exporter exporter = (Exporter) getWizardData(TargetPrefs.EXPORTER_KEY);
            if (exporter != null) {
                defaultName = exporter.getDefaultShareText();
            }
            String defaultSaveDir = (String) getWizardData(ExportSettings.DEST_LOCATION_KEY);
            if (defaultSaveDir == null) {
                // Return the Desktop for Windows, or home dir otherwise, like the FileChooser does.
                defaultSaveDir = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
            }


            JPanel fieldsPanel = UI.getFieldPanel(
                    "Display as:", UI.getWizardTextField(TargetPrefs.DISPLAY_NAME_KEY, defaultName, true, true),
                    "Save to:", UI.getWizardTextField(ExportSettings.DEST_LOCATION_KEY, defaultSaveDir, true, true),
                    "Always ask:", UI.getWizardCheckBox(ExportSettings.MUST_ALWAYS_ASK_LOCATION_KEY, false, true, true),
                    "Remember last dir:", UI.getWizardCheckBox(ExportSettings.MUST_REMEMBER_LAST_LOCATION_KEY, true, true, true),
                    "Copy path to clipboard:", UI.getWizardCheckBox(ExportSettings.MUST_COPY_PATH_KEY, true, true, true)
            );


            JPanel intermediatePanel = new JPanel();
            intermediatePanel.setPreferredSize(DETAIL_PANEL_PREFERRED_SIZE);

            intermediatePanel.add(fieldsPanel);
            add(intermediatePanel);
        }
    }

    @SuppressWarnings("rawtypes")
    public static class TextResultProducer implements WizardPage.WizardResultProducer {

        public TextResultProducer() {
        }

        @Override
        public Object finish(Map map) {
            try {
                final Exporter exporter = (Exporter) map.get(TargetPrefs.EXPORTER_KEY);
                if (exporter != null) {
                    // Persist
                    Target target = new Target();
                    target.setExporter(exporter);
                    target.setAccount((Account) map.get(TargetPrefs.ACCOUNT_KEY));
                    target.setDisplayName((String) map.get(TargetPrefs.DISPLAY_NAME_KEY));

                    // Collect non-generic keys in a "settings" map
                    List<Object> genericKeys = Arrays.asList(TargetPrefs.GENERIC_KEYS);
                    Map<String,Object> settingsMap = new HashMap<>();
                    for (Object key : map.keySet()) {
                        Object value = map.get(key);
                        if (value != null && !genericKeys.contains(key)) {
                            settingsMap.put((String)key, value);
                        }
                    }
                    // Inject those "settings" from map into a Settings object
                    final ExportSettings settings = new ExportSettings();
                    settings.moveFromMap(settingsMap);
                    // And remember it
                    target.setSettings(settings);

                    // Safeguard
                    if (!settingsMap.isEmpty()) {
                        System.err.println("Unhandled settings in map: " + settingsMap);
                    }

                    final TargetPrefs targetPrefs = Ginj.getTargetPrefs();
                    targetPrefs.getTargetList().add(target);
                    targetPrefs.save();
                    starWindow.notifyTargetListChange();

                    return Summary.create("The following target was configured successfully:\n\n" + map.get(TargetPrefs.DISPLAY_NAME_KEY) + "\n\n" + getAdditionalFinishText(map), map);
                }
                else {
                    System.err.println(map);
                    return Summary.create("There was an error saving target, no exporter in map.", map);
                }
            }
            catch (Exception e) {
                System.err.println(map);
                e.printStackTrace();
                return Summary.create("There was an error saving target, please check the console", map);
            }
        }

        /**
         * Can be overridden to add optional text to the "success" box
         */
        protected String getAdditionalFinishText(Map map) {
            return "";
        }

        @Override
        public boolean cancel(Map settings) {
            return true;
        }
    }

    private static class OAuthAccountResultProducer extends TextResultProducer implements WizardPage.WizardResultProducer {
        @Override
        @SuppressWarnings("rawtypes")
        protected String getAdditionalFinishText(Map map) {
            return "You can revoke " + Ginj.getAppName() + " authorizations at any time by visiting " + ((AbstractOAuth2Exporter) map.get(TargetPrefs.EXPORTER_KEY)).getOAuth2RevokeUrl();
        }
    }

    private static class GooglePhotosAccountResultProducer extends TextResultProducer implements WizardPage.WizardResultProducer {
        @Override
        @SuppressWarnings("rawtypes")
        protected String getAdditionalFinishText(Map map) {
            return "Please note that uploaded files will NOT be recompressed by Google Photos and their size will thus be counted towards your quota.\n\n" +
                    "You can revoke " + Ginj.getAppName() + " authorizations at any time by visiting " + ((AbstractOAuth2Exporter) map.get(TargetPrefs.EXPORTER_KEY)).getOAuth2RevokeUrl();
        }
    }

}
