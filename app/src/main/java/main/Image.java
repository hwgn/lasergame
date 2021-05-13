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
public enum Image {
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
    LASER_RED("stone_laser_red.png", "stone_laser_red.png", "stone_laser_red.png", "stone_laser_red.png"),
    /**
     * Stone with green laser source.
     */
    LASER_GREEN("stone_laser_green.png", "stone_laser_green.png", "stone_laser_green.png", "stone_laser_green.png"),
    /**
     * Stone with blue laser source.
     */
    LASER_BLUE("stone_laser_blue.png", "stone_laser_blue.png", "stone_laser_blue.png", "stone_laser_blue.png"),

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
    SWITCH_MAGENTA("floor_magenta.png", "stone_magenta.png");

    /**
     * The Image path. Allows easily changing the folder location of all images.
     */
    static private final String IMAGE_PATH = "img/";
    /**
     * The App. Is stored to later enable usage of draw().
     */
    static App app = null;
    /**
     * Tile size. Must be updated when the image is initialized.
     */
    static int tileSize = 0;

    /**
     * Stores the filenames of any given Image instance.
     */
    String[] filenames;
    /**
     * Storage of all images relating to that instance, once they have been initialized.
     */
    List<PImage> images = new ArrayList<>();

    /**
     * When initialising an Image with four image paths, the Tile-State is considered the four directions.
     *
     * @param north Image for Tile facing north / 0 degrees rotation.
     * @param east  Image for Tile facing east / 90 degrees rotation.
     * @param south Image for Tile facing south / 180 degrees rotation.
     * @param west  Image for Tile facing west / 270 degrees rotation.
     */
    Image(String north, String east, String south, String west) {
        filenames = new String[]{north, east, south, west};
    }

    /**
     * An Image initialised with two image image paths represents a boolean-like Image
     *
     * @param disabled Image representing the disabled state.
     * @param enabled  Image representing the enabled state.
     */
    Image(String disabled, String enabled) {
        filenames = new String[]{disabled, enabled};
    }

    /**
     * An image initialised with only one image path is used for static tiles.
     *
     * @param all Image representing all possible states.
     */
    Image(String all) {
        filenames = new String[]{all};
    }

    /**
     * Initialises all images and stores the PImages in the images ArrayList.
     *
     * @param a    instance of the app, needed to load images.
     * @param size size at which images should be drawn at.
     */
    public static void initialise(App a, int size) {
        app = a;
        tileSize = size;
        Arrays.stream(Image.values()).forEach(i ->
                Arrays.stream(i.filenames).forEach(s -> i.images.add(app.loadImage(IMAGE_PATH + s))));
    }

    /**
     * Draws an image at a specific position with a specific state.
     *
     * @param p     Position on canvas.
     * @param state value of the state to draw.
     */
    public void draw(PVector p, int state) {
        if (state >= images.size())
            throw new IllegalArgumentException("Tried to fetch image for nonexistent state!");
        app.image(images.get(state), p.x, p.y, tileSize, tileSize);
    }

    /**
     * Gets image of specific state.
     *
     * @return The image.
     */
    public PImage getImage(int i) {
        return images.get(i);
    }
}
