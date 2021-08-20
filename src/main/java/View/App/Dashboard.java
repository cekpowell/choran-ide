package View.App;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import java.io.File;

import View.Editor.*;
import View.FileStore.FileStore;
import View.Terminal.*;

/**
 * The main view for the application - contains an Editor, Terminal
 * and FileStore
 */
public class Dashboard extends BorderPane{
    // static variables
    private static final double editorDividerRatio = 0.67;
    private static final double filestoreDividerRatio = 0.88;
    
    // member variables
    private Editor editor;
    private Terminal terminal;
    private FileStore fileStore;

    /**
     * Class constructor.
     */
    public Dashboard(){
        // initializing
        this.editor = new Editor(this);
        this.terminal = new Terminal(this);
        this.fileStore = new FileStore(this);

        ///////////////////////////
        // CONTAINERS AND EXTRAS //
        ///////////////////////////

        // splitpane for editor and terminal
        SplitPane editAndTerminal = new SplitPane(this.editor,this.terminal);
        editAndTerminal.setDividerPositions(editorDividerRatio);

        // splitpane for edittor + terminal and filestore
        SplitPane editAndTerminalAndFileStore = new SplitPane(editAndTerminal, this.fileStore);
        editAndTerminalAndFileStore.setDividerPositions(filestoreDividerRatio);
        editAndTerminalAndFileStore.setOrientation(Orientation.VERTICAL);

        /////////////////
        // CONFIGURING //
        /////////////////

        // adding sections to dashboard
        this.setCenter(editAndTerminalAndFileStore);
    }


    /////////////////////////
    // GETTERS AND SETTERS //
    /////////////////////////


    public Editor getEditor(){
        return this.editor;
    }

    public Terminal getTerminal(){
        return this.terminal;
    }

    public FileStore getFileStore(){
        return this.fileStore;
    }
}
