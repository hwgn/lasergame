package engine;

import main.App;

import java.util.Map;
import java.util.Objects;

public class Tile {
    private final Type type;
    private final int initialState;
    private int state;
    private boolean collision;

    private Tile(Tile.Type type, int state) {
        this.type = type;
        this.state = this.initialState = state;
        this.collision = Objects.requireNonNullElseGet(type.collision, () -> state == 1);
    }

    private Tile(Tile.Type type, int initialState, int currentState, boolean collision) {
        this(type, initialState);
        this.state = currentState;
        this.collision = collision;
    }

    public static Tile of(Tile.Type type, int rotation) {
        return new Tile(type, rotation);
    }

    public Type getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public void resetState() {
        state = initialState;
    }

    public boolean getCollision() {
        return collision;
    }

    public void interact(int button, Map<Pair<Integer, Integer>, Tile> tiles) {
        switch (type) {
            case MIRROR -> state = button == App.RIGHT ? (state + 3) % 4 : (state + 1) % 4;

            case SWITCH_CYAN, SWITCH_YELLOW, SWITCH_MAGENTA -> tiles.values().stream()
                    .filter(t -> t.type.equals(this.type))
                    .forEach(t -> {
                        t.state = (t.state + 1) % 2;
                        t.collision = !t.collision;
                    });

            case SWITCH_RED, SWITCH_GREEN, SWITCH_BLUE -> {
                state = (state + 1) % 2;
                collision = !collision;
            }
        }
    }

    public Tile clone() {
        return new Tile(type, initialState, state, collision);
    }

    public enum Type {
        STONE(true, false, 1),
        STONE_BROKEN(true, false, 1),
        STONE_TARGET(true, false, 1),

        LASER_RED(false, false, 1),
        LASER_GREEN(false, false, 1),
        LASER_BLUE(false, false, 1),

        FLOOR(false, false, 0),
        NULL(false, false, 0),
        MIRROR(true, true, 1),

        SWITCH_RED(null, false, null),
        SWITCH_BLUE(null, false, null),
        SWITCH_GREEN(null, false, null),
        SWITCH_CYAN(null, true, null),
        SWITCH_YELLOW(null, true, null),
        SWITCH_MAGENTA(null, true, null);

        public final Boolean collision, canInteract;
        public final Integer layer;

        Type(Boolean collision, Boolean canInteract, Integer layer) {
            this.collision = collision;
            this.canInteract = canInteract;
            this.layer = layer;
        }

        public static Type getSwitchByColor(Laser.Color c) {
            return switch (c) {
                case BLUE -> SWITCH_BLUE;
                case RED -> SWITCH_RED;
                case GREEN -> SWITCH_GREEN;
            };
        }

        public boolean isLaserSwitch() {
            return this == SWITCH_RED || this == SWITCH_GREEN || this == SWITCH_BLUE;
        }

        public boolean isLaserSource() {
            return this == LASER_RED || this == LASER_GREEN || this == LASER_BLUE;
        }
    }
}
