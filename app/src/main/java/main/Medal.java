package main;

import processing.core.PImage;

public enum Medal {
    GOLD(0),
    SILVER(1),
    BRONZE(2),
    NONE(3);

    PImage image;

    Medal(int imageCode) {
        this.image = Image.MEDAL.getImages().get(imageCode);
    }
}
