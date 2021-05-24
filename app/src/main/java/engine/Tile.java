package engine;

import processing.core.PConstants;

import java.util.Map;
import java.util.Objects;

/**
 * The tile class. Stores information regarding a specific tile, such as its type and state.
 */
public class Tile {
    /**
     * The tile type.
     */
    private final Type type;
    /**
     * The initial state.
     */
    private final int initialState;
    /**
     * The current state.
     */
    private int state;
    /**
     * Declares if lasers (currently) collide with this tile.
     */
    private boolean collision;

    /**
     * The initial collision as set by the constructor.
     */
    private final boolean initialCollision;

    /**
     * Instantiates a new tile using a given type and state.
     *
     * @param type  the tile type.
     * @param state the tile state (usually rotation, refer to the Tile.Type enum for more information)
     */
    private Tile(Tile.Type type, int state) {
        this.type = type;
        this.state = this.initialState = state;
        this.collision = this.initialCollision = Objects.requireNonNullElseGet(type.getCollision(), () -> state == 1);
    }

    /**
     * Instantiates a new tile with pre-determined information. Used exclusively for cloning tiles.
     *
     * @param type         the type
     * @param initialState the initial state
     * @param currentState the current state
     * @param collision    the collision
     */
    private Tile(Tile.Type type, int initialState, int currentState, boolean collision) {
        this(type, initialState);
        this.state = currentState;
        this.collision = collision;
    }

    /**
     * of-Method, the only way to create new instances of Tile.
     *
     * @param type  the tile type.
     * @param state the initial state (usually rotation, refer to the Tile.Type enum for more information)
     * @return the resulting tile.
     */
    public static Tile of(Tile.Type type, int state) {
        return new Tile(type, state);
    }

    /**
     * Gets tile type.
     *
     * @return the type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets tile state.
     *
     * @return the state.
     */
    public int getState() {
        return state;
    }

    /**
     * Resets tile state to its initial state.
     */
    public void resetState() {
        state = initialState;
        collision = initialCollision;
    }

    /**
     * Gets the collision.
     *
     * @return true, if the tile has collision.
     */
    public boolean getCollision() {
        return collision;
    }

    /**
     * Interacts with the tile.
     *
     * @param button the encoded mouse button.
     * @param tiles  the tiles. Needed to update potential side effects (such as when a switch is pressed)
     */
    public void interact(int button, Map<Pair<Integer, Integer>, Tile> tiles) {
        switch (this.type) {
            case MIRROR -> state = button == PConstants.LEFT ? (state + 3) % 4 : (state + 1) % 4;

            case SWITCH_CYAN, SWITCH_YELLOW, SWITCH_MAGENTA -> tiles.values().stream()
                    .filter(t -> t.type.equals(this.type))
                    .forEach(t -> {
                        t.state = (t.state + 1) % 2;
                        t.collision = !t.collision;
                    });

            case SWITCH_RED, SWITCH_GREEN, SWITCH_BLUE -> {
                state = (initialState + 1) % 2;
                collision = !initialCollision;
            }

            default -> throw new IllegalArgumentException("This tile cannot be interacted with.");
        }
    }

    /**
     * Clones the instance.
     *
     * @return new instance with the same values.
     */
    public Tile clone() {
        return new Tile(type, initialState, state, collision);
    }

    /**
     * The Tile Type enum. Describes the possible kinds of tile.
     */
    public enum Type {
        /**
         * Stone, a basic wall.
         */
        STONE(true, false),
        /**
         * Broken stone, basic wall with small cosmetic features.
         */
        STONE_BROKEN(true, false),
        /**
         * Target. Place where lasers need to go.
         */
        STONE_TARGET(true, false),

        /**
         * Red laser emitter.
         */
        LASER_RED(true, false),
        /**
         * Green laser emitter.
         */
        LASER_GREEN(true, false),
        /**
         * Blue laser emitter.
         */
        LASER_BLUE(true, false),

        /**
         * Basic floor tile.
         * <p>
         * Generally speaking, using this tile is bad practice as it will override the rotation effects
         * generated when drawing floor tiles under all tiles in a specific map.
         * <p>
         * Use Type NULL instead for encoding levels.
         */
        FLOOR(false, false),
        /**
         * Null type. Will draw an empty (fully transparent) image in frontend and otherwise act just like a floor tile.
         */
        NULL(false, false),
        /**
         * Mirror. Can be interacted with (see interact() method).
         */
        MIRROR(true, true),

        /**
         * Red switch. Activated when a red laser hits its target.
         */
        SWITCH_RED(null, false),
        /**
         * Blue switch. Activated when a blue laser hits its target.
         */
        SWITCH_BLUE(null, false),
        /**
         * Green switch. Activated when a green laser hits its target.
         */
        SWITCH_GREEN(null, false),
        /**
         * Cyan switch. Can be interacted with to switch state between solid and non-solid.
         */
        SWITCH_CYAN(null, true),
        /**
         * Yellow switch. Can be interacted with to switch state between solid and non-solid.
         */
        SWITCH_YELLOW(null, true),
        /**
         * Magenta switch. Can be interacted with to switch state between solid and non-solid.
         */
        SWITCH_MAGENTA(null, true);

        /**
         * Predetermined collision. Can be null if the collision is determined by the state.
         */
        private final Boolean collision,
        /**
         * Predetermined intractability.
         */
        canInteract;

        /**
         * Instantiates a new Type.
         *
         * @param collision   if tiles of this type have collision. May be null for undetermined (interchangable) collision.
         * @param canInteract if tiles of this type can be interacted with.
         */
        Type(Boolean collision, Boolean canInteract) {
            this.collision = collision;
            this.canInteract = canInteract;
        }

        /**
         * Gets switch by laser color.
         *
         * @param c the laser color.
         * @return the appropriate switch for the given laser color.
         */
        public static Type getSwitchByColor(Laser.Color c) {
            return switch (c) {
                case BLUE -> SWITCH_BLUE;
                case RED -> SWITCH_RED;
                case GREEN -> SWITCH_GREEN;
            };
        }

        /**
         * Determines if the instance is of type laser switch.
         *
         * @return true, if a switch which a laser can interact with.
         */
        public boolean isLaserSwitch() {
            return this == SWITCH_RED || this == SWITCH_GREEN || this == SWITCH_BLUE;
        }

        /**
         * Determines if the instance is of type laser source.
         *
         * @return true, if a laser source.
         */
        public boolean isLaserSource() {
            return this == LASER_RED || this == LASER_GREEN || this == LASER_BLUE;
        }

        /**
         * Getter for canInteract variable.
         *
         * @return true, if canInteract.
         */
        public Boolean canInteract() {
            return canInteract;
        }

        /**
         * Getter for collision variable.
         *
         * @return true, if collision. May be null.
         */
        public Boolean getCollision() {
            return collision;
        }
    }
}
