package main;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

import com.melloware.jintellitype.JIntellitype;
import javafx.animation.FadeTransition;
import javafx.scene.image.Image;
import javafx.util.Duration;
import main.model.Movie;
import main.model.MovieListWrapper;
import main.view.MediaViewController;
import main.view.MovieEditDialogController;
import main.view.MovieOverviewController;
import main.view.WelcomeController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import static main.util.VideoDurationUtil.ReadVideoDuration;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private static final String moviesPath = "src/movies";
    private static final String infoPath = "info/MoviesInfo.xml";


    private String movieURL;
    private ResourceBundle resourceBundle;
    /**
     * The data as an observable list of movies.
     */
    private final ObservableList<Movie> movieData = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public MainApp() {
    }

    /**
     * Traverse the movies directory and read the properties file to initialize movieData.
     */
    private void initMovieData() throws Exception {
        File movies_directory = new File(moviesPath);
        if (!movies_directory.isDirectory()) {
            throw new Exception("movies_directory is not a directory");
        }

        File[] movie_list = movies_directory.listFiles();
        if (movie_list == null) {
            throw new Exception("movie_list is null");
        }


        File movieFile = new File(infoPath);
        if (movieFile.exists()) {
            loadMovieDataFromFile(movieFile);
        }
        ArrayList<String> fileNamesInData = new ArrayList<>();
        ArrayList<String> fileNamesInDirector = new ArrayList<>();

        for (Movie movie : movieData) {
            fileNamesInData.add(movie.getFileName());
        }

        for (File movie_file : movie_list) {
            String fileName = movie_file.getName();
            fileNamesInDirector.add(fileName);
            if (!movie_file.isDirectory() && !fileNamesInData.contains(fileName)) {// 如果movie data里面已经存在这个文件，则不再读取了
                String fileData[] = fileName.split("_");
                try {
                    fileData[2] = fileData[2].substring(0, fileData[2].lastIndexOf('.')); // delete suffix
                } catch (Exception ignored) {
                }

                String duration;
                try { // 读取视频长度顺便检查类型
                    duration = ReadVideoDuration(movie_file);
                } catch (Exception e) {
                    continue;
                }

                if (fileData.length == 3) {

                    movieData.add(new Movie(fileName, fileData[0], fileData[1], fileData[2], duration));
                } else {
                    movieData.add(new Movie(fileName, fileName, duration));
                }
            }
        }

        movieData.removeIf(movie -> !fileNamesInDirector.contains(movie.getFileName()));
        saveMovieDataToFile(movieFile);
    }

    /**
     * Returns the data as an observable list of movies.
     *
     * @return the movie data in observable list
     */
    public ObservableList<Movie> getMovieData() {
        return movieData;
    }

    /**
     * initialize root layout and show welcome page  fcvg
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("In Flight System");
        this.primaryStage.getIcons().add(new Image("main/appearance/picture/logo.png"));
        this.primaryStage.setResizable(false);
        initRootLayout();
//        showMovieOverview();
        showWelcome();

        primaryStage.setOnCloseRequest(event -> JIntellitype.getInstance().cleanUp());
    }

    /**
     * Shows the welcome page inside the root layout.
     */
    public void showWelcome() {
        try {
            // Load movie overview.
            FXMLLoader loader = new FXMLLoader();

            loader.setLocation(MainApp.class.getResource("view/Welcome.fxml"));
            AnchorPane welcome = loader.load();

            FadeTransition ft2 = new FadeTransition(Duration.millis(1500), welcome);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            ft2.play();

            // Set movie overview into the center of root layout.
            rootLayout.setCenter(welcome);

            // Give the controller access to the main app.
            WelcomeController controller = loader.getController();
            controller.setMainApp(this);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the root layout.
     */
    private void initRootLayout() {
        try {

//        resourceBundle = mainApp.getResourceBundle();
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param locale the locale of movie overview
     * Shows the movie overview inside the root layout.
     */
    public void showMovieOverview(Locale locale) {

        try {
            initMovieData();
            // Load movie overview.
            FXMLLoader loader = new FXMLLoader();

            String basename = "properties.config";
            loader.setResources(ResourceBundle.getBundle(basename, locale));

            loader.setLocation(MainApp.class.getResource("view/MovieOverview.fxml"));
            AnchorPane movieOverview = loader.load();
//                   movieOverview.getStylesheets().add(getClass().getResource("view/style.css").toExternalForm());

            FadeTransition ft2 = new FadeTransition(Duration.millis(1500), movieOverview);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            ft2.play();

            // Set movie overview into the center of root layout.
            rootLayout.setCenter(movieOverview);

            // Give the controller access to the main app.
            MovieOverviewController controller = loader.getController();
            controller.setResources(ResourceBundle.getBundle(basename, locale));
            this.resourceBundle = ResourceBundle.getBundle(basename, locale);
            controller.setMainApp(this);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the media view inside the root layout.
     * @param movieURL the movie url to play
     */
    public void showMediaView(String movieURL) {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MediaView.fxml"));
            AnchorPane mediaView = loader.load();
            this.movieURL = "movies/" + movieURL; //该路径为相对src的路径
//            primaryStage.setTitle("movie");

            rootLayout.setCenter(mediaView);
            MediaViewController controller = loader.getController();
            controller.setMainApp(this);
//            primaryStage.setFullScreen(true);
//全屏语句
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog to edit details for the specified movie. If the user
     * clicks OK, the changes are saved into the provided movie object and true
     * is returned.
     *
     * @param movie the movie object to be edited
     * @param locale the locale of movie edit dialog
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showMovieEditDialog(Movie movie, Locale locale) {
        try {
            // Load the fxml file and create a new stage for the popup movie.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MovieEditDialog.fxml"));

            String basename = "properties.config";
            loader.setResources(ResourceBundle.getBundle(basename, locale));

            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Movie");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Movie into the controller.
            MovieEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMovie(movie);
            controller.setMainApp(this);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads movie data from the specified file. The current movie data will
     * be replaced.
     *
     * @param file the XML file that store movie data
     */
    private void loadMovieDataFromFile(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(MovieListWrapper.class);
            Unmarshaller um = context.createUnmarshaller();

            // Reading XML from the file and unmarshalling.
            MovieListWrapper wrapper = (MovieListWrapper) um.unmarshal(file);

            movieData.clear();
            for (Movie movie : movieData) {
                movie.getFileName();
            }
            movieData.addAll(wrapper.getMovies());

            // Save the file path to the registry.
            saveMovieDataToFile(file);

        } catch (Exception e) { // catches ANY exception
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }
    }

    /**
     * Saves the current movie data to the specified file.
     *
     * @param file the XML file that store movie data
     */
    public void saveMovieDataToFile(File file) {
        try {
            JAXBContext context = JAXBContext
                    .newInstance(MovieListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our movie data.
            MovieListWrapper wrapper = new MovieListWrapper();
            wrapper.setMovies(movieData);

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);

            // Save the file path to the registry.
            setMovieFilePath(file);
        } catch (Exception e) { // catches ANY exception
            Dialogs.create().title("Error")
                    .masthead("Could not save data to file:\n" + file.getPath())
                    .showException(e);
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    private void setMovieFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
//            primaryStage.setTitle("AddressApp - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
//            primaryStage.setTitle("AddressApp");
        }
    }


    public String getMovieURL() {
        return movieURL;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Returns current resourceBundle
     *
     * @return resourceBundle
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}