package top.mothership.osubot.util;

public enum mods {
    NONE(0),            NOFAIL(1),      EASY(2),        HIDDEN(8),      HARDROCK(16),
    SUDDENDEATH(32),    DOUBLETIME(64), RELAX(128),     HALFTIME(256),  NIGHTCORE(512),
    FLASHLIGHT(1024),   AUTOPLAY(2048), SPUNOUT(4096),  AUTOPILOT(8192),PERFECT(16384),
    HDHR(24),           HDDT(72),       HDNC(584),      DTHDHR(88),     NCHDHR(600);
    private int i;
    private mods(int i) {
        this.i = i;
    }
    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }


}
