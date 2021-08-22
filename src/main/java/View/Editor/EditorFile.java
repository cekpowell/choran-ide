package View.Editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

import Controller.SystemController;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import View.Tools.ConfirmationWindow;
import View.Tools.ErrorAlert;

/**
 * Represents a file that is open within the editor.
 */
public abstract class EditorFile extends Tab{
    
    // constants
    private static final Image programImage = new Image("program.png");
    private static final Image programUnsavedImage = new Image("program-unsaved.png");
    private static final Image tableImage = new Image("table.png");
    private static final Image tableUnsavedImage = new Image("table-unsaved.png");
    private static final String unsavedLabel = "(Unsaved)";
    private static final KeyCombination keyCombCtrS = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination keyCombCtrR = new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN);
    
    // member variables
    private EditorFileType type;
    private FileContainer fileContainer;
    private File file;
    private String name;
    private EditorFileToolbar toolbar;
    private TextArea textArea;
    private String textAtLastSave;
    private boolean unsavedChanges;

    /**
     * Class constructor. Creates empty EditorFile instance with the provided file name.
     * 
     * @param fileContainer The FileContainer associated with this EditorFile.
     * @param name The name of the EditorFile.
     */
    public EditorFile(EditorFileType type, FileContainer fileContainer, String name){
        // initializing
        this.file = null;
        this.name = name;
        this.toolbar = new EditorFileToolbar(this, type);
        this.textArea = new TextArea();
        this.textAtLastSave = new String();
        this.unsavedChanges = true;
        this.init(type, fileContainer);

        // Configuring Member Variables //

        // setting tab graphic 
        if(this.type == EditorFileType.PROGRAM){
            this.setGraphic(new ImageView(programUnsavedImage));
        }
        else{
            this.setGraphic(new ImageView(tableUnsavedImage));
        }

        /////////////////
        // CONFIGURING //
        /////////////////

        this.setText(this.name + unsavedLabel);
    }

    /**
     * Class constructor. Creates new EditorFile instance from existing File object.
     * 
     * @param fileContainer The FileContainer associated with this EditorFile.
     * @param file The File object associated with the file.
     * @param runnable Boolean representing if the file is runable or not.
     */
    public EditorFile(EditorFileType type, FileContainer fileContainer, File file){
        // initializing
        this.type = type;
        this.fileContainer = fileContainer;
        this.file = file;
        this.name = file.getName();
        this.toolbar = new EditorFileToolbar(this, type);
        this.textArea = new TextArea();
        this.unsavedChanges = false;

        // Configuring Member Variables //

        // setting tab graphic 
        if(this.type == EditorFileType.PROGRAM){
            this.setGraphic(new ImageView(programImage));
        }
        else{
            this.setGraphic(new ImageView(tableImage));
        }

        // Adding the file content to the text area //
        try{
            String content = this.getFileContent();
            
            // setting the text area
            this.textArea.setText(content);

            // setting the text at last save
            this.textAtLastSave = content;
        }
        // handling error
        catch(Exception e){
            ErrorAlert.showErrorAlert(this.fileContainer.getScene().getWindow(), e);
        }

        // running init method
        this.init(type, fileContainer);

        /////////////////
        // CONFIGURING //
        /////////////////

        this.setText(this.name);
    }

    /**
     * Initalizer method due to multiple constructors.
     */
    public void init(EditorFileType type, FileContainer fileContainer){
        // initialzing member variables
        this.type = type;
        this.fileContainer = fileContainer;
        
        ///////////////////////////
        // CONTAINERS AND EXTRAS //
        ///////////////////////////

        BorderPane container = new BorderPane();
        container.setTop(this.toolbar);
        container.setCenter(this.textArea);

        /////////////////
        // CONFIGURING //
        /////////////////

        // content
        this.toolbar.setPadding(new Insets(10));
        this.setContent(container);

        /////////////
        // ACTIONS //
        /////////////

        // Tab Selection
        this.setOnSelectionChanged((e) ->{
            this.fileContainer.setCurrentEditorFile(this);
        });

        // Tab Close
        this.setOnCloseRequest((e) -> {
            // Unsaved Changes //
            if(this.unsavedChanges){
                // confirming the close
                boolean confirmClosed = ConfirmationWindow.showConfirmationWindow(this.fileContainer.getScene().getWindow(), 
                                                                                "Close Unsaved File", 
                                                                                "Are you sure you want to close '" + this.name + "' without saving?");

                if(confirmClosed){
                    // removing the program from the container
                    this.fileContainer.removeEditorFile(this);
                }
                else{
                    e.consume();
                }
            }
            // Saved //
            else{
                // removing the program from the container
                this.fileContainer.removeEditorFile(this);
            }
        });

        // Key Pressed
        this.textArea.setOnKeyPressed((e) -> {
            // CTRL + S 
            if(keyCombCtrS.match(e)){
                // saving file
                this.save();
            }
            // CTRL + R
            else if(keyCombCtrR.match(e)){
                // running file
                this.run();
            }
        });

        // Text area text changed
        this.textArea.textProperty().addListener((e) -> {
            // text has changed
            if(!this.textAtLastSave.equals(this.textArea.getText())){
                // there are no unsaved changes yet - need to update tab information
                if(!this.unsavedChanges){
                    this.unsavedChanges = true;
                    this.setText(this.name + unsavedLabel);

                    // setting tab graphic 
                    if(this.type == EditorFileType.PROGRAM){
                        this.setGraphic(new ImageView(programUnsavedImage));
                    }
                    else{
                        this.setGraphic(new ImageView(tableUnsavedImage));
                    }
                }
           }
           // text has gone back to last save
           else{
                // there were previously unsaved changes - need to update tab information
                if(this.unsavedChanges){
                    // upudating tab information
                    this.unsavedChanges = false;
                    this.setText(this.name);

                    // setting tab graphic 
                    if(this.type == EditorFileType.PROGRAM){
                        this.setGraphic(new ImageView(programImage));
                    }
                    else{
                        this.setGraphic(new ImageView(tableImage));
                    }
               }
           }
       });
    }

    //////////////////////////
    // READING FILE CONTENT //
    //////////////////////////

    /**
     * Returns the content stored within the file associated with the program at the time 
     * of the method being called.
     * 
     * @return The content from within the file as a String. 
     */
    public String getFileContent() throws Exception{
        // setting up file reader
        BufferedReader reader = new BufferedReader(new FileReader(this.file));

        // iterating through the file
        String content = "";
        while(reader.ready()){
            // getting next line
            String line = reader.readLine();

            // buildiing the content
            content += line + "\n";
        }

        // closiing the reader
        reader.close();

        // returning the content
        return content;
    }

    ////////////
    // SAVING //
    ////////////

    /**
     * Saves the program into a new file.
     */
    public void saveAs(){
        // getting the file to save the program to
        File chosenFile = this.getSaveFile();

        if(chosenFile != null){
            try{
                // attaching the chosen file
                this.file = chosenFile;

                // updating filestore if necesarry
                if(this.type == EditorFileType.TABLE){
                    SystemController.addTableToStore((Table) this);
                }

                // saving program
                this.writeContentToFile(this.file);
            }
            // handling error
            catch (Exception e) {
                this.file = null;
                ErrorAlert.showErrorAlert(this.fileContainer.getScene().getWindow(), e);
            }
        }
    }

    /**
     * Saves the content into it's file. If the EditorFile does not have a File,
     * a pop-up is displayed that allows the user to select one.
     */
    public void save(){

        // EditorFile Has File //

        if(this.file != null){
            // saving program
            this.writeContentToFile(this.file);
        }

        // EditorFile Has No File //

        else{
            // getting the file to save the program to
            File chosenFile = this.getSaveFile();

            if(chosenFile != null){
                try{
                    this.file = chosenFile;

                    // updating filestore if necesarry
                    if(this.type == EditorFileType.TABLE){
                        SystemController.addTableToStore((Table) this);
                    }

                    // saving program
                    this.writeContentToFile(this.file);
                }
                // handling error
                catch (Exception e) {
                    this.file = null;
                    ErrorAlert.showErrorAlert(this.fileContainer.getScene().getWindow(), e);
                }
            }
        }
    }

    /**
     * Writes the content of the EditorFile into the provided File.
     * 
     * @param file The File the EditorFile is being written into.
     * @return True if the file was writen, false if not.
     */
    private void writeContentToFile(File file){
        try {
            // writing program content to file
            OutputStream out = new FileOutputStream(file);
            out.write(this.textArea.getText().getBytes());
            out.close();

            // updating tab information
            this.unsavedChanges = false;
            this.name = file.getName();
            this.setText(this.name);
            this.textAtLastSave = this.textArea.getText();

            // setting tab graphic 
            if(this.type == EditorFileType.PROGRAM){
                this.setGraphic(new ImageView(programImage));
            }
            else{
                this.setGraphic(new ImageView(tableImage));
            }
        }
        // handling error
        catch (Exception e) {
            ErrorAlert.showErrorAlert(this.fileContainer.getScene().getWindow(), e);
        }
    }

    /**
     * Helper method to save an EditorFile that has not yet been saved. Opens
     * a file choser dialog and allows the user to select where the EditorFile will
     * be saved.
     * 
     * @param initialFilename The initial name of the file being saved.
     * @return The selected file if one was chosen, null if it was not.
     */
    private File getSaveFile(){
        // setting up the file choser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(this.name);
        fileChooser.getExtensionFilters().addAll(this.type.getExtensionFilter());

        // showing the saving dialog
        return fileChooser.showSaveDialog(this.fileContainer.getScene().getWindow());
    }

    /////////////
    // ZOOMING //
    /////////////

    /**
     * Zooms in to the editor.
     */
    public void zoomIn(){
        // TODO
    }

    /**
     * Zooms-out of the editor.
     */
    public void zoomOut(){
        // TODO
    }

    /////////////
    // RUNNING //
    /////////////

    /**
     * Runs the EditorFile (if it is runnable).
     */
    public abstract void run();

    /////////////////////////
    // GETTERS AND SETTERS //
    /////////////////////////

    public FileContainer getFileContainer(){
        return this.fileContainer;
    }

    public String getName(){
        return this.name;
    }

    public TextArea getTextArea(){
        return this.textArea;
    }

    public File getFile(){
        return this.file;
    }

    //////////////////////
    // EDITOR FILE TYPE //
    //////////////////////

    /**
     * Enumeration class for the type of Client object that can exist within the 
     * system. 
     * 
     * These Client types are specific to the Distributed Data Store system.
     */
    public enum EditorFileType {
        // types
        PROGRAM(new ExtensionFilter("CSVQL Program (*.cql)", "*.cql")), // Program 
        TABLE(new ExtensionFilter("Table (*.csv, *.tsv, *.txt", "*.csv", "*.txt", "*.tsv")); // Table 

        private ExtensionFilter extensionFilter;

        private EditorFileType(ExtensionFilter extensionFilter){
            this.extensionFilter = extensionFilter;
        }


        public ExtensionFilter getExtensionFilter(){
            return this.extensionFilter;
        }
    }
}