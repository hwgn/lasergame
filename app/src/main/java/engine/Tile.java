package engine;

import processing.core.PConstants;

import java.util.List;
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
     * The initial state (often rotation).
     */
    private final int initialState;
    /**
     * The initial collision as set by the constructor.
     */
    private final boolean initialCollision;
    /**
     * The current state.
     */
    private int state;
    /**
     * Declares if lasers (currently) collide with this tile.
     */
    private boolean collision;

    /**
     * Instantiates a new tile using a given type and state.
     *
     * @param type  the tile type.
     * @param state the tile state (usually rotation, refer to the {@link Tile.Type} enum for more information)
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
     * @param state the initial state (usually rotation, refer to the {@link Tile.Type} enum for more information)
     * @return the resulting tile.
     */
    protected static Tile of(Tile.Type type, int state) {
        return new Tile(type, state);
    }

    /**
     * Returns the position one step ahead of the given position, in the direction of the given rotation.
     *
     * @param pos      current position.
     * @param rotation current rotation.
     * @return new position one step into the given direction, from the given position.
     */
    private static Pair<Integer, Integer> getNextPosition(Pair<Integer, Integer> pos, int rotation) {
        Pair<Integer, Integer> move = List.of(Pair.of(0, -1), Pair.of(1, 0), Pair.of(0, 1), Pair.of(-1, 0)).get(rotation);

        return Pair.of(move.x() + pos.x(), move.y() + pos.y());
    }

    /**
     * Gets tile {@link #type}.
     *
     * @return the type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets tile {@link #state}.
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
     * Gets the {@link #collision}.
     *
     * @return true, if the tile has collision.
     */
    public boolean hasCollision() {
        return collision;
    }

    /**
     * Interacts with the tile. May result in different changes based on the {@link #type}.
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
                if (button != 0)
                    throw new IllegalArgumentException("This tile cannot be interacted with manually.");

                state = (initialState + 1) % 2;
                collision = !initialCollision;
            }

            case TUNNELS_LEFT, TUNNELS_RIGHT -> {
                this.state = (this.state + 1) % 2;
                this.collision = !this.collision;
            }

            default -> throw new IllegalArgumentException("This tile cannot be interacted with.");
        }
    }

    /**
     * Calculates the next position a laser would be at after leaving the tile from a specific direction.
     *
     * @param pos      the position of the tile itself, used to calculate the new position.
     * @param rotation the rotation the laser has entered the tile with.
     * @return the new position the laser will be at. Null, if the laser would not leave the tile.
     */
    protected Pair<Integer, Integer> getLaserStep(Pair<Integer, Integer> pos, int rotation) {

        if (this.collision) {
            if (this.type.equals(Type.MIRROR)) {
                if (this.state == rotation) rotation = (rotation + 1) % 4;
                else if (this.state == (rotation + 1) % 4) rotation = (3 + rotation) % 4;
                else return null;

            } else if (this.type.equals(Type.TUNNELS_LEFT)) {
                if (0 == rotation % 2) rotation = (rotation + 1) % 4;
                else rotation = (3 + rotation) % 4;

            } else if (this.type.equals(Type.TUNNELS_RIGHT)) {
                if (1 == rotation % 2) rotation = (rotation + 1) % 4;
                else rotation = (3 + rotation) % 4;

            } else if (!(getType().isLaserSource() && rotation == this.state))
                return null;
        }

        return getNextPosition(pos, rotation);
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
         * A chipped stone with a corner missing.
         */
        STONE_CHIPPED(true, false),

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
         * Use Type NULL instead for encoding levels, unless specific floor rotation is needed.
         */
        FLOOR(false, false),
        /**
         * Null type. Will draw an empty (fully transparent) image in frontend and otherwise act just like a floor tile.
         * <p>
         * Allows generations of floor pattern.
         */
        NULL(false, false),
        /**
         * Mirror. Can be interacted with (see interact() method).
         */
        MIRROR(true, true),

        TUNNELS_LEFT(null, true),

        TUNNELS_RIGHT(null, true),

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
        SWITCH_MAGENTA(null, true),

        /**
         * Some stones, purely for decoration.
         */
        RUBBLE(false, false);

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
         * Getter for collision variable, only used for instantiation of tiles.
         *
         * @return true, if collision. May be null.
         */
        private Boolean getCollision() {
            return collision;
        }
    }
}
