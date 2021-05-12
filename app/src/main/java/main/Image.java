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
    // Wall blocks
    STONE("stone_clean.png"),
    STONE_BROKEN("stone_broken1_0.png", "stone_broken1_90.png", "stone_broken1_180.png", "stone_broken1_270.png"),
    STONE_TARGET("stone_target.png"),

    // Laser emitters
    LASER_RED("stone_laser_red.png", "stone_laser_red.png", "stone_laser_red.png", "stone_laser_red.png"),
    LASER_GREEN("stone_laser_green.png", "stone_laser_green.png", "stone_laser_green.png", "stone_laser_green.png"),
    LASER_BLUE("stone_laser_blue.png", "stone_laser_blue.png", "stone_laser_blue.png", "stone_laser_blue.png"),

    // Floor blocks
    FLOOR("floor_0.png", "floor_90.png", "floor_180.png", "floor_270.png"),
    NULL("null.png"), // empty block, to be used as standard floor in json files

    // Mirror
    MIRROR("mirror_0.png", "mirror_90.png", "mirror_180.png", "mirror_270.png"),

    // Switching blocks
    SWITCH_RED("floor_red.png", "stone_red.png"),
    SWITCH_BLUE("floor_blue.png", "stone_blue.png"),
    SWITCH_GREEN("floor_green.png", "stone_green.png"),
    SWITCH_CYAN("floor_cyan.png", "stone_cyan.png"),
    SWITCH_YELLOW("floor_yellow.png", "stone_yellow.png"),
    SWITCH_MAGENTA("floor_magenta.png", "stone_magenta.png"),

    // Backgrounds
    BG_DARK_GREEN("bg_dark_green.png"),
    BG_LIGHT_VIOLET("bg_light_violet.png");

    static private final String IMAGE_PATH = "img/";
    static App app = null;
    static int tileSize = 0;

    String[] filenames;
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

    public PImage getImage() {
        return images.get(0);
    }
}
