package main;

import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles storage, initialisation and drawing of all Images.
 * Each Image can have multiple states, representing rotation or a boolean-like state.
 * Images are initialised with the static method initialise(), which will use the instance
 * of App to load all Images into memory.
 */
enum Image {
    /**
     * Stone wall.
     */
    STONE("stone_clean.png"),
    /**
     * Broken stone wall.
     */
    STONE_BROKEN("stone_broken1_0.png", "stone_broken1_90.png", "stone_broken1_180.png", "stone_broken1_270.png"),
    /**
     * Stone target.
     */
    STONE_TARGET("stone_target.png"),

    /**
     * Stone with red laser source.
     */
    LASER_RED("stone_laser_red.png"),
    /**
     * Stone with green laser source.
     */
    LASER_GREEN("stone_laser_green.png"),
    /**
     * Stone with blue laser source.
     */
    LASER_BLUE("stone_laser_blue.png"),

    // Floor blocks
    /**
     * Floor tile.
     */
    FLOOR("floor_0.png", "floor_90.png", "floor_180.png", "floor_270.png"),
    /**
     * Null image - contains no pixels and is used for the standard floor to enable automatic floor pattern generation.
     */
    NULL("null.png"),

    /**
     * Transparent images representing the mirror.
     */
    MIRROR("mirror_0.png", "mirror_90.png", "mirror_180.png", "mirror_270.png"),

    /**
     * Red (laser) switch.
     */
    SWITCH_RED("floor_red.png", "stone_red.png"),
    /**
     * Blue (laser) switch.
     */
    SWITCH_BLUE("floor_blue.png", "stone_blue.png"),
    /**
     * Green (laser) switch.
     */
    SWITCH_GREEN("floor_green.png", "stone_green.png"),
    /**
     * Cyan (intractable) switch.
     */
    SWITCH_CYAN("floor_cyan.png", "stone_cyan.png"),
    /**
     * Yellow (intractable) switch.
     */
    SWITCH_YELLOW("floor_yellow.png", "stone_yellow.png"),
    /**
     * Magenta (intractable) switch.
     */
    SWITCH_MAGENTA("floor_magenta.png", "stone_magenta.png"),

    MEDAL("gold.png", "silver.png", "bronze.png", "none.png");

    /**
     * The Image path. Allows easily changing the folder location of all images.
     */
    static private final String IMAGE_PATH = "img/";
    /**
     * The App. Is stored to later enable usage of draw().
     */
    static private App app = null;
    /**
     * Stores the filenames of any given Image instance.
     */
    private final String[] filenames;
    /**
     * Storage of all images relating to that instance, once they have been initialized.
     */
    private final List<PImage> images = new ArrayList<>();

    /**
     * When initialising an image with four image paths, the tile-state is considered the four directions, or the medal image, these are instead considered as gold - silver - bronze - none.
     * <p>
     * When initialised with two paths, they represent a boolean-like image.
     * <p>
     * When given only one path, this image is used for all states (or this image has no states).
     *
     * @param paths the given file paths, one for each possible state (by default)
     */
    Image(String... paths) {
        filenames = paths;
    }

    /**
     * Initialises all images and stores the PImages in the images ArrayList.
     *
     * @param a instance of the app, needed to load images.
     */
    public static void initialise(App a) {
        app = a;
        Arrays.stream(Image.values())
                .forEach(i -> Arrays.stream(i.filenames).forEach(s -> i.images.add(app.loadImage(IMAGE_PATH + s))));
    }

    /**
     * Draws an image at a specific position with a specific state.
     *
     * @param p     Position on canvas.
     * @param state value of the state to draw.
     */
    public void draw(PVector p, int state) {
        app.image(images.get(state % images.size()), p.x, p.y, app.getTileSize(), app.getTileSize());
    }

    /**
     * Gets the list of images for this specific instance.
     *
     * @return the list of images.
     */
    public List<PImage> getImages() {
        return images;
    }

}
