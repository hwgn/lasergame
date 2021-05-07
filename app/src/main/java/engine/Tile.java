package engine;

import main.App;
import main.Image;

import java.util.Map;
import java.util.Objects;

public class Tile {
    private final Type type;
    private final int initialState;
    private int state;
    private boolean collision;

    Tile(Tile.Type type, int state) {
        this.type = type;
        this.state = this.initialState = state;
        this.collision = Objects.requireNonNullElseGet(type.collision, () -> state == 1);
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

    public void setState(int i) {
        state = i;
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

    public enum Type {
        STONE(Image.STONE_CLEAN, true, false, 1),
        STONE_BROKEN(Image.STONE_BROKEN_1, true, false, 1),
        STONE_TARGET(Image.STONE_TARGET, true, false, 1),

        LASER_RED(Image.STONE_LASER_RED, false, false, 1),
        LASER_GREEN(Image.STONE_LASER_GREEN, false, false, 1),
        LASER_BLUE(Image.STONE_LASER_BLUE, false, false, 1),

        FLOOR(Image.FLOOR, false, false, 0),
        NULL(Image.NULL, false, false, 0),
        MIRROR(Image.MIRROR, true, true, 1),

        SWITCH_RED(Image.SWITCH_RED, null, false, null),
        SWITCH_BLUE(Image.SWITCH_BLUE, null, false, null),
        SWITCH_GREEN(Image.SWITCH_GREEN, null, false, null),
        SWITCH_CYAN(Image.SWITCH_CYAN, null, true, null),
        SWITCH_YELLOW(Image.SWITCH_YELLOW, null, true, null),
        SWITCH_MAGENTA(Image.SWITCH_MAGENTA, null, true, null);

        public final Boolean collision, canInteract;
        public final Integer layer;
        Image img;

        Type(Image img, Boolean collision, Boolean canInteract, Integer layer) {
            this.img = img;
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

        public Image getImage() {
            return img;
        }

        public boolean isLaserSwitch() {
            return this == SWITCH_RED || this == SWITCH_GREEN || this == SWITCH_BLUE;
        }

        public boolean isLaserSource() {
            return this == LASER_RED || this == LASER_GREEN || this == LASER_BLUE;
        }
    }
}
